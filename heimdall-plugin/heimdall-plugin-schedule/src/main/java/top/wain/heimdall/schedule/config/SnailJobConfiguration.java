package top.wain.heimdall.schedule.config;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import com.aizuda.snailjob.client.common.appender.SnailLogbackAppender;
import com.aizuda.snailjob.client.common.event.SnailClientStartingEvent;
import com.aizuda.snailjob.client.starter.EnableSnailJob;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import top.wain.heimdall.schedule.annotation.ConditionalOnEnabledScheduleJob;

/**
 * Snail Job 配置
 *
 * @author KAI
 * @since 2024/6/26 9:19
 */
@Configuration
@EnableSnailJob
@ConditionalOnEnabledScheduleJob
public class SnailJobConfiguration {

    /**
     * 日志上报
     */
    @EventListener(SnailClientStartingEvent.class)
    public void onStarting() {
        SnailLogbackAppender<ILoggingEvent> appender = new SnailLogbackAppender<>();
        appender.start();
        LoggerContext loggerContext = (LoggerContext)LoggerFactory.getILoggerFactory();
        Logger rootLogger = loggerContext.getLogger(Logger.ROOT_LOGGER_NAME);
        rootLogger.addAppender(appender);
    }
}