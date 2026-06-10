package top.wain.heimdall.system.service;

import cn.hutool.core.lang.tree.Tree;
import top.wain.heimdall.system.model.entity.MenuDO;
import top.wain.heimdall.system.model.query.MenuQuery;
import top.wain.heimdall.system.model.req.MenuReq;
import top.wain.heimdall.system.model.resp.MenuResp;
import top.continew.starter.data.service.IService;
import top.continew.starter.extension.crud.model.query.SortQuery;

import java.util.List;
import java.util.Set;

/**
 * 菜单业务接口
 *
 * @author WainZeng
 * @since 2023/2/15 20:30
 */
public interface MenuService extends IService<MenuDO> {

    List<Tree<Long>> tree(MenuQuery query, SortQuery sortQuery, boolean isSimple);

    MenuResp get(Long id);

    Long create(MenuReq req);

    void update(MenuReq req, Long id);

    void delete(List<Long> ids);

    /**
     * 根据用户 ID 查询
     *
     * @param userId 用户 ID
     * @return 权限码集合
     */
    Set<String> listPermissionByUserId(Long userId);

    /**
     * 根据角色 ID 查询
     *
     * @param roleId 角色 ID
     * @return 菜单列表
     */
    List<MenuResp> listByRoleId(Long roleId);

    /**
     * 查询租户排除的菜单 ID 列表
     *
     * @return 租户排除的菜单 ID 列表
     */
    List<Long> listExcludeTenantMenu();

    /**
     * 级联查询菜单及其所有层级子菜单 ID 列表
     *
     * @param menuIds 菜单 ID 列表
     * @return 包含自身及所有层级子菜单的 ID 列表
     */
    List<Long> listChildMenuIds(List<Long> menuIds);
}
