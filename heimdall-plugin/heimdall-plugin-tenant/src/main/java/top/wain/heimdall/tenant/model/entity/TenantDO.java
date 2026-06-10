package top.wain.heimdall.tenant.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import top.wain.heimdall.common.base.model.entity.BaseDO;
import top.wain.heimdall.common.enums.DisEnableStatusEnum;

import java.io.Serial;
import java.time.LocalDateTime;

/**
 * 租户实体
 *
 * @author 小熊
 * @author WainZeng
 * @since 2024/11/26 17:20
 */
@Data
@TableName("tenant")
public class TenantDO extends BaseDO {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 名称
     */
    private String name;

    /**
     * 编码
     */
    private String code;

    /**
     * 域名
     */
    private String domain;

    /**
     * 过期时间
     */
    private LocalDateTime expireTime;

    /**
     * 描述
     */
    private String description;

    /**
     * 状态
     */
    private DisEnableStatusEnum status;

    /**
     * 管理员用户
     */
    private Long adminUser;

    /**
     * 管理员用户名
     */
    private String adminUsername;

    /**
     * 套餐 ID
     */
    private Long packageId;
}