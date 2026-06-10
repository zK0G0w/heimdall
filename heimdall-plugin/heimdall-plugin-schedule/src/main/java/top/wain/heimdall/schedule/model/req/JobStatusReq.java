package top.wain.heimdall.schedule.model.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import top.wain.heimdall.schedule.enums.JobStatusEnum;

import java.io.Serial;
import java.io.Serializable;

/**
 * 任务状态修改请求参数
 *
 * @author KAI
 * @author WainZeng
 * @since 2024/6/27 9:24
 */
@Data
@Schema(description = "任务状态修改请求参数")
public class JobStatusReq implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 任务状态
     */
    @Schema(description = "任务状态", example = "1")
    @NotNull(message = "任务状态无效")
    private JobStatusEnum jobStatus;
}
