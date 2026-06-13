package top.wain.heimdall.auth.mfa;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import top.wain.heimdall.auth.mfa.mapper.UserMfaMapper;
import top.wain.heimdall.auth.mfa.model.entity.UserMfaDO;

import java.util.List;

/**
 * @Description: 恢复码验证器
 * @Author: WainZeng
 * @Date: 2026/06/13
 */
@Component
@RequiredArgsConstructor
public class RecoveryCodeMfaVerifier implements MfaVerifier {

    private final UserMfaMapper userMfaMapper;

    @Override
    public String type() {
        return "recovery";
    }

    @Override
    public boolean verify(Long userId, String code) {
        UserMfaDO mfa = userMfaMapper.selectOne(new LambdaQueryWrapper<UserMfaDO>().eq(UserMfaDO::getUserId, userId)
            .eq(UserMfaDO::getEnabled, 1));
        if (mfa == null || mfa.getBackupCodes() == null) {
            return false;
        }
        List<String> codes = JSONUtil.toList(mfa.getBackupCodes(), String.class);
        String normalizedCode = code.replace("-", "").toUpperCase();
        if (codes.contains(normalizedCode)) {
            codes.remove(normalizedCode);
            mfa.setBackupCodes(JSONUtil.toJsonStr(codes));
            userMfaMapper.updateById(mfa);
            return true;
        }
        return false;
    }
}
