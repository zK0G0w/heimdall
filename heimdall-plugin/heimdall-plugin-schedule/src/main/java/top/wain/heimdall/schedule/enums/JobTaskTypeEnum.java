package top.wain.heimdall.schedule.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import top.wain.heimdall.common.constant.UiConstants;
import top.continew.starter.core.enums.BaseEnum;

/**
 * 任务类型枚举
 *
 * @author WainZeng
 * @since 2024/7/11 22:28
 */
@Getter
@RequiredArgsConstructor
public enum JobTaskTypeEnum implements BaseEnum<Integer> {

    /**
     * 集群
     */
    CLUSTER(1, "集群", UiConstants.COLOR_PRIMARY),

    /**
     * 广播
     */
    BROADCAST(2, "广播", UiConstants.COLOR_PRIMARY),

    /**
     * 静态切片
     */
    SLICE(3, "静态切片", UiConstants.COLOR_PRIMARY),;

    private final Integer value;
    private final String description;
    private final String color;
}
