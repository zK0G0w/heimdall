package top.wain.heimdall.oauth2.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @Description: OAuth2 协议层异常处理器
 * @Author: WainZeng
 * @Date: 2026/06/11
 */
@Slf4j
@Order(-1)
@RestControllerAdvice(basePackages = "top.wain.heimdall.oauth2.controller")
public class Oauth2ExceptionHandler {

    @ExceptionHandler(Oauth2Exception.class)
    public ResponseEntity<Map<String, String>> handleOauth2Exception(Oauth2Exception e) {
        log.warn("OAuth2 协议错误: {} - {}", e.getError(), e.getErrorDescription());
        Map<String, String> body = new LinkedHashMap<>();
        body.put("error", e.getError());
        body.put("error_description", e.getErrorDescription());
        return ResponseEntity.status(e.getHttpStatus()).body(body);
    }
}
