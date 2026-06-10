package top.wain.heimdall.tenant.controller;

import cn.dev33.satoken.annotation.SaIgnore;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import top.wain.heimdall.tenant.service.TenantService;
import top.continew.starter.extension.tenant.annotation.TenantIgnore;
import top.continew.starter.log.annotation.Log;

/**
 * 公共 API
 *
 * @author WainZeng
 * @since 2025/7/15 20:32
 */
@Tag(name = "公共 API")
@Log(ignore = true)
@Validated
@RequiredArgsConstructor
@RestController("tenantCommonController")
@RequestMapping("/tenant/common")
public class CommonController {

    private final TenantService tenantService;

    @SaIgnore
    @TenantIgnore
    @Operation(summary = "根据域名查询租户 ID", description = "根据域名查询租户 ID")
    @GetMapping("/id")
    public Long getTenantIdByDomain(@RequestParam String domain) {
        return tenantService.getIdByDomain(domain);
    }
}
