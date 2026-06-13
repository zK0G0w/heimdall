package top.wain.heimdall.oauth2.config;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Description: OAuth2 模块配置
 * @Author: WainZeng
 * @Date: 2026/06/13
 */
@Configuration
public class Oauth2Configuration {

    @Bean
    public GroupedOpenApi oauth2Api() {
        return GroupedOpenApi.builder().group("oauth2").displayName("OAuth2 协议").pathsToMatch("/oauth2/**").build();
    }
}
