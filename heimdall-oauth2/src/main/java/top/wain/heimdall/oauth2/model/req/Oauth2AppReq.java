package top.wain.heimdall.oauth2.model.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import top.wain.heimdall.oauth2.enums.AppTypeEnum;

import java.io.Serial;
import java.io.Serializable;

/**
 * @Description: OAuth2 应用创建或修改请求参数
 * @Author: WainZeng
 * @Date: 2026/06/10
 */
@Data
@Schema(description = "OAuth2 应用创建或修改请求参数")
public class Oauth2AppReq implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "应用名称", example = "内部OA系统")
    @NotBlank(message = "应用名称不能为空")
    @Length(max = 100, message = "应用名称长度不能超过 {max} 个字符")
    private String appName;

    @Schema(description = "应用类型", example = "1")
    @NotNull(message = "应用类型不能为空")
    private AppTypeEnum appType;

    @Schema(description = "应用描述", example = "公司内部OA办公系统")
    @Length(max = 500, message = "应用描述长度不能超过 {max} 个字符")
    private String description;

    @Schema(description = "Logo URL")
    @Length(max = 500, message = "Logo URL 长度不能超过 {max} 个字符")
    private String logo;

    @Schema(description = "Access Token 有效期（秒）", example = "7200")
    private Integer accessTokenTtl;

    @Schema(description = "Refresh Token 有效期（秒）", example = "604800")
    private Integer refreshTokenTtl;

    @Schema(description = "是否允许静默授权", example = "false")
    private Boolean allowSilentAuth;

    @Schema(description = "允许的授权类型，逗号分隔", example = "authorization_code,refresh_token")
    @NotBlank(message = "授权类型不能为空")
    private String allowedGrantTypes;
}
