package top.wain.heimdall.generator.model.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 生成配置查询条件
 *
 * @author WainZeng
 * @since 2023/4/12 20:21
 */
@Data
@Schema(description = "生成配置查询条件")
public class GenConfigQuery implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 表名称
     */
    @Schema(description = "表名称", example = "sys_user")
    private String tableName;
}
