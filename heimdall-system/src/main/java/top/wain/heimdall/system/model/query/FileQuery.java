package top.wain.heimdall.system.model.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import top.wain.heimdall.system.enums.FileTypeEnum;
import top.continew.starter.data.annotation.Query;
import top.continew.starter.data.enums.QueryType;

import java.io.Serial;
import java.io.Serializable;

/**
 * 文件查询条件
 *
 * @author WainZeng
 * @since 2023/12/23 10:38
 */
@Data
@Schema(description = "文件查询条件")
public class FileQuery implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 名称
     */
    @Schema(description = "名称", example = "example")
    @Query(type = QueryType.LIKE)
    private String originalName;

    /**
     * 上级目录
     */
    @Schema(description = "上级目录", example = "/")
    @Query(type = QueryType.EQ)
    private String parentPath;

    /**
     * 类型
     */
    @Schema(description = "类型", example = "2")
    @Query(type = QueryType.EQ)
    private FileTypeEnum type;
}