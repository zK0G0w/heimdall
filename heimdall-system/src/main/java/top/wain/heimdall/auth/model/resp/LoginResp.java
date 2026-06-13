package top.wain.heimdall.auth.model.resp;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 登录响应参数
 *
 * @author WainZeng
 * @since 2022/12/21 20:42
 */
@Data
@Builder
@Schema(description = "登录响应参数")
public class LoginResp implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "令牌")
    private String token;

    @Schema(description = "租户 ID")
    private Long tenantId;

    @Schema(description = "是否需要 MFA 验证")
    private Boolean requiresMfa;

    @Schema(description = "是否需要先绑定 MFA")
    private Boolean requiresMfaSetup;

    @Schema(description = "MFA 临时凭证")
    private String mfaChallengeToken;
}
