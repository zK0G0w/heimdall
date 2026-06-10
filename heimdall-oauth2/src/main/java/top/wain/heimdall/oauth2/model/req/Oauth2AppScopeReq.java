package top.wain.heimdall.oauth2.model.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * @Description: OAuth2 应用 Scope 批量设置请求参数
 * @Author: WainZeng
 * @Date: 2026/06/10
 */
@Data
@Schema(description = "OAuth2 应用 Scope 批量设置请求参数")
public class Oauth2AppScopeReq implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "Scope ID 列表")
    @NotEmpty(message = "Scope 列表不能为空")
    private List<Long> scopeIds;
}
