package top.wain.heimdall.oauth2.model.resp;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * @Description: OAuth2 授权确认页数据响应
 * @Author: WainZeng
 * @Date: 2026/06/11
 */
@Data
public class Oauth2ConsentResp {

    @Schema(description = "应用名称")
    private String appName;
    @Schema(description = "应用 Logo")
    private String logo;
    @Schema(description = "授权请求 ID（Redis 中的标识）")
    private String authReqId;
    @Schema(description = "申请的 Scope 列表")
    private List<ScopeItem> scopes;

    @Data
    public static class ScopeItem {
        @Schema(description = "Scope 标识")
        private String code;
        @Schema(description = "Scope 显示名称")
        private String name;
        @Schema(description = "Scope 描述")
        private String description;
    }
}
