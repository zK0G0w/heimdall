package top.wain.heimdall.system.service;

import top.wain.heimdall.system.model.entity.ClientDO;
import top.wain.heimdall.system.model.query.ClientQuery;
import top.wain.heimdall.system.model.req.ClientReq;
import top.wain.heimdall.system.model.resp.ClientResp;
import top.continew.starter.data.service.IService;
import top.continew.starter.extension.crud.model.query.PageQuery;
import top.continew.starter.extension.crud.model.resp.BasePageResp;

import java.util.List;

/**
 * 客户端业务接口
 *
 * @author KAI
 * @author WainZeng
 * @since 2024/12/03 16:04
 */
public interface ClientService extends IService<ClientDO> {

    /**
     * 分页查询
     *
     * @param query     查询条件
     * @param pageQuery 分页查询条件
     * @return 分页列表信息
     */
    BasePageResp<ClientResp> page(ClientQuery query, PageQuery pageQuery);

    /**
     * 查询详情
     *
     * @param id ID
     * @return 详情信息
     */
    ClientResp get(Long id);

    /**
     * 创建数据
     *
     * @param req 创建请求参数
     * @return ID
     */
    Long create(ClientReq req);

    /**
     * 修改数据
     *
     * @param req 修改请求参数
     * @param id  ID
     */
    void update(ClientReq req, Long id);

    /**
     * 删除数据
     *
     * @param ids ID 列表
     */
    void delete(List<Long> ids);

    /**
     * 根据客户端 ID 查询
     *
     * @param clientId 客户端 ID
     * @return 客户端信息
     */
    ClientResp getByClientId(String clientId);
}
