package top.wain.heimdall.system.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import top.continew.starter.core.enums.BaseEnum;

/**
 * 顶人下线的范围枚举
 * 
 * @author KAI
 * @since 2025/10/28 14:05
 */
@Getter
@RequiredArgsConstructor
public enum ReplacedRangeEnum implements BaseEnum<String> {

    /**
     * 当前客户端类型
     */
    CURR_DEVICE_TYPE("CURR_DEVICE_TYPE", "当前客户端类型"),

    /**
     * 所有客户端类型
     */
    ALL_DEVICE_TYPE("ALL_DEVICE_TYPE", "所有客户端类型");

    private final String value;
    private final String description;
}
