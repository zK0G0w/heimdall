package top.wain.heimdall.common.api.system;

import cn.hutool.core.lang.tree.Tree;

import java.util.List;

/**
 * 菜单业务 API
 *
 * @author WainZeng
 * @since 2025/7/26 9:53
 */
public interface MenuApi {

    /**
     * 查询树结构列表
     *
     * @param excludeMenuIds 排除的菜单 ID 列表
     * @param isSimple       是否是简单树结构
     * @return 树结构列表
     */
    List<Tree<Long>> listTree(List<Long> excludeMenuIds, boolean isSimple);
}
