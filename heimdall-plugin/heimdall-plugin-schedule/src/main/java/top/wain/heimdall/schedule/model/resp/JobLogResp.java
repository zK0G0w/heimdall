package top.wain.heimdall.schedule.model.resp;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import top.wain.heimdall.schedule.enums.JobExecuteReasonEnum;
import top.wain.heimdall.schedule.enums.JobExecuteStatusEnum;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 任务日志响应参数
 *
 * @author KAI
 * @author WainZeng
 * @since 2024/6/27 22:50
 */
@Data
@Schema(description = "任务日志响应参数")
public class JobLogResp implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    @Schema(description = "ID", example = "1")
    private Long id;

    /**
     * 任务组
     */
    @Schema(description = "任务组", example = "heimdall")
    private String groupName;

    /**
     * 任务名称
     */
    @Schema(description = "任务名称", example = "定时任务1")
    private String jobName;

    /**
     * 任务 ID
     */
    @Schema(description = "任务ID", example = "1")
    private Long jobId;

    /**
     * 任务状态
     */
    @Schema(description = "任务状态", example = "3")
    private JobExecuteStatusEnum taskBatchStatus;

    /**
     * 操作原因
     */
    @Schema(description = "操作原因", example = "0")
    private JobExecuteReasonEnum operationReason;

    /**
     * 执行器类型
     */
    @Schema(description = "执行器类型", example = "1")
    private Integer executorType;

    /**
     * 执行器名称
     */
    @Schema(description = "执行器名称", example = "test")
    private String executorInfo;

    /**
     * 执行时间
     */
    @Schema(description = "执行时间", example = "2023-08-08 08:08:08", type = "string")
    private LocalDateTime executionAt;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间", example = "2023-08-08 08:08:08", type = "string")
    private LocalDateTime createDt;
}
