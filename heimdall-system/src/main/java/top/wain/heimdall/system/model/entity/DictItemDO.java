package top.wain.heimdall.system.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import top.wain.heimdall.common.enums.DisEnableStatusEnum;
import top.wain.heimdall.common.base.model.entity.BaseDO;

import java.io.Serial;

/**
 * 字典项实体
 *
 * @author WainZeng
 * @since 2023/9/11 21:29
 */
@Data
@TableName("sys_dict_item")
public class DictItemDO extends BaseDO {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 标签
     */
    private String label;

    /**
     * 值
     */
    private String value;

    /**
     * 标签颜色
     */
    private String color;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 描述
     */
    private String description;

    /**
     * 状态
     */
    private DisEnableStatusEnum status;

    /**
     * 字典ID
     */
    private Long dictId;
}