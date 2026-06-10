package top.wain.heimdall.oauth2.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import top.continew.starter.extension.crud.model.req.IdsReq;
import top.continew.starter.extension.crud.model.resp.IdResp;
import top.wain.heimdall.oauth2.model.req.Oauth2ScopeReq;
import top.wain.heimdall.oauth2.model.resp.Oauth2ScopeResp;
import top.wain.heimdall.oauth2.service.Oauth2ScopeService;

import java.util.List;

/**
 * @Description: OAuth2 Scope 管理 Controller
 * @Author: WainZeng
 * @Date: 2026/06/10
 */
@Tag(name = "OAuth2 Scope 管理 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/oauth2/scope")
public class Oauth2ScopeController {

    private final Oauth2ScopeService oauth2ScopeService;

    @Operation(summary = "查询 Scope 列表")
    @SaCheckPermission("oauth2:scope:list")
    @GetMapping
    public List<Oauth2ScopeResp> list() {
        return oauth2ScopeService.list();
    }

    @Operation(summary = "创建 Scope")
    @SaCheckPermission("oauth2:scope:create")
    @PostMapping
    public IdResp<Long> create(@RequestBody @Valid Oauth2ScopeReq req) {
        return new IdResp<>(oauth2ScopeService.create(req));
    }

    @Operation(summary = "修改 Scope")
    @Parameter(name = "id", description = "Scope ID", example = "1", in = ParameterIn.PATH)
    @SaCheckPermission("oauth2:scope:update")
    @PutMapping("/{id}")
    public void update(@RequestBody @Valid Oauth2ScopeReq req, @PathVariable("id") Long id) {
        oauth2ScopeService.update(req, id);
    }

    @Operation(summary = "批量删除 Scope")
    @SaCheckPermission("oauth2:scope:delete")
    @DeleteMapping
    public void batchDelete(@RequestBody @Valid IdsReq req) {
        oauth2ScopeService.delete(req.getIds());
    }
}
