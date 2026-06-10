package top.wain.heimdall.system.model.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import top.wain.heimdall.common.enums.DisEnableStatusEnum;
import top.continew.starter.data.annotation.Query;
import top.continew.starter.data.enums.QueryType;

import java.io.Serial;
import java.io.Serializable;

/**
 * 字典项查询条件
 *
 * @author WainZeng
 * @since 2023/9/11 21:29
 */
@Data
@Schema(description = "字典项查询条件")
public class DictItemQuery implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 关键词
     */
    @Schema(description = "关键词")
    @Query(columns = {"label", "description"}, type = QueryType.LIKE)
    private String description;

    /**
     * 状态
     */
    @Schema(description = "状态", example = "1")
    @Query(type = QueryType.EQ)
    private DisEnableStatusEnum status;

    /**
     * 字典 ID
     */
    @Schema(description = "字典 ID")
    @Query(type = QueryType.EQ)
    private Long dictId;
}