package top.wain.heimdall.oauth2.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import top.continew.starter.encrypt.field.annotation.FieldEncrypt;
import top.wain.heimdall.common.base.model.entity.BaseCreateDO;
import top.wain.heimdall.common.enums.DisEnableStatusEnum;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serial;
import java.time.LocalDateTime;

/**
 * @Description: OAuth2 应用密钥实体
 * @Author: WainZeng
 * @Date: 2026/06/10
 */
@Data
@TableName("oauth2_app_secret")
public class Oauth2AppSecretDO extends BaseCreateDO {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "所属应用 ID")
    private Long appId;
    @Schema(description = "客户端密钥（加密存储）")
    @FieldEncrypt
    private String clientSecret;
    @Schema(description = "状态")
    private DisEnableStatusEnum status;
    @Schema(description = "过期时间")
    private LocalDateTime expiresAt;
}
