package top.wain.heimdall.system.service;

import top.wain.heimdall.system.model.entity.DictItemDO;
import top.wain.heimdall.system.model.query.DictItemQuery;
import top.wain.heimdall.system.model.req.DictItemReq;
import top.wain.heimdall.system.model.resp.DictItemResp;
import top.continew.starter.data.service.IService;
import top.continew.starter.extension.crud.model.query.PageQuery;
import top.continew.starter.extension.crud.model.resp.BasePageResp;
import top.continew.starter.extension.crud.model.resp.LabelValueResp;

import java.util.List;

/**
 * 字典项业务接口
 *
 * @author WainZeng
 * @since 2023/9/11 21:29
 */
public interface DictItemService extends IService<DictItemDO> {

    BasePageResp<DictItemResp> page(DictItemQuery query, PageQuery pageQuery);

    DictItemResp get(Long id);

    Long create(DictItemReq req);

    void update(DictItemReq req, Long id);

    void delete(List<Long> ids);

    List<LabelValueResp> listByDictCode(String dictCode);

    void deleteByDictIds(List<Long> dictIds);

    List<String> listEnumDictNames();
}
