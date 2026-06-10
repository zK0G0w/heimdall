package top.wain.heimdall.common.config.doc;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 全局接口文档配置
 *
 * @author WainZeng
 * @since 2025/6/14 21:22
 */
@Configuration
public class GlobalSpringDocConfiguration {

    @Bean
    public GroupedOpenApi allApi() {
        return GroupedOpenApi.builder()
            .group("all")
            .displayName("全部接口")
            .pathsToMatch("/**")
            .packagesToExclude("/error")
            .build();
    }

    @Bean
    public GroupedOpenApi commonApi() {
        return GroupedOpenApi.builder()
            .group("common")
            .displayName("通用接口")
            .pathsToMatch("/captcha/**", "/dashboard/**")
            .build();
    }
}
