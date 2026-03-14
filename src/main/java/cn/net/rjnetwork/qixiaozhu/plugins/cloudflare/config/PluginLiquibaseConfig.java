package cn.net.rjnetwork.qixiaozhu.plugins.cloudflare.config;

import cn.net.rjnetwork.qixiaozhu.plugins.cloudflare.support.UnavailableDataSource;
import liquibase.integration.spring.SpringLiquibase;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import javax.sql.DataSource;

@Configuration
public class PluginLiquibaseConfig {

    @Bean("cloudflarePluginLiquibase")
    public SpringLiquibase cloudflarePluginLiquibase(
            @Qualifier("dataSource") DataSource pluginDataSource,
            Environment environment
    ) {
        return buildLiquibase(
                pluginDataSource,
                "classpath:db/changelog/db.changelog-master.yaml",
                environment.getProperty("spring.liquibase.enabled", Boolean.class, true)
        );
    }

    @Bean("cloudflareHostBootstrapLiquibase")
    public SpringLiquibase cloudflareHostBootstrapLiquibase(
            @Qualifier("hostDataSource") DataSource hostDataSource,
            Environment environment
    ) {
        boolean shouldRun = environment.getProperty("spring.liquibase.enabled", Boolean.class, true)
                && !(hostDataSource instanceof UnavailableDataSource);
        return buildLiquibase(
                hostDataSource,
                "classpath:db/changelog/db.changelog-host-bootstrap.yaml",
                shouldRun
        );
    }

    private SpringLiquibase buildLiquibase(DataSource dataSource, String changeLog, boolean shouldRun) {
        SpringLiquibase liquibase = new SpringLiquibase();
        liquibase.setDataSource(dataSource);
        liquibase.setChangeLog(changeLog);
        liquibase.setShouldRun(shouldRun);
        return liquibase;
    }
}
