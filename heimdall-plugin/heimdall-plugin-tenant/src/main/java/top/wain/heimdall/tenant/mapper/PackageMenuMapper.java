package top.wain.heimdall.tenant.mapper;

import org.apache.ibatis.annotations.Mapper;
import top.wain.heimdall.tenant.model.entity.PackageMenuDO;
import top.continew.starter.data.mapper.BaseMapper;

/**
 * 套餐和菜单关联 Mapper
 *
 * @author WainZeng
 * @since 2025/7/13 20:24
 */
@Mapper
public interface PackageMenuMapper extends BaseMapper<PackageMenuDO> {
}
