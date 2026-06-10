package top.wain.heimdall.common.model.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 租户信息
 *
 * @author WainZeng
 * @since 2025/7/23 21:05
 */
@Data
public class TenantDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    private Long id;

    /**
     * 名称
     */
    private String name;

    /**
     * 管理员用户名
     */
    private String adminUsername;

    /**
     * 管理员密码
     */
    private String adminPassword;

    /**
     * 套餐 ID
     */
    private Long packageId;
}
