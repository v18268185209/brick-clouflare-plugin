package cn.net.rjnetwork.qixiaozhu.plugins.cloudflare.config;

import cn.net.rjnetwork.qixiaozhu.plugins.cloudflare.support.CloudflareJdbcUrlUtils;
import cn.net.rjnetwork.qixiaozhu.plugins.cloudflare.support.UnavailableDataSource;
import com.zaxxer.hikari.HikariDataSource;
import com.zqzqq.bootkits.bootstrap.PluginContextHolder;
import com.zqzqq.bootkits.spring.SpringBeanFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DelegatingDataSource;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.lang.reflect.Method;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class PluginDataSourceConfig {

    private final CloudflarePluginProperties properties;

    @Bean
    @Primary
    public DataSource dataSource() {
        CloudflarePluginProperties.Datasource datasource = properties.getDatasource();
        String jdbcUrl = resolvePluginJdbcUrl();
        String username = firstNonBlank(
                datasource.getUsername(),
                properties.getHostDatasource().getUsername(),
                "root"
        );
        String password = firstNonNull(
                datasource.getPassword(),
                properties.getHostDatasource().getPassword(),
                ""
        );

        HikariDataSource dataSource = buildMysqlDataSource(
                datasource,
                jdbcUrl,
                username,
                password,
                "cloudflare-plugin-hikari"
        );
        log.info(
                "Cloudflare plugin datasource mode=isolated, dbType={}, jdbcUrl={}",
                properties.resolveDbType(),
                CloudflareJdbcUrlUtils.maskJdbcUrl(jdbcUrl)
        );
        return dataSource;
    }

    @Bean("hostDataSource")
    public DataSource hostDataSource() {
        DataSource hostDataSource = properties.getRuntimeMode() == CloudflarePluginProperties.RuntimeMode.STANDALONE
                ? null
                : resolveHostBeanDataSource();
        if (hostDataSource != null) {
            DataSource safeDataSource = safeCloneHostDataSource(hostDataSource);
            log.info(
                    "Cloudflare host bridge datasource mode=embedded, sourceBean={}, dbType={}",
                    properties.getHostDataSourceBeanName(),
                    properties.resolveDbType()
            );
            return safeDataSource;
        }

        String jdbcUrl = resolveHostJdbcUrl();
        if (!StringUtils.hasText(jdbcUrl)) {
            String reason = "Host datasource is unavailable. bean=" + properties.getHostDataSourceBeanName()
                    + ", host jdbc url is empty";
            log.warn(reason);
            return new UnavailableDataSource(reason);
        }

        CloudflarePluginProperties.Datasource hostDatasource = properties.getHostDatasource();
        String username = firstNonBlank(hostDatasource.getUsername(), "root");
        String password = firstNonNull(hostDatasource.getPassword(), "");
        HikariDataSource standaloneHost = buildMysqlDataSource(
                hostDatasource,
                jdbcUrl,
                username,
                password,
                "cloudflare-host-hikari"
        );
        log.info(
                "Cloudflare host bridge datasource mode=standalone, dbType={}, jdbcUrl={}",
                properties.resolveDbType()
                ,
                CloudflareJdbcUrlUtils.maskJdbcUrl(jdbcUrl)
        );
        return standaloneHost;
    }

    @Bean("hostJdbcTemplate")
    public JdbcTemplate hostJdbcTemplate(@Qualifier("hostDataSource") DataSource hostDataSource) {
        return new JdbcTemplate(hostDataSource);
    }

    private DataSource resolveHostBeanDataSource() {
        String beanName = StringUtils.hasText(properties.getHostDataSourceBeanName())
                ? properties.getHostDataSourceBeanName().trim()
                : "mainDataSource";
        try {
            return PluginContextHolder.getBeanFactory().getBean(beanName, DataSource.class);
        } catch (Exception ignore) {
            // keep trying
        }
        try {
            SpringBeanFactory springBeanFactory = PluginContextHolder.getMainApplicationContext().getSpringBeanFactory();
            return springBeanFactory.getBean(beanName, DataSource.class);
        } catch (Exception ex) {
            log.warn("Resolve host datasource '{}' failed: {}", beanName, ex.getMessage());
            return null;
        }
    }

    private String resolvePluginJdbcUrl() {
        String configured = properties.getDatasource().getUrl();
        if (StringUtils.hasText(configured)) {
            return configured.trim();
        }
        String hostJdbcUrl = resolveHostJdbcUrl();
        return CloudflareJdbcUrlUtils.deriveDatabaseUrl(hostJdbcUrl, "cloudflare", true);
    }

    private String resolveHostJdbcUrl() {
        String configured = properties.getHostDatasource().getUrl();
        if (StringUtils.hasText(configured)) {
            return configured.trim();
        }
        return CloudflareJdbcUrlUtils.DEFAULT_HOST_JDBC_URL;
    }

    private DataSource safeCloneHostDataSource(DataSource hostDataSource) {
        if (hostDataSource == null) {
            throw new IllegalArgumentException("hostDataSource is required");
        }

        String className = hostDataSource.getClass().getName();
        if ("com.alibaba.druid.pool.DruidDataSource".equals(className)) {
            try {
                Method cloneMethod = hostDataSource.getClass().getMethod("cloneDruidDataSource");
                Object cloned = cloneMethod.invoke(hostDataSource);
                if (cloned instanceof DataSource dataSource) {
                    return dataSource;
                }
            } catch (Exception ex) {
                log.warn("Clone Druid datasource failed, fallback to delegating datasource: {}", ex.getMessage());
            }
        }

        return new DelegatingDataSource(hostDataSource);
    }

    private HikariDataSource buildMysqlDataSource(
            CloudflarePluginProperties.Datasource datasource,
            String jdbcUrl,
            String username,
            String password,
            String poolName
    ) {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setDriverClassName(datasource.getDriverClassName());
        dataSource.setJdbcUrl(jdbcUrl);
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        dataSource.setMaximumPoolSize(Math.max(1, datasource.getMaximumPoolSize()));
        dataSource.setMinimumIdle(Math.max(0, datasource.getMinimumIdle()));
        dataSource.setConnectionTimeout(Math.max(5000L, datasource.getConnectionTimeoutMs()));
        dataSource.setPoolName(poolName);
        return dataSource;
    }

    private String firstNonBlank(String... values) {
        if (values == null) {
            return "";
        }
        for (String value : values) {
            if (StringUtils.hasText(value)) {
                return value.trim();
            }
        }
        return "";
    }

    private String firstNonNull(String... values) {
        if (values == null) {
            return "";
        }
        for (String value : values) {
            if (value != null) {
                return value;
            }
        }
        return "";
    }
}
