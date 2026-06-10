package top.wain.heimdall.oauth2.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import top.continew.starter.extension.crud.model.query.PageQuery;
import top.continew.starter.extension.crud.model.req.IdsReq;
import top.continew.starter.extension.crud.model.resp.BasePageResp;
import top.continew.starter.extension.crud.model.resp.IdResp;
import top.wain.heimdall.common.model.req.CommonStatusUpdateReq;
import top.wain.heimdall.oauth2.model.query.Oauth2AppQuery;
import top.wain.heimdall.oauth2.model.req.Oauth2AppRedirectUriReq;
import top.wain.heimdall.oauth2.model.req.Oauth2AppReq;
import top.wain.heimdall.oauth2.model.req.Oauth2AppScopeReq;
import top.wain.heimdall.oauth2.model.resp.Oauth2AppDetailResp;
import top.wain.heimdall.oauth2.model.resp.Oauth2AppResp;
import top.wain.heimdall.oauth2.service.Oauth2AppService;

/**
 * @Description: OAuth2 应用管理 Controller
 * @Author: WainZeng
 * @Date: 2026/06/10
 */
@Tag(name = "OAuth2 应用管理 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/oauth2/app")
public class Oauth2AppController {

    private final Oauth2AppService oauth2AppService;

    @Operation(summary = "分页查询应用列表")
    @SaCheckPermission("oauth2:app:list")
    @GetMapping
    public BasePageResp<Oauth2AppResp> page(@Valid Oauth2AppQuery query, @Valid PageQuery pageQuery) {
        return oauth2AppService.page(query, pageQuery);
    }

    @Operation(summary = "查询应用详情")
    @Parameter(name = "id", description = "应用 ID", example = "1", in = ParameterIn.PATH)
    @SaCheckPermission("oauth2:app:get")
    @GetMapping("/{id}")
    public Oauth2AppDetailResp get(@PathVariable("id") Long id) {
        return oauth2AppService.get(id);
    }

    @Operation(summary = "创建应用")
    @SaCheckPermission("oauth2:app:create")
    @PostMapping
    public IdResp<Long> create(@RequestBody @Valid Oauth2AppReq req) {
        return new IdResp<>(oauth2AppService.create(req));
    }

    @Operation(summary = "修改应用")
    @Parameter(name = "id", description = "应用 ID", example = "1", in = ParameterIn.PATH)
    @SaCheckPermission("oauth2:app:update")
    @PutMapping("/{id}")
    public void update(@RequestBody @Valid Oauth2AppReq req, @PathVariable("id") Long id) {
        oauth2AppService.update(req, id);
    }

    @Operation(summary = "批量删除应用")
    @SaCheckPermission("oauth2:app:delete")
    @DeleteMapping
    public void batchDelete(@RequestBody @Valid IdsReq req) {
        oauth2AppService.delete(req.getIds());
    }

    @Operation(summary = "修改应用状态")
    @Parameter(name = "id", description = "应用 ID", example = "1", in = ParameterIn.PATH)
    @SaCheckPermission("oauth2:app:update")
    @PutMapping("/{id}/status")
    public void updateStatus(@PathVariable("id") Long id, @RequestBody @Valid CommonStatusUpdateReq req) {
        oauth2AppService.updateStatus(id, req.getStatus().getValue());
    }

    @Operation(summary = "修改应用回调地址")
    @Parameter(name = "appId", description = "应用 ID", example = "1", in = ParameterIn.PATH)
    @SaCheckPermission("oauth2:app:update")
    @PutMapping("/{appId}/redirect-uri")
    public void updateRedirectUris(@PathVariable("appId") Long appId, @RequestBody @Valid Oauth2AppRedirectUriReq req) {
        oauth2AppService.updateRedirectUris(appId, req);
    }

    @Operation(summary = "修改应用授权 Scope")
    @Parameter(name = "appId", description = "应用 ID", example = "1", in = ParameterIn.PATH)
    @SaCheckPermission("oauth2:app:update")
    @PutMapping("/{appId}/scope")
    public void updateScopes(@PathVariable("appId") Long appId, @RequestBody @Valid Oauth2AppScopeReq req) {
        oauth2AppService.updateScopes(appId, req);
    }
}
