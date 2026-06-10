package top.wain.heimdall.system.service;

import top.wain.heimdall.common.context.RoleContext;
import top.wain.heimdall.system.model.entity.RoleDO;
import top.wain.heimdall.system.model.query.RoleQuery;
import top.wain.heimdall.system.model.req.RoleReq;
import top.wain.heimdall.system.model.req.RolePermissionUpdateReq;
import top.wain.heimdall.system.model.resp.role.RoleDetailResp;
import top.wain.heimdall.system.model.resp.role.RoleResp;
import top.continew.starter.data.service.IService;
import top.continew.starter.extension.crud.model.query.SortQuery;
import top.continew.starter.extension.crud.model.resp.LabelValueResp;

import java.util.List;
import java.util.Set;

/**
 * 角色业务接口
 *
 * @author WainZeng
 * @since 2023/2/8 23:15
 */
public interface RoleService extends IService<RoleDO> {

    /**
     * 查询列表
     *
     * @param query     查询条件
     * @param sortQuery 排序条件
     * @return 角色列表
     */
    List<RoleResp> list(RoleQuery query, SortQuery sortQuery);

    /**
     * 查询详情
     *
     * @param id ID
     * @return 角色详情
     */
    RoleDetailResp get(Long id);

    /**
     * 创建角色
     *
     * @param req 请求参数
     * @return 角色 ID
     */
    Long create(RoleReq req);

    /**
     * 修改角色
     *
     * @param req 请求参数
     * @param id  ID
     */
    void update(RoleReq req, Long id);

    /**
     * 删除角色
     *
     * @param ids ID 列表
     */
    void delete(List<Long> ids);

    /**
     * 查询字典列表
     *
     * @param query     查询条件
     * @param sortQuery 排序条件
     * @return 字典列表
     */
    List<LabelValueResp> dict(RoleQuery query, SortQuery sortQuery);

    /**
     * 修改角色权限
     *
     * @param id  角色 ID
     * @param req 请求参数
     */
    void updatePermission(Long id, RolePermissionUpdateReq req);

    /**
     * 分配角色给用户
     *
     * @param id      角色 ID
     * @param userIds 用户 ID 列表
     */
    void assignToUsers(Long id, List<Long> userIds);

    /**
     * 更新用户上下文
     *
     * @param roleId 角色 ID
     */
    void updateUserContext(Long roleId);

    /**
     * 根据用户 ID 查询权限码
     *
     * @param userId 用户 ID
     * @return 权限码集合
     */
    Set<String> listPermissionByUserId(Long userId);

    /**
     * 根据用户 ID 查询角色编码
     *
     * @param userId 用户 ID
     * @return 角色编码集合
     */
    Set<String> listCodeByUserId(Long userId);

    /**
     * 根据用户 ID 查询角色
     *
     * @param userId 用户 ID
     * @return 角色集合
     */
    Set<RoleContext> listByUserId(Long userId);

    /**
     * 根据编码查询 ID
     *
     * @param code 编码
     * @return ID
     */
    Long getIdByCode(String code);

    /**
     * 根据角色名称查询
     *
     * @param list 名称列表
     * @return 角色列表
     */
    List<RoleDO> listByNames(List<String> list);

    /**
     * 根据角色名称查询数量
     *
     * @param roleNames 名称列表
     * @return 角色数量
     */
    int countByNames(List<String> roleNames);
}
