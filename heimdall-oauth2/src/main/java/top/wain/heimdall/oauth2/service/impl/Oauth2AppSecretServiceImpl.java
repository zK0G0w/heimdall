package top.wain.heimdall.oauth2.service.impl;

import cn.hutool.core.bean.BeanUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.continew.starter.core.util.validation.CheckUtils;
import top.continew.starter.core.util.validation.ValidationUtils;
import top.wain.heimdall.common.enums.DisEnableStatusEnum;
import top.wain.heimdall.oauth2.mapper.Oauth2AppMapper;
import top.wain.heimdall.oauth2.mapper.Oauth2AppSecretMapper;
import top.wain.heimdall.oauth2.model.entity.Oauth2AppSecretDO;
import top.wain.heimdall.oauth2.model.resp.Oauth2AppSecretCreateResp;
import top.wain.heimdall.oauth2.model.resp.Oauth2AppSecretResp;
import top.wain.heimdall.oauth2.service.Oauth2AppSecretService;
import top.wain.heimdall.oauth2.util.ClientCredentialGenerator;

import java.util.List;

/**
 * @Description: OAuth2 应用密钥 Service 实现
 * @Author: WainZeng
 * @Date: 2026/06/10
 */
@Service
@RequiredArgsConstructor
public class Oauth2AppSecretServiceImpl implements Oauth2AppSecretService {

    private static final int MAX_SECRET_COUNT = 5;

    private final Oauth2AppMapper appMapper;
    private final Oauth2AppSecretMapper secretMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Oauth2AppSecretCreateResp create(Long appId) {
        // 校验应用存在
        CheckUtils.throwIfNull(appMapper.selectById(appId), "应用不存在");

        // 校验启用状态密钥数量上限
        long count = secretMapper.lambdaQuery()
            .eq(Oauth2AppSecretDO::getAppId, appId)
            .eq(Oauth2AppSecretDO::getStatus, DisEnableStatusEnum.ENABLE)
            .count();
        ValidationUtils.throwIf(count >= MAX_SECRET_COUNT, "密钥数量已达上限（最多 {} 个）", MAX_SECRET_COUNT);

        // 生成密钥明文
        String plainSecret = ClientCredentialGenerator.generateClientSecret();

        // 构建密钥实体并持久化
        Oauth2AppSecretDO secretDO = new Oauth2AppSecretDO();
        secretDO.setAppId(appId);
        secretDO.setClientSecret(plainSecret);
        secretDO.setStatus(DisEnableStatusEnum.ENABLE);
        secretMapper.insert(secretDO);

        // 返回含明文的创建响应（仅此一次）
        Oauth2AppSecretCreateResp resp = new Oauth2AppSecretCreateResp();
        resp.setId(secretDO.getId());
        resp.setClientSecret(plainSecret);
        return resp;
    }

    @Override
    public List<Oauth2AppSecretResp> list(Long appId) {
        // 校验应用存在
        CheckUtils.throwIfNull(appMapper.selectById(appId), "应用不存在");

        List<Oauth2AppSecretDO> secrets = secretMapper.lambdaQuery()
            .eq(Oauth2AppSecretDO::getAppId, appId)
            .orderByDesc(Oauth2AppSecretDO::getCreateTime)
            .list();

        return secrets.stream().map(secret -> {
            Oauth2AppSecretResp resp = BeanUtil.copyProperties(secret, Oauth2AppSecretResp.class);
            // 脱敏：展示前4位 + **** + 后4位
            String raw = secret.getClientSecret();
            if (raw != null && raw.length() >= 8) {
                resp.setClientSecret(raw.substring(0, 4) + "****" + raw.substring(raw.length() - 4));
            }
            return resp;
        }).toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long appId, Long secretId) {
        Oauth2AppSecretDO secret = secretMapper.selectById(secretId);
        CheckUtils.throwIfNull(secret, "密钥不存在");
        // 校验密钥归属
        ValidationUtils.throwIf(!appId.equals(secret.getAppId()), "密钥不属于该应用");
        secretMapper.deleteById(secretId);
    }
}
