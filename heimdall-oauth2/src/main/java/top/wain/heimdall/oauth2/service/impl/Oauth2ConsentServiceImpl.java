package top.wain.heimdall.oauth2.service.impl;

import cn.hutool.core.util.StrUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import top.wain.heimdall.oauth2.service.Oauth2ConsentService;
import top.wain.heimdall.oauth2.store.RedisOauth2TokenStore;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @Description: OAuth2 用户授权确认（Consent）管理实现，基于 Redis 存储授权记忆，支持查询、保存与撤销
 * @Author: WainZeng
 * @Date: 2026/06/11
 */
@Service
@RequiredArgsConstructor
public class Oauth2ConsentServiceImpl implements Oauth2ConsentService {

    private final RedisOauth2TokenStore tokenStore;

    @Override
    public boolean hasConsent(Long userId, String clientId, String scope) {
        String stored = tokenStore.getConsent(userId, clientId);
        if (StrUtil.isBlank(stored)) {
            return false;
        }
        // 存储格式为逗号分隔，请求格式为空格分隔
        Set<String> grantedScopes = Arrays.stream(stored.split(","))
            .map(String::trim)
            .filter(StrUtil::isNotBlank)
            .collect(Collectors.toSet());
        String[] requestedScopes = scope.split(" ");
        for (String s : requestedScopes) {
            if (StrUtil.isBlank(s)) {
                continue;
            }
            if (!grantedScopes.contains(s)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void saveConsent(Long userId, String clientId, String scope, int ttlSeconds) {
        // 将空格分隔的 scope 转换为逗号分隔后存储
        String normalized = scope.trim().replaceAll("\\s+", ",");
        tokenStore.storeConsent(userId, clientId, normalized, ttlSeconds);
    }

    @Override
    public void revokeConsent(Long userId, String clientId) {
        // TTL 为 1 秒，立即过期等效于删除
        tokenStore.storeConsent(userId, clientId, "", 1);
    }
}
