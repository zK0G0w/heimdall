package top.wain.heimdall.auth.mfa.model.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import top.continew.starter.encrypt.field.annotation.FieldEncrypt;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @Description: 用户 MFA 绑定实体
 * @Author: WainZeng
 * @Date: 2026/06/13
 */
@Data
@TableName("sys_user_mfa")
public class UserMfaDO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId
    private Long id;

    private Long userId;

    private String type;

    @FieldEncrypt
    private String secret;

    @FieldEncrypt
    private String backupCodes;

    private Integer enabled;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
