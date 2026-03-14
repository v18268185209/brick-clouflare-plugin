package cn.net.rjnetwork.qixiaozhu.plugins.cloudflare.support;

import org.springframework.util.StringUtils;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.StringJoiner;

public final class CloudflareJdbcUrlUtils {

    public static final String DEFAULT_PLUGIN_JDBC_URL =
            "jdbc:mysql://127.0.0.1:3306/cloudflare?useSSL=false&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&createDatabaseIfNotExist=true";

    public static final String DEFAULT_HOST_JDBC_URL =
            "jdbc:mysql://127.0.0.1:3306/eqadmin?useSSL=false&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull";

    private CloudflareJdbcUrlUtils() {
    }

    public static String deriveDatabaseUrl(String sourceUrl, String databaseName, boolean createDatabaseIfMissing) {
        if (!StringUtils.hasText(sourceUrl)) {
            return defaultUrlFor(databaseName);
        }

        String trimmed = sourceUrl.trim();
        int queryIndex = trimmed.indexOf('?');
        String base = queryIndex >= 0 ? trimmed.substring(0, queryIndex) : trimmed;
        String query = queryIndex >= 0 ? trimmed.substring(queryIndex + 1) : "";

        int schemeIndex = base.indexOf("://");
        if (schemeIndex < 0) {
            return trimmed;
        }

        int slashIndex = base.indexOf('/', schemeIndex + 3);
        String nextBase = slashIndex < 0
                ? base + "/" + databaseName
                : base.substring(0, slashIndex + 1) + databaseName;
        String nextQuery = createDatabaseIfMissing
                ? withQueryParameter(query, "createDatabaseIfNotExist", "true")
                : query;
        return StringUtils.hasText(nextQuery) ? nextBase + "?" + nextQuery : nextBase;
    }

    public static String maskJdbcUrl(String jdbcUrl) {
        if (!StringUtils.hasText(jdbcUrl)) {
            return "";
        }
        int queryIndex = jdbcUrl.indexOf('?');
        return queryIndex >= 0 ? jdbcUrl.substring(0, queryIndex) + "?..." : jdbcUrl;
    }

    private static String defaultUrlFor(String databaseName) {
        return "cloudflare".equalsIgnoreCase(databaseName) ? DEFAULT_PLUGIN_JDBC_URL : DEFAULT_HOST_JDBC_URL;
    }

    private static String withQueryParameter(String query, String key, String value) {
        Map<String, String> values = new LinkedHashMap<>();
        if (StringUtils.hasText(query)) {
            for (String item : query.split("&")) {
                if (!StringUtils.hasText(item)) {
                    continue;
                }
                int equalsIndex = item.indexOf('=');
                if (equalsIndex < 0) {
                    values.put(item, "");
                    continue;
                }
                values.put(item.substring(0, equalsIndex), item.substring(equalsIndex + 1));
            }
        }
        values.putIfAbsent(key, value);

        StringJoiner joiner = new StringJoiner("&");
        for (Map.Entry<String, String> entry : values.entrySet()) {
            if (!StringUtils.hasText(entry.getValue())) {
                joiner.add(entry.getKey());
                continue;
            }
            joiner.add(entry.getKey() + "=" + entry.getValue());
        }
        return joiner.toString();
    }
}
