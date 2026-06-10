package top.wain.heimdall.system.mapper;

import org.apache.ibatis.annotations.Mapper;
import top.wain.heimdall.system.model.entity.DictDO;
import top.continew.starter.data.mapper.BaseMapper;

/**
 * 字典 Mapper
 *
 * @author WainZeng
 * @since 2023/9/11 21:29
 */
@Mapper
public interface DictMapper extends BaseMapper<DictDO> {
}