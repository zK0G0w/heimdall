package top.wain.heimdall.system.config.sms;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.sms4j.core.factory.SmsFactory;
import org.dromara.sms4j.core.proxy.SmsProxyFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * 短信配置加载器
 *
 * @author luoqiz
 * @author WainZeng
 * @since 2025/03/15 22:15
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SmsConfigLoader implements ApplicationRunner {

    private final SmsReadConfigDatabaseImpl smsReadConfig;
    private final SmsLogProcessor smsLogProcessor;

    @Override
    public void run(ApplicationArguments args) {
        SmsFactory.createSmsBlend(smsReadConfig);
        SmsProxyFactory.addPreProcessor(smsLogProcessor);
        log.debug("短信配置初始化完成");
    }
}
