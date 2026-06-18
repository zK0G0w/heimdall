package top.wain.heimdall.oauth2.service;

/**
 * @Description: OIDC id_token 生成服务
 * @Author: WainZeng
 * @Date: 2026/06/18
 */
public interface OidcIdTokenService {

    /**
     * 生成 id_token（RS256 签名 JWT）
     *
     * @param userId   用户 ID（sub claim）
     * @param clientId 客户端标识（aud claim）
     * @param nonce    客户端传入的 nonce（可为 null）
     * @param ttl      有效期（秒）
     * @return 签名后的 JWT 字符串
     */
    String generate(Long userId, String clientId, String nonce, int ttl);
}
