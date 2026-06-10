package top.wain.heimdall.system.service;

import top.wain.heimdall.system.model.entity.DictDO;
import top.wain.heimdall.system.model.query.DictQuery;
import top.wain.heimdall.system.model.req.DictReq;
import top.wain.heimdall.system.model.resp.DictResp;
import top.continew.starter.data.service.IService;
import top.continew.starter.extension.crud.model.query.SortQuery;
import top.continew.starter.extension.crud.model.resp.LabelValueResp;

import java.util.List;

/**
 * 字典业务接口
 *
 * @author WainZeng
 * @since 2023/9/11 21:29
 */
public interface DictService extends IService<DictDO> {

    /**
     * 查询列表
     *
     * @param query     查询条件
     * @param sortQuery 排序查询条件
     * @return 列表信息
     */
    List<DictResp> list(DictQuery query, SortQuery sortQuery);

    /**
     * 查询详情
     *
     * @param id ID
     * @return 详情信息
     */
    DictResp get(Long id);

    /**
     * 创建数据
     *
     * @param req 创建请求参数
     * @return ID
     */
    Long create(DictReq req);

    /**
     * 修改数据
     *
     * @param req 修改请求参数
     * @param id  ID
     */
    void update(DictReq req, Long id);

    /**
     * 删除数据
     *
     * @param ids ID 列表
     */
    void delete(List<Long> ids);

    /**
     * 查询字典选项列表
     *
     * @param query     查询条件
     * @param sortQuery 排序查询条件
     * @return 字典选项列表
     */
    List<LabelValueResp> dict(DictQuery query, SortQuery sortQuery);

    /**
     * 查询枚举字典
     *
     * @return 枚举字典列表
     */
    List<LabelValueResp> listEnumDict();
}
