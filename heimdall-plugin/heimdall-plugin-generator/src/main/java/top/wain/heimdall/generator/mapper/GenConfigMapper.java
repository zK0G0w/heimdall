package top.wain.heimdall.generator.mapper;

import org.apache.ibatis.annotations.Mapper;
import top.wain.heimdall.generator.model.entity.GenConfigDO;
import top.continew.starter.data.mapper.BaseMapper;

/**
 * 生成配置 Mapper
 *
 * @author WainZeng
 * @since 2023/4/12 23:56
 */
@Mapper
public interface GenConfigMapper extends BaseMapper<GenConfigDO> {
}
