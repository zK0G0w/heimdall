package top.wain.heimdall.auth.handler;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import me.zhyd.oauth.model.AuthCallback;
import me.zhyd.oauth.model.AuthResponse;
import me.zhyd.oauth.model.AuthUser;
import me.zhyd.oauth.request.AuthRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import top.wain.heimdall.auth.AbstractLoginHandler;
import top.wain.heimdall.auth.enums.AuthTypeEnum;
import top.wain.heimdall.auth.model.req.SocialLoginReq;
import top.wain.heimdall.common.constant.RegexConstants;
import top.wain.heimdall.common.enums.DisEnableStatusEnum;
import top.wain.heimdall.common.enums.GenderEnum;
import top.wain.heimdall.common.enums.RoleCodeEnum;
import top.wain.heimdall.system.enums.MessageTemplateEnum;
import top.wain.heimdall.system.enums.MessageTypeEnum;
import top.wain.heimdall.system.model.entity.DeptDO;
import top.wain.heimdall.system.model.entity.user.UserDO;
import top.wain.heimdall.system.model.entity.user.UserSocialDO;
import top.wain.heimdall.system.model.req.MessageReq;
import top.wain.heimdall.system.model.resp.ClientResp;
import top.wain.heimdall.system.service.DeptService;
import top.wain.heimdall.system.service.MessageService;
import top.wain.heimdall.system.service.UserRoleService;
import top.wain.heimdall.system.service.UserSocialService;
import top.continew.starter.auth.justauth.AuthRequestFactory;
import top.continew.starter.core.autoconfigure.application.ApplicationProperties;
import top.continew.starter.core.util.validation.ValidationUtils;

import java.time.LocalDateTime;
import java.util.Collections;

/**
 * 第三方账号登录处理器
 *
 * @author KAI
 * @author WainZeng
 * @since 2024/12/25 14:21
 */
@Component
@RequiredArgsConstructor
public class SocialLoginHandler extends AbstractLoginHandler<SocialLoginReq> {

    private final AuthRequestFactory authRequestFactory;
    private final UserSocialService userSocialService;
    private final UserRoleService userRoleService;
    private final MessageService messageService;
    private final ApplicationProperties applicationProperties;
    private final DeptService deptService;

    @Override
    @Transactional
    public UserDO login(SocialLoginReq req, ClientResp client, HttpServletRequest request) {
        // 获取第三方登录信息
        AuthRequest authRequest = authRequestFactory.getAuthRequest(req.getSource());
        AuthCallback callback = new AuthCallback();
        callback.setCode(req.getCode());
        callback.setState(req.getState());
        AuthResponse<AuthUser> response = authRequest.login(callback);
        ValidationUtils.throwIf(!response.ok(), response.getMsg());
        AuthUser authUser = response.getData();
        // 如未绑定则自动注册新用户，保存或更新关联信息
        String source = authUser.getSource();
        String openId = authUser.getUuid();
        UserSocialDO userSocial = userSocialService.getBySourceAndOpenId(source, openId);
        UserDO user;
        if (userSocial == null) {
            String username = authUser.getUsername();
            String nickname = authUser.getNickname();
            UserDO existsUser = userService.getByUsername(username);
            String randomStr = RandomUtil.randomString(RandomUtil.BASE_CHAR, 5);
            if (existsUser != null || !ReUtil.isMatch(RegexConstants.USERNAME, username)) {
                username = randomStr + IdUtil.fastSimpleUUID();
            }
            if (!ReUtil.isMatch(RegexConstants.GENERAL_NAME, nickname)) {
                nickname = source.toLowerCase() + randomStr;
            }
            user = new UserDO();
            user.setUsername(username);
            user.setNickname(nickname);
            if (authUser.getGender() != null) {
                user.setGender(GenderEnum.valueOf(authUser.getGender().name()));
            }
            user.setAvatar(authUser.getAvatar());
            // 默认设置为系统内置数据的根部门 如果需要设置其他部门自行替换查询条件
            DeptDO deptDO = deptService.getOne(new LambdaQueryWrapper<DeptDO>().eq(DeptDO::getIsSystem, true)
                .eq(DeptDO::getParentId, 0));
            ValidationUtils.throwIf(deptDO == null, "未查询到系统内置部门");
            user.setDeptId(deptDO.getId());
            user.setStatus(DisEnableStatusEnum.ENABLE);
            userService.save(user);
            Long userId = user.getId();
            userRoleService.assignRolesToUser(Collections.singletonList(roleService
                .getIdByCode(RoleCodeEnum.GENERAL_USER.getCode())), userId);
            userSocial = new UserSocialDO();
            userSocial.setUserId(userId);
            userSocial.setSource(source);
            userSocial.setOpenId(openId);
            this.sendSecurityMsg(user);
        } else {
            user = BeanUtil.copyProperties(userService.getById(userSocial.getUserId()), UserDO.class);
        }
        // 检查用户状态
        super.checkUserStatus(user);
        userSocial.setMetaJson(JSONUtil.toJsonStr(authUser));
        userSocial.setLastLoginTime(LocalDateTime.now());
        userSocialService.saveOrUpdate(userSocial);
        return user;
    }

    @Override
    public void preLogin(SocialLoginReq req, ClientResp client, HttpServletRequest request) {
        super.preLogin(req, client, request);
        if (StpUtil.isLogin()) {
            StpUtil.logout();
        }
    }

    @Override
    public AuthTypeEnum getAuthType() {
        return AuthTypeEnum.SOCIAL;
    }

    /**
     * 发送安全消息
     *
     * @param user 用户信息
     */
    private void sendSecurityMsg(UserDO user) {
        MessageTemplateEnum template = MessageTemplateEnum.SOCIAL_REGISTER;
        MessageReq req = new MessageReq(MessageTypeEnum.SECURITY);
        req.setTitle(template.getTitle().formatted(applicationProperties.getName()));
        req.setContent(template.getContent().formatted(user.getNickname()));
        messageService.add(req, CollUtil.toList(user.getId().toString()));
    }
}
