package top.wain.heimdall.open.config;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 能力开放配置
 *
 * @author WainZeng
 * @since 2025/6/14 21:22
 */
@Configuration
public class OpenConfiguration {

    /**
     * API 文档分组配置
     */
    @Bean
    public GroupedOpenApi openApi() {
        return GroupedOpenApi.builder().group("open").displayName("能力开放").pathsToMatch("/open/**").build();
    }
}
