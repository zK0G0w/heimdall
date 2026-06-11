package top.wain.heimdall.common.api.system;

import cn.crane4j.annotation.ContainerMethod;
import cn.crane4j.annotation.MappingType;
import top.wain.heimdall.common.constant.ContainerConstants;

/**
 * 用户业务 API
 *
 * @author WainZeng
 * @since 2025/1/9 20:17
 */
public interface UserApi {

    /**
     * 根据 ID 查询昵称
     *
     * <p>
     * 数据填充容器 {@link ContainerConstants#USER_NICKNAME}
     * </p>
     *
     * @param id ID
     * @return 昵称
     */
    @ContainerMethod(namespace = ContainerConstants.USER_NICKNAME, type = MappingType.ORDER_OF_KEYS)
    String getNicknameById(Long id);

    /**
     * 重置密码
     *
     * @param newPassword 新密码
     * @param id          ID
     */
    void resetPassword(String newPassword, Long id);

    /**
     * 根据 ID 查询用户基础信息（用于 OAuth2 userinfo 端点）
     *
     * @param id 用户 ID
     * @return 用户基础信息，不存在时返回 null
     */
    UserInfo getUserInfoById(Long id);

    /**
     * 用户基础信息（供跨模块使用）
     *
     * @param nickname 昵称
     * @param avatar   头像
     * @param email    邮箱
     */
    record UserInfo(String nickname, String avatar, String email) {
    }
}
