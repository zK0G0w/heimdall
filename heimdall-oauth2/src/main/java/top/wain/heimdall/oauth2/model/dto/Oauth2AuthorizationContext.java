package top.wain.heimdall.oauth2.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @Description: OAuth2 授权请求上下文
 * @Author: WainZeng
 * @Date: 2026/06/11
 */
@Data
public class Oauth2AuthorizationContext {

    @Schema(description = "客户端标识")
    private String clientId;
    @Schema(description = "回调地址")
    private String redirectUri;
    @Schema(description = "授权范围，空格分隔")
    private String scope;
    @Schema(description = "客户端状态值，原样回传")
    private String state;
    @Schema(description = "响应类型（code）")
    private String responseType;
    @Schema(description = "PKCE code_challenge")
    private String codeChallenge;
    @Schema(description = "PKCE 验证方法（S256 / plain）")
    private String codeChallengeMethod;
    @Schema(description = "当前登录用户 ID")
    private Long userId;
}
