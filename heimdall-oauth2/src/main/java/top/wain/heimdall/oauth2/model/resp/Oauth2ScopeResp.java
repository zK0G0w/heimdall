package top.wain.heimdall.oauth2.model.resp;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import top.wain.heimdall.common.base.model.resp.BaseResp;

import java.io.Serial;

/**
 * @Description: OAuth2 Scope 响应参数
 * @Author: WainZeng
 * @Date: 2026/06/10
 */
@Data
@Schema(description = "OAuth2 Scope 响应参数")
public class Oauth2ScopeResp extends BaseResp {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "Scope 标识")
    private String scopeCode;

    @Schema(description = "Scope 名称")
    private String scopeName;

    @Schema(description = "描述")
    private String description;
}
