package top.wain.heimdall.schedule.config;

import feign.Logger;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import top.wain.heimdall.schedule.api.JobClient;
import top.continew.starter.core.autoconfigure.application.ApplicationProperties;

/**
 * Feign 配置
 *
 * @author WainZeng
 * @since 2025/3/28 21:17
 */
@Configuration
@RequiredArgsConstructor
public class FeignConfiguration {

    private final ApplicationProperties applicationProperties;

    @Value("${snail-job.server.api.url}")
    private String baseUrl;

    @Value("${snail-job.server.api.username}")
    private String username;

    @Value("${snail-job.server.api.password}")
    private String password;

    /**
     * 调度客户端
     */
    @Bean
    public JobClient jobClient() {
        return new JobClient(baseUrl, username, password);
    }

    /**
     * Feign 日志级别
     */
    @Bean
    public Logger.Level feignLoggerLevel() {
        return applicationProperties.isProduction() ? Logger.Level.BASIC : Logger.Level.FULL;
    }
}