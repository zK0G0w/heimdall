package top.wain.heimdall.oauth2.model.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * @Description: OAuth2 应用与 Scope 关联实体
 * @Author: WainZeng
 * @Date: 2026/06/10
 */
@Data
@NoArgsConstructor
@TableName("oauth2_app_scope")
public class Oauth2AppScopeDO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId
    private Long appId;
    private Long scopeId;

    public Oauth2AppScopeDO(Long appId, Long scopeId) {
        this.appId = appId;
        this.scopeId = scopeId;
    }
}
