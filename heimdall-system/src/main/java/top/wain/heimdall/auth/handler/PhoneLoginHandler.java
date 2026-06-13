package top.wain.heimdall.auth.handler;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import top.wain.heimdall.auth.AbstractLoginHandler;
import top.wain.heimdall.auth.enums.AuthTypeEnum;
import top.wain.heimdall.auth.model.req.PhoneLoginReq;
import top.wain.heimdall.common.constant.CacheConstants;
import top.wain.heimdall.system.model.entity.user.UserDO;
import top.wain.heimdall.system.model.resp.ClientResp;
import top.continew.starter.cache.redisson.util.RedisUtils;
import top.continew.starter.core.util.validation.ValidationUtils;

/**
 * 手机号登录处理器
 *
 * @author KAI
 * @author WainZeng
 * @since 2024/12/22 14:59
 */
@Component
public class PhoneLoginHandler extends AbstractLoginHandler<PhoneLoginReq> {

    @Override
    public UserDO login(PhoneLoginReq req, ClientResp client, HttpServletRequest request) {
        // 验证手机号
        UserDO user = userService.getByPhone(req.getPhone());
        ValidationUtils.throwIfNull(user, "此手机号未绑定本系统账号");
        // 检查用户状态
        super.checkUserStatus(user);
        return user;
    }

    @Override
    public void preLogin(PhoneLoginReq req, ClientResp client, HttpServletRequest request) {
        String phone = req.getPhone();
        String captchaKey = CacheConstants.CAPTCHA_KEY_PREFIX + phone;
        String captcha = RedisUtils.get(captchaKey);
        ValidationUtils.throwIfBlank(captcha, CAPTCHA_EXPIRED);
        ValidationUtils.throwIfNotEqualIgnoreCase(req.getCaptcha(), captcha, CAPTCHA_ERROR);
        RedisUtils.delete(captchaKey);
    }

    @Override
    public AuthTypeEnum getAuthType() {
        return AuthTypeEnum.PHONE;
    }
}