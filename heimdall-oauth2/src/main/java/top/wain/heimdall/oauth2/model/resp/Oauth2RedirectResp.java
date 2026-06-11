package top.wain.heimdall.oauth2.model.resp;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @Description: OAuth2 重定向响应（approve/deny 端点统一返回）
 * @Author: WainZeng
 * @Date: 2026/06/11
 */
@Data
@Schema(description = "OAuth2 重定向响应")
public class Oauth2RedirectResp implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "重定向 URL")
    private String redirectUrl;

    public Oauth2RedirectResp(String redirectUrl) {
        this.redirectUrl = redirectUrl;
    }
}
