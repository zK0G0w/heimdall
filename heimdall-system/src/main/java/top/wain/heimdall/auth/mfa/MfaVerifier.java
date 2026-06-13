package top.wain.heimdall.auth.mfa;

/**
 * @Description: MFA 验证器接口，支持多种验证方式扩展
 * @Author: WainZeng
 * @Date: 2026/06/13
 */
public interface MfaVerifier {

    String type();

    boolean verify(Long userId, String code);
}
