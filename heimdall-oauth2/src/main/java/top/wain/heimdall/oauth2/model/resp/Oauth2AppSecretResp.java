package top.wain.heimdall.oauth2.model.resp;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import top.wain.heimdall.common.enums.DisEnableStatusEnum;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @Description: OAuth2 应用密钥列表响应参数（脱敏）
 * @Author: WainZeng
 * @Date: 2026/06/10
 */
@Data
@Schema(description = "OAuth2 应用密钥响应参数")
public class Oauth2AppSecretResp implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "密钥 ID")
    private Long id;

    @Schema(description = "密钥值（脱敏）", example = "abcd****wxyz")
    private String clientSecret;

    @Schema(description = "状态")
    private DisEnableStatusEnum status;

    @Schema(description = "过期时间")
    private LocalDateTime expiresAt;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
