package top.wain.heimdall.tenant.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 套餐和菜单关联实体
 *
 * @author WainZeng
 * @since 2025/7/11 22:01
 */
@Data
@NoArgsConstructor
@TableName("tenant_package_menu")
public class PackageMenuDO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 套餐 ID
     */
    private Long packageId;

    /**
     * 菜单 ID
     */
    private Long menuId;

    public PackageMenuDO(Long packageId, Long menuId) {
        this.packageId = packageId;
        this.menuId = menuId;
    }
}