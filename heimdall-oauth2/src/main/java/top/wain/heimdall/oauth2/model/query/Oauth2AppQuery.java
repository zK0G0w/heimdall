package top.wain.heimdall.oauth2.model.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import top.wain.heimdall.common.enums.DisEnableStatusEnum;
import top.wain.heimdall.oauth2.enums.AppTypeEnum;
import top.continew.starter.data.annotation.Query;
import top.continew.starter.data.enums.QueryType;

import java.io.Serial;
import java.io.Serializable;

/**
 * @Description: OAuth2 应用查询条件
 * @Author: WainZeng
 * @Date: 2026/06/10
 */
@Data
@Schema(description = "OAuth2 应用查询条件")
public class Oauth2AppQuery implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "应用名称")
    @Query(type = QueryType.LIKE)
    private String appName;

    @Schema(description = "应用类型")
    @Query(type = QueryType.EQ)
    private AppTypeEnum appType;

    @Schema(description = "状态")
    @Query(type = QueryType.EQ)
    private DisEnableStatusEnum status;
}
