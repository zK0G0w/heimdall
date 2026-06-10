package top.wain.heimdall.system.model.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import top.wain.heimdall.common.enums.DisEnableStatusEnum;
import top.wain.heimdall.system.enums.StorageTypeEnum;
import top.continew.starter.data.annotation.Query;
import top.continew.starter.data.enums.QueryType;

import java.io.Serial;
import java.io.Serializable;

/**
 * 存储查询条件
 *
 * @author WainZeng
 * @since 2023/12/26 22:09
 */
@Data
@Schema(description = "存储查询条件")
public class StorageQuery implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 关键词
     */
    @Schema(description = "关键词", example = "本地存储")
    @Query(columns = {"name", "code", "description"}, type = QueryType.LIKE)
    private String description;

    /**
     * 状态
     */
    @Schema(description = "状态", example = "1")
    @Query(type = QueryType.EQ)
    private DisEnableStatusEnum status;

    /**
     * 类型
     */
    @Schema(description = "类型", example = "2")
    @Query(type = QueryType.EQ)
    private StorageTypeEnum type;
}