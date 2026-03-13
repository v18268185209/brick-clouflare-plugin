package cn.net.rjnetwork.qixiaozhu.plugins.cloudflare.bootstrap;

import cn.net.rjnetwork.qixiaozhu.plugins.cloudflare.support.UnavailableDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CloudflarePluginStartupBootstrapRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(CloudflarePluginStartupBootstrapRunner.class);
    private static final DateTimeFormatter BACKUP_TS_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final CloudflarePluginStartupProperties properties;
    private final Environment environment;
    private final JdbcTemplate hostJdbcTemplate;
    private final DataSource hostDataSource;

    public CloudflarePluginStartupBootstrapRunner(CloudflarePluginStartupProperties properties,
                                                  Environment environment,
                                                  @Qualifier("hostJdbcTemplate") JdbcTemplate hostJdbcTemplate,
                                                  @Qualifier("hostDataSource") DataSource hostDataSource) {
        this.properties = properties;
        this.environment = environment;
        this.hostJdbcTemplate = hostJdbcTemplate;
        this.hostDataSource = hostDataSource;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!properties.isEnabled()) {
            return;
        }
        try {
            if (properties.isStaticSyncEnabled()) {
                syncStaticAssets();
            }
            if (properties.isMicroappSyncEnabled()) {
                ensureMicroappInfo();
            }
        } catch (Exception ex) {
            if (properties.isFailFast()) {
                throw new IllegalStateException("cloudflare plugin startup bootstrap failed", ex);
            }
            log.warn("cloudflare plugin startup bootstrap skipped due to error: {}", ex.getMessage());
        }
    }

    private void syncStaticAssets() throws IOException {
        Path staticRoot = resolveStaticRoot(environment.getProperty("eqadmin.web.static-root", "./webapps"));
        String childrenDir = StringUtils.hasText(environment.getProperty("eqadmin.web.children-dir"))
                ? environment.getProperty("eqadmin.web.children-dir")
                : "childrens";
        String backupDir = StringUtils.hasText(environment.getProperty("eqadmin.web.bootstrap-backup-dir-name"))
                ? environment.getProperty("eqadmin.web.bootstrap-backup-dir-name")
                : properties.getBackupDirName();

        Path targetDir = staticRoot
                .resolve(childrenDir.trim())
                .resolve(properties.getTargetChildDirName())
                .toAbsolutePath()
                .normalize();
        Path childrenRoot = staticRoot.resolve(childrenDir.trim()).toAbsolutePath().normalize();
        if (!targetDir.startsWith(childrenRoot)) {
            throw new IllegalStateException("invalid target children directory: " + targetDir);
        }
        Files.createDirectories(targetDir.getParent());

        Map<String, Resource> classpathAssets = loadClasspathAssets(properties.getStaticClasspathRoot());
        Path sourceDir = resolveFileSourceDir();

        if (classpathAssets.isEmpty() && sourceDir == null) {
            String message = "cloudflare static assets source is empty. classpathRoot="
                    + properties.getStaticClasspathRoot() + ", fileSourceDir=" + properties.getStaticSourceDir();
            if (properties.isStaticSyncRequired()) {
                throw new IllegalStateException(message);
            }
            log.warn(message);
            return;
        }

        String sourceFingerprint = classpathAssets.isEmpty()
                ? computeDirectoryFingerprint(sourceDir)
                : computeClasspathFingerprint(classpathAssets);
        String targetFingerprint = computeDirectoryFingerprint(targetDir);
        if (StringUtils.hasText(sourceFingerprint) && Objects.equals(sourceFingerprint, targetFingerprint)) {
            log.info("cloudflare static assets are up-to-date, skip overwrite: {}", targetDir);
            return;
        }

        if (!isDirectoryEmpty(targetDir)) {
            Path backupRoot = staticRoot.resolve(backupDir).resolve("plugins").resolve(properties.getMicroappEnname());
            Path backupPath = backupRoot.resolve(properties.getTargetChildDirName() + "-" + BACKUP_TS_FORMATTER.format(LocalDateTime.now()));
            Files.createDirectories(backupRoot);
            copyDirectory(targetDir, backupPath);
            log.info("cloudflare static assets backup completed: {}", backupPath);
        }

        deleteDirectory(targetDir);
        Files.createDirectories(targetDir);

        int copied = classpathAssets.isEmpty()
                ? copyDirectoryFiles(sourceDir, targetDir)
                : copyClasspathAssets(classpathAssets, targetDir);

        if (copied <= 0 && properties.isStaticSyncRequired()) {
            throw new IllegalStateException("cloudflare static assets copied files count is zero");
        }
        log.info("cloudflare static assets synced to {}, copied={}", targetDir, copied);
    }

    private void ensureMicroappInfo() {
        if (!isHostDatabaseAvailable()) {
            log.info("skip microapp info sync: host datasource is unavailable");
            return;
        }
        if (!tableExists("qixiaozhu_microapp_info")) {
            log.info("skip microapp info sync: qixiaozhu_microapp_info table not found");
            return;
        }

        String enname = properties.getMicroappEnname();
        String baseUrl = properties.getMicroappBaseUrl();
        Map<String, Object> record = queryOne(
                "SELECT id,microapp_enname,microapp_zhname,base_url,timeout,iframe,container,`status`,schem " +
                        "FROM qixiaozhu_microapp_info WHERE microapp_enname = ? ORDER BY id DESC LIMIT 1",
                enname
        );
        if (record == null) {
            record = queryOne(
                    "SELECT id,microapp_enname,microapp_zhname,base_url,timeout,iframe,container,`status`,schem " +
                            "FROM qixiaozhu_microapp_info WHERE base_url = ? ORDER BY id DESC LIMIT 1",
                    baseUrl
            );
        }

        String expectedZhName = StringUtils.hasText(properties.getMicroappZhname())
                ? properties.getMicroappZhname()
                : enname;

        if (record == null) {
            hostJdbcTemplate.update(
                    "INSERT INTO qixiaozhu_microapp_info " +
                            "(microapp_enname,microapp_zhname,base_url,timeout,iframe,container,create_time,create_user_id,`status`,schem) " +
                            "VALUES (?,?,?,?,?,?,NOW(),?,?,?)",
                    enname,
                    expectedZhName,
                    baseUrl,
                    properties.getMicroappTimeout(),
                    properties.getMicroappIframe(),
                    properties.getMicroappContainer(),
                    properties.getMicroappCreateUserId(),
                    properties.getMicroappStatus(),
                    properties.getMicroappSchem()
            );
            log.info("created microapp info record: enname={}, baseUrl={}", enname, baseUrl);
            return;
        }

        Long id = asLong(record.get("id"));
        if (id == null) {
            throw new IllegalStateException("microapp info id is null when updating");
        }

        String actualZhName = asString(record.get("microapp_zhname"));
        String finalZhName = StringUtils.hasText(actualZhName) ? actualZhName : expectedZhName;

        boolean changed = false;
        changed |= !Objects.equals(asString(record.get("microapp_enname")), enname);
        changed |= !Objects.equals(asString(record.get("base_url")), baseUrl);
        changed |= !Objects.equals(asInteger(record.get("timeout")), properties.getMicroappTimeout());
        changed |= !Objects.equals(asInteger(record.get("iframe")), properties.getMicroappIframe());
        changed |= !Objects.equals(asString(record.get("container")), properties.getMicroappContainer());
        changed |= !Objects.equals(asInteger(record.get("status")), properties.getMicroappStatus());
        changed |= !Objects.equals(asString(record.get("schem")), properties.getMicroappSchem());
        changed |= !Objects.equals(actualZhName, finalZhName);

        if (!changed) {
            log.info("microapp info is up-to-date: enname={}, id={}", enname, id);
            return;
        }

        hostJdbcTemplate.update(
                "UPDATE qixiaozhu_microapp_info SET " +
                        "microapp_enname=?,microapp_zhname=?,base_url=?,timeout=?,iframe=?,container=?,`status`=?,schem=? " +
                        "WHERE id=?",
                enname,
                finalZhName,
                baseUrl,
                properties.getMicroappTimeout(),
                properties.getMicroappIframe(),
                properties.getMicroappContainer(),
                properties.getMicroappStatus(),
                properties.getMicroappSchem(),
                id
        );
        log.info("updated microapp info record: enname={}, id={}", enname, id);
    }

    private boolean tableExists(String tableName) {
        if (!isHostDatabaseAvailable()) {
            return false;
        }
        try (Connection connection = hostDataSource.getConnection()) {
            try (var resultSet = connection.getMetaData().getTables(connection.getCatalog(), null, tableName, null)) {
                return resultSet.next();
            }
        } catch (Exception ex) {
            log.warn("table existence check failed for {}: {}", tableName, ex.getMessage());
            return false;
        }
    }

    private Map<String, Resource> loadClasspathAssets(String classpathRoot) {
        String root = normalizeClasspathRoot(classpathRoot);
        if (!StringUtils.hasText(root)) {
            return new TreeMap<>();
        }

        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(getClass().getClassLoader());
        String pattern = "classpath*:" + root + "/**/*";
        Map<String, Resource> resources = new TreeMap<>();
        try {
            for (Resource resource : resolver.getResources(pattern)) {
                if (resource == null || !resource.isReadable()) {
                    continue;
                }
                String relativePath = extractRelativePath(resource, root);
                if (!StringUtils.hasText(relativePath)) {
                    continue;
                }
                resources.putIfAbsent(relativePath, resource);
            }
        } catch (Exception ex) {
            log.warn("failed to scan classpath assets: root={}, error={}", root, ex.getMessage());
        }
        return resources;
    }

    private int copyClasspathAssets(Map<String, Resource> assets, Path targetDir) throws IOException {
        int copied = 0;
        for (Map.Entry<String, Resource> entry : assets.entrySet()) {
            Path target = targetDir.resolve(entry.getKey()).normalize();
            if (!target.startsWith(targetDir)) {
                log.warn("skip suspicious classpath asset path: {}", entry.getKey());
                continue;
            }
            Files.createDirectories(target.getParent());
            try (InputStream inputStream = entry.getValue().getInputStream()) {
                Files.copy(inputStream, target, StandardCopyOption.REPLACE_EXISTING);
            }
            copied++;
        }
        return copied;
    }

    private Path resolveFileSourceDir() {
        if (StringUtils.hasText(properties.getStaticSourceDir())) {
            Path candidate = resolvePath(properties.getStaticSourceDir());
            if (Files.isDirectory(candidate)) {
                return candidate;
            }
        }

        String systemPath = System.getProperty("eqadmin.cloudflare.static-source-dir");
        if (StringUtils.hasText(systemPath)) {
            Path candidate = resolvePath(systemPath);
            if (Files.isDirectory(candidate)) {
                return candidate;
            }
        }

        Path devCandidate = Paths.get(System.getProperty("user.dir", "."), "sources", "dist")
                .toAbsolutePath()
                .normalize();
        if (Files.isDirectory(devCandidate)) {
            return devCandidate;
        }
        return null;
    }

    private int copyDirectoryFiles(Path sourceDir, Path targetDir) throws IOException {
        if (sourceDir == null || !Files.isDirectory(sourceDir)) {
            return 0;
        }
        int[] copied = {0};
        try (Stream<Path> stream = Files.walk(sourceDir)) {
            List<Path> all = stream.sorted().collect(Collectors.toList());
            for (Path path : all) {
                Path relative = sourceDir.relativize(path);
                Path target = targetDir.resolve(relative).normalize();
                if (!target.startsWith(targetDir)) {
                    continue;
                }
                if (Files.isDirectory(path)) {
                    Files.createDirectories(target);
                } else {
                    Files.createDirectories(target.getParent());
                    Files.copy(path, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES);
                    copied[0]++;
                }
            }
        }
        return copied[0];
    }

    private void copyDirectory(Path source, Path target) throws IOException {
        if (!Files.isDirectory(source)) {
            return;
        }
        try (Stream<Path> stream = Files.walk(source)) {
            List<Path> all = stream.sorted().collect(Collectors.toList());
            for (Path path : all) {
                Path relative = source.relativize(path);
                Path dest = target.resolve(relative).normalize();
                if (Files.isDirectory(path)) {
                    Files.createDirectories(dest);
                } else {
                    Files.createDirectories(dest.getParent());
                    Files.copy(path, dest, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES);
                }
            }
        }
    }

    private void deleteDirectory(Path dir) throws IOException {
        if (dir == null || !Files.exists(dir)) {
            return;
        }
        try (Stream<Path> stream = Files.walk(dir)) {
            List<Path> all = stream.sorted(Comparator.reverseOrder()).collect(Collectors.toList());
            for (Path path : all) {
                Files.deleteIfExists(path);
            }
        }
    }

    private boolean isDirectoryEmpty(Path dir) throws IOException {
        if (!Files.isDirectory(dir)) {
            return true;
        }
        try (Stream<Path> stream = Files.list(dir)) {
            return stream.findAny().isEmpty();
        }
    }

    private String computeClasspathFingerprint(Map<String, Resource> assets) {
        if (assets == null || assets.isEmpty()) {
            return "";
        }
        MessageDigest digest = newDigest();
        for (Map.Entry<String, Resource> entry : assets.entrySet()) {
            updateDigest(digest, entry.getKey());
            try (InputStream inputStream = entry.getValue().getInputStream()) {
                updateDigest(digest, inputStream);
            } catch (Exception ex) {
                log.warn("failed to read classpath asset for fingerprint: {}", entry.getKey());
                return "";
            }
        }
        return toHex(digest.digest());
    }

    private String computeDirectoryFingerprint(Path dir) {
        if (!Files.isDirectory(dir)) {
            return "";
        }
        List<Path> files;
        try (Stream<Path> stream = Files.walk(dir)) {
            files = stream.filter(Files::isRegularFile)
                    .sorted(Comparator.comparing(path -> toRelativePath(dir, path)))
                    .collect(Collectors.toList());
        } catch (Exception ex) {
            return "";
        }
        if (files.isEmpty()) {
            return "";
        }
        MessageDigest digest = newDigest();
        for (Path file : files) {
            updateDigest(digest, toRelativePath(dir, file));
            try (InputStream inputStream = Files.newInputStream(file)) {
                updateDigest(digest, inputStream);
            } catch (Exception ex) {
                return "";
            }
        }
        return toHex(digest.digest());
    }

    private Map<String, Object> queryOne(String sql, Object... args) {
        List<Map<String, Object>> rows = hostJdbcTemplate.query(sql, args, (rs, rowNum) -> {
            Map<String, Object> row = new HashMap<>();
            row.put("id", rs.getObject("id"));
            row.put("microapp_enname", rs.getObject("microapp_enname"));
            row.put("microapp_zhname", rs.getObject("microapp_zhname"));
            row.put("base_url", rs.getObject("base_url"));
            row.put("timeout", rs.getObject("timeout"));
            row.put("iframe", rs.getObject("iframe"));
            row.put("container", rs.getObject("container"));
            row.put("status", rs.getObject("status"));
            row.put("schem", rs.getObject("schem"));
            return row;
        });
        return rows.isEmpty() ? null : new LinkedHashMap<>(rows.get(0));
    }

    private boolean isHostDatabaseAvailable() {
        return !(hostDataSource instanceof UnavailableDataSource);
    }

    private static MessageDigest newDigest() {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (Exception ex) {
            throw new IllegalStateException("SHA-256 digest unavailable", ex);
        }
    }

    private static void updateDigest(MessageDigest digest, String value) {
        digest.update(value.getBytes(StandardCharsets.UTF_8));
        digest.update((byte) '\n');
    }

    private static void updateDigest(MessageDigest digest, InputStream inputStream) throws IOException {
        byte[] buffer = new byte[8192];
        int read;
        while ((read = inputStream.read(buffer)) != -1) {
            digest.update(buffer, 0, read);
        }
    }

    private static String toHex(byte[] bytes) {
        StringBuilder builder = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            String hex = Integer.toHexString(b & 0xff);
            if (hex.length() == 1) {
                builder.append('0');
            }
            builder.append(hex);
        }
        return builder.toString();
    }

    private static String toRelativePath(Path base, Path target) {
        return base.relativize(target).toString().replace('\\', '/');
    }

    private static Path resolveStaticRoot(String staticRoot) {
        String root = StringUtils.hasText(staticRoot) ? staticRoot : "./webapps";
        return Paths.get(root).toAbsolutePath().normalize();
    }

    private static Path resolvePath(String path) {
        return Paths.get(path).toAbsolutePath().normalize();
    }

    private static String normalizeClasspathRoot(String root) {
        String value = StringUtils.hasText(root) ? root.trim() : "";
        if (!StringUtils.hasText(value)) {
            return "";
        }
        while (value.startsWith("/")) {
            value = value.substring(1);
        }
        while (value.endsWith("/")) {
            value = value.substring(0, value.length() - 1);
        }
        return value;
    }

    private String extractRelativePath(Resource resource, String classpathRoot) {
        try {
            String url = resource.getURL().toString().replace("\\", "/");
            String marker = classpathRoot + "/";
            int index = url.indexOf(marker);
            if (index < 0) {
                return null;
            }
            String relativePath = url.substring(index + marker.length());
            if (!StringUtils.hasText(relativePath) || relativePath.endsWith("/")) {
                return null;
            }
            return relativePath;
        } catch (Exception ignored) {
            return null;
        }
    }

    private static String asString(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private static Long asLong(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        try {
            return Long.parseLong(String.valueOf(value));
        } catch (Exception ex) {
            return null;
        }
    }

    private static Integer asInteger(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.intValue();
        }
        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (Exception ex) {
            return null;
        }
    }
}
