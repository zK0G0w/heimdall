package top.wain.heimdall.auth.controller;

import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.session.SaTerminalInfo;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.StrUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.continew.starter.core.exception.BusinessException;
import top.wain.heimdall.auth.model.resp.UserSessionResp;
import top.wain.heimdall.common.context.UserContextHolder;
import top.wain.heimdall.common.context.UserExtraContext;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * @Description: 用户活跃会话管理（用户自助操作）
 * @Author: WainZeng
 * @Date: 2026/06/13
 */
@Tag(name = "用户会话管理 API")
@RestController
@RequestMapping("/auth/user/sessions")
public class UserSessionController {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Operation(summary = "查询活跃会话列表")
    @GetMapping
    public List<UserSessionResp> list() {
        Long userId = StpUtil.getLoginIdAsLong();
        String currentToken = StpUtil.getTokenValue();
        SaSession session = StpUtil.getSessionByLoginId(userId, false);
        if (session == null) {
            return List.of();
        }
        List<SaTerminalInfo> terminalList = session.getTerminalList();
        List<UserSessionResp> result = new ArrayList<>();
        for (SaTerminalInfo terminal : terminalList) {
            String tokenValue = terminal.getTokenValue();
            // 跳过已被踢出或过期的 token
            long activeTimeout = StpUtil.stpLogic.getTokenActiveTimeoutByToken(tokenValue);
            if (activeTimeout == -2) {
                continue;
            }
            UserSessionResp resp = new UserSessionResp();
            resp.setTokenId(tokenValue);
            resp.setTokenValue(maskToken(tokenValue));
            resp.setDeviceType(terminal.getDeviceType());
            resp.setIsCurrent(tokenValue.equals(currentToken));
            // 从用户额外上下文获取登录 IP 和登录时间
            UserExtraContext extraContext = UserContextHolder.getExtraContext(tokenValue);
            if (extraContext != null) {
                resp.setLoginIp(StrUtil.nullToDefault(extraContext.getIp(), ""));
                LocalDateTime loginTime = extraContext.getLoginTime();
                resp.setLoginTime(loginTime != null ? loginTime.format(FORMATTER) : "");
            } else {
                resp.setLoginIp("");
                resp.setLoginTime("");
            }
            result.add(resp);
        }
        return result;
    }

    @Operation(summary = "踢出指定会话")
    @DeleteMapping("/{token}")
    public void kickout(@PathVariable String token) {
        String currentToken = StpUtil.getTokenValue();
        if (token.equals(currentToken)) {
            throw new BusinessException("不能踢出当前会话");
        }
        StpUtil.kickoutByTokenValue(token);
    }

    @Operation(summary = "退出所有其他设备")
    @DeleteMapping
    public void kickoutAll() {
        Long userId = StpUtil.getLoginIdAsLong();
        String currentToken = StpUtil.getTokenValue();
        SaSession session = StpUtil.getSessionByLoginId(userId, false);
        if (session == null) {
            return;
        }
        List<SaTerminalInfo> terminalList = session.getTerminalList();
        for (SaTerminalInfo terminal : terminalList) {
            String tokenValue = terminal.getTokenValue();
            if (!tokenValue.equals(currentToken)) {
                StpUtil.kickoutByTokenValue(tokenValue);
            }
        }
    }

    /**
     * Token 脱敏处理
     */
    private String maskToken(String token) {
        if (StrUtil.isBlank(token) || token.length() <= 8) {
            return "****";
        }
        return token.substring(0, 4) + "..." + token.substring(token.length() - 4);
    }
}
