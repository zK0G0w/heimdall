package top.wain.heimdall.generator.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import top.wain.heimdall.generator.model.entity.FieldConfigDO;
import top.continew.starter.data.mapper.BaseMapper;

import java.util.List;

/**
 * 字段配置 Mapper
 *
 * @author WainZeng
 * @since 2023/4/12 23:56
 */
@Mapper
public interface FieldConfigMapper extends BaseMapper<FieldConfigDO> {

    /**
     * 根据表名称查询
     *
     * @param tableName 表名称
     * @return 字段配置信息
     */
    @Select("SELECT * FROM gen_field_config WHERE table_name = #{tableName} ORDER BY field_sort ASC")
    List<FieldConfigDO> selectListByTableName(@Param("tableName") String tableName);
}
