package top.wain.heimdall.common.config.response;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.core.MethodParameter;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;
import top.wain.heimdall.common.annotation.ExcludeFromGlobalResponse;
import top.wain.heimdall.common.model.R;

import java.util.List;

/**
 * @Description: 全局响应体自动包装，将 Controller 返回值统一包装为 R 格式
 * @Author: WainZeng
 * @Date: 2026/04/16
 */
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalResponseBodyAdvice implements ResponseBodyAdvice<Object> {

    private static final List<String> EXCLUDE_PACKAGES = List
        .of("io.swagger.**", "org.springdoc.**", "org.springframework.boot.actuate.*");

    private final ObjectMapper objectMapper;

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        // 方法或类上标注了 @ExcludeFromGlobalResponse，跳过包装
        if (returnType.hasMethodAnnotation(ExcludeFromGlobalResponse.class) || returnType.getDeclaringClass()
            .isAnnotationPresent(ExcludeFromGlobalResponse.class)) {
            return false;
        }
        // 返回类型已经是 R，跳过包装
        Class<?> parameterType = returnType.getParameterType();
        if (R.class.isAssignableFrom(parameterType)) {
            return false;
        }
        // 流式响应（Resource / byte[]），跳过包装
        if (Resource.class.isAssignableFrom(parameterType) || byte[].class.isAssignableFrom(parameterType)) {
            return false;
        }
        // 例外包路径匹配，跳过包装
        String declaringClassName = returnType.getDeclaringClass().getName();
        for (String pattern : EXCLUDE_PACKAGES) {
            if (matchPackagePattern(declaringClassName, pattern)) {
                return false;
            }
        }
        return true;
    }

    @SneakyThrows
    @Override
    public Object beforeBodyWrite(Object body,
                                  MethodParameter returnType,
                                  MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest request,
                                  ServerHttpResponse response) {
        if (body instanceof R) {
            return body;
        }
        // void 方法返回 null
        if (body == null) {
            if (String.class.equals(returnType.getParameterType())) {
                response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
                return objectMapper.writeValueAsString(R.ok());
            }
            return R.ok();
        }
        // String 类型需要手动序列化，否则被 StringHttpMessageConverter 处理会导致类型转换异常
        if (body instanceof String) {
            response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
            return objectMapper.writeValueAsString(R.ok(body));
        }
        return R.ok(body);
    }

    /**
     * 匹配包路径模式（支持 * 和 ** 通配符）
     */
    private boolean matchPackagePattern(String className, String pattern) {
        String regex = pattern.replace(".", "\\.").replace("\\.**", "\\..+").replace("\\.*", "\\.[^.]+");
        return className.matches(regex);
    }
}
