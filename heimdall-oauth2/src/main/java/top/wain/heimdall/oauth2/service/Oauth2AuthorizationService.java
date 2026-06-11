package top.wain.heimdall.oauth2.service;

import top.wain.heimdall.oauth2.model.req.Oauth2AuthorizeReq;
import top.wain.heimdall.oauth2.model.resp.Oauth2ConsentResp;

/**
 * @Description: OAuth2 授权流程编排接口，负责授权码申请、授权确认页数据获取、用户同意/拒绝授权等流程
 * @Author: WainZeng
 * @Date: 2026/06/11
 */
public interface Oauth2AuthorizationService {

    /**
     * 处理授权请求，判断是否需要用户确认授权
     *
     * @param req    授权请求参数
     * @param userId 当前登录用户 ID
     * @return 授权结果：已有 consent 则返回重定向 URL，否则返回 authReqId 要求前端展示授权确认页
     */
    AuthorizeResult handleAuthorize(Oauth2AuthorizeReq req, Long userId);

    /**
     * 获取授权确认页所需数据（应用信息 + scope 列表）
     *
     * @param authReqId 授权请求 ID
     * @return 授权确认页响应数据
     */
    Oauth2ConsentResp getConsentData(String authReqId);

    /**
     * 用户同意授权：保存 consent、生成授权码并返回重定向 URL
     *
     * @param authReqId     授权请求 ID
     * @param approvedScope 用户实际同意的 scope（为空则使用原始请求 scope）
     * @param userId        当前登录用户 ID
     * @return 带授权码的重定向 URL
     */
    String approveConsent(String authReqId, String approvedScope, Long userId);

    /**
     * 用户拒绝授权：返回携带 access_denied 错误的重定向 URL
     *
     * @param authReqId 授权请求 ID
     * @return 带错误信息的重定向 URL
     */
    String denyConsent(String authReqId);

    /**
     * 授权流程结果
     *
     * @param redirectUrl 已有 consent 时的重定向 URL，需要 consent 时为 null
     * @param authReqId   需要 consent 时的授权请求 ID，已有 consent 时为 null
     * @param needConsent 是否需要用户进行授权确认
     */
    record AuthorizeResult(String redirectUrl, String authReqId, boolean needConsent) {
    }
}
