package top.wain.heimdall.tenant.model.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import top.continew.starter.data.annotation.Query;
import top.continew.starter.data.enums.QueryType;

import java.io.Serial;
import java.io.Serializable;

/**
 * 租户查询条件
 *
 * @author 小熊
 * @author WainZeng
 * @since 2024/11/26 17:20
 */
@Data
@Schema(description = "租户查询条件")
public class TenantQuery implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 关键词
     */
    @Schema(description = "关键词", example = "Xxx租户")
    @Query(columns = {"name", "description"}, type = QueryType.LIKE)
    private String description;

    /**
     * 编码
     */
    @Schema(description = "编码", example = "T0stxiJK6RMH")
    @Query(type = QueryType.EQ)
    private String code;

    /**
     * 域名
     */
    @Schema(description = "域名", example = "admin.your-domain.com")
    @Query(type = QueryType.LIKE)
    private String domain;

    /**
     * 套餐 ID
     */
    @Schema(description = "套餐 ID", example = "1")
    @Query(type = QueryType.EQ)
    private Long packageId;
}