package top.wain.heimdall.schedule.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import top.wain.heimdall.schedule.api.JobClient;
import top.wain.heimdall.schedule.constant.JobConstants;

/**
 * Feign 请求拦截器
 *
 * @author WainZeng
 * @since 2025/3/28 21:17
 */
@Component
@RequiredArgsConstructor
public class FeignRequestInterceptor implements RequestInterceptor {

    private final JobClient jobClient;

    @Value("${snail-job.namespace}")
    private String namespace;

    @Override
    public void apply(RequestTemplate requestTemplate) {
        requestTemplate.header(JobConstants.NAMESPACE_ID_HEADER, namespace);
        requestTemplate.header(JobConstants.AUTH_TOKEN_HEADER, jobClient.getToken());
    }
}