package top.wain.heimdall.oauth2.model.resp;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import top.wain.heimdall.common.base.model.resp.BaseDetailResp;
import top.wain.heimdall.common.enums.DisEnableStatusEnum;
import top.wain.heimdall.oauth2.enums.AppTypeEnum;

import java.io.Serial;
import java.util.List;

/**
 * @Description: OAuth2 应用详情响应参数
 * @Author: WainZeng
 * @Date: 2026/06/10
 */
@Data
@Schema(description = "OAuth2 应用详情响应参数")
public class Oauth2AppDetailResp extends BaseDetailResp {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "应用名称")
    private String appName;

    @Schema(description = "应用类型")
    private AppTypeEnum appType;

    @Schema(description = "客户端标识")
    private String clientId;

    @Schema(description = "应用描述")
    private String description;

    @Schema(description = "Logo URL")
    private String logo;

    @Schema(description = "状态")
    private DisEnableStatusEnum status;

    @Schema(description = "Access Token 有效期（秒）")
    private Integer accessTokenTtl;

    @Schema(description = "Refresh Token 有效期（秒）")
    private Integer refreshTokenTtl;

    @Schema(description = "是否允许静默授权")
    private Boolean allowSilentAuth;

    @Schema(description = "允许的授权类型")
    private String allowedGrantTypes;

    @Schema(description = "回调地址列表")
    private List<String> redirectUris;

    @Schema(description = "Scope 列表")
    private List<Oauth2ScopeResp> scopes;
}
