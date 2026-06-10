package top.wain.heimdall.system.model.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import top.wain.heimdall.common.enums.SuccessFailureStatusEnum;
import top.continew.starter.data.annotation.Query;
import top.continew.starter.data.enums.QueryType;

import java.io.Serial;
import java.io.Serializable;

/**
 * 短信日志查询条件
 *
 * @author luoqiz
 * @author WainZeng
 * @since 2025/03/15 22:15
 */
@Data
@Schema(description = "短信日志查询条件")
public class SmsLogQuery implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 配置 ID
     */
    @Schema(description = "配置 ID", example = "1")
    @Query(type = QueryType.EQ)
    private Long configId;

    /**
     * 手机号
     */
    @Schema(description = "手机号", example = "18888888888")
    @Query(type = QueryType.EQ)
    private String phone;

    /**
     * 发送状态
     */
    @Schema(description = "发送状态", example = "1")
    @Query(type = QueryType.EQ)
    private SuccessFailureStatusEnum status;
}