package top.wain.heimdall.schedule.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import top.wain.heimdall.schedule.api.JobBatchApi;
import top.wain.heimdall.schedule.api.JobClient;
import top.wain.heimdall.schedule.model.query.JobLogQuery;
import top.wain.heimdall.schedule.model.resp.JobLogResp;
import top.wain.heimdall.schedule.service.JobLogService;
import top.continew.starter.extension.crud.model.resp.PageResp;

/**
 * 任务日志业务实现
 *
 * @author KAI
 * @author WainZeng
 * @since 2024/6/27 22:54
 */
@Service
@RequiredArgsConstructor
public class JobLogServiceImpl implements JobLogService {

    private final JobClient jobClient;
    private final JobBatchApi jobBatchApi;

    @Override
    public PageResp<JobLogResp> page(JobLogQuery query) {
        return jobClient.requestPage(() -> jobBatchApi.page(query));
    }

    @Override
    public boolean stop(Long id) {
        return Boolean.TRUE.equals(jobClient.request(() -> jobBatchApi.stop(id)));
    }

    @Override
    public boolean retry(Long id) {
        return Boolean.TRUE.equals(jobClient.request(() -> jobBatchApi.retry(id)));
    }
}
