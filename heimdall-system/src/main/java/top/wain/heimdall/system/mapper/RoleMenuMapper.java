package top.wain.heimdall.system.mapper;

import org.apache.ibatis.annotations.Mapper;
import top.wain.heimdall.system.model.entity.RoleMenuDO;
import top.continew.starter.data.mapper.BaseMapper;

import java.util.List;

/**
 * 角色和菜单 Mapper
 *
 * @author WainZeng
 * @since 2023/2/15 20:30
 */
@Mapper
public interface RoleMenuMapper extends BaseMapper<RoleMenuDO> {

    /**
     * 根据角色 ID 列表查询
     *
     * @param roleIds 角色 ID 列表
     * @return 菜单 ID 列表
     */
    List<Long> selectMenuIdByRoleIds(List<Long> roleIds);
}
