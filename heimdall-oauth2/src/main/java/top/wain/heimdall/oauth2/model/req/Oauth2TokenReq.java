package top.wain.heimdall.oauth2.model.req;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * @Description: OAuth2 令牌请求参数
 * @Author: WainZeng
 * @Date: 2026/06/11
 */
@Data
public class Oauth2TokenReq {

    @NotBlank(message = "grant_type 不能为空")
    private String grantType;

    private String code;
    private String redirectUri;
    private String clientId;
    private String clientSecret;
    private String codeVerifier;
    private String refreshToken;
    private String scope;
}
