package top.wain.heimdall.oauth2.model.entity;

import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @Description: 用户 OAuth2 授权记录
 * @Author: WainZeng
 * @Date: 2026/06/13
 */
@Data
@TableName("oauth2_user_grant")
public class Oauth2UserGrantDO {

    @Schema(description = "主键")
    private Long id;

    @Schema(description = "用户 ID")
    private Long userId;

    @Schema(description = "应用 ID")
    private Long appId;

    @Schema(description = "客户端标识")
    private String clientId;

    @Schema(description = "授权的 scope（逗号分隔）")
    private String scope;

    @Schema(description = "首次授权时间")
    private LocalDateTime grantedAt;

    @Schema(description = "最近授权更新时间")
    private LocalDateTime updatedAt;

    @TableLogic(value = "0", delval = "id")
    @Schema(description = "逻辑删除")
    private Long deleted;
}
