package top.wain.heimdall.tenant.model.resp;

import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;

/**
 * 租户详情响应参数
 *
 * @author 小熊
 * @author WainZeng
 * @since 2024/11/26 17:20
 */
@Data
@Schema(description = "租户详情响应参数")
public class TenantDetailResp extends TenantResp {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 租户管理员
     */
    @Schema(description = "租户管理员", example = "666")
    @ExcelProperty(value = "租户管理员", order = 11)
    private Long adminUser;
}