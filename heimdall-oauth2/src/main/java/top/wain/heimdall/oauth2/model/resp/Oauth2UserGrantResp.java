package top.wain.heimdall.oauth2.model.resp;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @Description: 用户已授权应用响应
 * @Author: WainZeng
 * @Date: 2026/06/13
 */
@Data
@Schema(description = "用户已授权应用响应")
public class Oauth2UserGrantResp {

    @Schema(description = "应用 ID")
    private Long appId;

    @Schema(description = "应用名称")
    private String appName;

    @Schema(description = "客户端标识")
    private String clientId;

    @Schema(description = "应用 Logo")
    private String logo;

    @Schema(description = "授权的 scope")
    private String scope;

    @Schema(description = "首次授权时间")
    private LocalDateTime grantedAt;

    @Schema(description = "最近授权更新时间")
    private LocalDateTime updatedAt;
}
