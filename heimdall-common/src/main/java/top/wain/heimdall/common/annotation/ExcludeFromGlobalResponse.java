package top.wain.heimdall.common.annotation;

import java.lang.annotation.*;

/**
 * @Description: 排除全局响应包装，适用于文件导出等流式响应场景
 * @Author: WainZeng
 * @Date: 2026/04/16
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ExcludeFromGlobalResponse {
}
