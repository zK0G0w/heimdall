package top.wain.heimdall.open.sign;

import cn.dev33.satoken.secure.SaSecureUtil;
import cn.dev33.satoken.sign.template.SaSignTemplate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import top.wain.heimdall.common.enums.DisEnableStatusEnum;
import top.wain.heimdall.open.model.entity.AppDO;
import top.wain.heimdall.open.service.AppService;
import top.continew.starter.core.util.validation.ValidationUtils;

import java.util.Map;

/**
 * API 参数签名算法
 *
 * @author chengzi
 * @author WainZeng
 * @since 2024/10/17 16:03
 */
@Component
@RequiredArgsConstructor
public class OpenApiSignTemplate extends SaSignTemplate {

    private final AppService appService;
    public static final String ACCESS_KEY = "accessKey";

    @Override
    public void checkParamMap(Map<String, String> paramMap) {
        // 获取必须的参数
        String timestampValue = paramMap.get(timestamp);
        String nonceValue = paramMap.get(nonce);
        String signValue = paramMap.get(sign);
        String accessKeyValue = paramMap.get(ACCESS_KEY);

        // 校验
        ValidationUtils.throwIfBlank(timestampValue, "timestamp不能为空");
        ValidationUtils.throwIfBlank(nonceValue, "nonce不能为空");
        ValidationUtils.throwIfBlank(signValue, "sign不能为空");
        ValidationUtils.throwIfBlank(accessKeyValue, "accessKey不能为空");
        AppDO app = appService.getByAccessKey(accessKeyValue);
        ValidationUtils.throwIfNull(app, "accessKey无效");
        ValidationUtils.throwIfEqual(DisEnableStatusEnum.DISABLE, app.getStatus(), "应用已被禁用, 请联系管理员");
        ValidationUtils.throwIf(app.isExpired(), "应用已过期, 请联系管理员");

        // 依次校验三个参数
        super.checkTimestamp(Long.parseLong(timestampValue));
        super.checkNonce(nonceValue);
        paramMap.put(key, app.getSecretKey());
        super.checkSign(paramMap, signValue);
    }

    @Override
    public String createSign(Map<String, ?> paramMap) {
        ValidationUtils.throwIfEmpty(paramMap.get(key), "秘钥缺失, 请检查应用配置");
        // 移除 sign 参数
        paramMap.remove(sign);
        // 计算签名
        return SaSecureUtil.md5(super.joinParamsDictSort(paramMap));
    }
}
