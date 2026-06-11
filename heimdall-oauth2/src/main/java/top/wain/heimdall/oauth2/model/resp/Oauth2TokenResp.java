package top.wain.heimdall.oauth2.model.resp;

import com.fasterxml.jackson.annotation.JsonProperty;
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

    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("refresh_token")
    private String refreshToken;

    @JsonProperty("token_type")
    private String tokenType;

    @JsonProperty("expires_in")
    private Integer expiresIn;

    private String scope;
}
