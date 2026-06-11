package top.wain.heimdall.oauth2.service.impl;

import cn.hutool.core.util.StrUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import top.wain.heimdall.common.enums.DisEnableStatusEnum;
import top.wain.heimdall.oauth2.constant.Oauth2Constants;
import top.wain.heimdall.oauth2.exception.Oauth2Exception;
import top.wain.heimdall.oauth2.mapper.Oauth2AppMapper;
import top.wain.heimdall.oauth2.mapper.Oauth2AppRedirectUriMapper;
import top.wain.heimdall.oauth2.mapper.Oauth2AppScopeMapper;
import top.wain.heimdall.oauth2.mapper.Oauth2AppSecretMapper;
import top.wain.heimdall.oauth2.mapper.Oauth2ScopeMapper;
import top.wain.heimdall.oauth2.model.entity.Oauth2AppDO;
import top.wain.heimdall.oauth2.model.entity.Oauth2AppRedirectUriDO;
import top.wain.heimdall.oauth2.model.entity.Oauth2AppScopeDO;
import top.wain.heimdall.oauth2.model.entity.Oauth2AppSecretDO;
import top.wain.heimdall.oauth2.model.entity.Oauth2ScopeDO;
import top.wain.heimdall.oauth2.service.Oauth2ClientValidator;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @Description: OAuth2 客户端校验实现，对 clientId、secret、redirectUri、scope、grantType 及 PKCE 执行合法性验证
 * @Author: WainZeng
 * @Date: 2026/06/11
 */
@Service
@RequiredArgsConstructor
public class Oauth2ClientValidatorImpl implements Oauth2ClientValidator {

    private final Oauth2AppMapper appMapper;
    private final Oauth2AppSecretMapper secretMapper;
    private final Oauth2AppRedirectUriMapper redirectUriMapper;
    private final Oauth2AppScopeMapper appScopeMapper;
    private final Oauth2ScopeMapper scopeMapper;

    @Override
    public Oauth2AppDO validateClientId(String clientId) {
        if (StrUtil.isBlank(clientId)) {
            throw new Oauth2Exception(Oauth2Constants.ERROR_INVALID_REQUEST, "client_id 不能为空");
        }
        Oauth2AppDO app = appMapper.lambdaQuery().eq(Oauth2AppDO::getClientId, clientId).one();
        if (app == null) {
            throw new Oauth2Exception(Oauth2Constants.ERROR_INVALID_CLIENT, "客户端不存在", 401);
        }
        if (DisEnableStatusEnum.DISABLE.equals(app.getStatus())) {
            throw new Oauth2Exception(Oauth2Constants.ERROR_INVALID_CLIENT, "客户端已被禁用", 401);
        }
        return app;
    }

    @Override
    public void validateClientSecret(Oauth2AppDO app, String clientSecret) {
        if (StrUtil.isBlank(clientSecret)) {
            throw new Oauth2Exception(Oauth2Constants.ERROR_INVALID_REQUEST, "client_secret 不能为空");
        }
        LocalDateTime now = LocalDateTime.now();
        // 查询该应用下所有启用状态的密钥（@FieldEncrypt 由 MyBatis-Plus 自动解密）
        List<Oauth2AppSecretDO> secrets = secretMapper.lambdaQuery()
            .eq(Oauth2AppSecretDO::getAppId, app.getId())
            .eq(Oauth2AppSecretDO::getStatus, DisEnableStatusEnum.ENABLE)
            .list();
        // 过滤未过期的密钥，并与传入值匹配
        boolean matched = secrets.stream()
            .filter(s -> s.getExpiresAt() == null || s.getExpiresAt().isAfter(now))
            .anyMatch(s -> clientSecret.equals(s.getClientSecret()));
        if (!matched) {
            throw new Oauth2Exception(Oauth2Constants.ERROR_INVALID_CLIENT, "client_secret 无效或已过期", 401);
        }
    }

    @Override
    public void validateRedirectUri(Oauth2AppDO app, String redirectUri) {
        // redirectUri 为空时跳过校验（部分流程不要求必传）
        if (StrUtil.isBlank(redirectUri)) {
            return;
        }
        List<Oauth2AppRedirectUriDO> registered = redirectUriMapper.lambdaQuery()
            .eq(Oauth2AppRedirectUriDO::getAppId, app.getId())
            .list();
        boolean matched = registered.stream().anyMatch(r -> redirectUri.equals(r.getUri()));
        if (!matched) {
            throw new Oauth2Exception(Oauth2Constants.ERROR_INVALID_REQUEST, "redirect_uri 未注册");
        }
    }

    @Override
    public void validateScope(Oauth2AppDO app, String scope) {
        // scope 为空时跳过校验
        if (StrUtil.isBlank(scope)) {
            return;
        }
        // 查询应用关联的 scopeId 列表
        List<Oauth2AppScopeDO> appScopes = appScopeMapper.lambdaQuery()
            .eq(Oauth2AppScopeDO::getAppId, app.getId())
            .list();
        if (appScopes.isEmpty()) {
            throw new Oauth2Exception(Oauth2Constants.ERROR_INVALID_SCOPE, "应用未配置任何 scope");
        }
        Set<Long> scopeIds = appScopes.stream().map(Oauth2AppScopeDO::getScopeId).collect(Collectors.toSet());
        // 查询允许的 scopeCode 集合
        List<Oauth2ScopeDO> allowedScopes = scopeMapper.lambdaQuery().in(Oauth2ScopeDO::getId, scopeIds).list();
        Set<String> allowedCodes = allowedScopes.stream().map(Oauth2ScopeDO::getScopeCode).collect(Collectors.toSet());
        // 校验请求的每个 scope 均在允许集合内
        String[] requested = scope.split(" ");
        for (String s : requested) {
            if (!allowedCodes.contains(s)) {
                throw new Oauth2Exception(Oauth2Constants.ERROR_INVALID_SCOPE, "scope [" + s + "] 未在应用允许范围内");
            }
        }
    }

    @Override
    public void validateGrantType(Oauth2AppDO app, String grantType) {
        if (StrUtil.isBlank(grantType)) {
            throw new Oauth2Exception(Oauth2Constants.ERROR_INVALID_REQUEST, "grant_type 不能为空");
        }
        String allowedGrantTypes = app.getAllowedGrantTypes();
        if (StrUtil.isBlank(allowedGrantTypes)) {
            throw new Oauth2Exception(Oauth2Constants.ERROR_UNAUTHORIZED_CLIENT, "应用未配置允许的授权类型");
        }
        Set<String> allowed = Arrays.stream(allowedGrantTypes.split(",")).map(String::trim).collect(Collectors.toSet());
        if (!allowed.contains(grantType)) {
            throw new Oauth2Exception(Oauth2Constants.ERROR_UNAUTHORIZED_CLIENT, "grant_type [" + grantType + "] 未被该应用授权");
        }
    }

    @Override
    public void validatePkce(Oauth2AppDO app, String codeChallenge, String codeChallengeMethod) {
        // 统计活跃密钥数量，无密钥则为公开客户端
        long secretCount = secretMapper.lambdaQuery()
            .eq(Oauth2AppSecretDO::getAppId, app.getId())
            .eq(Oauth2AppSecretDO::getStatus, DisEnableStatusEnum.ENABLE)
            .count();
        boolean isPublicClient = secretCount == 0;

        // 公开客户端必须携带 codeChallenge
        if (isPublicClient && StrUtil.isBlank(codeChallenge)) {
            throw new Oauth2Exception(Oauth2Constants.ERROR_INVALID_REQUEST, "公开客户端必须携带 code_challenge（PKCE）");
        }

        // 若携带了 codeChallenge，校验方法合法性
        if (StrUtil.isNotBlank(codeChallenge)) {
            String method = StrUtil.isBlank(codeChallengeMethod)
                ? Oauth2Constants.CODE_CHALLENGE_METHOD_PLAIN
                : codeChallengeMethod;
            boolean validMethod = Oauth2Constants.CODE_CHALLENGE_METHOD_S256
                .equals(method) || Oauth2Constants.CODE_CHALLENGE_METHOD_PLAIN.equals(method);
            if (!validMethod) {
                throw new Oauth2Exception(Oauth2Constants.ERROR_INVALID_REQUEST, "不支持的 code_challenge_method: " + method);
            }
        }
    }
}
