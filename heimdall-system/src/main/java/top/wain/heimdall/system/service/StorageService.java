package top.wain.heimdall.system.service;

import top.wain.heimdall.common.model.req.CommonStatusUpdateReq;
import top.wain.heimdall.system.model.entity.StorageDO;
import top.wain.heimdall.system.model.query.StorageQuery;
import top.wain.heimdall.system.model.req.StorageReq;
import top.wain.heimdall.system.model.resp.StorageResp;
import top.continew.starter.data.service.IService;
import top.continew.starter.extension.crud.model.query.SortQuery;

import java.util.List;

/**
 * 存储业务接口
 *
 * @author WainZeng
 * @since 2023/12/26 22:09
 */
public interface StorageService extends IService<StorageDO> {

    /**
     * 查询列表
     *
     * @param query     查询条件
     * @param sortQuery 排序查询条件
     * @return 列表信息
     */
    List<StorageResp> list(StorageQuery query, SortQuery sortQuery);

    /**
     * 查询详情
     *
     * @param id ID
     * @return 详情信息
     */
    StorageResp get(Long id);

    /**
     * 创建数据
     *
     * @param req 创建请求参数
     * @return ID
     */
    Long create(StorageReq req);

    /**
     * 修改数据
     *
     * @param req 修改请求参数
     * @param id  ID
     */
    void update(StorageReq req, Long id);

    /**
     * 删除数据
     *
     * @param ids ID 列表
     */
    void delete(List<Long> ids);

    /**
     * 修改状态
     *
     * @param req 状态信息
     * @param id  ID
     */
    void updateStatus(CommonStatusUpdateReq req, Long id);

    /**
     * 设置默认存储
     *
     * @param id ID
     */
    void setDefaultStorage(Long id);

    /**
     * 查询默认存储
     *
     * @return 存储配置
     */
    StorageDO getDefaultStorage();

    /**
     * 根据编码查询（如果编码为空，则返回默认存储）
     *
     * @param code 编码
     * @return 存储配置
     */
    StorageDO getByCode(String code);

    /**
     * 加载存储引擎
     *
     * @param storage 存储配置
     */
    void load(StorageDO storage);

    /**
     * 卸载存储引擎
     *
     * @param storage 存储配置
     */
    void unload(StorageDO storage);
}
