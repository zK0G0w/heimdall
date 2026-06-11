package top.wain.heimdall.oauth2.model.req;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * @Description: OAuth2 授权请求参数
 * @Author: WainZeng
 * @Date: 2026/06/11
 */
@Data
public class Oauth2AuthorizeReq {

    @NotBlank(message = "response_type 不能为空")
    private String responseType;

    @NotBlank(message = "client_id 不能为空")
    private String clientId;

    private String redirectUri;
    private String scope;
    private String state;
    private String codeChallenge;
    private String codeChallengeMethod;
}
