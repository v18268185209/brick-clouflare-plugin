package cn.net.rjnetwork.qixiaozhu.plugins.cloudflare.bootstrap;

import cn.net.rjnetwork.qixiaozhu.plugins.cloudflare.support.CloudflareJdbcUrlUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.io.FileSystemResource;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;

/**
 * When the plugin is launched standalone inside the local workspace, reuse the
 * sibling EqAdmin development/install configuration as low-friction defaults.
 */
public final class CloudflareStandaloneDefaults {

    private static final Logger log = LoggerFactory.getLogger(CloudflareStandaloneDefaults.class);

    private static final String EQADMIN_HOME_PROPERTY = "eqadmin.home";
    private static final String EQADMIN_HOME_ENV = "EQADMIN_HOME";
    private static final String INSTALL_OVERRIDES_PATH = "config/brick-bootkit-admin-install-overrides.properties";
    private static final String APPLICATION_DEV_PATH = "eq-admin-bases/eq-admin-start/src/main/resources/application-dev.yml";
    private static final String WEB_STATIC_ROOT_PATH = "eq-admin-bases/eq-admin-web/webapps";

    private CloudflareStandaloneDefaults() {
    }

    public static void applyFromLocalEqadmin() {
        Path eqadminHome = resolveEqadminHome();
        if (eqadminHome == null) {
            return;
        }

        boolean applied = false;
        applied |= applyFromPropertiesFile(eqadminHome.resolve(INSTALL_OVERRIDES_PATH), eqadminHome);
        applied |= applyFromYamlFile(eqadminHome.resolve(APPLICATION_DEV_PATH), eqadminHome);
        applied |= applyStaticRootFallback(eqadminHome.resolve(WEB_STATIC_ROOT_PATH));

        if (applied) {
            log.info("Loaded standalone defaults from local EqAdmin workspace: {}", eqadminHome);
        }
    }

    private static boolean applyFromPropertiesFile(Path path, Path eqadminHome) {
        if (!Files.isRegularFile(path)) {
            return false;
        }

        Properties properties = new Properties();
        try (InputStream inputStream = Files.newInputStream(path)) {
            properties.load(inputStream);
            return applyProperties(properties, path, eqadminHome);
        } catch (IOException ex) {
            log.warn("Failed to load EqAdmin properties file {}: {}", path, ex.getMessage());
            return false;
        }
    }

    private static boolean applyFromYamlFile(Path path, Path eqadminHome) {
        if (!Files.isRegularFile(path)) {
            return false;
        }

        try {
            YamlPropertiesFactoryBean factoryBean = new YamlPropertiesFactoryBean();
            factoryBean.setResources(new FileSystemResource(path));
            factoryBean.afterPropertiesSet();
            Properties properties = factoryBean.getObject();
            if (properties == null || properties.isEmpty()) {
                return false;
            }
            return applyProperties(properties, path, eqadminHome);
        } catch (Exception ex) {
            log.warn("Failed to load EqAdmin yaml file {}: {}", path, ex.getMessage());
            return false;
        }
    }

    private static boolean applyProperties(Properties properties, Path source, Path eqadminHome) {
        String hostUrl = firstNonBlank(
                properties.getProperty("cloudflare.plugin.host-datasource.url"),
                properties.getProperty("spring.datasource.url")
        );
        String hostUsername = firstNonBlank(
                properties.getProperty("cloudflare.plugin.host-datasource.username"),
                properties.getProperty("spring.datasource.username")
        );
        String hostPassword = firstNonBlank(
                properties.getProperty("cloudflare.plugin.host-datasource.password"),
                properties.getProperty("spring.datasource.password")
        );

        boolean applied = false;
        applied |= applyIfMissing(
                "cloudflare.plugin.host-datasource.url",
                hostUrl,
                source
        );
        applied |= applyIfMissing(
                "cloudflare.plugin.host-datasource.username",
                hostUsername,
                source
        );
        applied |= applyIfMissingAllowEmpty(
                "cloudflare.plugin.host-datasource.password",
                hostPassword,
                source
        );
        applied |= applyIfMissing(
                "cloudflare.plugin.datasource.url",
                firstNonBlank(
                        properties.getProperty("cloudflare.plugin.datasource.url"),
                        CloudflareJdbcUrlUtils.deriveDatabaseUrl(hostUrl, "cloudflare", true)
                ),
                source
        );
        applied |= applyIfMissing(
                "cloudflare.plugin.datasource.username",
                firstNonBlank(properties.getProperty("cloudflare.plugin.datasource.username"), hostUsername),
                source
        );
        applied |= applyIfMissingAllowEmpty(
                "cloudflare.plugin.datasource.password",
                firstNonBlank(properties.getProperty("cloudflare.plugin.datasource.password"), hostPassword),
                source
        );
        applied |= applyIfMissing(
                "eqadmin.web.static-root",
                resolveEqadminPath(properties.getProperty("eqadmin.web.static-root"), eqadminHome),
                source
        );
        return applied;
    }

    private static boolean applyStaticRootFallback(Path path) {
        if (hasExplicitStaticRoot() || !Files.isDirectory(path)) {
            return false;
        }
        return applySystemProperty("eqadmin.web.static-root", path.toAbsolutePath().normalize().toString(), path);
    }

    private static boolean applyIfMissing(String key, String value, Path source) {
        if (!StringUtils.hasText(value) || hasExplicitConfig(key)) {
            return false;
        }
        return applySystemProperty(key, value, source);
    }

    private static boolean applyIfMissingAllowEmpty(String key, String value, Path source) {
        if (value == null || hasExplicitConfig(key)) {
            return false;
        }
        return applySystemProperty(key, value, source);
    }

    private static boolean applySystemProperty(String key, String value, Path source) {
        if (Objects.equals(System.getProperty(key), value)) {
            return false;
        }
        System.setProperty(key, value);
        log.info("Applied standalone default property '{}' from {}", key, source);
        return true;
    }

    private static boolean hasExplicitConfig(String key) {
        return switch (key) {
            case "cloudflare.plugin.datasource.url" ->
                    StringUtils.hasText(System.getProperty(key))
                            || StringUtils.hasText(System.getenv("CLOUDFLARE_PLUGIN_DB_URL"));
            case "cloudflare.plugin.datasource.username" ->
                    StringUtils.hasText(System.getProperty(key))
                            || StringUtils.hasText(System.getenv("CLOUDFLARE_PLUGIN_DB_USERNAME"));
            case "cloudflare.plugin.datasource.password" ->
                    System.getProperty(key) != null
                            || System.getenv("CLOUDFLARE_PLUGIN_DB_PASSWORD") != null;
            case "cloudflare.plugin.host-datasource.url" ->
                    StringUtils.hasText(System.getProperty(key))
                            || StringUtils.hasText(System.getenv("CLOUDFLARE_PLUGIN_HOST_DB_URL"))
                            || StringUtils.hasText(System.getenv("EQADMIN_MYSQL_URL"))
                            || StringUtils.hasText(System.getProperty("spring.datasource.url"));
            case "cloudflare.plugin.host-datasource.username" ->
                    StringUtils.hasText(System.getProperty(key))
                            || StringUtils.hasText(System.getenv("CLOUDFLARE_PLUGIN_HOST_DB_USERNAME"))
                            || StringUtils.hasText(System.getenv("EQADMIN_MYSQL_USERNAME"))
                            || StringUtils.hasText(System.getProperty("spring.datasource.username"));
            case "cloudflare.plugin.host-datasource.password" ->
                    System.getProperty(key) != null
                            || System.getenv("CLOUDFLARE_PLUGIN_HOST_DB_PASSWORD") != null
                            || System.getenv("EQADMIN_MYSQL_PASSWORD") != null
                            || System.getProperty("spring.datasource.password") != null;
            case "eqadmin.web.static-root" -> hasExplicitStaticRoot();
            default -> StringUtils.hasText(System.getProperty(key));
        };
    }

    private static boolean hasExplicitStaticRoot() {
        return StringUtils.hasText(System.getProperty("eqadmin.web.static-root"))
                || StringUtils.hasText(System.getenv("EQADMIN_WEB_STATIC_ROOT"));
    }

    private static Path resolveEqadminHome() {
        Set<Path> candidates = new LinkedHashSet<>();
        String configuredHome = firstNonBlank(System.getProperty(EQADMIN_HOME_PROPERTY), System.getenv(EQADMIN_HOME_ENV));
        if (StringUtils.hasText(configuredHome)) {
            candidates.add(Paths.get(configuredHome).toAbsolutePath().normalize());
        }

        Path userDir = Paths.get(System.getProperty("user.dir", ".")).toAbsolutePath().normalize();
        candidates.add(userDir.resolve("..").resolve("eqadmin").normalize());
        candidates.add(userDir.resolve("eqadmin").normalize());

        for (Path candidate : candidates) {
            if (isEqadminHome(candidate)) {
                return candidate;
            }
        }
        return null;
    }

    private static boolean isEqadminHome(Path candidate) {
        if (candidate == null || !Files.isDirectory(candidate)) {
            return false;
        }

        List<Path> markers = new ArrayList<>();
        markers.add(candidate.resolve(INSTALL_OVERRIDES_PATH));
        markers.add(candidate.resolve(APPLICATION_DEV_PATH));
        markers.add(candidate.resolve("eq-admin-bases"));

        for (Path marker : markers) {
            if (Files.exists(marker)) {
                return true;
            }
        }
        return false;
    }

    private static String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return values.length == 0 ? null : values[values.length - 1];
    }

    private static String resolveEqadminPath(String value, Path eqadminHome) {
        if (!StringUtils.hasText(value)) {
            return value;
        }

        String resolved = resolvePlaceholderDefault(value.trim());
        if (!StringUtils.hasText(resolved)) {
            return resolved;
        }

        Path path = Paths.get(resolved);
        if (path.isAbsolute()) {
            return path.normalize().toString();
        }

        if (eqadminHome == null) {
            return path.normalize().toString();
        }

        return eqadminHome.resolve(path).normalize().toString();
    }

    private static String resolvePlaceholderDefault(String value) {
        if (!StringUtils.hasText(value)) {
            return value;
        }
        if (!(value.startsWith("${") && value.endsWith("}"))) {
            return value;
        }

        int colonIndex = value.indexOf(':');
        if (colonIndex < 0 || colonIndex >= value.length() - 1) {
            return value;
        }

        return value.substring(colonIndex + 1, value.length() - 1);
    }
}
