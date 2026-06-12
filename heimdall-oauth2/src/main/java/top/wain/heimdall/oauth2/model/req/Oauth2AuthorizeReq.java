package top.wain.heimdall.oauth2.model.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * @Description: OAuth2 授权请求参数
 * @Author: WainZeng
 * @Date: 2026/06/11
 */
@Data
@Schema(description = "OAuth2 授权请求参数")
public class Oauth2AuthorizeReq {

    @Schema(description = "响应类型（code）")
    @NotBlank(message = "response_type 不能为空")
    private String responseType;

    @Schema(description = "客户端标识")
    @NotBlank(message = "client_id 不能为空")
    private String clientId;

    @Schema(description = "回调地址")
    private String redirectUri;
    @Schema(description = "请求的授权范围，空格分隔")
    private String scope;
    @Schema(description = "客户端状态值，用于防止 CSRF")
    private String state;
    @Schema(description = "PKCE code_challenge")
    private String codeChallenge;
    @Schema(description = "PKCE 验证方法（S256 / plain）")
    private String codeChallengeMethod;
}
