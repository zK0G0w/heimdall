package top.wain.heimdall.system.service;

import jakarta.servlet.http.HttpServletResponse;
import top.wain.heimdall.system.model.entity.SmsLogDO;
import top.wain.heimdall.system.model.query.SmsLogQuery;
import top.wain.heimdall.system.model.req.SmsLogReq;
import top.wain.heimdall.system.model.resp.SmsLogResp;
import top.continew.starter.data.service.IService;
import top.continew.starter.extension.crud.model.query.PageQuery;
import top.continew.starter.extension.crud.model.query.SortQuery;
import top.continew.starter.extension.crud.model.resp.BasePageResp;

import java.util.List;

/**
 * 短信日志业务接口
 *
 * @author luoqiz
 * @since 2025/03/15 22:15
 */
public interface SmsLogService extends IService<SmsLogDO> {

    /**
     * 分页查询
     *
     * @param query     查询条件
     * @param pageQuery 分页查询条件
     * @return 分页列表信息
     */
    BasePageResp<SmsLogResp> page(SmsLogQuery query, PageQuery pageQuery);

    /**
     * 查询详情
     *
     * @param id ID
     * @return 详情信息
     */
    SmsLogResp get(Long id);

    /**
     * 创建数据
     *
     * @param req 创建请求参数
     * @return ID
     */
    Long create(SmsLogReq req);

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
    void export(SmsLogQuery query, SortQuery sortQuery, HttpServletResponse response);
}
