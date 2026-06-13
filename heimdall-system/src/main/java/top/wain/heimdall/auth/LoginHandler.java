package top.wain.heimdall.auth;

import jakarta.servlet.http.HttpServletRequest;
import top.wain.heimdall.auth.enums.AuthTypeEnum;
import top.wain.heimdall.auth.model.req.LoginReq;
import top.wain.heimdall.system.model.entity.user.UserDO;
import top.wain.heimdall.system.model.resp.ClientResp;

/**
 * 登录处理器
 *
 * @author KAI
 * @author WainZeng
 * @since 2024/12/22 14:52
 */
public interface LoginHandler<T extends LoginReq> {

    /**
     * 登录（第一因素验证）
     *
     * @param req     登录请求参数
     * @param client  客户端信息
     * @param request 请求对象
     * @return 验证通过的用户信息
     */
    UserDO login(T req, ClientResp client, HttpServletRequest request);

    /**
     * 登录前置处理
     *
     * @param req     登录请求参数
     * @param client  客户端信息
     * @param request 请求对象
     */
    void preLogin(T req, ClientResp client, HttpServletRequest request);

    /**
     * 登录后置处理
     *
     * @param req     登录请求参数
     * @param client  客户端信息
     * @param request 请求对象
     */
    void postLogin(T req, ClientResp client, HttpServletRequest request);

    /**
     * 获取认证类型
     *
     * @return 认证类型
     */
    AuthTypeEnum getAuthType();
}