package top.wain.heimdall.open.model.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import top.continew.starter.data.annotation.Query;
import top.continew.starter.data.enums.QueryType;

import java.io.Serial;
import java.io.Serializable;

/**
 * 应用查询条件
 *
 * @author chengzi
 * @author WainZeng
 * @since 2024/10/17 16:03
 */
@Data
@Schema(description = "应用查询条件")
public class AppQuery implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 关键词
     */
    @Schema(description = "关键词", example = "应用1")
    @Query(columns = {"name", "description"}, type = QueryType.LIKE)
    private String description;
}