package top.wain.heimdall.oauth2.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import top.wain.heimdall.common.base.model.entity.BaseDO;

import java.io.Serial;

/**
 * @Description: OAuth2 Scope 定义实体
 * @Author: WainZeng
 * @Date: 2026/06/10
 */
@Data
@TableName("oauth2_scope")
public class Oauth2ScopeDO extends BaseDO {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "Scope 标识（如 openid、profile）")
    private String scopeCode;
    @Schema(description = "Scope 显示名称")
    private String scopeName;
    @Schema(description = "描述")
    private String description;
}
