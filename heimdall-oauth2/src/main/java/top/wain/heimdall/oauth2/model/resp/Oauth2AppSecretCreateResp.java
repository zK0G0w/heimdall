package top.wain.heimdall.oauth2.model.resp;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @Description: OAuth2 应用密钥创建响应参数（含明文，仅展示一次）
 * @Author: WainZeng
 * @Date: 2026/06/10
 */
@Data
@Schema(description = "OAuth2 应用密钥创建响应参数")
public class Oauth2AppSecretCreateResp implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "密钥 ID")
    private Long id;

    @Schema(description = "密钥明文（仅此一次展示）")
    private String clientSecret;
}
