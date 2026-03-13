package cn.net.rjnetwork.qixiaozhu.plugins.cloudflare;

import com.zqzqq.bootkits.bootstrap.SpringPluginBootstrap;
import com.zqzqq.bootkits.bootstrap.coexist.CoexistAllowAutoConfiguration;
import cn.net.rjnetwork.qixiaozhu.plugins.cloudflare.bootstrap.CloudflarePluginStartupProperties;
import cn.net.rjnetwork.qixiaozhu.plugins.cloudflare.bootstrap.CloudflareStandaloneDefaults;
import cn.net.rjnetwork.qixiaozhu.plugins.cloudflare.config.CloudflarePluginProperties;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@Slf4j
@SpringBootApplication(
        exclude = {
                RedisAutoConfiguration.class,
                DataSourceAutoConfiguration.class,
                SecurityAutoConfiguration.class
        }
)
@EnableConfigurationProperties({
        CloudflarePluginProperties.class,
        CloudflarePluginStartupProperties.class
})
@MapperScan(basePackages = "cn.net.rjnetwork.qixiaozhu.plugins.cloudflare.mapper")
public class CloudflarePluginApplication extends SpringPluginBootstrap {

    public static void main(String[] args) {
        CloudflareStandaloneDefaults.applyFromLocalEqadmin();
        log.info("Starting Cloudflare plugin bootstrap...");
        new CloudflarePluginApplication().run(args);
    }

    @Override
    protected void configCoexistAllowAutoConfiguration(CoexistAllowAutoConfiguration c) {
        c.add("com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration");
    }
}
