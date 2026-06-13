package top.wain.heimdall.auth.mfa.model.resp;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @Description: MFA 状态响应
 * @Author: WainZeng
 * @Date: 2026/06/13
 */
@Data
@Schema(description = "MFA 状态响应")
public class MfaStatusResp {

    @Schema(description = "是否已绑定 MFA")
    private Boolean enabled;

    @Schema(description = "MFA 类型")
    private String type;

    @Schema(description = "是否被强制要求开启")
    private Boolean forced;

    @Schema(description = "剩余恢复码数量")
    private Integer remainingBackupCodes;
}
