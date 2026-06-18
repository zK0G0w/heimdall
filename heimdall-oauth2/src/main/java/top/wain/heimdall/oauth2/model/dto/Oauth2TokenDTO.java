package top.wain.heimdall.oauth2.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @Description: OAuth2 令牌聚合信息
 * @Author: WainZeng
 * @Date: 2026/06/11
 */
@Data
public class Oauth2TokenDTO {

    @Schema(description = "访问令牌")
    private String accessToken;
    @Schema(description = "刷新令牌")
    private String refreshToken;
    @Schema(description = "客户端标识")
    private String clientId;
    @Schema(description = "用户 ID（client_credentials 模式为 null）")
    private Long userId;
    @Schema(description = "授权范围")
    private String scope;
    @Schema(description = "授权类型")
    private String grantType;
    @Schema(description = "有效期（秒）")
    private Integer expiresIn;
    @Schema(description = "签发时间")
    private LocalDateTime issuedAt;
    @Schema(description = "过期时间")
    private LocalDateTime expiresAt;
    @Schema(description = "ID 令牌")
    private String idToken;
}
