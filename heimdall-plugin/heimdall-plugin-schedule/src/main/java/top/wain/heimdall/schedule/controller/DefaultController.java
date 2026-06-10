package top.wain.heimdall.schedule.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.wain.heimdall.schedule.annotation.ConditionalOnDisabledScheduleJob;
import top.wain.heimdall.common.model.R;

/**
 * 任务调度默认控制器
 *
 * @author WainZeng
 * @since 2025/10/25 12:28
 */
@RestController
@ConditionalOnDisabledScheduleJob
@RequestMapping({"/schedule/job", "/schedule/log"})
public class DefaultController {

    @RequestMapping("/**")
    public R error() {
        return R.fail(String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR
            .value()), "任务模块已禁用，请于对应环境配置文件中配置 snail-job.enabled 为 true 进行启用");
    }
}
