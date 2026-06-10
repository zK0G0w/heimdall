package top.wain.heimdall.system.api;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import top.wain.heimdall.common.api.system.RoleApi;
import top.wain.heimdall.system.service.RoleService;

/**
 * 角色业务 API 实现
 * 
 * @author WainZeng
 * @since 2025/7/26 9:39
 */
@Service
@RequiredArgsConstructor
public class RoleApiImpl implements RoleApi {

    private final RoleService baseService;

    @Override
    public Long getIdByCode(String code) {
        return baseService.getIdByCode(code);
    }

    @Override
    public void updateUserContext(Long roleId) {
        baseService.updateUserContext(roleId);
    }
}
