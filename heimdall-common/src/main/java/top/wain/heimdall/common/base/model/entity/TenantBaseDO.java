package top.wain.heimdall.common.base.model.entity;

import lombok.Data;

import java.io.Serial;

/**
 * 租户实体类基类
 *
 * <p>
 * 通用字段：ID、创建人、创建时间、修改人、修改时间、租户 ID
 * </p>
 *
 * @author WainZeng
 * @since 2025/7/17 20:20
 */
@Data
public class TenantBaseDO extends BaseDO {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 租户 ID
     */
    private Long tenantId;
}
