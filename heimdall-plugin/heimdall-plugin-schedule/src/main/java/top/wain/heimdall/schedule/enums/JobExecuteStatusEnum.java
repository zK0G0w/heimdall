package top.wain.heimdall.schedule.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import top.wain.heimdall.common.constant.UiConstants;
import top.continew.starter.core.enums.BaseEnum;

/**
 * 任务执行状态枚举
 *
 * @author WainZeng
 * @since 2024/7/11 22:28
 */
@Getter
@RequiredArgsConstructor
public enum JobExecuteStatusEnum implements BaseEnum<Integer> {

    /**
     * 待处理
     */
    WAITING(1, "待处理", UiConstants.COLOR_PRIMARY),

    /**
     * 运行中
     */
    RUNNING(2, "运行中", UiConstants.COLOR_WARNING),

    /**
     * 成功
     */
    SUCCEEDED(3, "成功", UiConstants.COLOR_SUCCESS),

    /**
     * 已失败
     */
    FAILED(4, "已失败", UiConstants.COLOR_ERROR),

    /**
     * 已停止
     */
    STOPPED(5, "已停止", UiConstants.COLOR_ERROR),

    /**
     * 已取消
     */
    CANCELED(6, "已取消", UiConstants.COLOR_DEFAULT),;

    private final Integer value;
    private final String description;
    private final String color;
}
