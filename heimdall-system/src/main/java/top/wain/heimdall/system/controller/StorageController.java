package top.wain.heimdall.system.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import top.wain.heimdall.common.model.req.CommonStatusUpdateReq;
import top.wain.heimdall.system.model.query.StorageQuery;
import top.wain.heimdall.system.model.req.StorageReq;
import top.wain.heimdall.system.model.resp.StorageResp;
import top.wain.heimdall.system.service.StorageService;
import top.continew.starter.extension.crud.model.query.SortQuery;
import top.continew.starter.extension.crud.model.req.IdsReq;
import top.continew.starter.extension.crud.model.resp.IdResp;

import java.util.List;

/**
 * 存储管理 API
 *
 * @author WainZeng
 * @since 2023/12/26 22:09
 */
@Tag(name = "存储管理 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/system/storage")
public class StorageController {

    private final StorageService storageService;

    @Operation(summary = "查询列表")
    @SaCheckPermission("system:storage:list")
    @GetMapping("/list")
    public List<StorageResp> list(@Valid StorageQuery query, @Valid SortQuery sortQuery) {
        return storageService.list(query, sortQuery);
    }

    @Operation(summary = "查询详情")
    @Parameter(name = "id", description = "ID", example = "1", in = ParameterIn.PATH)
    @SaCheckPermission("system:storage:get")
    @GetMapping("/{id}")
    public StorageResp get(@PathVariable("id") Long id) {
        return storageService.get(id);
    }

    @Operation(summary = "创建数据")
    @SaCheckPermission("system:storage:create")
    @PostMapping
    public IdResp<Long> create(@RequestBody @Valid StorageReq req) {
        return new IdResp<>(storageService.create(req));
    }

    @Operation(summary = "修改数据")
    @Parameter(name = "id", description = "ID", example = "1", in = ParameterIn.PATH)
    @SaCheckPermission("system:storage:update")
    @PutMapping("/{id}")
    public void update(@RequestBody @Valid StorageReq req, @PathVariable("id") Long id) {
        storageService.update(req, id);
    }

    @Operation(summary = "批量删除数据")
    @SaCheckPermission("system:storage:delete")
    @DeleteMapping
    public void batchDelete(@RequestBody @Valid IdsReq req) {
        storageService.delete(req.getIds());
    }

    @Operation(summary = "修改状态", description = "修改状态")
    @Parameter(name = "id", description = "ID", example = "1", in = ParameterIn.PATH)
    @SaCheckPermission("system:storage:updateStatus")
    @PutMapping("/{id}/status")
    public void updateStatus(@RequestBody @Valid CommonStatusUpdateReq req, @PathVariable("id") Long id) {
        storageService.updateStatus(req, id);
    }

    @Operation(summary = "设为默认存储", description = "设为默认存储")
    @Parameter(name = "id", description = "ID", example = "1", in = ParameterIn.PATH)
    @SaCheckPermission("system:storage:setDefault")
    @PutMapping("/{id}/default")
    public void setDefault(@PathVariable("id") Long id) {
        storageService.setDefaultStorage(id);
    }
}
