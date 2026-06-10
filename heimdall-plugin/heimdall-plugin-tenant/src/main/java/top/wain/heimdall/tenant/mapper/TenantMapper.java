package top.wain.heimdall.tenant.mapper;

import org.apache.ibatis.annotations.Mapper;
import top.wain.heimdall.tenant.model.entity.TenantDO;
import top.continew.starter.data.mapper.BaseMapper;

/**
 * 租户 Mapper
 *
 * @author 小熊
 * @since 2024/11/26 17:20
 */
@Mapper
public interface TenantMapper extends BaseMapper<TenantDO> {
}