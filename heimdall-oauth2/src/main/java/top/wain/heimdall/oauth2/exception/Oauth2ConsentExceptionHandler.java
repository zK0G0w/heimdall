package top.wain.heimdall.oauth2.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import top.wain.heimdall.common.model.R;
import top.wain.heimdall.oauth2.controller.Oauth2ConsentController;

/**
 * @Description: OAuth2 Consent 端点异常处理器，返回项目统一 R 响应格式
 * @Author: WainZeng
 * @Date: 2026/06/13
 */
@Slf4j
@Order(-1)
@RestControllerAdvice(assignableTypes = Oauth2ConsentController.class)
public class Oauth2ConsentExceptionHandler {

    @ExceptionHandler(Oauth2Exception.class)
    public R<Void> handleOauth2Exception(Oauth2Exception e) {
        log.warn("OAuth2 授权确认错误: {} - {}", e.getError(), e.getErrorDescription());
        return R.fail("1", e.getErrorDescription());
    }
}
