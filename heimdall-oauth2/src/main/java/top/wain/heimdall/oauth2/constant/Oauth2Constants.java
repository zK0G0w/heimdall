package top.wain.heimdall.oauth2.constant;

/**
 * @Description: OAuth2 协议层相关常量
 * @Author: WainZeng
 * @Date: 2026/06/11
 */
public class Oauth2Constants {

    // ==================== Redis Key 前缀 ====================

    /** 分隔符 */
    public static final String DELIMITER = ":";

    /** OAuth2 授权码 Redis key 前缀 */
    public static final String AUTH_CODE_KEY_PREFIX = "OAUTH2:AUTH_CODE" + DELIMITER;

    /** OAuth2 Access Token Redis key 前缀 */
    public static final String ACCESS_TOKEN_KEY_PREFIX = "OAUTH2:ACCESS_TOKEN" + DELIMITER;

    /** OAuth2 Refresh Token Redis key 前缀 */
    public static final String REFRESH_TOKEN_KEY_PREFIX = "OAUTH2:REFRESH_TOKEN" + DELIMITER;

    /** OAuth2 用户授权 Consent Redis key 前缀 */
    public static final String CONSENT_KEY_PREFIX = "OAUTH2:CONSENT" + DELIMITER;

    /** OAuth2 PKCE Code Verifier Redis key 前缀 */
    public static final String PKCE_CODE_VERIFIER_KEY_PREFIX = "OAUTH2:PKCE" + DELIMITER;

    // ==================== 默认 TTL（秒） ====================

    /** 授权码默认有效期：10 分钟 */
    public static final int DEFAULT_AUTH_CODE_TTL = 600;

    /** Access Token 默认有效期：2 小时 */
    public static final int DEFAULT_ACCESS_TOKEN_TTL = 7200;

    /** Refresh Token 默认有效期：7 天 */
    public static final int DEFAULT_REFRESH_TOKEN_TTL = 604800;

    /** Consent 默认有效期：30 天 */
    public static final int DEFAULT_CONSENT_TTL = 2592000;

    // ==================== OAuth2 错误码 ====================

    /** 无效请求 */
    public static final String ERROR_INVALID_REQUEST = "invalid_request";

    /** 未授权客户端 */
    public static final String ERROR_UNAUTHORIZED_CLIENT = "unauthorized_client";

    /** 拒绝访问 */
    public static final String ERROR_ACCESS_DENIED = "access_denied";

    /** 不支持的响应类型 */
    public static final String ERROR_UNSUPPORTED_RESPONSE_TYPE = "unsupported_response_type";

    /** 无效的 Scope */
    public static final String ERROR_INVALID_SCOPE = "invalid_scope";

    /** 服务端错误 */
    public static final String ERROR_SERVER_ERROR = "server_error";

    /** 无效客户端 */
    public static final String ERROR_INVALID_CLIENT = "invalid_client";

    /** 无效授权 */
    public static final String ERROR_INVALID_GRANT = "invalid_grant";

    /** 不支持的授权类型 */
    public static final String ERROR_UNSUPPORTED_GRANT_TYPE = "unsupported_grant_type";

    // ==================== 响应类型 ====================

    /** 授权码响应类型 */
    public static final String RESPONSE_TYPE_CODE = "code";

    /** Token 响应类型（隐式流，已不推荐） */
    public static final String RESPONSE_TYPE_TOKEN = "token";

    // ==================== Token 类型 ====================

    /** Bearer Token 类型 */
    public static final String TOKEN_TYPE_BEARER = "Bearer";

    // ==================== PKCE 方法 ====================

    /** PKCE Plain 方法（不推荐，仅兼容） */
    public static final String PKCE_METHOD_PLAIN = "plain";

    /** PKCE S256 方法（推荐） */
    public static final String PKCE_METHOD_S256 = "S256";

    private Oauth2Constants() {
    }
}
