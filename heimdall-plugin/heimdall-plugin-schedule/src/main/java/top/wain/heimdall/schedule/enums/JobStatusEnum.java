package top.wain.heimdall.schedule.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import top.wain.heimdall.common.constant.UiConstants;
import top.continew.starter.core.enums.BaseEnum;

/**
 * 任务状态枚举
 *
 * @author WainZeng
 * @since 2024/7/11 22:28
 */
@Getter
@RequiredArgsConstructor
public enum JobStatusEnum implements BaseEnum<Integer> {

    /**
     * 禁用
     */
    DISABLED(0, "禁用", UiConstants.COLOR_ERROR),

    /**
     * 启用
     */
    ENABLED(1, "启用", UiConstants.COLOR_SUCCESS),;

    private final Integer value;
    private final String description;
    private final String color;
}
