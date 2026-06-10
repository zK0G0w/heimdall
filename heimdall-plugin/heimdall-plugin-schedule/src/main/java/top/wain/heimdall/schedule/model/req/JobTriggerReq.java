package top.wain.heimdall.schedule.model.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 任务执行请求参数
 *
 * @author WainZeng
 * @since 2025/3/26 21:50
 */
@Data
@Schema(description = "任务执行请求参数")
public class JobTriggerReq implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    @Schema(description = "ID", example = "1")
    @NotNull(message = "ID不能为空")
    private Long jobId;

    /**
     * 方法参数
     */
    @Schema(description = "方法参数")
    private String tmpArgsStr;
}
