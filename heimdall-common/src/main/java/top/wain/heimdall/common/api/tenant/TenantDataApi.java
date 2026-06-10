package top.wain.heimdall.common.api.tenant;

import top.wain.heimdall.common.model.dto.TenantDTO;

/**
 * 租户数据 API
 * 
 * @author 小熊
 * @author WainZeng
 * @since 2024/12/2 20:08
 */
public interface TenantDataApi {

    /**
     * 初始化数据
     *
     * @param tenant 租户信息
     */
    void init(TenantDTO tenant);

    /**
     * 清除数据
     */
    void clear();
}
