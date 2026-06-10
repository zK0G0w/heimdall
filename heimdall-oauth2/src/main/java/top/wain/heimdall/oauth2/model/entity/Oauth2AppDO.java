package top.wain.heimdall.oauth2.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import top.wain.heimdall.common.base.model.entity.TenantBaseDO;
import top.wain.heimdall.common.enums.DisEnableStatusEnum;
import top.wain.heimdall.oauth2.enums.AppTypeEnum;

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

    private String appName;
    private AppTypeEnum appType;
    private String clientId;
    private String description;
    private String logo;
    private DisEnableStatusEnum status;
    private Integer accessTokenTtl;
    private Integer refreshTokenTtl;
    private Boolean allowSilentAuth;
    private String allowedGrantTypes;
}
