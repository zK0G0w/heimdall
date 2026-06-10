package top.wain.heimdall.system.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.hutool.core.collection.CollUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import top.wain.heimdall.system.enums.NoticeMethodEnum;
import top.wain.heimdall.system.model.query.NoticeQuery;
import top.wain.heimdall.system.model.req.NoticeReq;
import top.wain.heimdall.system.model.resp.notice.NoticeDetailResp;
import top.wain.heimdall.system.model.resp.notice.NoticeResp;
import top.wain.heimdall.system.service.NoticeService;
import top.continew.starter.core.util.validation.ValidationUtils;
import top.continew.starter.extension.crud.model.query.PageQuery;
import top.continew.starter.extension.crud.model.req.IdsReq;
import top.continew.starter.extension.crud.model.resp.BasePageResp;
import top.continew.starter.extension.crud.model.resp.IdResp;
import top.continew.starter.log.annotation.Log;

import java.util.Arrays;
import java.util.List;

/**
 * 公告管理 API
 *
 * @author WainZeng
 * @since 2023/8/20 10:55
 */
@Log(module = "公告管理")
@Tag(name = "公告管理 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/system/notice")
public class NoticeController {

    private final NoticeService noticeService;

    @Operation(summary = "分页查询列表")
    @SaCheckPermission("system:notice:list")
    @GetMapping
    public BasePageResp<NoticeResp> page(@Valid NoticeQuery query, @Valid PageQuery pageQuery) {
        return noticeService.page(query, pageQuery);
    }

    @Operation(summary = "查询详情")
    @Parameter(name = "id", description = "ID", example = "1", in = ParameterIn.PATH)
    @SaCheckPermission("system:notice:get")
    @GetMapping("/{id}")
    public NoticeDetailResp get(@PathVariable("id") Long id) {
        return noticeService.get(id);
    }

    @Operation(summary = "创建数据")
    @SaCheckPermission("system:notice:create")
    @PostMapping
    public IdResp<Long> create(@RequestBody @Valid NoticeReq req) {
        this.validateNoticeMethods(req);
        return new IdResp<>(noticeService.create(req));
    }

    @Operation(summary = "修改数据")
    @Parameter(name = "id", description = "ID", example = "1", in = ParameterIn.PATH)
    @SaCheckPermission("system:notice:update")
    @PutMapping("/{id}")
    public void update(@RequestBody @Valid NoticeReq req, @PathVariable("id") Long id) {
        this.validateNoticeMethods(req);
        noticeService.update(req, id);
    }

    @Operation(summary = "批量删除数据")
    @SaCheckPermission("system:notice:delete")
    @DeleteMapping
    public void batchDelete(@RequestBody @Valid IdsReq req) {
        noticeService.delete(req.getIds());
    }

    /**
     * 校验通知方式
     */
    private void validateNoticeMethods(NoticeReq req) {
        List<Integer> noticeMethods = req.getNoticeMethods();
        if (CollUtil.isNotEmpty(noticeMethods)) {
            List<Integer> validMethods = Arrays.stream(NoticeMethodEnum.values())
                .map(NoticeMethodEnum::getValue)
                .toList();
            noticeMethods.forEach(method -> ValidationUtils.throwIf(!validMethods
                .contains(method), "通知方式 [{}] 不正确", method));
        }
    }
}
