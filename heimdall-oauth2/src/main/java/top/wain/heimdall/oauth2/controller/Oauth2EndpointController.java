package top.wain.heimdall.oauth2.controller;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import top.wain.heimdall.common.api.system.UserApi;
import top.wain.heimdall.oauth2.constant.Oauth2Constants;
import top.wain.heimdall.oauth2.exception.Oauth2Exception;
import top.wain.heimdall.oauth2.handler.GrantTypeHandler;
import top.wain.heimdall.oauth2.handler.GrantTypeHandlerFactory;
import top.wain.heimdall.oauth2.model.dto.Oauth2TokenDTO;
import top.wain.heimdall.oauth2.model.entity.Oauth2AppDO;
import top.wain.heimdall.oauth2.model.req.Oauth2AuthorizeReq;
import top.wain.heimdall.oauth2.model.req.Oauth2TokenReq;
import top.wain.heimdall.oauth2.model.resp.Oauth2AuthorizeResp;
import top.wain.heimdall.oauth2.model.resp.Oauth2ConsentResp;
import top.wain.heimdall.oauth2.model.resp.Oauth2IntrospectResp;
import top.wain.heimdall.oauth2.model.resp.Oauth2RedirectResp;
import top.wain.heimdall.oauth2.model.resp.Oauth2TokenResp;
import top.wain.heimdall.oauth2.model.resp.Oauth2UserInfoResp;
import top.wain.heimdall.oauth2.service.Oauth2AuthorizationService;
import top.wain.heimdall.oauth2.service.Oauth2ClientValidator;
import top.wain.heimdall.oauth2.service.Oauth2TokenService;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

/**
 * @Description: OAuth2 协议端点 Controller，实现 RFC 6749 标准授权/令牌/撤销/自省及 OIDC userinfo 端点
 * @Author: WainZeng
 * @Date: 2026/06/11
 */
@Tag(name = "OAuth2 协议端点 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/oauth2")
public class Oauth2EndpointController {

    private final Oauth2AuthorizationService authorizationService;
    private final GrantTypeHandlerFactory grantTypeHandlerFactory;
    private final Oauth2TokenService tokenService;
    private final Oauth2ClientValidator clientValidator;
    private final UserApi userApi;

    @Value("${application.url}")
    private String frontendUrl;

    /**
     * 授权端点：校验参数合法性后转发到前端授权页
     * JWT 模式下浏览器导航无法携带 token，登录态判断由前端完成
     */
    @Operation(summary = "授权端点")
    @GetMapping("/authorize")
    public void authorize(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // OAuth2 协议参数为 snake_case，手动提取
        Oauth2AuthorizeReq req = new Oauth2AuthorizeReq();
        req.setResponseType(request.getParameter(Oauth2Constants.PARAM_RESPONSE_TYPE));
        req.setClientId(request.getParameter(Oauth2Constants.PARAM_CLIENT_ID));
        req.setRedirectUri(request.getParameter(Oauth2Constants.PARAM_REDIRECT_URI));
        req.setScope(request.getParameter(Oauth2Constants.PARAM_SCOPE));
        req.setState(request.getParameter(Oauth2Constants.PARAM_STATE));
        req.setCodeChallenge(request.getParameter(Oauth2Constants.PARAM_CODE_CHALLENGE));
        req.setCodeChallengeMethod(request.getParameter(Oauth2Constants.PARAM_CODE_CHALLENGE_METHOD));

        // 仅做参数安全校验（client_id 有效、redirect_uri 在白名单），不检查登录态
        authorizationService.validateAuthorizeRequest(req);

        // 暂存授权请求到 Redis，获取 auth_req_id
        String authReqId = authorizationService.storeAuthorizeRequest(req);

        // 无条件跳转前端授权页，由前端判断登录态并驱动后续流程
        response.sendRedirect(frontendUrl + "/oauth2/authorize?auth_req_id=" + authReqId);
    }

    /**
     * 授权确认页数据端点：检查 consent 记忆，已授权则直接返回 redirectUrl，否则返回确认页数据
     */
    @Operation(summary = "获取授权确认页数据")
    @GetMapping("/consent")
    public Oauth2AuthorizeResp getConsentData(@RequestParam("auth_req_id") String authReqId) {
        StpUtil.checkLogin();
        Long userId = StpUtil.getLoginIdAsLong();

        // 先尝试用已有 consent 直接颁发 code
        String redirectUrl = authorizationService.tryDirectAuthorize(authReqId, userId);
        if (redirectUrl != null) {
            Oauth2AuthorizeResp resp = new Oauth2AuthorizeResp();
            resp.setNeedConsent(false);
            resp.setRedirectUrl(redirectUrl);
            return resp;
        }

        // 需要用户确认授权
        Oauth2ConsentResp consentData = authorizationService.getConsentData(authReqId);
        Oauth2AuthorizeResp resp = new Oauth2AuthorizeResp();
        resp.setNeedConsent(true);
        resp.setConsentData(consentData);
        return resp;
    }

    /**
     * 用户同意授权端点：保存 consent 并生成授权码，返回重定向 URL
     */
    @Operation(summary = "用户同意授权")
    @PostMapping("/consent/approve")
    public Oauth2RedirectResp approveConsent(@RequestParam("auth_req_id") String authReqId,
                                             @RequestParam(value = "scope", required = false) String scope) {
        StpUtil.checkLogin();
        Long userId = StpUtil.getLoginIdAsLong();
        String redirectUrl = authorizationService.approveConsent(authReqId, scope, userId);
        return new Oauth2RedirectResp(redirectUrl);
    }

    /**
     * 用户拒绝授权端点：返回携带 access_denied 错误的重定向 URL
     */
    @Operation(summary = "用户拒绝授权")
    @PostMapping("/consent/deny")
    public Oauth2RedirectResp denyConsent(@RequestParam("auth_req_id") String authReqId) {
        StpUtil.checkLogin();
        String redirectUrl = authorizationService.denyConsent(authReqId);
        return new Oauth2RedirectResp(redirectUrl);
    }

    /**
     * 令牌端点：根据 grant_type 颁发 access_token
     */
    @Operation(summary = "令牌端点")
    @PostMapping("/token")
    public Oauth2TokenResp token(HttpServletRequest request) {
        // OAuth2 协议参数为 snake_case，手动提取
        Oauth2TokenReq req = new Oauth2TokenReq();
        req.setGrantType(request.getParameter(Oauth2Constants.PARAM_GRANT_TYPE));
        req.setCode(request.getParameter(Oauth2Constants.PARAM_CODE));
        req.setRedirectUri(request.getParameter(Oauth2Constants.PARAM_REDIRECT_URI));
        req.setClientId(request.getParameter(Oauth2Constants.PARAM_CLIENT_ID));
        req.setClientSecret(request.getParameter(Oauth2Constants.PARAM_CLIENT_SECRET));
        req.setCodeVerifier(request.getParameter(Oauth2Constants.PARAM_CODE_VERIFIER));
        req.setRefreshToken(request.getParameter(Oauth2Constants.PARAM_REFRESH_TOKEN));
        req.setScope(request.getParameter(Oauth2Constants.PARAM_SCOPE));

        // 优先从 Basic Auth 头中提取客户端凭证
        extractBasicAuth(request, req);
        GrantTypeHandler handler = grantTypeHandlerFactory.getHandler(req.getGrantType());
        if (handler == null) {
            throw new Oauth2Exception(Oauth2Constants.ERROR_UNSUPPORTED_GRANT_TYPE, "不支持的授权类型: " + req.getGrantType());
        }
        Oauth2TokenDTO tokenDTO = handler.handle(req);
        return Oauth2TokenResp.builder()
            .accessToken(tokenDTO.getAccessToken())
            .refreshToken(tokenDTO.getRefreshToken())
            .tokenType(Oauth2Constants.TOKEN_TYPE_BEARER)
            .expiresIn(tokenDTO.getExpiresIn())
            .scope(tokenDTO.getScope())
            .build();
    }

    /**
     * 令牌撤销端点（RFC 7009）：撤销 access_token 或 refresh_token
     */
    @Operation(summary = "撤销令牌")
    @PostMapping("/revoke")
    public void revoke(@RequestParam("token") String token,
                       @RequestParam("client_id") String clientId,
                       @RequestParam(value = "client_secret", required = false) String clientSecret) {
        Oauth2AppDO app = clientValidator.validateClientId(clientId);
        if (StrUtil.isNotBlank(clientSecret)) {
            clientValidator.validateClientSecret(app, clientSecret);
        }
        tokenService.revoke(token);
    }

    /**
     * 令牌自省端点（RFC 7662）：查询令牌元数据
     */
    @Operation(summary = "令牌自省")
    @PostMapping("/introspect")
    public Oauth2IntrospectResp introspect(@RequestParam("token") String token,
                                           @RequestParam("client_id") String clientId,
                                           @RequestParam(value = "client_secret", required = false) String clientSecret) {
        Oauth2AppDO app = clientValidator.validateClientId(clientId);
        if (StrUtil.isNotBlank(clientSecret)) {
            clientValidator.validateClientSecret(app, clientSecret);
        }
        Map<String, String> info = tokenService.introspect(token);
        Oauth2IntrospectResp resp = new Oauth2IntrospectResp();
        if (info == null) {
            resp.setActive(false);
            return resp;
        }
        resp.setActive(true);
        resp.setClientId(info.get("client_id"));
        resp.setScope(info.get("scope"));
        resp.setTokenType(Oauth2Constants.TOKEN_TYPE_BEARER);
        if (StrUtil.isNotBlank(info.get("user_id"))) {
            resp.setSub(info.get("user_id"));
        }
        return resp;
    }

    /**
     * 用户信息端点（OIDC）：根据 Bearer Token 返回授权范围内的用户信息
     */
    @Operation(summary = "用户信息端点")
    @GetMapping("/userinfo")
    public Oauth2UserInfoResp userinfo(HttpServletRequest request) {
        // 从 Authorization: Bearer <token> 中提取令牌
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new Oauth2Exception(Oauth2Constants.ERROR_ACCESS_DENIED, "缺少有效的 Bearer Token", 401);
        }
        String accessToken = authHeader.substring(7);
        Map<String, String> info = tokenService.introspect(accessToken);

        Oauth2UserInfoResp resp = new Oauth2UserInfoResp();
        String scope = info.getOrDefault("scope", "");
        String userIdStr = info.get("user_id");

        // openid scope：返回 sub（用户唯一标识）
        if (StrUtil.containsAny(scope, "openid") && StrUtil.isNotBlank(userIdStr)) {
            resp.setSub(userIdStr);
        }

        // profile / email scope：从 UserApi 获取用户详情
        if (StrUtil.isNotBlank(userIdStr) && (scope.contains("profile") || scope.contains("email"))) {
            Long userId = Long.parseLong(userIdStr);
            UserApi.UserInfo userInfo = userApi.getUserInfoById(userId);
            if (userInfo != null) {
                if (scope.contains("profile")) {
                    resp.setNickname(userInfo.nickname());
                    resp.setAvatar(userInfo.avatar());
                }
                if (scope.contains("email")) {
                    resp.setEmail(userInfo.email());
                }
            }
        }
        return resp;
    }

    // -------------------------------------------------------------------------
    // 私有辅助方法
    // -------------------------------------------------------------------------

    /**
     * 从 Basic Auth 头提取 client_id / client_secret，覆盖请求参数中的空值
     */
    private void extractBasicAuth(HttpServletRequest request, Oauth2TokenReq req) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Basic ")) {
            String base64 = authHeader.substring(6);
            String credentials = new String(Base64.getDecoder().decode(base64), StandardCharsets.UTF_8);
            String[] parts = credentials.split(":", 2);
            if (parts.length == 2) {
                if (StrUtil.isBlank(req.getClientId())) {
                    req.setClientId(parts[0]);
                }
                if (StrUtil.isBlank(req.getClientSecret())) {
                    req.setClientSecret(parts[1]);
                }
            }
        }
    }

    /**
     * 根据授权请求参数重建完整的 /oauth2/authorize URL（含请求路径与查询参数）
     */
    private String buildAuthorizeUrl(HttpServletRequest request, Oauth2AuthorizeReq req) {
        StringBuilder sb = new StringBuilder();
        // 取请求的 scheme + host + port + context path
        sb.append(request.getRequestURL());
        sb.append("?response_type=").append(URLUtil.encode(req.getResponseType(), StandardCharsets.UTF_8));
        sb.append("&client_id=").append(URLUtil.encode(req.getClientId(), StandardCharsets.UTF_8));
        if (StrUtil.isNotBlank(req.getRedirectUri())) {
            sb.append("&redirect_uri=").append(URLUtil.encode(req.getRedirectUri(), StandardCharsets.UTF_8));
        }
        if (StrUtil.isNotBlank(req.getScope())) {
            sb.append("&scope=").append(URLUtil.encode(req.getScope(), StandardCharsets.UTF_8));
        }
        if (StrUtil.isNotBlank(req.getState())) {
            sb.append("&state=").append(URLUtil.encode(req.getState(), StandardCharsets.UTF_8));
        }
        if (StrUtil.isNotBlank(req.getCodeChallenge())) {
            sb.append("&code_challenge=").append(URLUtil.encode(req.getCodeChallenge(), StandardCharsets.UTF_8));
        }
        if (StrUtil.isNotBlank(req.getCodeChallengeMethod())) {
            sb.append("&code_challenge_method=")
                .append(URLUtil.encode(req.getCodeChallengeMethod(), StandardCharsets.UTF_8));
        }
        return sb.toString();
    }
}
