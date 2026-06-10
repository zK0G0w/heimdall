package top.wain.heimdall.system.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.dromara.x.file.storage.core.FileInfo;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import top.wain.heimdall.system.model.query.FileQuery;
import top.wain.heimdall.system.model.req.FileReq;
import top.wain.heimdall.system.model.resp.file.FileDirCalcSizeResp;
import top.wain.heimdall.system.model.resp.file.FileResp;
import top.wain.heimdall.system.model.resp.file.FileStatisticsResp;
import top.wain.heimdall.system.model.resp.file.FileUploadResp;
import top.wain.heimdall.system.service.FileService;
import top.continew.starter.core.util.validation.ValidationUtils;
import top.continew.starter.extension.crud.model.query.PageQuery;
import top.continew.starter.extension.crud.model.req.IdsReq;
import top.continew.starter.extension.crud.model.resp.BasePageResp;
import top.continew.starter.extension.crud.model.resp.IdResp;
import top.continew.starter.log.annotation.Log;

import java.io.IOException;

/**
 * 文件管理 API
 *
 * @author WainZeng
 * @since 2023/12/23 10:38
 */
@Tag(name = "文件管理 API")
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/system/file")
public class FileController {

    private final FileService fileService;

    @Operation(summary = "分页查询列表")
    @SaCheckPermission("system:file:list")
    @GetMapping
    public BasePageResp<FileResp> page(@Valid FileQuery query, @Valid PageQuery pageQuery) {
        return fileService.page(query, pageQuery);
    }

    @Operation(summary = "修改数据")
    @Parameter(name = "id", description = "ID", example = "1", in = ParameterIn.PATH)
    @SaCheckPermission("system:file:update")
    @PutMapping("/{id}")
    public void update(@RequestBody @Valid FileReq req, @PathVariable("id") Long id) {
        fileService.update(req, id);
    }

    @Operation(summary = "批量删除数据")
    @SaCheckPermission("system:file:delete")
    @DeleteMapping
    public void batchDelete(@RequestBody @Valid IdsReq req) {
        fileService.delete(req.getIds());
    }

    /**
     * 上传文件
     * <p>
     * 公共上传文件请使用 {@link CommonController#upload}
     * </p>
     *
     * @param file       文件
     * @param parentPath 上级目录
     * @return 文件上传响应参数
     * @throws IOException /
     */
    @Operation(summary = "上传文件", description = "上传文件")
    @Parameter(name = "parentPath", description = "上级目录（默认：/yyyy/MM/dd）", example = "/", in = ParameterIn.QUERY)
    @SaCheckPermission("system:file:upload")
    @PostMapping("/upload")
    public FileUploadResp upload(@NotNull(message = "文件不能为空") @RequestPart MultipartFile file,
                                 @RequestParam(required = false) String parentPath) throws IOException {
        ValidationUtils.throwIf(file::isEmpty, "文件不能为空");
        FileInfo fileInfo = fileService.upload(file, parentPath);
        return FileUploadResp.builder()
            .id(fileInfo.getId())
            .url(fileInfo.getUrl())
            .thUrl(fileInfo.getThUrl())
            .metadata(fileInfo.getMetadata())
            .build();
    }

    @Operation(summary = "创建文件夹", description = "创建文件夹")
    @SaCheckPermission("system:file:createDir")
    @PostMapping("/dir")
    public IdResp<Long> createDir(@RequestBody @Valid FileReq req) {
        ValidationUtils.throwIfBlank(req.getParentPath(), "上级目录不能为空");
        return new IdResp<>(fileService.createDir(req));
    }

    @Operation(summary = "计算文件夹大小", description = "计算文件夹大小")
    @SaCheckPermission("system:file:calcDirSize")
    @GetMapping("/dir/{id}/size")
    public FileDirCalcSizeResp calcDirSize(@PathVariable Long id) {
        return new FileDirCalcSizeResp(fileService.calcDirSize(id));
    }

    @Log(ignore = true)
    @Operation(summary = "查询文件资源统计", description = "查询文件资源统计")
    @SaCheckPermission("system:file:list")
    @GetMapping("/statistics")
    public FileStatisticsResp statistics() {
        return fileService.statistics();
    }

    @Log(ignore = true)
    @Operation(summary = "检测文件是否存在", description = "检测文件是否存在")
    @SaCheckPermission("system:file:check")
    @GetMapping("/check")
    public FileResp checkFile(String fileHash) {
        return fileService.check(fileHash);
    }
}
