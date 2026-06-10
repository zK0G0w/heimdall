package top.wain.heimdall.schedule.annotation;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import top.continew.starter.core.constant.PropertiesConstants;

import java.lang.annotation.*;

/**
 * 是否禁用 Snail Job 判断注解
 *
 * @author WainZeng
 * @since 2025/10/25 12:28
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@Documented
@ConditionalOnProperty(prefix = "snail-job", name = PropertiesConstants.ENABLED, havingValue = "false")
public @interface ConditionalOnDisabledScheduleJob {
}