package top.wain.heimdall.oauth2.handler;

import cn.hutool.core.util.StrUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import top.wain.heimdall.oauth2.constant.Oauth2Constants;
import top.wain.heimdall.oauth2.enums.GrantTypeEnum;
import top.wain.heimdall.oauth2.exception.Oauth2Exception;
import top.wain.heimdall.oauth2.model.dto.Oauth2TokenDTO;
import top.wain.heimdall.oauth2.model.entity.Oauth2AppDO;
import top.wain.heimdall.oauth2.model.req.Oauth2TokenReq;
import top.wain.heimdall.oauth2.service.Oauth2ClientValidator;
import top.wain.heimdall.oauth2.service.Oauth2TokenService;

/**
 * @Description: 客户端凭证模式处理器，校验 clientId 和 clientSecret 后仅颁发 access_token（无 refresh_token）
 * @Author: WainZeng
 * @Date: 2026/06/11
 */
@Component
@RequiredArgsConstructor
public class ClientCredentialsHandler implements GrantTypeHandler {

    private final Oauth2ClientValidator clientValidator;
    private final Oauth2TokenService tokenService;

    @Override
    public Oauth2TokenDTO handle(Oauth2TokenReq req) {
        // 1. 校验必填参数
        if (StrUtil.isBlank(req.getClientId())) {
            throw new Oauth2Exception(Oauth2Constants.ERROR_INVALID_REQUEST, "client_id 不能为空");
        }
        if (StrUtil.isBlank(req.getClientSecret())) {
            throw new Oauth2Exception(Oauth2Constants.ERROR_INVALID_REQUEST, "client_secret 不能为空");
        }

        // 2. 校验 clientId，获取应用
        Oauth2AppDO app = clientValidator.validateClientId(req.getClientId());

        // 3. 校验授权类型
        clientValidator.validateGrantType(app, GrantTypeEnum.CLIENT_CREDENTIALS.getValue());

        // 4. 校验客户端密钥
        clientValidator.validateClientSecret(app, req.getClientSecret());

        // 5. 若携带 scope，则校验合法性
        if (StrUtil.isNotBlank(req.getScope())) {
            clientValidator.validateScope(app, req.getScope());
        }

        // 6. 仅颁发 access_token（client_credentials 模式无用户主体，不颁发 refresh_token）
        return tokenService.issueAccessTokenOnly(app, req.getScope(), GrantTypeEnum.CLIENT_CREDENTIALS.getValue());
    }

    @Override
    public GrantTypeEnum getGrantType() {
        return GrantTypeEnum.CLIENT_CREDENTIALS;
    }
}
