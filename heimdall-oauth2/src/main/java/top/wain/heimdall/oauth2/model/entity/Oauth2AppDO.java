package top.wain.heimdall.oauth2.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import top.wain.heimdall.common.base.model.entity.TenantBaseDO;
import top.wain.heimdall.common.enums.DisEnableStatusEnum;
import top.wain.heimdall.oauth2.enums.AppTypeEnum;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serial;

/**
 * @Description: OAuth2 应用实体
 * @Author: WainZeng
 * @Date: 2026/06/10
 */
@Data
@TableName("oauth2_app")
public class Oauth2AppDO extends TenantBaseDO {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "应用名称")
    private String appName;
    @Schema(description = "应用类型")
    private AppTypeEnum appType;
    @Schema(description = "客户端标识（系统生成）")
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
    @Schema(description = "是否允许静默授权（跳过 consent 确认）")
    private Boolean allowSilentAuth;
    @Schema(description = "允许的授权类型，逗号分隔")
    private String allowedGrantTypes;
    @Schema(description = "用户授权 Consent 有效期（秒），null 表示使用系统默认值")
    private Integer consentTtl;
}
