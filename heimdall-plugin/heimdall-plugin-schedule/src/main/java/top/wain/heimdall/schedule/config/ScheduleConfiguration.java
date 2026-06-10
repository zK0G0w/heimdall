package top.wain.heimdall.schedule.config;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 任务调度配置
 *
 * @author WainZeng
 * @since 2025/6/14 21:22
 */
@Configuration
public class ScheduleConfiguration {

    /**
     * API 文档分组配置
     */
    @Bean
    public GroupedOpenApi scheduleApi() {
        return GroupedOpenApi.builder().group("schedule").displayName("任务调度").pathsToMatch("/schedule/**").build();
    }
}
