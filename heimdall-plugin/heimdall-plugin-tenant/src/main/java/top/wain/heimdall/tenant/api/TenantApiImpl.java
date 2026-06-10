package top.wain.heimdall.tenant.api;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import top.wain.heimdall.common.api.tenant.TenantApi;
import top.wain.heimdall.tenant.constant.TenantCacheConstants;
import top.wain.heimdall.tenant.mapper.TenantMapper;
import top.wain.heimdall.tenant.model.entity.TenantDO;
import top.continew.starter.cache.redisson.util.RedisUtils;
import top.continew.starter.extension.crud.model.entity.BaseIdDO;

/**
 * 租户业务 API 实现
 *
 * @author WainZeng
 * @since 2025/7/23 21:13
 */
@Service
@RequiredArgsConstructor
public class TenantApiImpl implements TenantApi {

    private final TenantMapper baseMapper;

    @Override
    public void bindAdminUser(Long tenantId, Long userId) {
        baseMapper.lambdaUpdate().set(TenantDO::getAdminUser, userId).eq(BaseIdDO::getId, tenantId).update();
        // 更新租户缓存
        TenantDO entity = baseMapper.selectById(tenantId);
        RedisUtils.set(TenantCacheConstants.TENANT_KEY_PREFIX + tenantId, entity);
    }
}
