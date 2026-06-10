package top.wain.heimdall.schedule.model.query;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;
import top.wain.heimdall.schedule.enums.JobExecuteStatusEnum;
import top.continew.starter.validation.constraints.EnumValue;

import java.io.Serial;
import java.time.LocalDateTime;

/**
 * 任务日志查询条件
 *
 * @author KAI
 * @since 2024/6/27 23:58
 */
@Data
@Schema(description = "任务日志查询条件")
public class JobLogQuery extends JobPageQuery {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 任务 ID
     */
    @Schema(description = "任务ID", example = "1")
    private Long jobId;

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
     * 任务批次状态
     */
    @Schema(description = "任务批次状态", example = "1")
    @EnumValue(value = JobExecuteStatusEnum.class, message = "任务批次状态无效")
    private Integer taskBatchStatus;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间", example = "2023-08-08 00:00:00,2023-08-08 23:59:59")
    @Size(max = 2, message = "创建时间必须是一个范围")
    private LocalDateTime[] datetimeRange;
}