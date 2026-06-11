package top.wain.heimdall.oauth2.handler;

import cn.hutool.core.util.StrUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import top.continew.starter.core.util.validation.ValidationUtils;
import top.wain.heimdall.oauth2.constant.Oauth2Constants;
import top.wain.heimdall.oauth2.enums.GrantTypeEnum;
import top.wain.heimdall.oauth2.model.dto.Oauth2TokenDTO;
import top.wain.heimdall.oauth2.model.entity.Oauth2AppDO;
import top.wain.heimdall.oauth2.model.req.Oauth2TokenReq;
import top.wain.heimdall.oauth2.service.Oauth2ClientValidator;
import top.wain.heimdall.oauth2.service.Oauth2TokenService;
import top.wain.heimdall.oauth2.store.RedisOauth2TokenStore;

import java.util.Map;

/**
 * @Description: 刷新令牌模式处理器，校验 refresh_token 有效性后颁发新令牌对
 * @Author: WainZeng
 * @Date: 2026/06/11
 */
@Component
@RequiredArgsConstructor
public class RefreshTokenHandler implements GrantTypeHandler {

    private final Oauth2ClientValidator clientValidator;
    private final Oauth2TokenService tokenService;
    private final RedisOauth2TokenStore tokenStore;

    @Override
    public Oauth2TokenDTO handle(Oauth2TokenReq req) {
        // 1. 校验必填参数
        ValidationUtils.throwIf(StrUtil.isBlank(req.getRefreshToken()), "refresh_token 不能为空");
        ValidationUtils.throwIf(StrUtil.isBlank(req.getClientId()), "client_id 不能为空");

        // 2. 校验 clientId，获取应用
        Oauth2AppDO app = clientValidator.validateClientId(req.getClientId());

        // 3. 校验授权类型
        clientValidator.validateGrantType(app, GrantTypeEnum.REFRESH_TOKEN.getValue());

        // 4. 若携带 clientSecret，则校验密钥
        if (StrUtil.isNotBlank(req.getClientSecret())) {
            clientValidator.validateClientSecret(app, req.getClientSecret());
        }

        // 5. 获取 refresh_token 存储信息，不存在则视为无效
        Map<String, String> refreshTokenData = tokenStore.getRefreshToken(req.getRefreshToken());
        ValidationUtils
            .throwIf(refreshTokenData == null, Oauth2Constants.ERROR_INVALID_GRANT + ": refresh_token 无效或已过期");

        // 6. 校验 refresh_token 归属的客户端与请求一致
        String tokenClientId = refreshTokenData.get("client_id");
        ValidationUtils.throwIf(!req.getClientId()
            .equals(tokenClientId), Oauth2Constants.ERROR_INVALID_GRANT + ": client_id 不匹配");

        // 7. 刷新令牌对，旧令牌对作废，颁发新令牌对
        Oauth2TokenDTO result = tokenService.refresh(req.getRefreshToken(), app);
        ValidationUtils.throwIf(result == null, Oauth2Constants.ERROR_INVALID_GRANT + ": refresh_token 刷新失败");

        return result;
    }

    @Override
    public GrantTypeEnum getGrantType() {
        return GrantTypeEnum.REFRESH_TOKEN;
    }
}
