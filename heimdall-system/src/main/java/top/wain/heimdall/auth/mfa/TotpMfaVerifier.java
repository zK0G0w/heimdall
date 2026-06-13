package top.wain.heimdall.auth.mfa;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.eatthepath.otp.TimeBasedOneTimePasswordGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import top.wain.heimdall.auth.mfa.mapper.UserMfaMapper;
import top.wain.heimdall.auth.mfa.model.entity.UserMfaDO;

import javax.crypto.spec.SecretKeySpec;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;

/**
 * @Description: TOTP 验证器（Google Authenticator 兼容）
 * @Author: WainZeng
 * @Date: 2026/06/13
 */
@Component
@RequiredArgsConstructor
public class TotpMfaVerifier implements MfaVerifier {

    private static final String ALGORITHM = "HmacSHA1";
    private static final int CODE_LENGTH = 6;
    private static final long TIME_STEP_SECONDS = 30;

    private final UserMfaMapper userMfaMapper;

    @Override
    public String type() {
        return "totp";
    }

    @Override
    public boolean verify(Long userId, String code) {
        UserMfaDO mfa = userMfaMapper.selectOne(new LambdaQueryWrapper<UserMfaDO>().eq(UserMfaDO::getUserId, userId)
            .eq(UserMfaDO::getEnabled, 1));
        if (mfa == null) {
            return false;
        }
        return verifyCode(mfa.getSecret(), code);
    }

    /**
     * 验证 TOTP 码（允许 ±1 周期偏差）
     */
    public boolean verifyCode(String base64Secret, String code) {
        try {
            TimeBasedOneTimePasswordGenerator totp = new TimeBasedOneTimePasswordGenerator(Duration
                .ofSeconds(TIME_STEP_SECONDS), CODE_LENGTH);
            byte[] keyBytes = Base64.getDecoder().decode(base64Secret);
            SecretKeySpec key = new SecretKeySpec(keyBytes, ALGORITHM);
            Instant now = Instant.now();
            for (int i = -1; i <= 1; i++) {
                Instant checkTime = now.plusSeconds(i * TIME_STEP_SECONDS);
                String generated = String.format("%0" + CODE_LENGTH + "d", totp
                    .generateOneTimePassword(key, checkTime));
                if (generated.equals(code)) {
                    return true;
                }
            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }
}
