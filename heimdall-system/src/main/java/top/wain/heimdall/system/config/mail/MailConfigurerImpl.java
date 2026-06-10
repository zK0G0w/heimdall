package top.wain.heimdall.system.config.mail;

import cn.hutool.core.map.MapUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import top.wain.heimdall.common.constant.GlobalConstants;
import top.wain.heimdall.system.enums.OptionCategoryEnum;
import top.wain.heimdall.system.service.OptionService;
import top.continew.starter.messaging.mail.core.MailConfig;
import top.continew.starter.messaging.mail.core.MailConfigurer;

import java.util.Map;

/**
 * 邮件配置实现
 *
 * @author WainZeng
 * @since 2024/5/30 22:32
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MailConfigurerImpl implements MailConfigurer {

    private final OptionService optionService;

    @Override
    public MailConfig getMailConfig() {
        // 查询邮件配置
        Map<String, String> map = optionService.getByCategory(OptionCategoryEnum.MAIL);
        // 封装邮件配置
        MailConfig mailConfig = new MailConfig();
        mailConfig.setProtocol(MapUtil.getStr(map, "MAIL_PROTOCOL"));
        mailConfig.setHost(MapUtil.getStr(map, "MAIL_HOST"));
        mailConfig.setPort(MapUtil.getInt(map, "MAIL_PORT"));
        mailConfig.setUsername(MapUtil.getStr(map, "MAIL_USERNAME"));
        mailConfig.setPassword(MapUtil.getStr(map, "MAIL_PASSWORD"));
        mailConfig.setSslEnabled(GlobalConstants.Boolean.YES.equals(MapUtil.getInt(map, "MAIL_SSL_ENABLED")));
        if (mailConfig.isSslEnabled()) {
            mailConfig.setSslPort(MapUtil.getInt(map, "MAIL_SSL_PORT"));
        }
        return mailConfig;
    }
}
