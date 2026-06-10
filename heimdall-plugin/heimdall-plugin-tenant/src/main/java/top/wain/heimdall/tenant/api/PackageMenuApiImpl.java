package top.wain.heimdall.tenant.api;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import top.wain.heimdall.common.api.tenant.PackageMenuApi;
import top.wain.heimdall.tenant.service.PackageMenuService;

import java.util.List;

/**
 * 套餐和菜单关联业务 API 实现
 *
 * @author WainZeng
 * @since 2025/7/23 21:13
 */
@Service
@RequiredArgsConstructor
public class PackageMenuApiImpl implements PackageMenuApi {

    private final PackageMenuService baseService;

    @Override
    public List<Long> listMenuIdsByPackageId(Long packageId) {
        return baseService.listMenuIdsByPackageId(packageId);
    }
}
