package top.wain.heimdall.oauth2.handler;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import top.wain.heimdall.oauth2.constant.Oauth2Constants;
import top.wain.heimdall.oauth2.exception.Oauth2Exception;
import top.wain.heimdall.oauth2.enums.GrantTypeEnum;
import top.wain.heimdall.oauth2.model.dto.Oauth2TokenDTO;
import top.wain.heimdall.oauth2.model.entity.Oauth2AppDO;
import top.wain.heimdall.oauth2.model.req.Oauth2TokenReq;
import top.wain.heimdall.oauth2.service.Oauth2ClientValidator;
import top.wain.heimdall.oauth2.service.Oauth2TokenService;
import top.wain.heimdall.oauth2.store.RedisOauth2TokenStore;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

/**
 * @Description: 授权码模式处理器，校验 code、PKCE 及客户端信息后颁发令牌对
 * @Author: WainZeng
 * @Date: 2026/06/11
 */
@Component
@RequiredArgsConstructor
public class AuthorizationCodeHandler implements GrantTypeHandler {

    private final Oauth2ClientValidator clientValidator;
    private final Oauth2TokenService tokenService;
    private final RedisOauth2TokenStore tokenStore;

    @Override
    public Oauth2TokenDTO handle(Oauth2TokenReq req) {
        // 1. 校验必填参数
        if (StrUtil.isBlank(req.getCode())) {
            throw new Oauth2Exception(Oauth2Constants.ERROR_INVALID_REQUEST, "code 不能为空");
        }
        if (StrUtil.isBlank(req.getClientId())) {
            throw new Oauth2Exception(Oauth2Constants.ERROR_INVALID_REQUEST, "client_id 不能为空");
        }

        // 2. 校验 clientId，获取应用
        Oauth2AppDO app = clientValidator.validateClientId(req.getClientId());

        // 3. 校验授权类型
        clientValidator.validateGrantType(app, GrantTypeEnum.AUTHORIZATION_CODE.getValue());

        // 4. 若携带 clientSecret，则校验密钥
        if (StrUtil.isNotBlank(req.getClientSecret())) {
            clientValidator.validateClientSecret(app, req.getClientSecret());
        }

        // 5. 消费授权码（一次性使用）
        Map<String, String> codeData = tokenStore.consumeAuthorizationCode(req.getCode());
        if (codeData == null) {
            throw new Oauth2Exception(Oauth2Constants.ERROR_INVALID_GRANT, "授权码无效或已过期");
        }

        // 6. 校验授权码归属的客户端与请求一致
        String codeClientId = codeData.get("client_id");
        if (!req.getClientId().equals(codeClientId)) {
            throw new Oauth2Exception(Oauth2Constants.ERROR_INVALID_GRANT, "client_id 不匹配");
        }

        // 7. 若授权码携带 redirect_uri，则校验与请求一致
        String codeRedirectUri = codeData.get("redirect_uri");
        if (StrUtil.isNotBlank(codeRedirectUri)) {
            if (!codeRedirectUri.equals(req.getRedirectUri())) {
                throw new Oauth2Exception(Oauth2Constants.ERROR_INVALID_GRANT, "redirect_uri 不匹配");
            }
        }

        // 8. PKCE 校验
        String codeChallenge = codeData.get("code_challenge");
        if (StrUtil.isNotBlank(codeChallenge)) {
            if (StrUtil.isBlank(req.getCodeVerifier())) {
                throw new Oauth2Exception(Oauth2Constants.ERROR_INVALID_REQUEST, "code_verifier 不能为空");
            }
            String method = codeData.get("code_challenge_method");
            if (StrUtil.isBlank(method)) {
                method = Oauth2Constants.CODE_CHALLENGE_METHOD_PLAIN;
            }
            verifyPkce(req.getCodeVerifier(), codeChallenge, method);
        }

        // 9. 颁发令牌对（含 nonce 用于 id_token）
        String userId = codeData.get("user_id");
        String scope = codeData.get("scope");
        String nonce = codeData.get("nonce");
        Long userIdLong = StrUtil.isNotBlank(userId) ? Long.valueOf(userId) : null;
        return tokenService.issueTokenPair(app, userIdLong, scope, GrantTypeEnum.AUTHORIZATION_CODE.getValue(), nonce);
    }

    @Override
    public GrantTypeEnum getGrantType() {
        return GrantTypeEnum.AUTHORIZATION_CODE;
    }

    /**
     * 校验 PKCE code_verifier 与 code_challenge 是否匹配
     *
     * @param codeVerifier  客户端提交的 verifier
     * @param codeChallenge 授权码中存储的 challenge
     * @param method        挑战方法（S256 或 plain）
     */
    private void verifyPkce(String codeVerifier, String codeChallenge, String method) {
        String computed;
        if (Oauth2Constants.CODE_CHALLENGE_METHOD_S256.equals(method)) {
            byte[] sha256Bytes = DigestUtil.sha256(codeVerifier.getBytes(StandardCharsets.US_ASCII));
            computed = Base64.getUrlEncoder().withoutPadding().encodeToString(sha256Bytes);
        } else {
            // plain 模式：verifier 直接与 challenge 比对
            computed = codeVerifier;
        }
        if (!computed.equals(codeChallenge)) {
            throw new Oauth2Exception(Oauth2Constants.ERROR_INVALID_GRANT, "code_verifier 校验失败");
        }
    }
}
