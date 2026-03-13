package cn.net.rjnetwork.qixiaozhu.plugins.cloudflare.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;

@Data
@ConfigurationProperties(prefix = "cloudflare.plugin")
public class CloudflarePluginProperties {

    public enum RuntimeMode {
        EMBEDDED_SHARED,
        STANDALONE
    }

    private String apiBaseUrl = "https://api.cloudflare.com/client/v4";

    private int connectTimeoutMs = 10000;

    private int readTimeoutMs = 20000;

    private int maxRetries = 2;

    private int retryBackoffMs = 400;

    /**
     * 32 bytes is recommended for AES-256.
     */
    private String encryptionKey = "change-this-32-byte-cloudflare-key!";

    /**
     * embedded-shared: resolve host bootstrap datasource from EqAdmin first.
     * standalone: use explicitly configured host bootstrap datasource only.
     */
    private RuntimeMode runtimeMode = RuntimeMode.EMBEDDED_SHARED;

    /**
     * Optional override. Supported values: mysql.
     */
    private String dbType = "";

    private String hostDataSourceBeanName = "mainDataSource";

    private Datasource datasource = new Datasource();

    private Datasource hostDatasource = new Datasource();

    private Market market = new Market();

    public String resolveDbType() {
        if (StringUtils.hasText(dbType)) {
            return dbType.trim().toLowerCase();
        }
        return "mysql";
    }

    @Data
    public static class Datasource {

        private String url = "";

        private String username = "";

        private String password = "";

        private String driverClassName = "com.mysql.cj.jdbc.Driver";

        private Integer maximumPoolSize = 10;

        private Integer minimumIdle = 1;

        private Long connectionTimeoutMs = 30000L;
    }

    @Data
    public static class Market {

        private boolean enabled = true;

        private boolean signatureEnabled = true;

        /**
         * External worker base url, example: https://license.example.workers.dev
         */
        private String baseUrl = "";

        private String accessKey = "";

        private String accessSecret = "";

        private String adminUsername = "";

        private String adminPassword = "";

        private int sessionRenewBeforeMs = 300000;
    }
}
