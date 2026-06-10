package top.wain.heimdall.tenant.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import top.wain.heimdall.common.api.system.UserApi;
import top.wain.heimdall.common.util.SecureUtils;
import top.wain.heimdall.tenant.model.entity.TenantDO;
import top.wain.heimdall.tenant.model.query.TenantQuery;
import top.wain.heimdall.tenant.model.req.TenantAdminUserPwdUpdateReq;
import top.wain.heimdall.tenant.model.req.TenantReq;
import top.wain.heimdall.tenant.model.resp.TenantDetailResp;
import top.wain.heimdall.tenant.model.resp.TenantResp;
import top.wain.heimdall.tenant.service.TenantService;
import top.continew.starter.extension.crud.model.query.PageQuery;
import top.continew.starter.extension.crud.model.resp.BasePageResp;
import top.continew.starter.extension.crud.model.resp.IdResp;
import top.continew.starter.extension.tenant.util.TenantUtils;
import top.continew.starter.log.annotation.Log;

/**
 * 租户管理 API
 *
 * @author 小熊
 * @author WainZeng
 * @since 2024/11/26 17:20
 */
@Log(module = "租户管理")
@Tag(name = "租户管理 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/tenant/management")
public class TenantController {

    private final TenantService tenantService;
    private final UserApi userApi;

    @Operation(summary = "分页查询列表")
    @SaCheckPermission("tenant:management:list")
    @GetMapping
    public BasePageResp<TenantResp> page(@Valid TenantQuery query, @Valid PageQuery pageQuery) {
        return tenantService.page(query, pageQuery);
    }

    @Operation(summary = "查询详情")
    @Parameter(name = "id", description = "ID", example = "1", in = ParameterIn.PATH)
    @SaCheckPermission("tenant:management:get")
    @GetMapping("/{id}")
    public TenantDetailResp get(@PathVariable("id") Long id) {
        return tenantService.get(id);
    }

    @Operation(summary = "创建数据")
    @SaCheckPermission("tenant:management:create")
    @PostMapping
    public IdResp<Long> create(@RequestBody @Valid TenantReq req) {
        return new IdResp<>(tenantService.create(req));
    }

    @Operation(summary = "修改数据")
    @Parameter(name = "id", description = "ID", example = "1", in = ParameterIn.PATH)
    @SaCheckPermission("tenant:management:update")
    @PutMapping("/{id}")
    public void update(@RequestBody @Valid TenantReq req, @PathVariable("id") Long id) {
        tenantService.update(req, id);
    }

    @Operation(summary = "删除数据")
    @Parameter(name = "id", description = "ID", example = "1", in = ParameterIn.PATH)
    @SaCheckPermission("tenant:management:delete")
    @DeleteMapping("/{id}")
    public void delete(@PathVariable("id") Long id) {
        tenantService.delete(id);
    }

    @Operation(summary = "修改租户管理员密码", description = "修改租户管理员密码")
    @SaCheckPermission("tenant:management:updateAdminUserPwd")
    @PutMapping("/{id}/admin/pwd")
    public void updateAdminUserPwd(@RequestBody @Valid TenantAdminUserPwdUpdateReq req, @PathVariable Long id) {
        TenantDO tenant = tenantService.getById(id);
        TenantUtils.execute(id, () -> {
            String password = SecureUtils.decryptPasswordByRsaPrivateKey(req.getPassword(), "新密码解密失败");
            userApi.resetPassword(password, tenant.getAdminUser());
        });
    }
}
