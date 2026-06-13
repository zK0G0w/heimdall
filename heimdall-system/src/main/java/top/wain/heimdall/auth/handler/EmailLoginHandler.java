package top.wain.heimdall.auth.handler;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import top.wain.heimdall.auth.AbstractLoginHandler;
import top.wain.heimdall.auth.enums.AuthTypeEnum;
import top.wain.heimdall.auth.model.req.EmailLoginReq;
import top.wain.heimdall.common.constant.CacheConstants;
import top.wain.heimdall.system.model.entity.user.UserDO;
import top.wain.heimdall.system.model.resp.ClientResp;
import top.continew.starter.cache.redisson.util.RedisUtils;
import top.continew.starter.core.util.validation.ValidationUtils;

/**
 * 邮箱登录处理器
 *
 * @author KAI
 * @author WainZeng
 * @since 2024/12/22 14:58
 */
@Component
public class EmailLoginHandler extends AbstractLoginHandler<EmailLoginReq> {

    @Override
    public UserDO login(EmailLoginReq req, ClientResp client, HttpServletRequest request) {
        // 验证邮箱
        UserDO user = userService.getByEmail(req.getEmail());
        ValidationUtils.throwIfNull(user, "此邮箱未绑定本系统账号");
        // 检查用户状态
        super.checkUserStatus(user);
        return user;
    }

    @Override
    public void preLogin(EmailLoginReq req, ClientResp client, HttpServletRequest request) {
        String email = req.getEmail();
        String captchaKey = CacheConstants.CAPTCHA_KEY_PREFIX + email;
        String captcha = RedisUtils.get(captchaKey);
        ValidationUtils.throwIfBlank(captcha, CAPTCHA_EXPIRED);
        ValidationUtils.throwIfNotEqualIgnoreCase(req.getCaptcha(), captcha, CAPTCHA_ERROR);
        RedisUtils.delete(captchaKey);
    }

    @Override
    public AuthTypeEnum getAuthType() {
        return AuthTypeEnum.EMAIL;
    }
}