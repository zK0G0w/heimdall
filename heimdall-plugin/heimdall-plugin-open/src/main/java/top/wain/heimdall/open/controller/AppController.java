package top.wain.heimdall.open.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import top.wain.heimdall.common.annotation.ExcludeFromGlobalResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import top.wain.heimdall.open.model.query.AppQuery;
import top.wain.heimdall.open.model.req.AppReq;
import top.wain.heimdall.open.model.resp.AppDetailResp;
import top.wain.heimdall.open.model.resp.AppResp;
import top.wain.heimdall.open.model.resp.AppSecretResp;
import top.wain.heimdall.open.service.AppService;
import top.continew.starter.extension.crud.model.query.PageQuery;
import top.continew.starter.extension.crud.model.query.SortQuery;
import top.continew.starter.extension.crud.model.req.IdsReq;
import top.continew.starter.extension.crud.model.resp.BasePageResp;
import top.continew.starter.extension.crud.model.resp.IdResp;

/**
 * 应用管理 API
 *
 * @author chengzi
 * @author WainZeng
 * @since 2024/10/17 16:03
 */
@Tag(name = "应用管理 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/open/app")
public class AppController {

    private final AppService appService;

    @Operation(summary = "分页查询列表")
    @SaCheckPermission("open:app:list")
    @GetMapping
    public BasePageResp<AppResp> page(@Valid AppQuery query, @Valid PageQuery pageQuery) {
        return appService.page(query, pageQuery);
    }

    @Operation(summary = "查询详情")
    @Parameter(name = "id", description = "ID", example = "1", in = ParameterIn.PATH)
    @SaCheckPermission("open:app:get")
    @GetMapping("/{id}")
    public AppDetailResp get(@PathVariable("id") Long id) {
        return appService.get(id);
    }

    @Operation(summary = "创建数据")
    @SaCheckPermission("open:app:create")
    @PostMapping
    public IdResp<Long> create(@RequestBody @Valid AppReq req) {
        return new IdResp<>(appService.create(req));
    }

    @Operation(summary = "修改数据")
    @Parameter(name = "id", description = "ID", example = "1", in = ParameterIn.PATH)
    @SaCheckPermission("open:app:update")
    @PutMapping("/{id}")
    public void update(@RequestBody @Valid AppReq req, @PathVariable("id") Long id) {
        appService.update(req, id);
    }

    @Operation(summary = "批量删除数据")
    @SaCheckPermission("open:app:delete")
    @DeleteMapping
    public void batchDelete(@RequestBody @Valid IdsReq req) {
        appService.delete(req.getIds());
    }

    @ExcludeFromGlobalResponse
    @Operation(summary = "导出数据")
    @SaCheckPermission("open:app:export")
    @GetMapping("/export")
    public void export(@Valid AppQuery query, @Valid SortQuery sortQuery, HttpServletResponse response) {
        appService.export(query, sortQuery, response);
    }

    @Operation(summary = "获取密钥", description = "获取应用密钥")
    @Parameter(name = "id", description = "ID", example = "1", in = ParameterIn.PATH)
    @SaCheckPermission("open:app:secret")
    @GetMapping("/{id}/secret")
    public AppSecretResp getSecret(@PathVariable Long id) {
        return appService.getSecret(id);
    }

    @Operation(summary = "重置密钥", description = "重置应用密钥")
    @Parameter(name = "id", description = "ID", example = "1", in = ParameterIn.PATH)
    @SaCheckPermission("open:app:resetSecret")
    @PatchMapping("/{id}/secret")
    public void resetSecret(@PathVariable Long id) {
        appService.resetSecret(id);
    }
}
