package ${packageName}.${subPackageName};

import cn.dev33.satoken.annotation.SaCheckPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import top.continew.starter.extension.crud.model.query.PageQuery;
import top.continew.starter.extension.crud.model.query.SortQuery;
import top.continew.starter.extension.crud.model.req.IdsReq;
import top.continew.starter.extension.crud.model.resp.IdResp;
import top.continew.starter.extension.crud.model.resp.PageResp;
import ${packageName}.model.query.${classNamePrefix}Query;
import ${packageName}.model.req.${classNamePrefix}Req;
import ${packageName}.model.resp.${classNamePrefix}DetailResp;
import ${packageName}.model.resp.${classNamePrefix}Resp;
import ${packageName}.service.${classNamePrefix}Service;

import java.util.List;

/**
 * ${businessName}管理 API
 *
 * @author ${author}
 * @since ${datetime}
 */
@Tag(name = "${businessName}管理 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/${apiModuleName}/${apiName}")
public class ${className} {

    private final ${classNamePrefix}Service ${classNamePrefix?uncap_first}Service;

    @Operation(summary = "分页查询列表")
    @SaCheckPermission("${apiModuleName}:${apiName}:list")
    @GetMapping
    public PageResp<${classNamePrefix}Resp> page(@Valid ${classNamePrefix}Query query, @Valid PageQuery pageQuery) {
        return ${classNamePrefix?uncap_first}Service.page(query, pageQuery);
    }

    @Operation(summary = "查询详情")
    @Parameter(name = "id", description = "ID", example = "1", in = ParameterIn.PATH)
    @SaCheckPermission("${apiModuleName}:${apiName}:get")
    @GetMapping("/{id}")
    public ${classNamePrefix}DetailResp get(@PathVariable("id") Long id) {
        return ${classNamePrefix?uncap_first}Service.get(id);
    }

    @Operation(summary = "创建数据")
    @SaCheckPermission("${apiModuleName}:${apiName}:create")
    @PostMapping
    public IdResp<Long> create(@RequestBody @Valid ${classNamePrefix}Req req) {
        return new IdResp<>(${classNamePrefix?uncap_first}Service.create(req));
    }

    @Operation(summary = "修改数据")
    @Parameter(name = "id", description = "ID", example = "1", in = ParameterIn.PATH)
    @SaCheckPermission("${apiModuleName}:${apiName}:update")
    @PutMapping("/{id}")
    public void update(@RequestBody @Valid ${classNamePrefix}Req req, @PathVariable("id") Long id) {
        ${classNamePrefix?uncap_first}Service.update(req, id);
    }

    @Operation(summary = "批量删除数据")
    @SaCheckPermission("${apiModuleName}:${apiName}:delete")
    @DeleteMapping
    public void batchDelete(@RequestBody @Valid IdsReq req) {
        ${classNamePrefix?uncap_first}Service.delete(req.getIds());
    }
}
