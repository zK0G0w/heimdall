package top.wain.heimdall.tenant.model.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import top.wain.heimdall.common.enums.DisEnableStatusEnum;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 套餐创建或修改请求参数
 *
 * @author 小熊
 * @author WainZeng
 * @since 2024/11/26 11:25
 */
@Data
@Schema(description = "套餐创建或修改请求参数")
public class PackageReq implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 名称
     */
    @Schema(description = "名称", example = "初级套餐")
    @NotBlank(message = "名称不能为空")
    @Length(max = 30, message = "名称长度不能超过 {max} 个字符")
    private String name;

    /**
     * 排序
     */
    @Schema(description = "排序", example = "1")
    private Integer sort;

    /**
     * 菜单选择是否父子节点关联
     */
    @Schema(description = "菜单选择是否父子节点关联", example = "true")
    private Boolean menuCheckStrictly;

    /**
     * 描述
     */
    @Schema(description = "描述", example = "初级套餐描述")
    @Length(max = 200, message = "描述长度不能超过 {max} 个字符")
    private String description;

    /**
     * 状态
     */
    @Schema(description = "状态", example = "1")
    private DisEnableStatusEnum status;

    /**
     * 关联的菜单 ID 列表
     */
    @Schema(description = "关联的菜单 ID 列表", example = "[1000, 1010, 1011]")
    @NotEmpty(message = "关联菜单不能为空")
    private List<Long> menuIds;
}