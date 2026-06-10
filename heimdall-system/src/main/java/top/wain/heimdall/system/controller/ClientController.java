package top.wain.heimdall.system.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import top.wain.heimdall.system.model.query.ClientQuery;
import top.wain.heimdall.system.model.req.ClientReq;
import top.wain.heimdall.system.model.resp.ClientResp;
import top.wain.heimdall.system.service.ClientService;
import top.continew.starter.extension.crud.model.query.PageQuery;
import top.continew.starter.extension.crud.model.req.IdsReq;
import top.continew.starter.extension.crud.model.resp.BasePageResp;
import top.continew.starter.extension.crud.model.resp.IdResp;

/**
 * 客户端管理 API
 *
 * @author KAI
 * @since 2024/12/03 16:04
 */
@Tag(name = "客户端管理 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/system/client")
public class ClientController {

    private final ClientService clientService;

    @Operation(summary = "分页查询列表")
    @SaCheckPermission("system:client:list")
    @GetMapping
    public BasePageResp<ClientResp> page(@Valid ClientQuery query, @Valid PageQuery pageQuery) {
        return clientService.page(query, pageQuery);
    }

    @Operation(summary = "查询详情")
    @Parameter(name = "id", description = "ID", example = "1", in = ParameterIn.PATH)
    @SaCheckPermission("system:client:get")
    @GetMapping("/{id}")
    public ClientResp get(@PathVariable("id") Long id) {
        return clientService.get(id);
    }

    @Operation(summary = "创建数据")
    @SaCheckPermission("system:client:create")
    @PostMapping
    public IdResp<Long> create(@RequestBody @Valid ClientReq req) {
        return new IdResp<>(clientService.create(req));
    }

    @Operation(summary = "修改数据")
    @Parameter(name = "id", description = "ID", example = "1", in = ParameterIn.PATH)
    @SaCheckPermission("system:client:update")
    @PutMapping("/{id}")
    public void update(@RequestBody @Valid ClientReq req, @PathVariable("id") Long id) {
        clientService.update(req, id);
    }

    @Operation(summary = "批量删除数据")
    @SaCheckPermission("system:client:delete")
    @DeleteMapping
    public void batchDelete(@RequestBody @Valid IdsReq req) {
        clientService.delete(req.getIds());
    }
}
