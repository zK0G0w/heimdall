package top.wain.heimdall.oauth2.model.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import java.io.Serial;
import java.io.Serializable;

/**
 * @Description: OAuth2 Scope 创建或修改请求参数
 * @Author: WainZeng
 * @Date: 2026/06/10
 */
@Data
@Schema(description = "OAuth2 Scope 创建或修改请求参数")
public class Oauth2ScopeReq implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "Scope 标识", example = "user:read")
    @NotBlank(message = "Scope 标识不能为空")
    @Length(max = 100, message = "Scope 标识长度不能超过 {max} 个字符")
    private String scopeCode;

    @Schema(description = "Scope 名称", example = "读取用户信息")
    @NotBlank(message = "Scope 名称不能为空")
    @Length(max = 100, message = "Scope 名称长度不能超过 {max} 个字符")
    private String scopeName;

    @Schema(description = "描述")
    @Length(max = 500, message = "描述长度不能超过 {max} 个字符")
    private String description;
}
