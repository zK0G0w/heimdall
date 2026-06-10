package top.wain.heimdall.config.satoken;

import cn.dev33.satoken.fun.SaParamFunction;
import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.stp.StpUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.lang.Nullable;
import top.wain.heimdall.common.context.UserContext;
import top.wain.heimdall.common.context.UserContextHolder;
import top.continew.starter.core.util.ServletUtils;
import top.continew.starter.extension.tenant.context.TenantContextHolder;
import top.continew.starter.json.jackson.util.JSONUtils;
import top.wain.heimdall.common.model.R;

/**
 * Sa-Token 扩展拦截器
 *
 * @author WainZeng
 * @since 2024/10/10 20:25
 */
@Slf4j
public class SaExtensionInterceptor extends SaInterceptor {

    public SaExtensionInterceptor(SaParamFunction<Object> auth) {
        super(auth);
    }

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {
        boolean flag = super.preHandle(request, response, handler);
        if (!flag || !StpUtil.isLogin()) {
            return flag;
        }
        // 设置上下文
        UserContext userContext = UserContextHolder.getContext();
        if (userContext == null) {
            return true;
        }
        // 检查用户租户权限
        if (TenantContextHolder.isTenantEnabled()) {
            Long userTenantId = userContext.getTenantId();
            Long tenantId = TenantContextHolder.getTenantId();
            if (!userTenantId.equals(tenantId)) {
                R r = R.fail(String.valueOf(HttpStatus.FORBIDDEN.value()), "您当前没有访问该租户的权限");
                ServletUtils.writeJSON(response, JSONUtils.toJsonStr(r));
                return false;
            }
        }
        UserContextHolder.getExtraContext();
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request,
                                HttpServletResponse response,
                                Object handler,
                                @Nullable Exception e) throws Exception {
        // 清除上下文
        try {
            super.afterCompletion(request, response, handler, e);
        } finally {
            UserContextHolder.clearContext();
        }
    }
}
