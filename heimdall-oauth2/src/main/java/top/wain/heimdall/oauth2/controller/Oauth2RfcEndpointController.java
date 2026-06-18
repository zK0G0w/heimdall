package top.wain.heimdall.oauth2.controller;

import cn.hutool.core.util.StrUtil;
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
import top.wain.heimdall.common.annotation.ExcludeFromGlobalResponse;
import top.wain.heimdall.common.api.system.UserApi;
import top.wain.heimdall.oauth2.constant.Oauth2Constants;
import top.wain.heimdall.oauth2.exception.Oauth2Exception;
import top.wain.heimdall.oauth2.handler.GrantTypeHandler;
import top.wain.heimdall.oauth2.handler.GrantTypeHandlerFactory;
import top.wain.heimdall.oauth2.model.dto.Oauth2TokenDTO;
import top.wain.heimdall.oauth2.model.entity.Oauth2AppDO;
import top.wain.heimdall.oauth2.model.req.Oauth2AuthorizeReq;
import top.wain.heimdall.oauth2.model.req.Oauth2TokenReq;
import top.wain.heimdall.oauth2.model.resp.Oauth2IntrospectResp;
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
 * @Description: OAuth2 RFC 协议端点，实现 RFC 6749 标准授权/令牌/撤销/自省及 OIDC userinfo 端点。
 *               响应格式遵循 OAuth2 协议规范，不经过项目全局 R 包装。
 * @Author: WainZeng
 * @Date: 2026/06/13
 */
@Tag(name = "OAuth2 协议端点 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/oauth2")
@ExcludeFromGlobalResponse
public class Oauth2RfcEndpointController {

    private final Oauth2AuthorizationService authorizationService;
    private final GrantTypeHandlerFactory grantTypeHandlerFactory;
    private final Oauth2TokenService tokenService;
    private final Oauth2ClientValidator clientValidator;
    private final UserApi userApi;

    @Value("${application.url}")
    private String frontendUrl;

    /**
     * 授权端点：校验参数合法性后转发到前端授权页
     */
    @Operation(summary = "授权端点")
    @GetMapping("/authorize")
    public void authorize(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Oauth2AuthorizeReq req = new Oauth2AuthorizeReq();
        req.setResponseType(request.getParameter(Oauth2Constants.PARAM_RESPONSE_TYPE));
        req.setClientId(request.getParameter(Oauth2Constants.PARAM_CLIENT_ID));
        req.setRedirectUri(request.getParameter(Oauth2Constants.PARAM_REDIRECT_URI));
        req.setScope(request.getParameter(Oauth2Constants.PARAM_SCOPE));
        req.setState(request.getParameter(Oauth2Constants.PARAM_STATE));
        req.setCodeChallenge(request.getParameter(Oauth2Constants.PARAM_CODE_CHALLENGE));
        req.setCodeChallengeMethod(request.getParameter(Oauth2Constants.PARAM_CODE_CHALLENGE_METHOD));

        authorizationService.validateAuthorizeRequest(req);
        String authReqId = authorizationService.storeAuthorizeRequest(req);
        response.sendRedirect(frontendUrl + "/oauth2/authorize?auth_req_id=" + authReqId);
    }

    /**
     * 令牌端点：根据 grant_type 颁发 access_token
     */
    @Operation(summary = "令牌端点")
    @PostMapping("/token")
    public Oauth2TokenResp token(HttpServletRequest request) {
        Oauth2TokenReq req = new Oauth2TokenReq();
        req.setGrantType(request.getParameter(Oauth2Constants.PARAM_GRANT_TYPE));
        req.setCode(request.getParameter(Oauth2Constants.PARAM_CODE));
        req.setRedirectUri(request.getParameter(Oauth2Constants.PARAM_REDIRECT_URI));
        req.setClientId(request.getParameter(Oauth2Constants.PARAM_CLIENT_ID));
        req.setClientSecret(request.getParameter(Oauth2Constants.PARAM_CLIENT_SECRET));
        req.setCodeVerifier(request.getParameter(Oauth2Constants.PARAM_CODE_VERIFIER));
        req.setRefreshToken(request.getParameter(Oauth2Constants.PARAM_REFRESH_TOKEN));
        req.setScope(request.getParameter(Oauth2Constants.PARAM_SCOPE));

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
            .idToken(tokenDTO.getIdToken())
            .build();
    }

    /**
     * 令牌撤销端点（RFC 7009）
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
     * 令牌自省端点（RFC 7662）
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
     * 用户信息端点（OIDC）
     */
    @Operation(summary = "用户信息端点")
    @GetMapping("/userinfo")
    public Oauth2UserInfoResp userinfo(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new Oauth2Exception(Oauth2Constants.ERROR_ACCESS_DENIED, "缺少有效的 Bearer Token", 401);
        }
        String accessToken = authHeader.substring(7);
        Map<String, String> info = tokenService.introspect(accessToken);

        Oauth2UserInfoResp resp = new Oauth2UserInfoResp();
        String scope = info.getOrDefault("scope", "");
        String userIdStr = info.get("user_id");

        if (StrUtil.containsAny(scope, "openid") && StrUtil.isNotBlank(userIdStr)) {
            resp.setSub(userIdStr);
        }

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
}
