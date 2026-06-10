package top.wain.heimdall.tenant.config;

import cn.hutool.core.util.StrUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import top.wain.heimdall.common.config.TenantExtensionProperties;
import top.wain.heimdall.tenant.service.TenantService;
import top.continew.starter.core.util.ServletUtils;
import top.continew.starter.core.util.validation.CheckUtils;
import top.continew.starter.extension.tenant.config.TenantProvider;
import top.continew.starter.extension.tenant.context.TenantContext;

/**
 * 默认租户提供者
 *
 * @author 小熊
 * @author WainZeng
 * @since 2024/12/12 15:35
 */
@RequiredArgsConstructor
public class DefaultTenantProvider implements TenantProvider {

    private final TenantExtensionProperties tenantExtensionProperties;
    private final TenantService tenantService;

    @Override
    public TenantContext getByTenantId(String tenantIdAsString, boolean verify) {
        TenantContext context = new TenantContext();
        Long defaultTenantId = tenantExtensionProperties.getDefaultTenantId();
        context.setTenantId(defaultTenantId);
        // 默认租户
        if (defaultTenantId.toString().equals(tenantIdAsString)) {
            return context;
        }
        Long tenantId;
        // 未指定租户
        if (StrUtil.isBlank(tenantIdAsString)) {
            // 检查是否指定了租户编码（登录相关接口）
            HttpServletRequest request = ServletUtils.getRequest();
            String tenantCode = request.getHeader(tenantExtensionProperties.getTenantCodeHeader());
            if (StrUtil.isBlank(tenantCode)) {
                return context;
            }
            Long id = tenantService.getIdByCode(tenantCode);
            CheckUtils.throwIfNull(id, "编码为 [%s] 的租户不存在".formatted(tenantCode));
            tenantId = id;
        } else {
            // 指定租户
            tenantId = Long.parseLong(tenantIdAsString);
        }
        // 检查租户状态
        if (verify) {
            tenantService.checkStatus(tenantId);
        }
        context.setTenantId(tenantId);
        return context;
    }
}
