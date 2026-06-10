package top.wain.heimdall.open.model.resp;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 应用密钥响应参数
 *
 * @author chengzi
 * @author WainZeng
 * @since 2024/10/17 16:03
 */
@Data
@Schema(description = "应用密钥响应参数")
public class AppSecretResp implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Access Key（访问密钥）
     */
    @Schema(description = "Access Key（访问密钥）", example = "YjUyMGJjYjIxNTE0NDAxMWE1NmRiY2")
    private String accessKey;

    /**
     * Secret Key（私有密钥）
     */
    @Schema(description = "Secret Key（私有密钥）", example = "MDI2YzQ3YTU1NGEyNDM1ZWIwNTU5NmNjNmZjM2M2Nzg=")
    private String secretKey;
}
