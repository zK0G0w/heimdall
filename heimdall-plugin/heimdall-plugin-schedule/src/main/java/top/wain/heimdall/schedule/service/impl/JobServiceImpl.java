package top.wain.heimdall.schedule.service.impl;

import com.aizuda.snailjob.client.job.core.openapi.SnailJobOpenApi;
import com.aizuda.snailjob.common.core.enums.StatusEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import top.wain.heimdall.schedule.api.JobApi;
import top.wain.heimdall.schedule.api.JobClient;
import top.wain.heimdall.schedule.api.JobGroupApi;
import top.wain.heimdall.schedule.enums.JobStatusEnum;
import top.wain.heimdall.schedule.model.query.JobQuery;
import top.wain.heimdall.schedule.model.req.JobReq;
import top.wain.heimdall.schedule.model.req.JobStatusReq;
import top.wain.heimdall.schedule.model.req.JobTriggerReq;
import top.wain.heimdall.schedule.model.resp.JobResp;
import top.wain.heimdall.schedule.service.JobService;
import top.continew.starter.extension.crud.model.resp.PageResp;

import java.util.List;
import java.util.Set;

/**
 * 任务业务实现
 *
 * @author KAI
 * @author WainZeng
 * @since 2024/6/25 17:25
 */
@Service
@RequiredArgsConstructor
public class JobServiceImpl implements JobService {

    private final JobClient jobClient;
    private final JobApi jobApi;
    private final JobGroupApi jobGroupApi;

    @Override
    public PageResp<JobResp> page(JobQuery query) {
        return jobClient.requestPage(() -> jobApi.page(query));
    }

    @Override
    public boolean create(JobReq req) {
        return Boolean.TRUE.equals(jobClient.request(() -> jobApi.create(req)));
    }

    @Override
    public boolean update(JobReq req, Long id) {
        req.setId(id);
        return Boolean.TRUE.equals(jobClient.request(() -> jobApi.update(req)));
    }

    @Override
    public boolean updateStatus(JobStatusReq req, Long id) {
        return SnailJobOpenApi.updateJobStatus(id)
            .setStatus(JobStatusEnum.DISABLED.equals(req.getJobStatus()) ? StatusEnum.NO : StatusEnum.YES)
            .execute();
    }

    @Override
    public boolean delete(Long id) {
        return SnailJobOpenApi.deleteJob(Set.of(id)).execute();
    }

    @Override
    public boolean trigger(JobTriggerReq req) {
        return Boolean.TRUE.equals(jobClient.request(() -> jobApi.trigger(req)));
    }

    @Override
    public List<String> listGroup() {
        return jobClient.request(jobGroupApi::listGroup);
    }
}