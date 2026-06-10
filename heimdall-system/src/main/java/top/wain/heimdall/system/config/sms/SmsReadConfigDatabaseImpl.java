package top.wain.heimdall.system.config.sms;

import cn.hutool.core.collection.CollUtil;
import lombok.RequiredArgsConstructor;

import org.dromara.sms4j.core.datainterface.SmsReadConfig;
import org.dromara.sms4j.provider.config.BaseConfig;
import org.springframework.stereotype.Component;

import top.wain.heimdall.common.enums.DisEnableStatusEnum;
import top.wain.heimdall.system.model.query.SmsConfigQuery;
import top.wain.heimdall.system.model.resp.SmsConfigResp;
import top.wain.heimdall.system.service.SmsConfigService;
import top.continew.starter.core.util.CollUtils;

import java.util.List;

/**
 * 短信配置读取-数据源实现
 *
 * @author luoqiz
 * @author WainZeng
 * @since 2025/03/15 22:15
 */
@Component
@RequiredArgsConstructor
public class SmsReadConfigDatabaseImpl implements SmsReadConfig {

    private final SmsConfigService smsConfigService;

    @Override
    public BaseConfig getSupplierConfig(String configId) {
        Long id = Long.parseLong(configId);
        SmsConfigResp smsConfig = smsConfigService.get(id);
        if (DisEnableStatusEnum.DISABLE.equals(smsConfig.getStatus())) {
            return null;
        }
        return SmsConfigUtil.from(smsConfig);
    }

    @Override
    public List<BaseConfig> getSupplierConfigList() {
        SmsConfigQuery query = new SmsConfigQuery();
        query.setStatus(DisEnableStatusEnum.ENABLE);
        List<SmsConfigResp> list = smsConfigService.list(query);
        if (CollUtil.isEmpty(list)) {
            return List.of();
        }
        return CollUtils.mapToList(list, SmsConfigUtil::from);
    }
}