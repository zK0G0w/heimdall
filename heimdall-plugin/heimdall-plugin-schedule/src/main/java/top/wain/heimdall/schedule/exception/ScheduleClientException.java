package top.wain.heimdall.schedule.exception;

import top.continew.starter.core.exception.BaseException;

/**
 * 调度客户端异常
 *
 * @author WainZeng
 * @since 2025/5/21 22:05
 */
public class ScheduleClientException extends BaseException {

    public ScheduleClientException(String message) {
        super(message);
    }

    public ScheduleClientException(String message, Throwable cause) {
        super(message, cause);
    }
}
