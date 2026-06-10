package top.wain.heimdall.oauth2.util;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.UUID;

/**
 * @Description: OAuth2 客户端凭证生成工具
 * @Author: WainZeng
 * @Date: 2026/06/10
 */
public final class ClientCredentialGenerator {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private ClientCredentialGenerator() {
    }

    /**
     * 生成 client_id（UUID 去短横，32位十六进制）
     */
    public static String generateClientId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 生成 client_secret（32字节随机数，Base64URL 编码，43字符）
     */
    public static String generateClientSecret() {
        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
