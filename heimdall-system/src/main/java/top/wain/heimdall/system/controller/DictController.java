package top.wain.heimdall.system.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import top.wain.heimdall.common.constant.CacheConstants;
import top.wain.heimdall.system.model.query.DictQuery;
import top.wain.heimdall.system.model.req.DictReq;
import top.wain.heimdall.system.model.resp.DictResp;
import top.wain.heimdall.system.service.DictService;
import top.continew.starter.cache.redisson.util.RedisUtils;
import top.continew.starter.extension.crud.model.query.SortQuery;
import top.continew.starter.extension.crud.model.req.IdsReq;
import top.continew.starter.extension.crud.model.resp.IdResp;

import java.util.List;

/**
 * 字典管理 API
 *
 * @author WainZeng
 * @since 2023/9/11 21:29
 */
@Tag(name = "字典管理 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/system/dict")
public class DictController {

    private final DictService dictService;

    @Operation(summary = "查询列表")
    @SaCheckPermission("system:dict:list")
    @GetMapping("/list")
    public List<DictResp> list(@Valid DictQuery query, @Valid SortQuery sortQuery) {
        return dictService.list(query, sortQuery);
    }

    @Operation(summary = "查询详情")
    @Parameter(name = "id", description = "ID", example = "1", in = ParameterIn.PATH)
    @SaCheckPermission("system:dict:get")
    @GetMapping("/{id}")
    public DictResp get(@PathVariable("id") Long id) {
        return dictService.get(id);
    }

    @Operation(summary = "创建数据")
    @SaCheckPermission("system:dict:create")
    @PostMapping
    public IdResp<Long> create(@RequestBody @Valid DictReq req) {
        return new IdResp<>(dictService.create(req));
    }

    @Operation(summary = "修改数据")
    @Parameter(name = "id", description = "ID", example = "1", in = ParameterIn.PATH)
    @SaCheckPermission("system:dict:update")
    @PutMapping("/{id}")
    public void update(@RequestBody @Valid DictReq req, @PathVariable("id") Long id) {
        dictService.update(req, id);
    }

    @Operation(summary = "批量删除数据")
    @SaCheckPermission("system:dict:delete")
    @DeleteMapping
    public void batchDelete(@RequestBody @Valid IdsReq req) {
        dictService.delete(req.getIds());
    }

    @Operation(summary = "清除缓存", description = "清除缓存")
    @SaCheckPermission("system:dict:clearCache")
    @DeleteMapping("/cache/{code}")
    public void clearCache(@PathVariable String code) {
        RedisUtils.deleteByPattern(CacheConstants.DICT_KEY_PREFIX + code);
    }
}
