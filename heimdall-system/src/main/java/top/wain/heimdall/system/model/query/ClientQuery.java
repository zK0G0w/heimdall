package top.wain.heimdall.system.model.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import top.wain.heimdall.common.enums.DisEnableStatusEnum;
import top.continew.starter.data.annotation.Query;
import top.continew.starter.data.enums.QueryType;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 客户端查询条件
 *
 * @author KAI
 * @author WainZeng
 * @since 2024/12/03 16:04
 */
@Data
@Schema(description = "客户端查询条件")
public class ClientQuery implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 客户端类型
     */
    @Schema(description = "客户端类型", example = "PC")
    @Query(type = QueryType.EQ)
    private String clientType;

    /**
     * 认证类型
     */
    @Schema(description = "认证类型", example = "ACCOUNT")
    @Query(type = QueryType.IN)
    private List<String> authType;

    /**
     * 状态
     */
    @Schema(description = "状态", example = "1")
    @Query(type = QueryType.EQ)
    private DisEnableStatusEnum status;
}