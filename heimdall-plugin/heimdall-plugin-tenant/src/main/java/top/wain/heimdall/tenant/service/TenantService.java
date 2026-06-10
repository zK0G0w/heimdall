package top.wain.heimdall.tenant.service;

import top.wain.heimdall.tenant.model.entity.TenantDO;
import top.wain.heimdall.tenant.model.query.TenantQuery;
import top.wain.heimdall.tenant.model.req.TenantReq;
import top.wain.heimdall.tenant.model.resp.TenantDetailResp;
import top.wain.heimdall.tenant.model.resp.TenantResp;
import top.continew.starter.data.service.IService;
import top.continew.starter.extension.crud.model.query.PageQuery;
import top.continew.starter.extension.crud.model.resp.BasePageResp;

import java.util.List;

/**
 * 租户业务接口
 *
 * @author 小熊
 * @author WainZeng
 * @since 2024/11/26 17:20
 */
public interface TenantService extends IService<TenantDO> {

    /**
     * 分页查询列表
     *
     * @param query     查询条件
     * @param pageQuery 分页条件
     * @return 分页列表
     */
    BasePageResp<TenantResp> page(TenantQuery query, PageQuery pageQuery);

    /**
     * 查询详情
     *
     * @param id ID
     * @return 详情
     */
    TenantDetailResp get(Long id);

    /**
     * 创建数据
     *
     * @param req 请求参数
     * @return ID
     */
    Long create(TenantReq req);

    /**
     * 修改数据
     *
     * @param req 请求参数
     * @param id  ID
     */
    void update(TenantReq req, Long id);

    /**
     * 删除数据
     *
     * @param id ID
     */
    void delete(Long id);

    /**
     * 根据域名查询
     *
     * @param domain 域名
     * @return ID
     */
    Long getIdByDomain(String domain);

    /**
     * 根据编码查询
     *
     * @param code 编码
     * @return ID
     */
    Long getIdByCode(String code);

    /**
     * 检查租户状态
     *
     * @param id ID
     */
    void checkStatus(Long id);

    /**
     * 更新租户菜单
     *
     * @param newMenuIds 新菜单 ID 列表
     * @param packageId  套餐 ID
     */
    void updateTenantMenu(List<Long> newMenuIds, Long packageId);

    /**
     * 根据套餐 ID 查询数量
     *
     * @param packageIds 套餐 ID 列表
     * @return 租户数量
     */
    Long countByPackageIds(List<Long> packageIds);
}
