package top.wain.heimdall.oauth2.model.resp;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import top.wain.heimdall.common.base.model.resp.BaseResp;
import top.wain.heimdall.common.enums.DisEnableStatusEnum;
import top.wain.heimdall.oauth2.enums.AppTypeEnum;

import java.io.Serial;

/**
 * @Description: OAuth2 应用列表响应参数
 * @Author: WainZeng
 * @Date: 2026/06/10
 */
@Data
@Schema(description = "OAuth2 应用列表响应参数")
public class Oauth2AppResp extends BaseResp {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "应用名称")
    private String appName;

    @Schema(description = "应用类型")
    private AppTypeEnum appType;

    @Schema(description = "客户端标识")
    private String clientId;

    @Schema(description = "应用描述")
    private String description;

    @Schema(description = "状态")
    private DisEnableStatusEnum status;
}
