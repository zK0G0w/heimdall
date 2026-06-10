package top.wain.heimdall.schedule.api;

import com.aizuda.snailjob.common.core.model.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import top.wain.heimdall.schedule.config.FeignRequestInterceptor;

import java.util.List;

/**
 * 任务组 REST API
 *
 * @author WainZeng
 * @since 2025/3/28 22:25
 */
@FeignClient(value = "job-group", url = "${snail-job.server.api.url}", path = "/group", configuration = FeignRequestInterceptor.class)
public interface JobGroupApi {

    /**
     * 查询分组列表
     *
     * @return 响应信息
     */
    @GetMapping("/all/group-name/list")
    Result<List<String>> listGroup();
}
