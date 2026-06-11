package top.wain.heimdall.oauth2.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import top.wain.heimdall.oauth2.constant.Oauth2Constants;
import top.wain.heimdall.oauth2.model.dto.Oauth2TokenDTO;
import top.wain.heimdall.oauth2.model.entity.Oauth2AppDO;
import top.wain.heimdall.oauth2.service.Oauth2TokenService;
import top.wain.heimdall.oauth2.store.RedisOauth2TokenStore;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * @Description: OAuth2 令牌生命周期管理 Service 实现
 * @Author: WainZeng
 * @Date: 2026/06/11
 */
@Service
@RequiredArgsConstructor
public class Oauth2TokenServiceImpl implements Oauth2TokenService {

    private final RedisOauth2TokenStore tokenStore;

    @Override
    public Oauth2TokenDTO issueTokenPair(Oauth2AppDO app, Long userId, String scope, String grantType) {
        // 取应用自定义 TTL，未配置则使用系统默认值
        int accessTtl = app.getAccessTokenTtl() != null
            ? app.getAccessTokenTtl()
            : Oauth2Constants.DEFAULT_ACCESS_TOKEN_TTL;
        int refreshTtl = app.getRefreshTokenTtl() != null
            ? app.getRefreshTokenTtl()
            : Oauth2Constants.DEFAULT_REFRESH_TOKEN_TTL;

        String accessToken = tokenStore.storeAccessToken(app.getClientId(), userId, scope, grantType, accessTtl);
        String refreshToken = tokenStore.storeRefreshToken(app.getClientId(), userId, scope, accessToken, refreshTtl);

        return buildTokenDTO(accessToken, refreshToken, app.getClientId(), userId, scope, grantType, accessTtl);
    }

    @Override
    public Oauth2TokenDTO issueAccessTokenOnly(Oauth2AppDO app, String scope, String grantType) {
        // client_credentials 模式：无用户主体，不颁发 refresh_token
        int accessTtl = app.getAccessTokenTtl() != null
            ? app.getAccessTokenTtl()
            : Oauth2Constants.DEFAULT_ACCESS_TOKEN_TTL;

        String accessToken = tokenStore.storeAccessToken(app.getClientId(), null, scope, grantType, accessTtl);

        return buildTokenDTO(accessToken, null, app.getClientId(), null, scope, grantType, accessTtl);
    }

    @Override
    public Map<String, String> introspect(String accessToken) {
        return tokenStore.getAccessToken(accessToken);
    }

    @Override
    public void revoke(String token) {
        // 优先尝试作为 access_token 撤销
        Map<String, String> accessTokenData = tokenStore.getAccessToken(token);
        if (accessTokenData != null) {
            tokenStore.revokeAccessToken(token);
            return;
        }
        // 否则尝试作为 refresh_token 撤销
        Map<String, String> refreshTokenData = tokenStore.getRefreshToken(token);
        if (refreshTokenData != null) {
            // 同时撤销关联的 access_token
            String associatedAccessToken = refreshTokenData.get("access_token");
            if (associatedAccessToken != null && !associatedAccessToken.isEmpty()) {
                tokenStore.revokeAccessToken(associatedAccessToken);
            }
            tokenStore.removeRefreshToken(token);
        }
    }

    @Override
    public Oauth2TokenDTO refresh(String refreshToken, Oauth2AppDO app) {
        Map<String, String> refreshTokenData = tokenStore.getRefreshToken(refreshToken);
        if (refreshTokenData == null) {
            // refresh_token 不存在或已过期
            return null;
        }

        // 提取旧令牌关联信息
        String oldAccessToken = refreshTokenData.get("access_token");
        String userId = refreshTokenData.get("user_id");
        String scope = refreshTokenData.get("scope");

        // 作废旧 access_token 和 refresh_token
        if (oldAccessToken != null && !oldAccessToken.isEmpty()) {
            tokenStore.revokeAccessToken(oldAccessToken);
        }
        tokenStore.removeRefreshToken(refreshToken);

        // 颁发新令牌对
        Long userIdLong = (userId != null && !userId.isEmpty()) ? Long.valueOf(userId) : null;
        return issueTokenPair(app, userIdLong, scope, "refresh_token");
    }

    /**
     * 构建令牌 DTO
     */
    private Oauth2TokenDTO buildTokenDTO(String accessToken,
                                         String refreshToken,
                                         String clientId,
                                         Long userId,
                                         String scope,
                                         String grantType,
                                         int accessTtl) {
        LocalDateTime now = LocalDateTime.now();
        Oauth2TokenDTO dto = new Oauth2TokenDTO();
        dto.setAccessToken(accessToken);
        dto.setRefreshToken(refreshToken);
        dto.setClientId(clientId);
        dto.setUserId(userId);
        dto.setScope(scope);
        dto.setGrantType(grantType);
        dto.setExpiresIn(accessTtl);
        dto.setIssuedAt(now);
        dto.setExpiresAt(now.plusSeconds(accessTtl));
        return dto;
    }
}
