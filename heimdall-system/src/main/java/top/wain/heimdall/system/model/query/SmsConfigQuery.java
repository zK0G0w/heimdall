package top.wain.heimdall.system.model.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import top.wain.heimdall.common.enums.DisEnableStatusEnum;
import top.continew.starter.data.annotation.Query;
import top.continew.starter.data.enums.QueryType;

import java.io.Serial;
import java.io.Serializable;

/**
 * 短信配置查询条件
 *
 * @author luoqiz
 * @author WainZeng
 * @since 2025/03/15 18:41
 */
@Data
@Schema(description = "短信配置查询条件")
public class SmsConfigQuery implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 名称
     */
    @Schema(description = "名称", example = "短信配置1")
    @Query(type = QueryType.LIKE)
    private String name;

    /**
     * 厂商
     */
    @Schema(description = "厂商", example = "cloopen")
    @Query(type = QueryType.EQ)
    private String supplier;

    /**
     * Access Key
     */
    @Schema(description = "Access Key", example = "7aaf0708674db3ee05676ecbc2f31b7b")
    @Query(type = QueryType.EQ)
    private String accessKey;

    /**
     * 状态
     */
    @Schema(description = "状态", example = "1")
    @Query(type = QueryType.EQ)
    private DisEnableStatusEnum status;
}