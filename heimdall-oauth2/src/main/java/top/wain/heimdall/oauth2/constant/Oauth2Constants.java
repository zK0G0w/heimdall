package top.wain.heimdall.oauth2.constant;

/**
 * @Description: OAuth2 协议常量
 * @Author: WainZeng
 * @Date: 2026/06/11
 */
public final class Oauth2Constants {

    private Oauth2Constants() {
    }

    // Redis key 前缀
    public static final String KEY_PREFIX = "oauth2:";
    public static final String ACCESS_TOKEN_KEY = KEY_PREFIX + "access_token:";
    public static final String REFRESH_TOKEN_KEY = KEY_PREFIX + "refresh_token:";
    public static final String AUTHORIZATION_CODE_KEY = KEY_PREFIX + "authorization_code:";
    public static final String AUTH_REQUEST_KEY = KEY_PREFIX + "auth_request:";
    public static final String CONSENT_KEY = KEY_PREFIX + "consent:";
    public static final String USER_TOKENS_KEY = KEY_PREFIX + "user_tokens:";

    // 默认 TTL（秒）
    public static final int DEFAULT_ACCESS_TOKEN_TTL = 7200;
    public static final int DEFAULT_REFRESH_TOKEN_TTL = 604800;
    public static final int DEFAULT_CONSENT_TTL = 15552000;
    public static final int AUTHORIZATION_CODE_TTL = 300;
    public static final int AUTH_REQUEST_TTL = 600;

    // OAuth2 错误码
    public static final String ERROR_INVALID_REQUEST = "invalid_request";
    public static final String ERROR_INVALID_CLIENT = "invalid_client";
    public static final String ERROR_INVALID_GRANT = "invalid_grant";
    public static final String ERROR_UNAUTHORIZED_CLIENT = "unauthorized_client";
    public static final String ERROR_UNSUPPORTED_GRANT_TYPE = "unsupported_grant_type";
    public static final String ERROR_INVALID_SCOPE = "invalid_scope";
    public static final String ERROR_ACCESS_DENIED = "access_denied";

    // 响应类型
    public static final String RESPONSE_TYPE_CODE = "code";

    // Token 类型
    public static final String TOKEN_TYPE_BEARER = "Bearer";

    // PKCE 方法
    public static final String CODE_CHALLENGE_METHOD_S256 = "S256";
    public static final String CODE_CHALLENGE_METHOD_PLAIN = "plain";
}
