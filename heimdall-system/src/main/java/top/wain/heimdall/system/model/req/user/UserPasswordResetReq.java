package top.wain.heimdall.system.model.req.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 用户密码重置请求参数
 *
 * @author WainZeng
 * @since 2024/2/2 22:50
 */
@Data
@Schema(description = "用户密码重置请求参数")
public class UserPasswordResetReq implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 新密码
     */
    @Schema(description = "新密码", example = "RSA 公钥加密的新密码")
    @NotBlank(message = "新密码不能为空")
    private String newPassword;
}
