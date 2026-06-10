package top.wain.heimdall.generator.model.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import top.wain.heimdall.generator.model.entity.FieldConfigDO;
import top.wain.heimdall.generator.model.entity.GenConfigDO;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 代码生成配置请求参数
 *
 * @author WainZeng
 * @since 2023/8/8 20:40
 */
@Data
@Schema(description = "代码生成配置请求参数")
public class GenConfigReq implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 字段配置信息
     */
    @Valid
    @Schema(description = "字段配置信息")
    @NotEmpty(message = "字段配置不能为空")
    private List<FieldConfigDO> fieldConfigs = new ArrayList<>();

    /**
     * 生成配置信息
     */
    @Valid
    @Schema(description = "生成配置信息")
    @NotNull(message = "生成配置不能为空")
    private GenConfigDO genConfig;
}
