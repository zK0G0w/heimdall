package top.wain.heimdall.system.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.tree.Tree;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import top.wain.heimdall.system.model.query.RoleQuery;
import top.wain.heimdall.system.model.query.RoleUserQuery;
import top.wain.heimdall.system.model.req.RoleReq;
import top.wain.heimdall.system.model.req.RolePermissionUpdateReq;
import top.wain.heimdall.system.model.resp.role.RoleDetailResp;
import top.wain.heimdall.system.model.resp.role.RolePermissionResp;
import top.wain.heimdall.system.model.resp.role.RoleResp;
import top.wain.heimdall.system.model.resp.role.RoleUserResp;
import top.wain.heimdall.system.service.MenuService;
import top.wain.heimdall.system.service.RoleService;
import top.wain.heimdall.system.service.UserRoleService;
import top.continew.starter.extension.crud.model.query.PageQuery;
import top.continew.starter.extension.crud.model.query.SortQuery;
import top.continew.starter.extension.crud.model.req.IdsReq;
import top.continew.starter.extension.crud.model.resp.IdResp;
import top.continew.starter.extension.crud.model.resp.LabelValueResp;
import top.continew.starter.extension.crud.model.resp.PageResp;
import top.continew.starter.log.annotation.Log;

import java.util.List;

/**
 * 角色管理 API
 *
 * @author WainZeng
 * @since 2023/2/8 23:11
 */
@Log(module = "角色管理")
@Tag(name = "角色管理 API")
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/system/role")
public class RoleController {

    private final RoleService roleService;
    private final UserRoleService userRoleService;
    private final MenuService menuService;

    @Operation(summary = "查询列表")
    @SaCheckPermission("system:role:list")
    @GetMapping("/list")
    public List<RoleResp> list(@Valid RoleQuery query, @Valid SortQuery sortQuery) {
        return roleService.list(query, sortQuery);
    }

    @Operation(summary = "查询详情")
    @Parameter(name = "id", description = "ID", example = "1", in = ParameterIn.PATH)
    @SaCheckPermission("system:role:get")
    @GetMapping("/{id}")
    public RoleDetailResp get(@PathVariable("id") Long id) {
        return roleService.get(id);
    }

    @Operation(summary = "创建数据")
    @SaCheckPermission("system:role:create")
    @PostMapping
    public IdResp<Long> create(@RequestBody @Valid RoleReq req) {
        return new IdResp<>(roleService.create(req));
    }

    @Operation(summary = "修改数据")
    @Parameter(name = "id", description = "ID", example = "1", in = ParameterIn.PATH)
    @SaCheckPermission("system:role:update")
    @PutMapping("/{id}")
    public void update(@RequestBody @Valid RoleReq req, @PathVariable("id") Long id) {
        roleService.update(req, id);
    }

    @Operation(summary = "批量删除数据")
    @SaCheckPermission("system:role:delete")
    @DeleteMapping
    public void batchDelete(@RequestBody @Valid IdsReq req) {
        roleService.delete(req.getIds());
    }

    @Operation(summary = "查询字典列表")
    @GetMapping("/dict")
    public List<LabelValueResp> dict(@Valid RoleQuery query, @Valid SortQuery sortQuery) {
        return roleService.dict(query, sortQuery);
    }

    @Operation(summary = "查询角色权限树列表", description = "查询角色权限树列表")
    @SaCheckPermission("system:role:updatePermission")
    @GetMapping("/permission/tree")
    public List<RolePermissionResp> listPermissionTree() {
        List<Tree<Long>> treeList = menuService.tree(null, null, false);
        return BeanUtil.copyToList(treeList, RolePermissionResp.class);
    }

    @Operation(summary = "修改权限", description = "修改角色的功能权限")
    @SaCheckPermission("system:role:updatePermission")
    @PutMapping("/{id}/permission")
    public void updatePermission(@PathVariable("id") Long id, @RequestBody @Valid RolePermissionUpdateReq req) {
        roleService.updatePermission(id, req);
    }

    @Operation(summary = "分页查询关联用户", description = "分页查询角色关联的用户列表")
    @Parameter(name = "id", description = "ID", example = "1", in = ParameterIn.PATH)
    @SaCheckPermission("system:role:list")
    @GetMapping("/{id}/user")
    public PageResp<RoleUserResp> pageUser(@PathVariable("id") Long id,
                                           @Valid RoleUserQuery query,
                                           @Valid PageQuery pageQuery) {
        query.setRoleId(id);
        return userRoleService.pageUser(query, pageQuery);
    }

    @Operation(summary = "分配用户", description = "批量分配角色给用户")
    @SaCheckPermission("system:role:assign")
    @PostMapping("/{id}/user")
    public void assignToUsers(@PathVariable("id") Long id,
                              @RequestBody @NotEmpty(message = "用户ID列表不能为空") List<Long> userIds) {
        roleService.assignToUsers(id, userIds);
    }

    @Operation(summary = "取消分配用户", description = "批量取消分配角色给用户")
    @SaCheckPermission("system:role:unassign")
    @DeleteMapping("/user")
    public void unassignFromUsers(@RequestBody @NotEmpty(message = "用户列表不能为空") List<Long> userRoleIds) {
        userRoleService.deleteByIds(userRoleIds);
    }

    @Operation(summary = "查询关联用户ID", description = "查询角色关联的用户ID列表")
    @Parameter(name = "id", description = "ID", example = "1", in = ParameterIn.PATH)
    @SaCheckPermission("system:role:list")
    @GetMapping("/{id}/user/id")
    public List<Long> listUserId(@PathVariable("id") Long id) {
        return userRoleService.listUserIdByRoleId(id);
    }
}
