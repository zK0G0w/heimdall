package top.wain.heimdall.system.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import top.wain.heimdall.common.enums.DisEnableStatusEnum;
import top.wain.heimdall.common.base.model.entity.BaseDO;

import java.io.Serial;

/**
 * 部门实体
 *
 * @author WainZeng
 * @since 2023/1/22 13:50
 */
@Data
@TableName("sys_dept")
public class DeptDO extends BaseDO {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 名称
     */
    private String name;

    /**
     * 上级部门 ID
     */
    private Long parentId;

    /**
     * 祖级列表
     */
    private String ancestors;

    /**
     * 描述
     */
    private String description;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 状态
     */
    private DisEnableStatusEnum status;

    /**
     * 是否为系统内置数据
     */
    private Boolean isSystem;
}
