package top.wain.heimdall.tenant.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.hutool.core.lang.tree.Tree;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import top.wain.heimdall.common.api.system.MenuApi;
import top.wain.heimdall.common.config.TenantExtensionProperties;
import top.wain.heimdall.tenant.model.query.PackageQuery;
import top.wain.heimdall.tenant.model.req.PackageReq;
import top.wain.heimdall.tenant.model.resp.PackageDetailResp;
import top.wain.heimdall.tenant.model.resp.PackageResp;
import top.wain.heimdall.tenant.service.PackageService;
import top.continew.starter.extension.crud.model.query.PageQuery;
import top.continew.starter.extension.crud.model.query.SortQuery;
import top.continew.starter.extension.crud.model.resp.BasePageResp;
import top.continew.starter.extension.crud.model.resp.IdResp;
import top.continew.starter.extension.crud.model.resp.LabelValueResp;
import top.continew.starter.log.annotation.Log;

import java.util.List;

/**
 * 套餐管理 API
 *
 * @author 小熊
 * @author WainZeng
 * @since 2024/11/26 11:25
 */
@Log(module = "套餐管理")
@Tag(name = "套餐管理 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/tenant/package")
public class PackageController {

    private final PackageService packageService;
    private final TenantExtensionProperties tenantExtensionProperties;
    private final MenuApi menuApi;

    @Operation(summary = "分页查询列表")
    @SaCheckPermission("tenant:package:list")
    @GetMapping
    public BasePageResp<PackageResp> page(@Valid PackageQuery query, @Valid PageQuery pageQuery) {
        return packageService.page(query, pageQuery);
    }

    @Operation(summary = "查询列表")
    @SaCheckPermission("tenant:package:list")
    @GetMapping("/list")
    public List<PackageResp> list(@Valid PackageQuery query, @Valid SortQuery sortQuery) {
        return packageService.list(query, sortQuery);
    }

    @Operation(summary = "查询详情")
    @Parameter(name = "id", description = "ID", example = "1", in = ParameterIn.PATH)
    @SaCheckPermission("tenant:package:get")
    @GetMapping("/{id}")
    public PackageDetailResp get(@PathVariable("id") Long id) {
        return packageService.get(id);
    }

    @Operation(summary = "创建数据")
    @SaCheckPermission("tenant:package:create")
    @PostMapping
    public IdResp<Long> create(@RequestBody @Valid PackageReq req) {
        return new IdResp<>(packageService.create(req));
    }

    @Operation(summary = "修改数据")
    @Parameter(name = "id", description = "ID", example = "1", in = ParameterIn.PATH)
    @SaCheckPermission("tenant:package:update")
    @PutMapping("/{id}")
    public void update(@RequestBody @Valid PackageReq req, @PathVariable("id") Long id) {
        packageService.update(req, id);
    }

    @Operation(summary = "删除数据")
    @Parameter(name = "id", description = "ID", example = "1", in = ParameterIn.PATH)
    @SaCheckPermission("tenant:package:delete")
    @DeleteMapping("/{id}")
    public void delete(@PathVariable("id") Long id) {
        packageService.delete(id);
    }

    @Operation(summary = "查询字典列表")
    @GetMapping("/dict")
    public List<LabelValueResp> dict(@Valid PackageQuery query, @Valid SortQuery sortQuery) {
        return packageService.dict(query, sortQuery);
    }

    @Operation(summary = "查询租户套餐菜单", description = "查询租户套餐菜单树列表")
    @SaCheckPermission("tenant:package:list")
    @GetMapping("/menu/tree")
    public List<Tree<Long>> listMenuTree(@RequestParam(required = false, defaultValue = "true") Boolean isSimple) {
        return menuApi.listTree(tenantExtensionProperties.getIgnoreMenus(), isSimple);
    }
}
