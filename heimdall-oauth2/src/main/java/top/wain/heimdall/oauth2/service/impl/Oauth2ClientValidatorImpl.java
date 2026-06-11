package top.wain.heimdall.oauth2.service.impl;

import cn.hutool.core.util.StrUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import top.continew.starter.core.util.validation.CheckUtils;
import top.continew.starter.core.util.validation.ValidationUtils;
import top.wain.heimdall.common.enums.DisEnableStatusEnum;
import top.wain.heimdall.oauth2.constant.Oauth2Constants;
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
        ValidationUtils.throwIf(StrUtil.isBlank(clientId), "client_id 不能为空");
        Oauth2AppDO app = appMapper.lambdaQuery().eq(Oauth2AppDO::getClientId, clientId).one();
        CheckUtils.throwIfNull(app, "客户端不存在");
        ValidationUtils.throwIf(DisEnableStatusEnum.DISABLE.equals(app.getStatus()), "客户端已被禁用");
        return app;
    }

    @Override
    public void validateClientSecret(Oauth2AppDO app, String clientSecret) {
        ValidationUtils.throwIf(StrUtil.isBlank(clientSecret), "client_secret 不能为空");
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
        ValidationUtils.throwIf(!matched, "client_secret 无效或已过期");
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
        ValidationUtils.throwIf(!matched, "redirect_uri 未注册");
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
            ValidationUtils.throwIf(true, "应用未配置任何 scope");
        }
        Set<Long> scopeIds = appScopes.stream().map(Oauth2AppScopeDO::getScopeId).collect(Collectors.toSet());
        // 查询允许的 scopeCode 集合
        List<Oauth2ScopeDO> allowedScopes = scopeMapper.lambdaQuery().in(Oauth2ScopeDO::getId, scopeIds).list();
        Set<String> allowedCodes = allowedScopes.stream().map(Oauth2ScopeDO::getScopeCode).collect(Collectors.toSet());
        // 校验请求的每个 scope 均在允许集合内
        String[] requested = scope.split(" ");
        for (String s : requested) {
            ValidationUtils.throwIf(!allowedCodes.contains(s), "scope [{}] 未在应用允许范围内", s);
        }
    }

    @Override
    public void validateGrantType(Oauth2AppDO app, String grantType) {
        ValidationUtils.throwIf(StrUtil.isBlank(grantType), "grant_type 不能为空");
        String allowedGrantTypes = app.getAllowedGrantTypes();
        ValidationUtils.throwIf(StrUtil.isBlank(allowedGrantTypes), "应用未配置允许的授权类型");
        Set<String> allowed = Arrays.stream(allowedGrantTypes.split(",")).map(String::trim).collect(Collectors.toSet());
        ValidationUtils.throwIf(!allowed.contains(grantType), "grant_type [{}] 未被该应用授权", grantType);
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
        ValidationUtils.throwIf(isPublicClient && StrUtil.isBlank(codeChallenge), "公开客户端必须携带 code_challenge（PKCE）");

        // 若携带了 codeChallenge，校验方法合法性
        if (StrUtil.isNotBlank(codeChallenge)) {
            String method = StrUtil.isBlank(codeChallengeMethod)
                ? Oauth2Constants.CODE_CHALLENGE_METHOD_PLAIN
                : codeChallengeMethod;
            boolean validMethod = Oauth2Constants.CODE_CHALLENGE_METHOD_S256
                .equals(method) || Oauth2Constants.CODE_CHALLENGE_METHOD_PLAIN.equals(method);
            ValidationUtils.throwIf(!validMethod, "不支持的 code_challenge_method: {}", method);
        }
    }
}
