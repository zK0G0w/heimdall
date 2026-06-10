package top.wain.heimdall.system.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import top.wain.heimdall.common.annotation.ExcludeFromGlobalResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import top.wain.heimdall.common.util.SecureUtils;
import top.wain.heimdall.system.model.query.UserQuery;
import top.wain.heimdall.system.model.req.user.UserImportReq;
import top.wain.heimdall.system.model.req.user.UserPasswordResetReq;
import top.wain.heimdall.system.model.req.user.UserReq;
import top.wain.heimdall.system.model.req.user.UserRoleUpdateReq;
import top.wain.heimdall.system.model.resp.user.UserDetailResp;
import top.wain.heimdall.system.model.resp.user.UserImportParseResp;
import top.wain.heimdall.system.model.resp.user.UserImportResp;
import top.wain.heimdall.system.model.resp.user.UserResp;
import top.wain.heimdall.system.service.UserService;
import top.continew.starter.core.util.validation.ValidationUtils;
import top.continew.starter.extension.crud.model.query.PageQuery;
import top.continew.starter.extension.crud.model.query.SortQuery;
import top.continew.starter.extension.crud.model.req.IdsReq;
import top.continew.starter.extension.crud.model.resp.BasePageResp;
import top.continew.starter.extension.crud.model.resp.IdResp;
import top.continew.starter.extension.crud.model.resp.LabelValueResp;
import top.continew.starter.log.annotation.Log;

import java.io.IOException;
import java.util.List;

/**
 * 用户管理 API
 *
 * @author WainZeng
 * @since 2023/2/20 21:00
 */
@Log(module = "用户管理")
@Tag(name = "用户管理 API")
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/system/user")
public class UserController {

    private final UserService userService;

    @Operation(summary = "分页查询列表")
    @SaCheckPermission("system:user:list")
    @GetMapping
    public BasePageResp<UserResp> page(@Valid UserQuery query, @Valid PageQuery pageQuery) {
        return userService.page(query, pageQuery);
    }

    @Operation(summary = "查询列表")
    @SaCheckPermission("system:user:list")
    @GetMapping("/list")
    public List<UserResp> list(@Valid UserQuery query, @Valid SortQuery sortQuery) {
        return userService.list(query, sortQuery);
    }

    @Operation(summary = "查询详情")
    @Parameter(name = "id", description = "ID", example = "1", in = ParameterIn.PATH)
    @SaCheckPermission("system:user:get")
    @GetMapping("/{id}")
    public UserDetailResp get(@PathVariable("id") Long id) {
        return userService.get(id);
    }

    @Operation(summary = "创建数据")
    @SaCheckPermission("system:user:create")
    @PostMapping
    public IdResp<Long> create(@RequestBody @Valid UserReq req) {
        return new IdResp<>(userService.create(req));
    }

    @Operation(summary = "修改数据")
    @Parameter(name = "id", description = "ID", example = "1", in = ParameterIn.PATH)
    @SaCheckPermission("system:user:update")
    @PutMapping("/{id}")
    public void update(@RequestBody @Valid UserReq req, @PathVariable("id") Long id) {
        userService.update(req, id);
    }

    @Operation(summary = "批量删除数据")
    @SaCheckPermission("system:user:delete")
    @DeleteMapping
    public void batchDelete(@RequestBody @Valid IdsReq req) {
        userService.delete(req.getIds());
    }

    @ExcludeFromGlobalResponse
    @Operation(summary = "导出数据")
    @SaCheckPermission("system:user:export")
    @GetMapping("/export")
    public void export(@Valid UserQuery query, @Valid SortQuery sortQuery, HttpServletResponse response) {
        userService.export(query, sortQuery, response);
    }

    @Operation(summary = "查询字典列表")
    @GetMapping("/dict")
    public List<LabelValueResp> dict(@Valid UserQuery query, @Valid SortQuery sortQuery) {
        return userService.dict(query, sortQuery);
    }

    @Operation(summary = "下载导入模板", description = "下载导入模板")
    @SaCheckPermission("system:user:import")
    @GetMapping(value = "/import/template", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public void downloadImportTemplate(HttpServletResponse response) throws IOException {
        userService.downloadImportTemplate(response);
    }

    @Operation(summary = "解析导入数据", description = "解析导入数据")
    @SaCheckPermission("system:user:import")
    @PostMapping("/import/parse")
    public UserImportParseResp parseImport(@RequestPart @NotNull(message = "文件不能为空") MultipartFile file) {
        ValidationUtils.throwIf(file::isEmpty, "文件不能为空");
        return userService.parseImport(file);
    }

    @Operation(summary = "导入数据", description = "导入数据")
    @SaCheckPermission("system:user:import")
    @PostMapping(value = "/import")
    public UserImportResp importUser(@RequestBody @Valid UserImportReq req) {
        return userService.importUser(req);
    }

    @Operation(summary = "重置密码", description = "重置用户登录密码")
    @Parameter(name = "id", description = "ID", example = "1", in = ParameterIn.PATH)
    @SaCheckPermission("system:user:resetPwd")
    @PatchMapping("/{id}/password")
    public void resetPassword(@RequestBody @Valid UserPasswordResetReq req, @PathVariable Long id) {
        String newPassword = SecureUtils.decryptPasswordByRsaPrivateKey(req.getNewPassword(), "新密码解密失败", true);
        req.setNewPassword(newPassword);
        userService.resetPassword(req, id);
    }

    @Operation(summary = "分配角色", description = "为用户新增或移除角色")
    @Parameter(name = "id", description = "ID", example = "1", in = ParameterIn.PATH)
    @SaCheckPermission("system:user:updateRole")
    @PatchMapping("/{id}/role")
    public void updateRole(@RequestBody @Valid UserRoleUpdateReq updateReq, @PathVariable Long id) {
        userService.updateRole(updateReq, id);
    }
}
