package top.wain.heimdall.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import top.wain.heimdall.common.constant.UiConstants;
import top.continew.starter.core.enums.BaseEnum;

/**
 * 启用/禁用状态枚举
 *
 * @author WainZeng
 * @since 2022/12/29 22:38
 */
@Getter
@RequiredArgsConstructor
public enum DisEnableStatusEnum implements BaseEnum<Integer> {

    /**
     * 启用
     */
    ENABLE(1, "启用", UiConstants.COLOR_SUCCESS),

    /**
     * 禁用
     */
    DISABLE(2, "禁用", UiConstants.COLOR_ERROR),;

    private final Integer value;
    private final String description;
    private final String color;
}
