package top.wain.heimdall.system.service;

import cn.hutool.core.lang.tree.Tree;
import jakarta.servlet.http.HttpServletResponse;
import top.wain.heimdall.system.model.entity.DeptDO;
import top.wain.heimdall.system.model.query.DeptQuery;
import top.wain.heimdall.system.model.req.DeptReq;
import top.wain.heimdall.system.model.resp.DeptResp;
import top.continew.starter.data.service.IService;
import top.continew.starter.extension.crud.model.query.SortQuery;

import java.util.List;
import java.util.Set;

/**
 * 部门业务接口
 *
 * @author WainZeng
 * @since 2023/1/22 17:54
 */
public interface DeptService extends IService<DeptDO> {

    List<Tree<Long>> tree(DeptQuery query, SortQuery sortQuery, boolean isSimple);

    DeptResp get(Long id);

    Long create(DeptReq req);

    void update(DeptReq req, Long id);

    void delete(List<Long> ids);

    void export(DeptQuery query, SortQuery sortQuery, HttpServletResponse response);

    /**
     * 查询子部门列表
     *
     * @param id ID
     * @return 子部门列表
     */
    List<DeptDO> listChildren(Long id);

    /**
     * 通过名称查询部门
     *
     * @param list 名称列表
     * @return 部门列表
     */
    List<DeptDO> listByNames(List<String> list);

    /**
     * 通过名称查询部门数量
     *
     * @param deptNames 名称列表
     * @return 部门数量
     */
    int countByNames(Set<String> deptNames);
}
