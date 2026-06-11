package top.wain.heimdall.oauth2.service;

import top.wain.heimdall.oauth2.model.entity.Oauth2AppDO;

/**
 * @Description: OAuth2 客户端校验接口，负责 clientId、secret、redirectUri、scope、grantType 及 PKCE 的合法性验证
 * @Author: WainZeng
 * @Date: 2026/06/11
 */
public interface Oauth2ClientValidator {

    /**
     * 根据 clientId 查找应用，校验存在且已启用
     *
     * @param clientId 客户端 ID
     * @return 应用实体
     */
    Oauth2AppDO validateClientId(String clientId);

    /**
     * 校验客户端密钥是否与应用的活跃密钥匹配
     *
     * @param app          应用实体
     * @param clientSecret 待校验密钥明文
     */
    void validateClientSecret(Oauth2AppDO app, String clientSecret);

    /**
     * 校验回调地址是否在应用已注册列表中
     *
     * @param app         应用实体
     * @param redirectUri 待校验回调地址
     */
    void validateRedirectUri(Oauth2AppDO app, String redirectUri);

    /**
     * 校验请求的 scope 是否均在应用允许范围内（空格分隔）
     *
     * @param app   应用实体
     * @param scope 待校验 scope，空格分隔
     */
    void validateScope(Oauth2AppDO app, String scope);

    /**
     * 校验授权类型是否在应用允许列表中
     *
     * @param app       应用实体
     * @param grantType 待校验授权类型
     */
    void validateGrantType(Oauth2AppDO app, String grantType);

    /**
     * 校验 PKCE 参数：公开客户端必须携带 codeChallenge；若携带则方法只允许 S256 或 plain
     *
     * @param app                 应用实体
     * @param codeChallenge       PKCE 挑战码
     * @param codeChallengeMethod PKCE 方法
     */
    void validatePkce(Oauth2AppDO app, String codeChallenge, String codeChallengeMethod);
}
