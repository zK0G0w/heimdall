package top.wain.heimdall.tenant.model.resp;

import cn.crane4j.annotation.AssembleMethod;
import cn.crane4j.annotation.ContainerMethod;
import cn.crane4j.annotation.MappingType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import top.wain.heimdall.tenant.service.PackageMenuService;

import java.io.Serial;
import java.util.List;

/**
 * 套餐详情响应参数
 *
 * @author 小熊
 * @author WainZeng
 * @since 2024/11/26 11:25
 */
@Data
@Schema(description = "套餐详情响应参数")
@AssembleMethod(key = "id", prop = ":menuIds", targetType = PackageMenuService.class, method = @ContainerMethod(bindMethod = "listMenuIdsByPackageId", type = MappingType.ORDER_OF_KEYS))
public class PackageDetailResp extends PackageResp {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 关联的菜单 ID 列表
     */
    @Schema(description = "关联的菜单 ID 列表", example = "[1000, 1010, 1011]")
    private List<Long> menuIds;
}