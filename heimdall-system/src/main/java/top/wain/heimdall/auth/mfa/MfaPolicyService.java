package top.wain.heimdall.auth.mfa;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import top.wain.heimdall.auth.mfa.mapper.UserMfaMapper;
import top.wain.heimdall.auth.mfa.model.entity.UserMfaDO;
import top.wain.heimdall.common.context.RoleContext;
import top.wain.heimdall.system.service.OptionService;
import top.wain.heimdall.system.service.RoleService;

import java.util.Set;
import java.util.function.Function;

/**
 * @Description: MFA 策略判定服务
 * @Author: WainZeng
 * @Date: 2026/06/13
 */
@Service
@RequiredArgsConstructor
public class MfaPolicyService {

    private static final String MFA_FORCE_CONFIG_KEY = "sys_mfa_force_enabled";

    private final RoleService roleService;
    private final OptionService optionService;
    private final UserMfaMapper userMfaMapper;

    /**
     * 判断用户是否需要 MFA 验证（被策略强制或自己已开启）
     */
    public boolean isRequired(Long userId) {
        // 角色级：任一角色 forceMfa=true 则必须
        Set<RoleContext> roles = roleService.listByUserId(userId);
        for (RoleContext role : roles) {
            if (Boolean.TRUE.equals(role.getForceMfa())) {
                return true;
            }
        }
        // 全局级：配置项为 true 则必须
        String globalConfig = optionService.getValueByCode(MFA_FORCE_CONFIG_KEY, Function.identity());
        return "true".equalsIgnoreCase(globalConfig);
    }

    /**
     * 判断用户是否已绑定并激活 MFA
     */
    public boolean isEnabled(Long userId) {
        UserMfaDO mfa = userMfaMapper.selectOne(new LambdaQueryWrapper<UserMfaDO>().eq(UserMfaDO::getUserId, userId)
            .eq(UserMfaDO::getEnabled, 1));
        return mfa != null;
    }
}
