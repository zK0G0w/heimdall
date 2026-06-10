package top.wain.heimdall.tenant.mapper;

import org.apache.ibatis.annotations.Mapper;
import top.continew.starter.data.mapper.BaseMapper;
import top.wain.heimdall.tenant.model.entity.PackageDO;

/**
 * 套餐 Mapper
 *
 * @author 小熊
 * @since 2024/11/26 11:25
 */
@Mapper
public interface PackageMapper extends BaseMapper<PackageDO> {
}