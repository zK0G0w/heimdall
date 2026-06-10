package top.wain.heimdall.system.controller;

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
import top.wain.heimdall.system.model.query.SmsLogQuery;
import top.wain.heimdall.system.model.resp.SmsLogResp;
import top.wain.heimdall.system.service.SmsLogService;
import top.continew.starter.extension.crud.model.query.PageQuery;
import top.continew.starter.extension.crud.model.query.SortQuery;
import top.continew.starter.extension.crud.model.req.IdsReq;
import top.continew.starter.extension.crud.model.resp.BasePageResp;

/**
 * 短信日志管理 API
 *
 * @author luoqiz
 * @author WainZeng
 * @since 2025/03/15 22:15
 */
@Tag(name = "短信日志管理 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/system/sms/log")
public class SmsLogController {

    private final SmsLogService smsLogService;

    @Operation(summary = "分页查询列表")
    @SaCheckPermission("system:smsLog:list")
    @GetMapping
    public BasePageResp<SmsLogResp> page(@Valid SmsLogQuery query, @Valid PageQuery pageQuery) {
        return smsLogService.page(query, pageQuery);
    }

    @Operation(summary = "查询详情")
    @Parameter(name = "id", description = "ID", example = "1", in = ParameterIn.PATH)
    @SaCheckPermission("system:smsLog:get")
    @GetMapping("/{id}")
    public SmsLogResp get(@PathVariable("id") Long id) {
        return smsLogService.get(id);
    }

    @Operation(summary = "批量删除数据")
    @SaCheckPermission("system:smsLog:delete")
    @DeleteMapping
    public void batchDelete(@RequestBody @Valid IdsReq req) {
        smsLogService.delete(req.getIds());
    }

    @ExcludeFromGlobalResponse
    @Operation(summary = "导出数据")
    @SaCheckPermission("system:smsLog:export")
    @GetMapping("/export")
    public void export(@Valid SmsLogQuery query, @Valid SortQuery sortQuery, HttpServletResponse response) {
        smsLogService.export(query, sortQuery, response);
    }
}
