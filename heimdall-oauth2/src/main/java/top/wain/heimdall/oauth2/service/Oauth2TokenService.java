package top.wain.heimdall.oauth2.service;

import top.wain.heimdall.oauth2.model.dto.Oauth2TokenDTO;
import top.wain.heimdall.oauth2.model.entity.Oauth2AppDO;

import java.util.Map;

/**
 * @Description: OAuth2 令牌生命周期管理
 * @Author: WainZeng
 * @Date: 2026/06/11
 */
public interface Oauth2TokenService {

    /**
     * 颁发令牌对（access_token + refresh_token）
     */
    Oauth2TokenDTO issueTokenPair(Oauth2AppDO app, Long userId, String scope, String grantType);

    /**
     * 颁发令牌对（含 nonce，用于 OIDC id_token 签发）
     */
    Oauth2TokenDTO issueTokenPair(Oauth2AppDO app, Long userId, String scope, String grantType, String nonce);

    /**
     * 仅颁发 access_token（client_credentials 模式）
     */
    Oauth2TokenDTO issueAccessTokenOnly(Oauth2AppDO app, String scope, String grantType);

    /**
     * 查询 access_token 信息
     */
    Map<String, String> introspect(String accessToken);

    /**
     * 撤销令牌（支持 access_token 和 refresh_token）
     */
    void revoke(String token);

    /**
     * 刷新令牌（旧令牌对失效，颁发新令牌对）
     */
    Oauth2TokenDTO refresh(String refreshToken, Oauth2AppDO app);
}
