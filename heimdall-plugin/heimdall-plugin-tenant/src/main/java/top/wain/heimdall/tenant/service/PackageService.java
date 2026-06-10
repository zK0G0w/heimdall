package top.wain.heimdall.tenant.service;

import top.wain.heimdall.tenant.model.entity.PackageDO;
import top.wain.heimdall.tenant.model.query.PackageQuery;
import top.wain.heimdall.tenant.model.req.PackageReq;
import top.wain.heimdall.tenant.model.resp.PackageDetailResp;
import top.wain.heimdall.tenant.model.resp.PackageResp;
import top.continew.starter.data.service.IService;
import top.continew.starter.extension.crud.model.query.PageQuery;
import top.continew.starter.extension.crud.model.query.SortQuery;
import top.continew.starter.extension.crud.model.resp.BasePageResp;
import top.continew.starter.extension.crud.model.resp.LabelValueResp;

import java.util.List;

/**
 * 套餐业务接口
 *
 * @author 小熊
 * @since 2024/11/26 11:25
 */
public interface PackageService extends IService<PackageDO> {

    /**
     * 分页查询列表
     *
     * @param query     查询条件
     * @param pageQuery 分页条件
     * @return 分页结果
     */
    BasePageResp<PackageResp> page(PackageQuery query, PageQuery pageQuery);

    /**
     * 查询列表
     *
     * @param query     查询条件
     * @param sortQuery 排序条件
     * @return 列表结果
     */
    List<PackageResp> list(PackageQuery query, SortQuery sortQuery);

    /**
     * 查询详情
     *
     * @param id ID
     * @return 详情
     */
    PackageDetailResp get(Long id);

    /**
     * 创建数据
     *
     * @param req 请求参数
     * @return ID
     */
    Long create(PackageReq req);

    /**
     * 修改数据
     *
     * @param req 请求参数
     * @param id  ID
     */
    void update(PackageReq req, Long id);

    /**
     * 删除数据
     *
     * @param id ID
     */
    void delete(Long id);

    /**
     * 查询字典列表
     *
     * @param query     查询条件
     * @param sortQuery 排序条件
     * @return 字典列表
     */
    List<LabelValueResp> dict(PackageQuery query, SortQuery sortQuery);

    /**
     * 检查套餐状态
     *
     * @param id ID
     */
    void checkStatus(Long id);
}
