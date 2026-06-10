package top.wain.heimdall.oauth2.model.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * @Description: OAuth2 应用回调地址批量设置请求参数
 * @Author: WainZeng
 * @Date: 2026/06/10
 */
@Data
@Schema(description = "OAuth2 应用回调地址批量设置请求参数")
public class Oauth2AppRedirectUriReq implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "回调地址列表")
    @NotEmpty(message = "回调地址列表不能为空")
    private List<String> uris;
}
