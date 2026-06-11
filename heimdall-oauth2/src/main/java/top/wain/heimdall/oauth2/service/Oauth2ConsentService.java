package top.wain.heimdall.oauth2.service;

/**
 * @Description: OAuth2 用户授权确认（Consent）管理接口，负责授权记忆的查询、保存与撤销
 * @Author: WainZeng
 * @Date: 2026/06/11
 */
public interface Oauth2ConsentService {

    /**
     * 检查用户是否已对指定客户端授予所有请求的 scope
     *
     * @param userId   用户 ID
     * @param clientId 客户端 ID
     * @param scope    请求的 scope，空格分隔
     * @return true 表示已全部授权，false 表示需重新确认
     */
    boolean hasConsent(Long userId, String clientId, String scope);

    /**
     * 保存用户对指定客户端的授权确认记录
     *
     * @param userId     用户 ID
     * @param clientId   客户端 ID
     * @param scope      授权的 scope，空格分隔
     * @param ttlSeconds 有效期（秒）
     */
    void saveConsent(Long userId, String clientId, String scope, int ttlSeconds);

    /**
     * 撤销用户对指定客户端的授权确认记录
     *
     * @param userId   用户 ID
     * @param clientId 客户端 ID
     */
    void revokeConsent(Long userId, String clientId);
}
