package top.wain.heimdall.oauth2.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import top.wain.heimdall.oauth2.constant.Oauth2Constants;
import top.wain.heimdall.oauth2.exception.Oauth2Exception;
import top.wain.heimdall.oauth2.mapper.Oauth2AppScopeMapper;
import top.wain.heimdall.oauth2.mapper.Oauth2ScopeMapper;
import top.wain.heimdall.oauth2.model.dto.Oauth2AuthorizationContext;
import top.wain.heimdall.oauth2.model.entity.Oauth2AppDO;
import top.wain.heimdall.oauth2.model.entity.Oauth2AppScopeDO;
import top.wain.heimdall.oauth2.model.entity.Oauth2ScopeDO;
import top.wain.heimdall.oauth2.model.req.Oauth2AuthorizeReq;
import top.wain.heimdall.oauth2.model.resp.Oauth2ConsentResp;
import top.wain.heimdall.oauth2.service.Oauth2AuthorizationService;
import top.wain.heimdall.oauth2.service.Oauth2ClientValidator;
import top.wain.heimdall.oauth2.service.Oauth2ConsentService;
import top.wain.heimdall.oauth2.store.RedisOauth2TokenStore;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @Description: OAuth2 授权流程编排实现，负责授权码申请、授权确认页数据获取、用户同意/拒绝授权等流程的编排与协调
 * @Author: WainZeng
 * @Date: 2026/06/11
 */
@Service
@RequiredArgsConstructor
public class Oauth2AuthorizationServiceImpl implements Oauth2AuthorizationService {

    private final Oauth2ClientValidator clientValidator;
    private final Oauth2ConsentService consentService;
    private final RedisOauth2TokenStore tokenStore;
    private final Oauth2AppScopeMapper appScopeMapper;
    private final Oauth2ScopeMapper scopeMapper;

    @Override
    public AuthorizeResult handleAuthorize(Oauth2AuthorizeReq req, Long userId) {
        // 仅支持 code 响应类型
        if (!Oauth2Constants.RESPONSE_TYPE_CODE.equals(req.getResponseType())) {
            throw new Oauth2Exception(Oauth2Constants.ERROR_INVALID_REQUEST, "不支持的 response_type: " + req
                .getResponseType());
        }

        // 校验 clientId、redirectUri、grantType
        Oauth2AppDO app = clientValidator.validateClientId(req.getClientId());
        clientValidator.validateRedirectUri(app, req.getRedirectUri());
        clientValidator.validateGrantType(app, "authorization_code");

        // 确定最终 scope：请求未传则从应用配置中取默认值
        String scope = req.getScope();
        if (StrUtil.isBlank(scope)) {
            scope = getDefaultScope(app);
        }
        if (StrUtil.isNotBlank(scope)) {
            clientValidator.validateScope(app, scope);
        }

        // 校验 PKCE
        clientValidator.validatePkce(app, req.getCodeChallenge(), req.getCodeChallengeMethod());

        // 构建授权上下文
        Oauth2AuthorizationContext context = buildContext(req, app.getClientId(), scope, userId);

        // 检查用户是否已授权过该 scope
        if (consentService.hasConsent(userId, app.getClientId(), scope)) {
            // 已有 consent，直接生成授权码并重定向
            String code = tokenStore.storeAuthorizationCode(context);
            String redirectUrl = buildRedirectUrl(req.getRedirectUri(), code, req.getState());
            return new AuthorizeResult(redirectUrl, null, false);
        }

        // 需要用户授权确认，暂存授权请求并返回 authReqId
        String authReqId = tokenStore.storeAuthRequest(context);
        return new AuthorizeResult(null, authReqId, true);
    }

    @Override
    public Oauth2ConsentResp getConsentData(String authReqId) {
        Map<String, String> authRequest = tokenStore.getAuthRequest(authReqId);
        if (authRequest == null) {
            throw new Oauth2Exception(Oauth2Constants.ERROR_INVALID_REQUEST, "授权请求已过期");
        }

        String clientId = authRequest.get("client_id");
        Oauth2AppDO app = clientValidator.validateClientId(clientId);

        // 构建基础响应
        Oauth2ConsentResp resp = new Oauth2ConsentResp();
        resp.setAppName(app.getAppName());
        resp.setLogo(app.getLogo());
        resp.setAuthReqId(authReqId);

        // 解析 scope 并查询 scope 详情
        String scopeStr = authRequest.get("scope");
        if (StrUtil.isNotBlank(scopeStr)) {
            List<String> scopeCodes = Arrays.stream(scopeStr.split(" "))
                .filter(StrUtil::isNotBlank)
                .collect(Collectors.toList());
            List<Oauth2ScopeDO> scopeDOList = scopeMapper.lambdaQuery()
                .in(Oauth2ScopeDO::getScopeCode, scopeCodes)
                .list();
            List<Oauth2ConsentResp.ScopeItem> scopeItems = scopeDOList.stream().map(s -> {
                Oauth2ConsentResp.ScopeItem item = new Oauth2ConsentResp.ScopeItem();
                item.setCode(s.getScopeCode());
                item.setName(s.getScopeName());
                item.setDescription(s.getDescription());
                return item;
            }).collect(Collectors.toList());
            resp.setScopes(scopeItems);
        }

        return resp;
    }

    @Override
    public String approveConsent(String authReqId, String approvedScope, Long userId) {
        Map<String, String> authRequest = tokenStore.getAuthRequest(authReqId);
        if (authRequest == null) {
            throw new Oauth2Exception(Oauth2Constants.ERROR_INVALID_REQUEST, "授权请求已过期");
        }

        String clientId = authRequest.get("client_id");
        Oauth2AppDO app = clientValidator.validateClientId(clientId);

        // 确定最终生效的 scope：优先使用用户实际勾选的，否则使用原始请求 scope
        String finalScope = StrUtil.isNotBlank(approvedScope) ? approvedScope : authRequest.get("scope");

        // 保存 consent，使用应用配置的 TTL，未配置则使用系统默认值
        int consentTtl = app.getConsentTtl() != null ? app.getConsentTtl() : Oauth2Constants.DEFAULT_CONSENT_TTL;
        consentService.saveConsent(userId, clientId, finalScope, consentTtl);

        // 构建授权上下文并生成授权码
        Oauth2AuthorizationContext context = new Oauth2AuthorizationContext();
        context.setClientId(clientId);
        context.setRedirectUri(authRequest.get("redirect_uri"));
        context.setScope(finalScope);
        context.setState(authRequest.get("state"));
        context.setResponseType(authRequest.get("response_type"));
        context.setCodeChallenge(authRequest.get("code_challenge"));
        context.setCodeChallengeMethod(authRequest.get("code_challenge_method"));
        context.setUserId(userId);

        String code = tokenStore.storeAuthorizationCode(context);

        // 清理暂存的授权请求
        tokenStore.removeAuthRequest(authReqId);

        return buildRedirectUrl(authRequest.get("redirect_uri"), code, authRequest.get("state"));
    }

    @Override
    public String denyConsent(String authReqId) {
        Map<String, String> authRequest = tokenStore.getAuthRequest(authReqId);
        if (authRequest == null) {
            throw new Oauth2Exception(Oauth2Constants.ERROR_INVALID_REQUEST, "授权请求已过期");
        }

        tokenStore.removeAuthRequest(authReqId);

        // 构建携带 access_denied 错误的重定向 URL
        String redirectUri = authRequest.get("redirect_uri");
        String state = authRequest.get("state");
        String separator = redirectUri.contains("?") ? "&" : "?";
        String encodedDesc = URLUtil.encode("用户拒绝授权");
        StringBuilder url = new StringBuilder(redirectUri).append(separator)
            .append("error=")
            .append(Oauth2Constants.ERROR_ACCESS_DENIED)
            .append("&error_description=")
            .append(encodedDesc);
        if (StrUtil.isNotBlank(state)) {
            url.append("&state=").append(state);
        }
        return url.toString();
    }

    // -------------------------------------------------------------------------
    // 私有辅助方法
    // -------------------------------------------------------------------------

    /**
     * 构建授权重定向 URL，附加授权码及可选 state
     */
    private String buildRedirectUrl(String redirectUri, String code, String state) {
        String separator = redirectUri.contains("?") ? "&" : "?";
        StringBuilder url = new StringBuilder(redirectUri).append(separator).append("code=").append(code);
        if (StrUtil.isNotBlank(state)) {
            url.append("&state=").append(state);
        }
        return url.toString();
    }

    /**
     * 获取应用默认 scope（空格分隔），若未配置任何 scope 则返回空字符串
     */
    private String getDefaultScope(Oauth2AppDO app) {
        List<Oauth2AppScopeDO> appScopes = appScopeMapper.lambdaQuery()
            .eq(Oauth2AppScopeDO::getAppId, app.getId())
            .list();
        if (appScopes.isEmpty()) {
            return "";
        }
        Set<Long> scopeIds = appScopes.stream().map(Oauth2AppScopeDO::getScopeId).collect(Collectors.toSet());
        List<Oauth2ScopeDO> scopes = scopeMapper.lambdaQuery().in(Oauth2ScopeDO::getId, scopeIds).list();
        return scopes.stream().map(Oauth2ScopeDO::getScopeCode).collect(Collectors.joining(" "));
    }

    /**
     * 从授权请求对象构建授权上下文
     */
    private Oauth2AuthorizationContext buildContext(Oauth2AuthorizeReq req,
                                                    String clientId,
                                                    String scope,
                                                    Long userId) {
        Oauth2AuthorizationContext context = new Oauth2AuthorizationContext();
        context.setClientId(clientId);
        context.setRedirectUri(req.getRedirectUri());
        context.setScope(scope);
        context.setState(req.getState());
        context.setResponseType(req.getResponseType());
        context.setCodeChallenge(req.getCodeChallenge());
        context.setCodeChallengeMethod(req.getCodeChallengeMethod());
        context.setUserId(userId);
        return context;
    }
}
