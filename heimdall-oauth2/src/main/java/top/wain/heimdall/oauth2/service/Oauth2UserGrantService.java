package top.wain.heimdall.oauth2.service;

import top.wain.heimdall.oauth2.model.resp.Oauth2UserGrantResp;

import java.util.List;

/**
 * @Description: 用户 OAuth2 授权记录管理
 * @Author: WainZeng
 * @Date: 2026/06/13
 */
public interface Oauth2UserGrantService {

    /**
     * 查询用户已授权的应用列表
     */
    List<Oauth2UserGrantResp> listByUserId(Long userId);

    /**
     * 记录用户授权（approveConsent 时调用）
     */
    void saveOrUpdateGrant(Long userId, Long appId, String clientId, String scope);

    /**
     * 撤销指定应用的授权
     */
    void revokeGrant(Long userId, Long appId);

    /**
     * 撤销用户所有应用的授权
     */
    void revokeAllGrants(Long userId);
}
