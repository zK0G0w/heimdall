package top.wain.heimdall.open.handler;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.handler.SaAnnotationHandlerInterface;
import org.springframework.stereotype.Component;
import top.wain.heimdall.open.util.OpenApiUtils;

import java.lang.reflect.AnnotatedElement;

import static cn.dev33.satoken.annotation.handler.SaCheckPermissionHandler._checkMethod;

/**
 * 重定义注解 SaCheckPermission 的处理器
 *
 * @author chengzi
 * @since 2024/10/25 12:03
 */
@Component
public class SaCheckPermissionHandler implements SaAnnotationHandlerInterface<SaCheckPermission> {

    @Override
    public Class<SaCheckPermission> getHandlerAnnotationClass() {
        return SaCheckPermission.class;
    }

    @Override
    public void checkMethod(SaCheckPermission saCheckPermission, AnnotatedElement annotatedElement) {
        if (!OpenApiUtils.isSignParamExists()) {
            _checkMethod(saCheckPermission.type(), saCheckPermission.value(), saCheckPermission
                .mode(), saCheckPermission.orRole());
        }
    }
}
