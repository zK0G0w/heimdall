package top.wain.heimdall.open.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import top.wain.heimdall.open.model.entity.AppDO;
import top.continew.starter.data.mapper.BaseMapper;
import top.continew.starter.encrypt.field.annotation.FieldEncrypt;

/**
 * 应用 Mapper
 *
 * @author chengzi
 * @since 2024/10/17 16:03
 */
@Mapper
public interface AppMapper extends BaseMapper<AppDO> {

    /**
     * 根据 Access Key 查询
     *
     * @param accessKey Access Key
     * @return 应用信息
     */
    @Select("select * from sys_app where deleted = 0 AND access_key = #{accessKey}")
    AppDO selectByAccessKey(@FieldEncrypt @Param("accessKey") String accessKey);
}