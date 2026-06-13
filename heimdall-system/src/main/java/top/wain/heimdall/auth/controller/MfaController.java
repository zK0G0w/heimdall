package top.wain.heimdall.auth.controller;

import cn.dev33.satoken.annotation.SaIgnore;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.json.JSONUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import top.continew.starter.cache.redisson.util.RedisUtils;
import top.continew.starter.core.exception.BadRequestException;
import top.wain.heimdall.auth.AbstractLoginHandler;
import top.wain.heimdall.auth.LoginHandlerFactory;
import top.wain.heimdall.auth.mfa.MfaVerifier;
import top.wain.heimdall.auth.mfa.model.resp.MfaSetupResp;
import top.wain.heimdall.auth.mfa.model.resp.MfaStatusResp;
import top.wain.heimdall.auth.mfa.service.MfaService;
import top.wain.heimdall.auth.model.resp.LoginResp;
import top.wain.heimdall.system.model.entity.user.UserDO;
import top.wain.heimdall.system.model.resp.ClientResp;
import top.wain.heimdall.system.service.ClientService;
import top.wain.heimdall.system.service.UserService;

import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * @Description: MFA 多因素认证端点
 * @Author: WainZeng
 * @Date: 2026/06/13
 */
@Tag(name = "MFA 多因素认证 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/auth/mfa")
public class MfaController {

    private static final String CHALLENGE_KEY_PREFIX = "mfa:challenge:";
    private static final int MAX_ATTEMPTS = 5;
    private static final String ATTEMPTS_KEY_PREFIX = "mfa:attempts:";

    private final MfaService mfaService;
    private final List<MfaVerifier> mfaVerifiers;
    private final UserService userService;
    private final ClientService clientService;
    private final LoginHandlerFactory loginHandlerFactory;

    @SaIgnore
    @Operation(summary = "MFA 验证（登录第二步）")
    @PostMapping("/verify")
    public LoginResp verify(@RequestParam("mfa_challenge_token") String challengeToken,
                            @RequestParam("code") String code,
                            @RequestParam(value = "type", defaultValue = "totp") String type) {
        // 速率限制
        String attemptsKey = ATTEMPTS_KEY_PREFIX + challengeToken;
        Integer attempts = RedisUtils.get(attemptsKey);
        if (attempts != null && attempts >= MAX_ATTEMPTS) {
            RedisUtils.delete(CHALLENGE_KEY_PREFIX + challengeToken);
            RedisUtils.delete(attemptsKey);
            throw new BadRequestException("验证次数超限，请重新登录");
        }

        // 获取挑战上下文
        String contextJson = RedisUtils.get(CHALLENGE_KEY_PREFIX + challengeToken);
        if (contextJson == null) {
            throw new BadRequestException("验证已过期，请重新登录");
        }
        Map<String, Object> context = JSONUtil.parseObj(contextJson);
        Long userId = Long.valueOf(context.get("userId").toString());

        // 验证 MFA 码
        MfaVerifier verifier = mfaVerifiers.stream()
            .filter(v -> v.type().equals(type))
            .findFirst()
            .orElseThrow(() -> new BadRequestException("不支持的验证类型"));
        if (!verifier.verify(userId, code)) {
            RedisUtils.set(attemptsKey, (attempts == null ? 0 : attempts) + 1, Duration.ofMinutes(5));
            throw new BadRequestException("验证码不正确");
        }

        // 验证通过，签发正式 token
        RedisUtils.delete(CHALLENGE_KEY_PREFIX + challengeToken);
        RedisUtils.delete(attemptsKey);
        return authenticateFromContext(context);
    }

    @SaIgnore
    @Operation(summary = "开始绑定 TOTP")
    @PostMapping("/setup/init")
    public MfaSetupResp setupInit(@RequestParam(value = "mfa_challenge_token", required = false) String challengeToken) {
        Long userId = resolveUserId(challengeToken);
        return mfaService.initSetup(userId);
    }

    @SaIgnore
    @Operation(summary = "确认绑定 TOTP")
    @PostMapping("/setup/confirm")
    public List<String> setupConfirm(@RequestParam("code") String code,
                                     @RequestParam(value = "mfa_challenge_token", required = false) String challengeToken) {
        Long userId = resolveUserId(challengeToken);
        return mfaService.confirmSetup(userId, code);
    }

    @Operation(summary = "解绑 MFA")
    @PostMapping("/disable")
    public void disable(@RequestParam("code") String code) {
        StpUtil.checkLogin();
        Long userId = StpUtil.getLoginIdAsLong();
        mfaService.disable(userId, code);
    }

    @Operation(summary = "重新生成恢复码")
    @PostMapping("/backup-codes/regenerate")
    public List<String> regenerateBackupCodes(@RequestParam("code") String code) {
        StpUtil.checkLogin();
        Long userId = StpUtil.getLoginIdAsLong();
        return mfaService.regenerateBackupCodes(userId, code);
    }

    @Operation(summary = "查询 MFA 状态")
    @GetMapping("/status")
    public MfaStatusResp status() {
        StpUtil.checkLogin();
        Long userId = StpUtil.getLoginIdAsLong();
        return mfaService.getStatus(userId);
    }

    /**
     * 双模鉴权：已登录优先，否则从 challengeToken 中获取 userId
     */
    private Long resolveUserId(String challengeToken) {
        if (StpUtil.isLogin()) {
            return StpUtil.getLoginIdAsLong();
        }
        if (challengeToken == null) {
            throw new BadRequestException("请先登录或提供有效的 MFA 凭证");
        }
        String contextJson = RedisUtils.get(CHALLENGE_KEY_PREFIX + challengeToken);
        if (contextJson == null) {
            throw new BadRequestException("凭证已过期，请重新登录");
        }
        Map<String, Object> context = JSONUtil.parseObj(contextJson);
        return Long.valueOf(context.get("userId").toString());
    }

    /**
     * 从挑战上下文恢复认证参数并签发 token
     */
    private LoginResp authenticateFromContext(Map<String, Object> context) {
        Long userId = Long.valueOf(context.get("userId").toString());
        String clientId = context.get("clientId").toString();
        UserDO user = userService.getById(userId);
        ClientResp client = clientService.getByClientId(clientId);
        AbstractLoginHandler<?> handler = (AbstractLoginHandler<?>)loginHandlerFactory.getAnyHandler();
        return handler.authenticate(user, client);
    }
}
