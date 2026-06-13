package top.wain.heimdall.auth.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.tree.Tree;
import cn.hutool.core.lang.tree.TreeNodeConfig;
import cn.hutool.core.lang.tree.TreeUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.json.JSONUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import top.continew.starter.cache.redisson.util.RedisUtils;
import top.continew.starter.core.util.validation.ValidationUtils;
import top.continew.starter.extension.crud.annotation.TreeField;
import top.continew.starter.extension.crud.autoconfigure.CrudProperties;
import top.wain.heimdall.auth.AbstractLoginHandler;
import top.wain.heimdall.auth.LoginHandler;
import top.wain.heimdall.auth.LoginHandlerFactory;
import top.wain.heimdall.auth.enums.AuthTypeEnum;
import top.wain.heimdall.auth.mfa.MfaPolicyService;
import top.wain.heimdall.auth.model.req.LoginReq;
import top.wain.heimdall.auth.model.resp.LoginResp;
import top.wain.heimdall.auth.model.resp.RouteResp;
import top.wain.heimdall.auth.service.AuthService;
import top.wain.heimdall.common.context.RoleContext;
import top.wain.heimdall.common.enums.DisEnableStatusEnum;
import top.wain.heimdall.system.constant.SystemConstants;
import top.wain.heimdall.system.enums.MenuTypeEnum;
import top.wain.heimdall.system.model.entity.user.UserDO;
import top.wain.heimdall.system.model.resp.ClientResp;
import top.wain.heimdall.system.model.resp.MenuResp;
import top.wain.heimdall.system.service.ClientService;
import top.wain.heimdall.system.service.MenuService;
import top.wain.heimdall.system.service.RoleService;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 认证业务实现
 *
 * @author WainZeng
 * @since 2022/12/21 21:49
 */
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final LoginHandlerFactory loginHandlerFactory;
    private final ClientService clientService;
    private final RoleService roleService;
    private final MenuService menuService;
    private final CrudProperties crudProperties;
    private final MfaPolicyService mfaPolicyService;

    @Override
    public LoginResp login(LoginReq req, HttpServletRequest request) {
        AuthTypeEnum authType = req.getAuthType();
        // 校验客户端
        ClientResp client = clientService.getByClientId(req.getClientId());
        ValidationUtils.throwIfNull(client, "客户端不存在");
        ValidationUtils.throwIf(DisEnableStatusEnum.DISABLE.equals(client.getStatus()), "客户端已禁用");
        ValidationUtils.throwIf(!client.getAuthType().contains(authType.getValue()), "该客户端暂未授权 [{}] 认证", authType
            .getDescription());
        // 获取处理器
        LoginHandler<LoginReq> loginHandler = loginHandlerFactory.getHandler(authType);
        // 登录前置处理
        loginHandler.preLogin(req, client, request);
        // 第一因素验证
        UserDO user = loginHandler.login(req, client, request);
        // 登录后置处理
        loginHandler.postLogin(req, client, request);

        // MFA 检查
        Long userId = user.getId();
        boolean mfaEnabled = mfaPolicyService.isEnabled(userId);
        boolean mfaRequired = mfaPolicyService.isRequired(userId);

        if (mfaEnabled) {
            // 已绑定 MFA，需要验证第二因素
            String challengeToken = createMfaChallenge(user, client);
            return LoginResp.builder().requiresMfa(true).mfaChallengeToken(challengeToken).build();
        }
        if (mfaRequired) {
            // 被强制但未绑定，引导绑定流程
            String challengeToken = createMfaChallenge(user, client);
            return LoginResp.builder().requiresMfaSetup(true).mfaChallengeToken(challengeToken).build();
        }

        // 不需要 MFA，直接签发 token
        AbstractLoginHandler<?> handler = (AbstractLoginHandler<?>)loginHandler;
        return handler.authenticate(user, client);
    }

    private String createMfaChallenge(UserDO user, ClientResp client) {
        String token = IdUtil.fastSimpleUUID();
        Map<String, Object> context = new HashMap<>();
        context.put("userId", user.getId());
        context.put("clientId", client.getClientId());
        context.put("clientType", client.getClientType());
        context.put("activeTimeout", client.getActiveTimeout());
        context.put("timeout", client.getTimeout());
        context.put("isConcurrent", client.getIsConcurrent());
        context.put("replacedRange", client.getReplacedRange() != null ? client.getReplacedRange().getValue() : null);
        context.put("maxLoginCount", client.getMaxLoginCount());
        context.put("overflowLogoutMode", client.getOverflowLogoutMode() != null
            ? client.getOverflowLogoutMode().getValue()
            : null);
        RedisUtils.set("mfa:challenge:" + token, JSONUtil.toJsonStr(context), Duration.ofMinutes(5));
        return token;
    }

    @Override
    public List<RouteResp> buildRouteTree(Long userId) {
        Set<RoleContext> roleSet = roleService.listByUserId(userId);
        if (CollUtil.isEmpty(roleSet)) {
            return new ArrayList<>(0);
        }
        // 查询菜单列表
        Set<MenuResp> menuSet = new LinkedHashSet<>();
        if (roleSet.stream().anyMatch(r -> SystemConstants.SUPER_ADMIN_ROLE_ID.equals(r.getId()))) {
            menuSet.addAll(menuService.listByRoleId(SystemConstants.SUPER_ADMIN_ROLE_ID));
        } else {
            roleSet.forEach(r -> menuSet.addAll(menuService.listByRoleId(r.getId())));
        }
        List<MenuResp> menuList = menuSet.stream().filter(m -> !MenuTypeEnum.BUTTON.equals(m.getType())).toList();
        if (CollUtil.isEmpty(menuList)) {
            return new ArrayList<>(0);
        }
        // 构建路由树
        TreeField treeField = MenuResp.class.getDeclaredAnnotation(TreeField.class);
        TreeNodeConfig treeNodeConfig = crudProperties.getTreeDictModel().genTreeNodeConfig(treeField);
        List<Tree<Long>> treeList = TreeUtil.build(menuList, treeField.rootId(), treeNodeConfig, (m, tree) -> {
            tree.setId(m.getId());
            tree.setParentId(m.getParentId());
            tree.setName(m.getTitle());
            tree.setWeight(m.getSort());
            tree.putExtra("type", m.getType().getValue());
            tree.putExtra("path", m.getPath());
            tree.putExtra("name", m.getName());
            tree.putExtra("component", m.getComponent());
            tree.putExtra("redirect", m.getRedirect());
            tree.putExtra("icon", m.getIcon());
            tree.putExtra("isExternal", m.getIsExternal());
            tree.putExtra("isCache", m.getIsCache());
            tree.putExtra("isHidden", m.getIsHidden());
            tree.putExtra("permission", m.getPermission());
        });
        return BeanUtil.copyToList(treeList, RouteResp.class);
    }
}
