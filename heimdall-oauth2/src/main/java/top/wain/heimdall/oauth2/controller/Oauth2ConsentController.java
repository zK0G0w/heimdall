package top.wain.heimdall.oauth2.controller;

import cn.dev33.satoken.stp.StpUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import top.wain.heimdall.oauth2.model.resp.Oauth2AuthorizeResp;
import top.wain.heimdall.oauth2.model.resp.Oauth2ConsentResp;
import top.wain.heimdall.oauth2.model.resp.Oauth2RedirectResp;
import top.wain.heimdall.oauth2.service.Oauth2AuthorizationService;

/**
 * @Description: OAuth2 授权确认端点，面向自有前端，响应格式遵循项目 R 规范
 * @Author: WainZeng
 * @Date: 2026/06/13
 */
@Tag(name = "OAuth2 授权确认 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/oauth2/consent")
public class Oauth2ConsentController {

    private final Oauth2AuthorizationService authorizationService;

    /**
     * 获取授权确认页数据：检查 consent 记忆，已授权则直接返回 redirectUrl
     */
    @Operation(summary = "获取授权确认页数据")
    @GetMapping
    public Oauth2AuthorizeResp getConsentData(@RequestParam("auth_req_id") String authReqId) {
        StpUtil.checkLogin();
        Long userId = StpUtil.getLoginIdAsLong();

        String redirectUrl = authorizationService.tryDirectAuthorize(authReqId, userId);
        if (redirectUrl != null) {
            Oauth2AuthorizeResp resp = new Oauth2AuthorizeResp();
            resp.setNeedConsent(false);
            resp.setRedirectUrl(redirectUrl);
            return resp;
        }

        Oauth2ConsentResp consentData = authorizationService.getConsentData(authReqId);
        Oauth2AuthorizeResp resp = new Oauth2AuthorizeResp();
        resp.setNeedConsent(true);
        resp.setConsentData(consentData);
        return resp;
    }

    /**
     * 用户同意授权：保存 consent 并生成授权码，返回重定向 URL
     */
    @Operation(summary = "用户同意授权")
    @PostMapping("/approve")
    public Oauth2RedirectResp approveConsent(@RequestParam("auth_req_id") String authReqId,
                                             @RequestParam(value = "scope", required = false) String scope) {
        StpUtil.checkLogin();
        Long userId = StpUtil.getLoginIdAsLong();
        String redirectUrl = authorizationService.approveConsent(authReqId, scope, userId);
        return new Oauth2RedirectResp(redirectUrl);
    }

    /**
     * 用户拒绝授权：返回携带 access_denied 错误的重定向 URL
     */
    @Operation(summary = "用户拒绝授权")
    @PostMapping("/deny")
    public Oauth2RedirectResp denyConsent(@RequestParam("auth_req_id") String authReqId) {
        StpUtil.checkLogin();
        String redirectUrl = authorizationService.denyConsent(authReqId);
        return new Oauth2RedirectResp(redirectUrl);
    }
}
