package top.wain.heimdall.system.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import top.wain.heimdall.system.model.query.SmsConfigQuery;
import top.wain.heimdall.system.model.req.SmsConfigReq;
import top.wain.heimdall.system.model.resp.SmsConfigResp;
import top.wain.heimdall.system.service.SmsConfigService;
import top.continew.starter.extension.crud.model.query.PageQuery;
import top.continew.starter.extension.crud.model.req.IdsReq;
import top.continew.starter.extension.crud.model.resp.BasePageResp;
import top.continew.starter.extension.crud.model.resp.IdResp;

/**
 * 短信配置管理 API
 *
 * @author luoqiz
 * @author WainZeng
 * @since 2025/03/15 18:41
 */
@Tag(name = "短信配置管理 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/system/sms/config")
public class SmsConfigController {

    private final SmsConfigService smsConfigService;

    @Operation(summary = "分页查询列表")
    @SaCheckPermission("system:smsConfig:list")
    @GetMapping
    public BasePageResp<SmsConfigResp> page(@Valid SmsConfigQuery query, @Valid PageQuery pageQuery) {
        return smsConfigService.page(query, pageQuery);
    }

    @Operation(summary = "查询详情")
    @Parameter(name = "id", description = "ID", example = "1", in = ParameterIn.PATH)
    @SaCheckPermission("system:smsConfig:get")
    @GetMapping("/{id}")
    public SmsConfigResp get(@PathVariable("id") Long id) {
        return smsConfigService.get(id);
    }

    @Operation(summary = "创建数据")
    @SaCheckPermission("system:smsConfig:create")
    @PostMapping
    public IdResp<Long> create(@RequestBody @Valid SmsConfigReq req) {
        return new IdResp<>(smsConfigService.create(req));
    }

    @Operation(summary = "修改数据")
    @Parameter(name = "id", description = "ID", example = "1", in = ParameterIn.PATH)
    @SaCheckPermission("system:smsConfig:update")
    @PutMapping("/{id}")
    public void update(@RequestBody @Valid SmsConfigReq req, @PathVariable("id") Long id) {
        smsConfigService.update(req, id);
    }

    @Operation(summary = "批量删除数据")
    @SaCheckPermission("system:smsConfig:delete")
    @DeleteMapping
    public void batchDelete(@RequestBody @Valid IdsReq req) {
        smsConfigService.delete(req.getIds());
    }

    @Operation(summary = "设为默认配置", description = "设为默认配置")
    @Parameter(name = "id", description = "ID", example = "1", in = ParameterIn.PATH)
    @SaCheckPermission("system:smsConfig:setDefault")
    @PutMapping("/{id}/default")
    public void setDefault(@PathVariable("id") Long id) {
        smsConfigService.setDefaultConfig(id);
    }
}
