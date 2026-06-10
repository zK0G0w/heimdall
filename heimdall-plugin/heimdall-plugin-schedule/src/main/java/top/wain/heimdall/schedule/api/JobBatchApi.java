package top.wain.heimdall.schedule.api;

import com.aizuda.snailjob.common.core.model.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import top.wain.heimdall.schedule.config.FeignRequestInterceptor;
import top.wain.heimdall.schedule.model.JobPageResult;
import top.wain.heimdall.schedule.model.query.JobLogQuery;
import top.wain.heimdall.schedule.model.resp.JobLogResp;

import java.util.List;

/**
 * 任务批次 REST API
 *
 * @author KAI
 * @author WainZeng
 * @since 2024/6/27 23:03
 */
@FeignClient(value = "job-batch", url = "${snail-job.server.api.url}", path = "/job", configuration = FeignRequestInterceptor.class)
public interface JobBatchApi {

    /**
     * 分页查询列表
     *
     * @param query 查询条件
     * @return 响应信息
     */
    @GetMapping("/batch/list")
    JobPageResult<List<JobLogResp>> page(@SpringQueryMap JobLogQuery query);

    /**
     * 停止
     *
     * @param id ID
     * @return 响应信息
     */
    @PostMapping("/batch/stop/{id}")
    Result<Boolean> stop(@PathVariable("id") Long id);

    /**
     * 重试
     *
     * @param id ID
     * @return 响应信息
     */
    @PostMapping("/batch/retry/{id}")
    Result<Boolean> retry(@PathVariable("id") Long id);
}
