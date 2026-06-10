package top.wain.heimdall.system.mapper;

import org.apache.ibatis.annotations.Mapper;
import top.wain.heimdall.system.model.entity.StorageDO;
import top.continew.starter.data.mapper.BaseMapper;

/**
 * 存储 Mapper
 *
 * @author WainZeng
 * @since 2023/12/26 22:09
 */
@Mapper
public interface StorageMapper extends BaseMapper<StorageDO> {
}