package top.wain.heimdall.oauth2.store;

import cn.hutool.core.util.IdUtil;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RBucket;
import org.redisson.api.RMap;
import org.redisson.api.RSet;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;
import top.wain.heimdall.oauth2.constant.Oauth2Constants;
import top.wain.heimdall.oauth2.model.dto.Oauth2AuthorizationContext;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @Description: OAuth2 Redis 令牌存储层，负责 access_token、refresh_token、授权码、授权请求及授权确认的 Redis 读写操作
 * @Author: WainZeng
 * @Date: 2026/06/11
 */
@Component
@RequiredArgsConstructor
public class RedisOauth2TokenStore {

    private final RedissonClient redissonClient;

    // -------------------------------------------------------------------------
    // Access Token
    // -------------------------------------------------------------------------

    /**
     * 生成并存储 access_token，同时在用户反向索引 Set 中记录该 token
     *
     * @param clientId   客户端 ID
     * @param userId     用户 ID（client_credentials 模式可为 null）
     * @param scope      授权范围
     * @param grantType  授权类型
     * @param ttlSeconds 过期时长（秒）
     * @return 生成的 token 值
     */
    public String storeAccessToken(String clientId, Long userId, String scope, String grantType, int ttlSeconds) {
        String tokenValue = IdUtil.fastSimpleUUID();
        String key = Oauth2Constants.ACCESS_TOKEN_KEY + tokenValue;
        LocalDateTime now = LocalDateTime.now();

        RMap<String, String> map = redissonClient.getMap(key);
        map.put("client_id", clientId);
        map.put("user_id", userId != null ? userId.toString() : "");
        map.put("scope", scope != null ? scope : "");
        map.put("grant_type", grantType != null ? grantType : "");
        map.put("issued_at", now.toString());
        map.put("expires_at", now.plusSeconds(ttlSeconds).toString());
        map.expire(Duration.ofSeconds(ttlSeconds));

        // 写入用户反向索引（便于按用户撤销所有令牌）
        if (userId != null) {
            RSet<String> userTokens = redissonClient.getSet(Oauth2Constants.USER_TOKENS_KEY + userId);
            userTokens.add(tokenValue);
        }
        return tokenValue;
    }

    /**
     * 获取 access_token 的 Hash 字段，不存在则返回 null
     *
     * @param tokenValue token 值
     * @return Hash 字段 Map，或 null
     */
    public Map<String, String> getAccessToken(String tokenValue) {
        RMap<String, String> map = redissonClient.getMap(Oauth2Constants.ACCESS_TOKEN_KEY + tokenValue);
        if (!map.isExists()) {
            return null;
        }
        return map.readAllMap();
    }

    /**
     * 删除 access_token
     *
     * @param tokenValue token 值
     */
    public void removeAccessToken(String tokenValue) {
        redissonClient.getMap(Oauth2Constants.ACCESS_TOKEN_KEY + tokenValue).delete();
    }

    /**
     * 撤销 access_token：删除 token 并从用户反向索引中移除
     *
     * @param tokenValue token 值
     */
    public void revokeAccessToken(String tokenValue) {
        RMap<String, String> map = redissonClient.getMap(Oauth2Constants.ACCESS_TOKEN_KEY + tokenValue);
        String userId = map.get("user_id");
        map.delete();
        if (userId != null && !userId.isEmpty()) {
            RSet<String> userTokens = redissonClient.getSet(Oauth2Constants.USER_TOKENS_KEY + userId);
            userTokens.remove(tokenValue);
        }
    }

    // -------------------------------------------------------------------------
    // Refresh Token
    // -------------------------------------------------------------------------

    /**
     * 生成并存储 refresh_token
     *
     * @param clientId    客户端 ID
     * @param userId      用户 ID
     * @param scope       授权范围
     * @param accessToken 关联的 access_token 值
     * @param ttlSeconds  过期时长（秒）
     * @return 生成的 refresh_token 值
     */
    public String storeRefreshToken(String clientId, Long userId, String scope, String accessToken, int ttlSeconds) {
        String tokenValue = IdUtil.fastSimpleUUID();
        String key = Oauth2Constants.REFRESH_TOKEN_KEY + tokenValue;
        LocalDateTime now = LocalDateTime.now();

        RMap<String, String> map = redissonClient.getMap(key);
        map.put("client_id", clientId);
        map.put("user_id", userId != null ? userId.toString() : "");
        map.put("scope", scope != null ? scope : "");
        map.put("access_token", accessToken != null ? accessToken : "");
        map.put("issued_at", now.toString());
        map.put("expires_at", now.plusSeconds(ttlSeconds).toString());
        map.expire(Duration.ofSeconds(ttlSeconds));
        return tokenValue;
    }

    /**
     * 获取 refresh_token 的 Hash 字段，不存在则返回 null
     *
     * @param tokenValue refresh_token 值
     * @return Hash 字段 Map，或 null
     */
    public Map<String, String> getRefreshToken(String tokenValue) {
        RMap<String, String> map = redissonClient.getMap(Oauth2Constants.REFRESH_TOKEN_KEY + tokenValue);
        if (!map.isExists()) {
            return null;
        }
        return map.readAllMap();
    }

    /**
     * 删除 refresh_token
     *
     * @param tokenValue refresh_token 值
     */
    public void removeRefreshToken(String tokenValue) {
        redissonClient.getMap(Oauth2Constants.REFRESH_TOKEN_KEY + tokenValue).delete();
    }

    // -------------------------------------------------------------------------
    // Authorization Code
    // -------------------------------------------------------------------------

    /**
     * 生成并存储授权码，TTL 为 AUTHORIZATION_CODE_TTL 秒
     *
     * @param context 授权请求上下文
     * @return 生成的授权码
     */
    public String storeAuthorizationCode(Oauth2AuthorizationContext context) {
        String codeValue = IdUtil.fastSimpleUUID();
        String key = Oauth2Constants.AUTHORIZATION_CODE_KEY + codeValue;
        LocalDateTime now = LocalDateTime.now();

        RMap<String, String> map = redissonClient.getMap(key);
        map.put("client_id", context.getClientId() != null ? context.getClientId() : "");
        map.put("user_id", context.getUserId() != null ? context.getUserId().toString() : "");
        map.put("scope", context.getScope() != null ? context.getScope() : "");
        map.put("redirect_uri", context.getRedirectUri() != null ? context.getRedirectUri() : "");
        map.put("code_challenge", context.getCodeChallenge() != null ? context.getCodeChallenge() : "");
        map.put("code_challenge_method", context.getCodeChallengeMethod() != null
            ? context.getCodeChallengeMethod()
            : "");
        map.put("nonce", context.getNonce() != null ? context.getNonce() : "");
        map.put("issued_at", now.toString());
        map.expire(Duration.ofSeconds(Oauth2Constants.AUTHORIZATION_CODE_TTL));
        return codeValue;
    }

    /**
     * 消费授权码（一次性使用）：读取后立即删除，不存在则返回 null
     *
     * @param codeValue 授权码
     * @return Hash 字段 Map，或 null
     */
    public Map<String, String> consumeAuthorizationCode(String codeValue) {
        RMap<String, String> map = redissonClient.getMap(Oauth2Constants.AUTHORIZATION_CODE_KEY + codeValue);
        if (!map.isExists()) {
            return null;
        }
        Map<String, String> data = map.readAllMap();
        map.delete();
        return data;
    }

    // -------------------------------------------------------------------------
    // Auth Request（CIBA / 设备流等异步授权请求暂存）
    // -------------------------------------------------------------------------

    /**
     * 存储授权请求上下文，TTL 为 AUTH_REQUEST_TTL 秒
     *
     * @param context 授权请求上下文
     * @return 生成的 authReqId
     */
    public String storeAuthRequest(Oauth2AuthorizationContext context) {
        String authReqId = IdUtil.fastSimpleUUID();
        String key = Oauth2Constants.AUTH_REQUEST_KEY + authReqId;

        RMap<String, String> map = redissonClient.getMap(key);
        map.put("client_id", context.getClientId() != null ? context.getClientId() : "");
        map.put("redirect_uri", context.getRedirectUri() != null ? context.getRedirectUri() : "");
        map.put("scope", context.getScope() != null ? context.getScope() : "");
        map.put("state", context.getState() != null ? context.getState() : "");
        map.put("response_type", context.getResponseType() != null ? context.getResponseType() : "");
        map.put("code_challenge", context.getCodeChallenge() != null ? context.getCodeChallenge() : "");
        map.put("code_challenge_method", context.getCodeChallengeMethod() != null
            ? context.getCodeChallengeMethod()
            : "");
        map.put("nonce", context.getNonce() != null ? context.getNonce() : "");
        map.expire(Duration.ofSeconds(Oauth2Constants.AUTH_REQUEST_TTL));
        return authReqId;
    }

    /**
     * 获取授权请求上下文，不存在则返回 null
     *
     * @param authReqId 授权请求 ID
     * @return Hash 字段 Map，或 null
     */
    public Map<String, String> getAuthRequest(String authReqId) {
        RMap<String, String> map = redissonClient.getMap(Oauth2Constants.AUTH_REQUEST_KEY + authReqId);
        if (!map.isExists()) {
            return null;
        }
        return map.readAllMap();
    }

    /**
     * 删除授权请求上下文
     *
     * @param authReqId 授权请求 ID
     */
    public void removeAuthRequest(String authReqId) {
        redissonClient.getMap(Oauth2Constants.AUTH_REQUEST_KEY + authReqId).delete();
    }

    // -------------------------------------------------------------------------
    // Consent（用户授权确认记录）
    // -------------------------------------------------------------------------

    /**
     * 存储用户对指定客户端的授权确认 scope
     *
     * @param userId     用户 ID
     * @param clientId   客户端 ID
     * @param scope      已授权范围
     * @param ttlSeconds 过期时长（秒），小于等于 0 表示永不过期
     */
    public void storeConsent(Long userId, String clientId, String scope, int ttlSeconds) {
        String key = Oauth2Constants.CONSENT_KEY + userId + ":" + clientId;
        RBucket<String> bucket = redissonClient.getBucket(key);
        bucket.set(scope != null ? scope : "");
        if (ttlSeconds > 0) {
            bucket.expire(Duration.ofSeconds(ttlSeconds));
        }
    }

    /**
     * 获取用户对指定客户端的授权确认 scope，不存在则返回 null
     *
     * @param userId   用户 ID
     * @param clientId 客户端 ID
     * @return 已授权 scope 字符串，或 null
     */
    public String getConsent(Long userId, String clientId) {
        String key = Oauth2Constants.CONSENT_KEY + userId + ":" + clientId;
        RBucket<String> bucket = redissonClient.getBucket(key);
        return bucket.get();
    }

    /**
     * 删除用户对指定客户端的 consent 记忆
     */
    public void removeConsent(Long userId, String clientId) {
        String key = Oauth2Constants.CONSENT_KEY + userId + ":" + clientId;
        redissonClient.getBucket(key).delete();
    }

    /**
     * 撤销用户在指定客户端下的所有令牌
     */
    public void revokeTokensByClientId(Long userId, String clientId) {
        RSet<String> userTokens = redissonClient.getSet(Oauth2Constants.USER_TOKENS_KEY + userId);
        if (!userTokens.isExists()) {
            return;
        }
        Set<String> toRemove = new HashSet<>();
        for (String tokenValue : userTokens) {
            RMap<String, String> tokenData = redissonClient.getMap(Oauth2Constants.ACCESS_TOKEN_KEY + tokenValue);
            if (!tokenData.isExists()) {
                toRemove.add(tokenValue);
                continue;
            }
            if (clientId.equals(tokenData.get("client_id"))) {
                tokenData.delete();
                toRemove.add(tokenValue);
            }
        }
        userTokens.removeAll(toRemove);
    }

    /**
     * 撤销用户的所有 OAuth2 令牌
     */
    public void revokeAllTokens(Long userId) {
        RSet<String> userTokens = redissonClient.getSet(Oauth2Constants.USER_TOKENS_KEY + userId);
        if (!userTokens.isExists()) {
            return;
        }
        for (String tokenValue : userTokens) {
            RMap<String, String> tokenData = redissonClient.getMap(Oauth2Constants.ACCESS_TOKEN_KEY + tokenValue);
            tokenData.delete();
        }
        userTokens.delete();
    }
}
