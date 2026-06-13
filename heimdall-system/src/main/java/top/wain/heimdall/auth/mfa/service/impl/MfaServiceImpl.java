package top.wain.heimdall.auth.mfa.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import top.continew.starter.cache.redisson.util.RedisUtils;
import top.continew.starter.core.exception.BadRequestException;
import top.wain.heimdall.auth.mfa.MfaPolicyService;
import top.wain.heimdall.auth.mfa.TotpMfaVerifier;
import top.wain.heimdall.auth.mfa.mapper.UserMfaMapper;
import top.wain.heimdall.auth.mfa.model.entity.UserMfaDO;
import top.wain.heimdall.auth.mfa.model.resp.MfaSetupResp;
import top.wain.heimdall.auth.mfa.model.resp.MfaStatusResp;
import top.wain.heimdall.auth.mfa.service.MfaService;
import top.wain.heimdall.system.service.UserService;

import javax.crypto.KeyGenerator;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/**
 * @Description: MFA 业务实现
 * @Author: WainZeng
 * @Date: 2026/06/13
 */
@Service
@RequiredArgsConstructor
public class MfaServiceImpl implements MfaService {

    private static final String SETUP_KEY_PREFIX = "mfa:setup:";
    private static final Duration SETUP_TTL = Duration.ofMinutes(10);
    private static final String ISSUER = "Heimdall";
    private static final int BACKUP_CODE_COUNT = 8;
    private static final int BACKUP_CODE_LENGTH = 8;

    private final UserMfaMapper userMfaMapper;
    private final TotpMfaVerifier totpMfaVerifier;
    private final MfaPolicyService mfaPolicyService;
    private final UserService userService;

    @Override
    public MfaSetupResp initSetup(Long userId) {
        byte[] keyBytes = generateKey();
        String base64Secret = Base64.getEncoder().encodeToString(keyBytes);

        RedisUtils.set(SETUP_KEY_PREFIX + userId, base64Secret, SETUP_TTL);

        String username = userService.getById(userId).getUsername();
        String otpauthUri = String
            .format("otpauth://totp/%s:%s?secret=%s&issuer=%s&algorithm=SHA1&digits=6&period=30", ISSUER, username, base32Encode(keyBytes), ISSUER);

        MfaSetupResp resp = new MfaSetupResp();
        resp.setSecret(base64Secret);
        resp.setQrcodeUri(otpauthUri);
        return resp;
    }

    @Override
    public List<String> confirmSetup(Long userId, String code) {
        String secret = RedisUtils.get(SETUP_KEY_PREFIX + userId);
        if (secret == null) {
            throw new BadRequestException("绑定已过期，请删除 Authenticator App 中的旧条目后重新扫码绑定");
        }

        if (!totpMfaVerifier.verifyCode(secret, code)) {
            throw new BadRequestException("验证码不正确");
        }

        List<String> backupCodes = generateBackupCodes();

        UserMfaDO mfa = userMfaMapper.selectOne(new LambdaQueryWrapper<UserMfaDO>().eq(UserMfaDO::getUserId, userId));
        if (mfa == null) {
            mfa = new UserMfaDO();
            mfa.setId(IdUtil.getSnowflakeNextId());
            mfa.setUserId(userId);
            mfa.setType("totp");
            mfa.setSecret(secret);
            mfa.setBackupCodes(JSONUtil.toJsonStr(backupCodes));
            mfa.setEnabled(1);
            userMfaMapper.insert(mfa);
        } else {
            mfa.setSecret(secret);
            mfa.setBackupCodes(JSONUtil.toJsonStr(backupCodes));
            mfa.setEnabled(1);
            userMfaMapper.updateById(mfa);
        }

        RedisUtils.delete(SETUP_KEY_PREFIX + userId);
        return backupCodes;
    }

    @Override
    public void disable(Long userId, String code) {
        if (mfaPolicyService.isRequired(userId)) {
            throw new BadRequestException("管理员已要求开启多因素认证，无法关闭");
        }
        if (!totpMfaVerifier.verify(userId, code)) {
            throw new BadRequestException("验证码不正确");
        }
        userMfaMapper.delete(new LambdaQueryWrapper<UserMfaDO>().eq(UserMfaDO::getUserId, userId));
    }

    @Override
    public List<String> regenerateBackupCodes(Long userId, String code) {
        if (!totpMfaVerifier.verify(userId, code)) {
            throw new BadRequestException("验证码不正确");
        }
        UserMfaDO mfa = userMfaMapper.selectOne(new LambdaQueryWrapper<UserMfaDO>().eq(UserMfaDO::getUserId, userId)
            .eq(UserMfaDO::getEnabled, 1));
        if (mfa == null) {
            throw new BadRequestException("MFA 未启用");
        }
        List<String> newCodes = generateBackupCodes();
        mfa.setBackupCodes(JSONUtil.toJsonStr(newCodes));
        userMfaMapper.updateById(mfa);
        return newCodes;
    }

    @Override
    public MfaStatusResp getStatus(Long userId) {
        MfaStatusResp resp = new MfaStatusResp();
        UserMfaDO mfa = userMfaMapper.selectOne(new LambdaQueryWrapper<UserMfaDO>().eq(UserMfaDO::getUserId, userId)
            .eq(UserMfaDO::getEnabled, 1));
        if (mfa != null) {
            resp.setEnabled(true);
            resp.setType(mfa.getType());
            List<String> codes = JSONUtil.toList(mfa.getBackupCodes(), String.class);
            resp.setRemainingBackupCodes(codes.size());
        } else {
            resp.setEnabled(false);
            resp.setRemainingBackupCodes(0);
        }
        resp.setForced(mfaPolicyService.isRequired(userId));
        return resp;
    }

    private byte[] generateKey() {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance("HmacSHA1");
            keyGenerator.init(160);
            return keyGenerator.generateKey().getEncoded();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private List<String> generateBackupCodes() {
        List<String> codes = new ArrayList<>(BACKUP_CODE_COUNT);
        for (int i = 0; i < BACKUP_CODE_COUNT; i++) {
            codes.add(RandomUtil.randomString("ABCDEFGHJKLMNPQRSTUVWXYZ23456789", BACKUP_CODE_LENGTH));
        }
        return codes;
    }

    private String base32Encode(byte[] data) {
        String base32Chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";
        StringBuilder sb = new StringBuilder();
        int buffer = 0;
        int bitsLeft = 0;
        for (byte b : data) {
            buffer = (buffer << 8) | (b & 0xFF);
            bitsLeft += 8;
            while (bitsLeft >= 5) {
                sb.append(base32Chars.charAt((buffer >> (bitsLeft - 5)) & 0x1F));
                bitsLeft -= 5;
            }
        }
        if (bitsLeft > 0) {
            sb.append(base32Chars.charAt((buffer << (5 - bitsLeft)) & 0x1F));
        }
        return sb.toString();
    }
}
