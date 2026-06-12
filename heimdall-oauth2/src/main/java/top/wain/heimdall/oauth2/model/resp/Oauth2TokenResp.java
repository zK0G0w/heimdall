package top.wain.heimdall.oauth2.model.resp;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/**
 * @Description: OAuth2 令牌响应
 * @Author: WainZeng
 * @Date: 2026/06/11
 */
@Data
@Builder
public class Oauth2TokenResp {

    @Schema(description = "访问令牌")
    @JsonProperty("access_token")
    private String accessToken;

    @Schema(description = "刷新令牌")
    @JsonProperty("refresh_token")
    private String refreshToken;

    @Schema(description = "令牌类型（Bearer）")
    @JsonProperty("token_type")
    private String tokenType;

    @Schema(description = "有效期（秒）")
    @JsonProperty("expires_in")
    private Integer expiresIn;

    @Schema(description = "授权范围")
    private String scope;
}
