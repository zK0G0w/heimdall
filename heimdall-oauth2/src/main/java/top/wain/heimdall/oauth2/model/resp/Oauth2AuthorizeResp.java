package top.wain.heimdall.oauth2.model.resp;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @Description: OAuth2 授权决策响应（consent 端点统一返回）
 * @Author: WainZeng
 * @Date: 2026/06/11
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "OAuth2 授权决策响应")
public class Oauth2AuthorizeResp implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "是否需要用户确认授权")
    private Boolean needConsent;

    @Schema(description = "重定向 URL（needConsent=false 时返回）")
    private String redirectUrl;

    @Schema(description = "授权确认页数据（needConsent=true 时返回）")
    private Oauth2ConsentResp consentData;
}
