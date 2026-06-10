package top.wain.heimdall.system.model.entity.user;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import top.wain.heimdall.common.base.model.entity.BaseDO;
import top.wain.heimdall.common.enums.DisEnableStatusEnum;
import top.wain.heimdall.common.enums.GenderEnum;
import top.continew.starter.encrypt.field.annotation.FieldEncrypt;
import top.continew.starter.encrypt.password.encoder.encryptor.PasswordEncoderEncryptor;
import top.continew.starter.extension.crud.annotation.DictModel;

import java.io.Serial;
import java.time.LocalDateTime;

/**
 * 用户实体
 *
 * @author WainZeng
 * @since 2022/12/21 20:42
 */
@Data
@DictModel(labelKey = "nickname", extraKeys = {"username"})
@TableName("sys_user")
public class UserDO extends BaseDO {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 用户名
     */
    private String username;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 密码
     */
    @FieldEncrypt(encryptor = PasswordEncoderEncryptor.class)
    private String password;

    /**
     * 性别
     */
    private GenderEnum gender;

    /**
     * 邮箱
     */
    @FieldEncrypt
    @TableField(insertStrategy = FieldStrategy.NOT_EMPTY)
    private String email;

    /**
     * 手机号码
     */
    @FieldEncrypt
    @TableField(insertStrategy = FieldStrategy.NOT_EMPTY)
    private String phone;

    /**
     * 头像地址
     */
    private String avatar;

    /**
     * 描述
     */
    private String description;

    /**
     * 状态
     */
    private DisEnableStatusEnum status;

    /**
     * 是否为系统内置数据
     */
    private Boolean isSystem;

    /**
     * 最后一次修改密码时间
     */
    private LocalDateTime pwdResetTime;

    /**
     * 部门 ID
     */
    private Long deptId;
}
