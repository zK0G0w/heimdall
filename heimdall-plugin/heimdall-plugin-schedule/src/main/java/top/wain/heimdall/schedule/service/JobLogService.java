package top.wain.heimdall.schedule.service;

import top.wain.heimdall.schedule.model.query.JobLogQuery;
import top.wain.heimdall.schedule.model.resp.JobLogResp;
import top.continew.starter.extension.crud.model.resp.PageResp;

/**
 * 任务日志业务接口
 *
 * @author KAI
 * @author WainZeng
 * @since 2024/6/27 22:52
 */
public interface JobLogService {

    /**
     * 分页查询列表
     *
     * @param query 查询条件
     * @return 分页列表信息
     */
    PageResp<JobLogResp> page(JobLogQuery query);

    /**
     * 停止
     *
     * @param id ID
     * @return 停止结果
     */
    boolean stop(Long id);

    /**
     * 重试
     *
     * @param id ID
     * @return 重试结果
     */
    boolean retry(Long id);
}
