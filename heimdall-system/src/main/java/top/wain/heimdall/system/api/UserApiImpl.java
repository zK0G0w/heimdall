package top.wain.heimdall.system.api;

import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.anno.Cached;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import top.wain.heimdall.common.api.system.UserApi;
import top.wain.heimdall.common.constant.CacheConstants;
import top.wain.heimdall.system.mapper.user.UserMapper;
import top.wain.heimdall.system.model.req.user.UserPasswordResetReq;
import top.wain.heimdall.system.service.UserService;

/**
 * 用户业务 API 实现
 *
 * @author WainZeng
 * @since 2025/7/23 20:57
 */
@Service
@RequiredArgsConstructor
public class UserApiImpl implements UserApi {

    private final UserService baseService;
    private final UserMapper baseMapper;

    @Override
    @Cached(key = "#id", name = CacheConstants.USER_KEY_PREFIX, cacheType = CacheType.BOTH, syncLocal = true)
    public String getNicknameById(Long id) {
        return baseMapper.selectNicknameById(id);
    }

    @Override
    public void resetPassword(String newPassword, Long id) {
        UserPasswordResetReq req = new UserPasswordResetReq();
        req.setNewPassword(newPassword);
        baseService.resetPassword(req, id);
    }
}
