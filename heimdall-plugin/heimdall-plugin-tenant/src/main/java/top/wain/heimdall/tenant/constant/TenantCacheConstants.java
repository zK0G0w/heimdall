package top.wain.heimdall.tenant.constant;

import top.wain.heimdall.common.constant.CacheConstants;

/**
 * 租户缓存相关常量
 *
 * @author WainZeng
 * @since 2025/7/14 20:35
 */
public class TenantCacheConstants {

    /**
     * 分隔符
     */
    public static final String DELIMITER = CacheConstants.DELIMITER;

    /**
     * 租户前缀
     */
    public static final String TENANT_KEY_PREFIX = "TENANT" + DELIMITER;

    private TenantCacheConstants() {
    }
}
