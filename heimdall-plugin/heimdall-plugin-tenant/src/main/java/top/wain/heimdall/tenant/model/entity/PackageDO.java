package top.wain.heimdall.tenant.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import top.wain.heimdall.common.base.model.entity.BaseDO;
import top.wain.heimdall.common.enums.DisEnableStatusEnum;
import top.continew.starter.extension.crud.annotation.DictModel;

import java.io.Serial;

/**
 * 套餐实体
 *
 * @author 小熊
 * @author WainZeng
 * @since 2024/11/26 11:25
 */
@Data
@DictModel
@TableName("tenant_package")
public class PackageDO extends BaseDO {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 名称
     */
    private String name;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 菜单选择是否父子节点关联
     */
    private Boolean menuCheckStrictly;

    /**
     * 描述
     */
    private String description;

    /**
     * 状态
     */
    private DisEnableStatusEnum status;
}