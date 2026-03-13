package cn.net.rjnetwork.qixiaozhu.plugins.cloudflare.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import javax.sql.DataSource;
import java.sql.Connection;
import java.time.Duration;

@Configuration
public class BeanConfig {

    @Bean
    public RestTemplate restTemplate(
            RestTemplateBuilder builder,
            CloudflarePluginProperties properties
    ) {
        return builder
                .connectTimeout(Duration.ofMillis(properties.getConnectTimeoutMs()))
                .readTimeout(Duration.ofMillis(properties.getReadTimeoutMs()))
                .build();
    }

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor(
            CloudflarePluginProperties properties,
            DataSource dataSource
    ) {
        String dbTypeValue = resolveDbType(properties, dataSource);
        DbType dbType = "sqlite".equalsIgnoreCase(dbTypeValue) ? DbType.SQLITE : DbType.MYSQL;
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(dbType));
        return interceptor;
    }

    private String resolveDbType(CloudflarePluginProperties properties, DataSource dataSource) {
        String configured = properties.resolveDbType();
        try (Connection connection = dataSource.getConnection()) {
            String url = connection.getMetaData().getURL();
            if (url != null) {
                String lower = url.toLowerCase();
                if (lower.contains("sqlite")) {
                    return "sqlite";
                }
                if (lower.contains("mysql")) {
                    return "mysql";
                }
            }
        } catch (Exception ignore) {
            // keep configured value
        }
        return configured;
    }
}
