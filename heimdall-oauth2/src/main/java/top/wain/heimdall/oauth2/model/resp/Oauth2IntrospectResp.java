package top.wain.heimdall.oauth2.model.resp;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @Description: OAuth2 令牌自省响应（RFC 7662）
 * @Author: WainZeng
 * @Date: 2026/06/11
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "OAuth2 令牌自省响应")
public class Oauth2IntrospectResp implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "令牌是否有效")
    private Boolean active;

    @Schema(description = "客户端 ID")
    @JsonProperty("client_id")
    private String clientId;

    @Schema(description = "授权范围")
    private String scope;

    @Schema(description = "令牌类型")
    @JsonProperty("token_type")
    private String tokenType;

    @Schema(description = "用户标识")
    private String sub;
}
