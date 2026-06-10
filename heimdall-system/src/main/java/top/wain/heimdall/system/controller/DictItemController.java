package top.wain.heimdall.system.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import top.wain.heimdall.system.model.query.DictItemQuery;
import top.wain.heimdall.system.model.req.DictItemReq;
import top.wain.heimdall.system.model.resp.DictItemResp;
import top.wain.heimdall.system.service.DictItemService;
import top.continew.starter.extension.crud.model.query.PageQuery;
import top.continew.starter.extension.crud.model.req.IdsReq;
import top.continew.starter.extension.crud.model.resp.BasePageResp;
import top.continew.starter.extension.crud.model.resp.IdResp;
import top.continew.starter.log.annotation.Log;

/**
 * 字典项管理 API
 *
 * @author WainZeng
 * @since 2023/9/11 21:29
 */
@Log(module = "字典管理")
@Tag(name = "字典项管理 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/system/dict/item")
public class DictItemController {

    private final DictItemService dictItemService;

    @Operation(summary = "分页查询列表")
    @SaCheckPermission("system:dictItem:list")
    @GetMapping
    public BasePageResp<DictItemResp> page(@Valid DictItemQuery query, @Valid PageQuery pageQuery) {
        return dictItemService.page(query, pageQuery);
    }

    @Operation(summary = "查询详情")
    @Parameter(name = "id", description = "ID", example = "1", in = ParameterIn.PATH)
    @SaCheckPermission("system:dictItem:get")
    @GetMapping("/{id}")
    public DictItemResp get(@PathVariable("id") Long id) {
        return dictItemService.get(id);
    }

    @Operation(summary = "创建数据")
    @SaCheckPermission("system:dictItem:create")
    @PostMapping
    public IdResp<Long> create(@RequestBody @Valid DictItemReq req) {
        return new IdResp<>(dictItemService.create(req));
    }

    @Operation(summary = "修改数据")
    @Parameter(name = "id", description = "ID", example = "1", in = ParameterIn.PATH)
    @SaCheckPermission("system:dictItem:update")
    @PutMapping("/{id}")
    public void update(@RequestBody @Valid DictItemReq req, @PathVariable("id") Long id) {
        dictItemService.update(req, id);
    }

    @Operation(summary = "批量删除数据")
    @SaCheckPermission("system:dictItem:delete")
    @DeleteMapping
    public void batchDelete(@RequestBody @Valid IdsReq req) {
        dictItemService.delete(req.getIds());
    }
}
