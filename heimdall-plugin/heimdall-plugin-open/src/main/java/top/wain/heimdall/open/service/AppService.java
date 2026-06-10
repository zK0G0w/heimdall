package top.wain.heimdall.open.service;

import jakarta.servlet.http.HttpServletResponse;
import top.wain.heimdall.open.model.entity.AppDO;
import top.wain.heimdall.open.model.query.AppQuery;
import top.wain.heimdall.open.model.req.AppReq;
import top.wain.heimdall.open.model.resp.AppDetailResp;
import top.wain.heimdall.open.model.resp.AppResp;
import top.wain.heimdall.open.model.resp.AppSecretResp;
import top.continew.starter.data.service.IService;
import top.continew.starter.extension.crud.model.query.PageQuery;
import top.continew.starter.extension.crud.model.query.SortQuery;
import top.continew.starter.extension.crud.model.resp.BasePageResp;

import java.util.List;

/**
 * 应用业务接口
 *
 * @author chengzi
 * @author WainZeng
 * @since 2024/10/17 16:03
 */
public interface AppService extends IService<AppDO> {

    /**
     * 分页查询列表
     *
     * @param query     查询条件
     * @param pageQuery 分页查询条件
     * @return 分页列表信息
     */
    BasePageResp<AppResp> page(AppQuery query, PageQuery pageQuery);

    /**
     * 查询详情
     *
     * @param id ID
     * @return 详情信息
     */
    AppDetailResp get(Long id);

    /**
     * 创建数据
     *
     * @param req 创建请求参数
     * @return ID
     */
    Long create(AppReq req);

    /**
     * 修改数据
     *
     * @param req 修改请求参数
     * @param id  ID
     */
    void update(AppReq req, Long id);

    /**
     * 删除数据
     *
     * @param ids ID 列表
     */
    void delete(List<Long> ids);

    /**
     * 导出数据
     *
     * @param query     查询条件
     * @param sortQuery 排序查询条件
     * @param response  响应对象
     */
    void export(AppQuery query, SortQuery sortQuery, HttpServletResponse response);

    /**
     * 获取密钥
     *
     * @param id ID
     * @return 密钥信息
     */
    AppSecretResp getSecret(Long id);

    /**
     * 重置密钥
     *
     * @param id ID
     */
    void resetSecret(Long id);

    /**
     * 根据 Access Key 查询
     *
     * @param accessKey Access Key
     * @return 应用信息
     */
    AppDO getByAccessKey(String accessKey);
}
