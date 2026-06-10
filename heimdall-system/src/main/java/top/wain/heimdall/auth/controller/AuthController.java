package top.wain.heimdall.auth.controller;

import cn.dev33.satoken.annotation.SaIgnore;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.bean.BeanUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import me.zhyd.oauth.request.AuthRequest;
import me.zhyd.oauth.utils.AuthStateUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import top.wain.heimdall.auth.model.req.LoginReq;
import top.wain.heimdall.auth.model.resp.LoginResp;
import top.wain.heimdall.auth.model.resp.RouteResp;
import top.wain.heimdall.auth.model.resp.SocialAuthAuthorizeResp;
import top.wain.heimdall.auth.model.resp.UserInfoResp;
import top.wain.heimdall.auth.service.AuthService;
import top.wain.heimdall.common.context.UserContext;
import top.wain.heimdall.common.context.UserContextHolder;
import top.wain.heimdall.system.enums.SocialSourceEnum;
import top.wain.heimdall.system.model.resp.user.UserDetailResp;
import top.wain.heimdall.system.service.UserService;
import top.continew.starter.auth.justauth.AuthRequestFactory;
import top.continew.starter.log.annotation.Log;
import top.continew.starter.validation.constraints.EnumValue;

import java.util.List;

/**
 * 认证 API
 *
 * @author WainZeng
 * @since 2022/12/21 20:37
 */
@Tag(name = "认证 API")
@Log(module = "登录")
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final UserService userService;
    private final AuthRequestFactory authRequestFactory;

    @SaIgnore
    @Operation(summary = "登录", description = "用户登录")
    @PostMapping("/login")
    public LoginResp login(@RequestBody @Valid LoginReq req, HttpServletRequest request) {
        return authService.login(req, request);
    }

    @Operation(summary = "登出", description = "注销用户的当前登录")
    @Parameter(name = "Authorization", description = "令牌", required = true, example = "Bearer xxxx-xxxx-xxxx-xxxx", in = ParameterIn.HEADER)
    @PostMapping("/logout")
    public Object logout() {
        Object loginId = StpUtil.getLoginId(-1L);
        StpUtil.logout();
        return loginId;
    }

    @SaIgnore
    @Operation(summary = "三方账号登录授权", description = "三方账号登录授权")
    @Parameter(name = "source", description = "来源", example = "gitee", in = ParameterIn.PATH)
    @GetMapping("/{source}")
    public SocialAuthAuthorizeResp authorize(@PathVariable @EnumValue(value = SocialSourceEnum.class, message = "第三方平台无效") String source) {
        AuthRequest authRequest = authRequestFactory.getAuthRequest(source);
        return SocialAuthAuthorizeResp.builder()
            .authorizeUrl(authRequest.authorize(AuthStateUtils.createState()))
            .build();
    }

    @Log(ignore = true)
    @Operation(summary = "获取用户信息", description = "获取登录用户信息")
    @GetMapping("/user/info")
    public UserInfoResp getUserInfo() {
        UserContext userContext = UserContextHolder.getContext();
        UserDetailResp userDetailResp = userService.get(userContext.getId());
        UserInfoResp userInfoResp = BeanUtil.copyProperties(userDetailResp, UserInfoResp.class);
        userInfoResp.setPermissions(userContext.getPermissions());
        userInfoResp.setRoles(userContext.getRoleCodes());
        userInfoResp.setPwdExpired(userContext.isPasswordExpired());
        return userInfoResp;
    }

    @Log(ignore = true)
    @Operation(summary = "获取路由信息", description = "获取登录用户的路由信息")
    @GetMapping("/user/route")
    public List<RouteResp> listRoute() {
        return authService.buildRouteTree(UserContextHolder.getUserId());
    }
}
