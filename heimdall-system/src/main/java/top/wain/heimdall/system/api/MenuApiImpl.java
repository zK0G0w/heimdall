package top.wain.heimdall.system.api;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.tree.Tree;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import top.wain.heimdall.common.api.system.MenuApi;
import top.wain.heimdall.common.enums.DisEnableStatusEnum;
import top.wain.heimdall.system.model.query.MenuQuery;
import top.wain.heimdall.system.service.MenuService;

import java.util.List;

/**
 * 菜单业务 API 实现
 *
 * @author WainZeng
 * @since 2025/7/26 9:53
 */
@Service
@RequiredArgsConstructor
public class MenuApiImpl implements MenuApi {

    private final MenuService baseService;

    @Override
    public List<Tree<Long>> listTree(List<Long> excludeMenuIds, boolean isSimple) {
        MenuQuery query = new MenuQuery();
        query.setStatus(DisEnableStatusEnum.ENABLE);
        // 过滤掉租户不能使用的菜单（级联排除其所有层级子菜单，避免父级被忽略但子菜单仍可分配）
        if (CollUtil.isNotEmpty(excludeMenuIds)) {
            query.setExcludeMenuIdList(baseService.listChildMenuIds(excludeMenuIds));
        }
        return baseService.tree(query, null, isSimple);
    }
}
