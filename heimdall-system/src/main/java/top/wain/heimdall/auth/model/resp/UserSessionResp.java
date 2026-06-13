package top.wain.heimdall.auth.model.resp;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @Description: 用户活跃会话响应
 * @Author: WainZeng
 * @Date: 2026/06/13
 */
@Data
@Schema(description = "用户活跃会话响应")
public class UserSessionResp {

    @Schema(description = "Token 值（脱敏）")
    private String tokenValue;

    @Schema(description = "设备类型")
    private String deviceType;

    @Schema(description = "登录 IP")
    private String loginIp;

    @Schema(description = "登录时间")
    private String loginTime;

    @Schema(description = "是否为当前会话")
    private Boolean isCurrent;
}
