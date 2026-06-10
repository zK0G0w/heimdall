package top.wain.heimdall.system.config.sms;

import cn.hutool.core.util.NumberUtil;
import cn.hutool.json.JSONUtil;
import lombok.RequiredArgsConstructor;
import org.dromara.sms4j.api.entity.SmsResponse;
import org.dromara.sms4j.api.proxy.CoreMethodProcessor;
import org.springframework.stereotype.Component;
import top.wain.heimdall.common.enums.SuccessFailureStatusEnum;
import top.wain.heimdall.system.model.req.SmsLogReq;
import top.wain.heimdall.system.service.SmsLogService;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * 短信日志处理器
 *
 * @author luoqiz
 * @author WainZeng
 * @since 2025/03/15 22:15
 */
@Component
@RequiredArgsConstructor
public class SmsLogProcessor implements CoreMethodProcessor {

    private final SmsLogService smsLogService;

    @Override
    public Object postProcessor(SmsResponse result, Object[] param) {
        if (NumberUtil.isNumber(result.getConfigId())) {
            SmsLogReq req = new SmsLogReq();
            req.setConfigId(Long.parseLong(result.getConfigId()));
            req.setPhone(param[0].toString());
            req.setParams(JSONUtil.toJsonStr(param[1]));
            req.setStatus(result.isSuccess() ? SuccessFailureStatusEnum.SUCCESS : SuccessFailureStatusEnum.FAILURE);
            req.setResMsg(JSONUtil.toJsonStr(result.getData()));
            smsLogService.create(req);
        }
        return CoreMethodProcessor.super.postProcessor(result, param);
    }

    @Override
    public void sendMessagePreProcess(String phone, Object message) {
        // do nothing
    }

    @Override
    public void sendMessageByTemplatePreProcess(String phone,
                                                String templateId,
                                                LinkedHashMap<String, String> messages) {
        // do nothing
    }

    @Override
    public void massTextingPreProcess(List<String> phones, String message) {
        // do nothing
    }

    @Override
    public void massTextingByTemplatePreProcess(List<String> phones,
                                                String templateId,
                                                LinkedHashMap<String, String> messages) {
        // do nothing
    }
}