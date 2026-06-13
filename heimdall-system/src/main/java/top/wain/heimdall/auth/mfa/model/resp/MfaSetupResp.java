package top.wain.heimdall.auth.mfa.model.resp;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @Description: MFA 绑定初始化响应
 * @Author: WainZeng
 * @Date: 2026/06/13
 */
@Data
@Schema(description = "MFA 绑定初始化响应")
public class MfaSetupResp {

    @Schema(description = "TOTP 密钥（Base64）")
    private String secret;

    @Schema(description = "otpauth URI（用于生成二维码）")
    private String qrcodeUri;
}
