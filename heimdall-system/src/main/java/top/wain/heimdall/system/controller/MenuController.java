package top.wain.heimdall.system.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.hutool.core.lang.tree.Tree;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import top.wain.heimdall.common.constant.CacheConstants;
import top.wain.heimdall.system.model.query.MenuQuery;
import top.wain.heimdall.system.model.req.MenuReq;
import top.wain.heimdall.system.model.resp.MenuResp;
import top.wain.heimdall.system.service.MenuService;
import top.continew.starter.cache.redisson.util.RedisUtils;
import top.continew.starter.core.constant.StringConstants;
import top.continew.starter.core.util.URLUtils;
import top.continew.starter.core.util.validation.ValidationUtils;
import top.continew.starter.extension.crud.model.query.SortQuery;
import top.continew.starter.extension.crud.model.req.IdsReq;
import top.continew.starter.extension.crud.model.resp.IdResp;
import top.continew.starter.log.annotation.Log;

import java.util.List;

/**
 * 菜单管理 API
 *
 * @author WainZeng
 * @since 2023/2/15 20:35
 */
@Log(module = "菜单管理")
@Tag(name = "菜单管理 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/system/menu")
public class MenuController {

    private final MenuService menuService;

    @Operation(summary = "查询树列表")
    @SaCheckPermission("system:menu:list")
    @GetMapping("/tree")
    public List<Tree<Long>> tree(@Valid MenuQuery query, @Valid SortQuery sortQuery) {
        return menuService.tree(query, sortQuery, false);
    }

    @Operation(summary = "查询树型字典列表")
    @GetMapping("/dict/tree")
    public List<Tree<Long>> treeDict(@Valid MenuQuery query, @Valid SortQuery sortQuery) {
        return menuService.tree(query, sortQuery, true);
    }

    @Operation(summary = "查询详情")
    @Parameter(name = "id", description = "ID", example = "1", in = ParameterIn.PATH)
    @SaCheckPermission("system:menu:get")
    @GetMapping("/{id}")
    public MenuResp get(@PathVariable("id") Long id) {
        return menuService.get(id);
    }

    @Operation(summary = "创建数据")
    @SaCheckPermission("system:menu:create")
    @PostMapping
    public IdResp<Long> create(@RequestBody @Valid MenuReq req) {
        this.validateMenuReq(req);
        return new IdResp<>(menuService.create(req));
    }

    @Operation(summary = "修改数据")
    @Parameter(name = "id", description = "ID", example = "1", in = ParameterIn.PATH)
    @SaCheckPermission("system:menu:update")
    @PutMapping("/{id}")
    public void update(@RequestBody @Valid MenuReq req, @PathVariable("id") Long id) {
        this.validateMenuReq(req);
        menuService.update(req, id);
    }

    @Operation(summary = "批量删除数据")
    @SaCheckPermission("system:menu:delete")
    @DeleteMapping
    public void batchDelete(@RequestBody @Valid IdsReq req) {
        menuService.delete(req.getIds());
    }

    @Operation(summary = "清除缓存", description = "清除缓存")
    @SaCheckPermission("system:menu:clearCache")
    @DeleteMapping("/cache")
    public void clearCache() {
        RedisUtils.deleteByPattern(CacheConstants.ROLE_MENU_KEY_PREFIX + StringConstants.ASTERISK);
    }

    /**
     * 校验并修正菜单请求参数
     */
    private void validateMenuReq(MenuReq req) {
        Boolean isExternal = ObjectUtil.defaultIfNull(req.getIsExternal(), false);
        String path = req.getPath();
        ValidationUtils.throwIf(Boolean.TRUE.equals(isExternal) && !URLUtils
            .isHttpUrl(path), "路由地址格式不正确，请以 http:// 或 https:// 开头");
        // 非外链菜单参数修正
        if (Boolean.FALSE.equals(isExternal)) {
            ValidationUtils.throwIf(URLUtils.isHttpUrl(path), "路由地址格式不正确");
            req.setPath(StrUtil.isBlank(path) ? path : StrUtil.prependIfMissing(path, StringConstants.SLASH));
            req.setName(StrUtil.removePrefix(req.getName(), StringConstants.SLASH));
            req.setComponent(StrUtil.removePrefix(req.getComponent(), StringConstants.SLASH));
        }
    }
}
