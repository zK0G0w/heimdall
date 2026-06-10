package top.wain.heimdall.schedule.exception;

import top.continew.starter.core.exception.BaseException;

/**
 * 调度服务异常
 *
 * @author WainZeng
 * @since 2025/5/21 22:05
 */
public class ScheduleServerException extends BaseException {

    public ScheduleServerException(String message) {
        super(message);
    }

    public ScheduleServerException(String message, Throwable cause) {
        super(message, cause);
    }
}
