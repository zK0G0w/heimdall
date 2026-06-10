package top.wain.heimdall.system.service;

import top.wain.heimdall.system.model.entity.SmsConfigDO;
import top.wain.heimdall.system.model.query.SmsConfigQuery;
import top.wain.heimdall.system.model.req.SmsConfigReq;
import top.wain.heimdall.system.model.resp.SmsConfigResp;
import top.continew.starter.data.service.IService;
import top.continew.starter.extension.crud.model.query.PageQuery;
import top.continew.starter.extension.crud.model.resp.BasePageResp;

import java.util.List;

/**
 * 短信配置业务接口
 *
 * @author luoqiz
 * @since 2025/03/15 18:41
 */
public interface SmsConfigService extends IService<SmsConfigDO> {

    /**
     * 分页查询列表
     *
     * @param query     查询条件
     * @param pageQuery 分页查询条件
     * @return 分页列表信息
     */
    BasePageResp<SmsConfigResp> page(SmsConfigQuery query, PageQuery pageQuery);

    /**
     * 查询详情
     *
     * @param id ID
     * @return 详情信息
     */
    SmsConfigResp get(Long id);

    /**
     * 创建数据
     *
     * @param req 创建请求参数
     * @return ID
     */
    Long create(SmsConfigReq req);

    /**
     * 修改数据
     *
     * @param req 修改请求参数
     * @param id  ID
     */
    void update(SmsConfigReq req, Long id);

    /**
     * 删除数据
     *
     * @param ids ID 列表
     */
    void delete(List<Long> ids);

    /**
     * 根据条件查询列表
     *
     * @param query 查询条件
     * @return 列表信息
     */
    List<SmsConfigResp> list(SmsConfigQuery query);

    /**
     * 设置默认配置
     *
     * @param id ID
     */
    void setDefaultConfig(Long id);

    /**
     * 获取默认短信配置
     *
     * @return 默认短信配置
     */
    SmsConfigDO getDefaultConfig();
}
