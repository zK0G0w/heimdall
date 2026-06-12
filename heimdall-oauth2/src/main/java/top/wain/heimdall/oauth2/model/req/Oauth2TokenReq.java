package top.wain.heimdall.oauth2.model.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * @Description: OAuth2 令牌请求参数
 * @Author: WainZeng
 * @Date: 2026/06/11
 */
@Data
@Schema(description = "OAuth2 令牌请求参数")
public class Oauth2TokenReq {

    @Schema(description = "授权类型")
    @NotBlank(message = "grant_type 不能为空")
    private String grantType;

    @Schema(description = "授权码（authorization_code 模式）")
    private String code;
    @Schema(description = "回调地址（需与授权请求一致）")
    private String redirectUri;
    @Schema(description = "客户端标识")
    private String clientId;
    @Schema(description = "客户端密钥")
    private String clientSecret;
    @Schema(description = "PKCE 验证码")
    private String codeVerifier;
    @Schema(description = "刷新令牌（refresh_token 模式）")
    private String refreshToken;
    @Schema(description = "请求的授权范围")
    private String scope;
}
