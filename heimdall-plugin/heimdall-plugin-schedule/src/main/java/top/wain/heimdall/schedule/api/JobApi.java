package top.wain.heimdall.schedule.api;

import com.aizuda.snailjob.common.core.model.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import top.wain.heimdall.schedule.config.FeignRequestInterceptor;
import top.wain.heimdall.schedule.model.JobPageResult;
import top.wain.heimdall.schedule.model.query.JobQuery;
import top.wain.heimdall.schedule.model.req.JobReq;
import top.wain.heimdall.schedule.model.req.JobTriggerReq;
import top.wain.heimdall.schedule.model.resp.JobResp;

import java.util.List;

/**
 * 任务 REST API
 *
 * @author KAI
 * @author WainZeng
 * @since 2024/6/25 18:20
 */
@FeignClient(value = "job", url = "${snail-job.server.api.url}", path = "/job", configuration = FeignRequestInterceptor.class)
public interface JobApi {

    /**
     * 分页查询列表
     *
     * @param query 查询条件
     * @return 响应信息
     */
    @GetMapping("/page/list")
    JobPageResult<List<JobResp>> page(@SpringQueryMap JobQuery query);

    /**
     * 新增
     *
     * @param req 请求参数
     * @return 响应信息
     */
    @PostMapping
    Result<Boolean> create(@RequestBody JobReq req);

    /**
     * 修改
     *
     * @param req 请求参数
     * @return 响应信息
     */
    @PutMapping
    Result<Boolean> update(@RequestBody JobReq req);

    /**
     * 执行
     *
     * @param req 请求参数
     * @return 响应信息
     */
    @PostMapping("/trigger")
    Result<Boolean> trigger(@RequestBody JobTriggerReq req);
}
