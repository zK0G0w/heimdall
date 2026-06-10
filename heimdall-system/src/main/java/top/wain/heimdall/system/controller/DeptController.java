package top.wain.heimdall.system.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.hutool.core.lang.tree.Tree;
import top.wain.heimdall.common.annotation.ExcludeFromGlobalResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import top.wain.heimdall.system.model.query.DeptQuery;
import top.wain.heimdall.system.model.req.DeptReq;
import top.wain.heimdall.system.model.resp.DeptResp;
import top.wain.heimdall.system.service.DeptService;
import top.continew.starter.extension.crud.model.query.SortQuery;
import top.continew.starter.extension.crud.model.req.IdsReq;
import top.continew.starter.extension.crud.model.resp.IdResp;
import top.continew.starter.log.annotation.Log;

import java.util.List;

/**
 * 部门管理 API
 *
 * @author WainZeng
 * @since 2023/1/22 17:50
 */
@Log(module = "部门管理")
@Tag(name = "部门管理 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/system/dept")
public class DeptController {

    private final DeptService deptService;

    @Operation(summary = "查询树列表")
    @SaCheckPermission("system:dept:list")
    @GetMapping("/tree")
    public List<Tree<Long>> tree(@Valid DeptQuery query, @Valid SortQuery sortQuery) {
        return deptService.tree(query, sortQuery, false);
    }

    @Operation(summary = "查询树型字典列表")
    @GetMapping("/dict/tree")
    public List<Tree<Long>> treeDict(@Valid DeptQuery query, @Valid SortQuery sortQuery) {
        return deptService.tree(query, sortQuery, true);
    }

    @Operation(summary = "查询详情")
    @Parameter(name = "id", description = "ID", example = "1", in = ParameterIn.PATH)
    @SaCheckPermission("system:dept:get")
    @GetMapping("/{id}")
    public DeptResp get(@PathVariable("id") Long id) {
        return deptService.get(id);
    }

    @Operation(summary = "创建数据")
    @SaCheckPermission("system:dept:create")
    @PostMapping
    public IdResp<Long> create(@RequestBody @Valid DeptReq req) {
        return new IdResp<>(deptService.create(req));
    }

    @Operation(summary = "修改数据")
    @Parameter(name = "id", description = "ID", example = "1", in = ParameterIn.PATH)
    @SaCheckPermission("system:dept:update")
    @PutMapping("/{id}")
    public void update(@RequestBody @Valid DeptReq req, @PathVariable("id") Long id) {
        deptService.update(req, id);
    }

    @Operation(summary = "批量删除数据")
    @SaCheckPermission("system:dept:delete")
    @DeleteMapping
    public void batchDelete(@RequestBody @Valid IdsReq req) {
        deptService.delete(req.getIds());
    }

    @ExcludeFromGlobalResponse
    @Operation(summary = "导出数据")
    @SaCheckPermission("system:dept:export")
    @GetMapping("/export")
    public void export(@Valid DeptQuery query, @Valid SortQuery sortQuery, HttpServletResponse response) {
        deptService.export(query, sortQuery, response);
    }
}
