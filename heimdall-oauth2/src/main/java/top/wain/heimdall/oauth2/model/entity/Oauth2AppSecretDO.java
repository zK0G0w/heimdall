package top.wain.heimdall.oauth2.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import top.continew.starter.encrypt.field.annotation.FieldEncrypt;
import top.wain.heimdall.common.base.model.entity.BaseCreateDO;
import top.wain.heimdall.common.enums.DisEnableStatusEnum;

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

    private Long appId;
    @FieldEncrypt
    private String clientSecret;
    private DisEnableStatusEnum status;
    private LocalDateTime expiresAt;
}
