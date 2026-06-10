package top.wain.heimdall.oauth2.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
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

    private String scopeCode;
    private String scopeName;
    private String description;
}
