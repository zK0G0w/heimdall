package top.wain.heimdall.auth.service;

import jakarta.servlet.http.HttpServletRequest;
import top.wain.heimdall.auth.model.req.LoginReq;
import top.wain.heimdall.auth.model.resp.LoginResp;
import top.wain.heimdall.auth.model.resp.RouteResp;

import java.util.List;

/**
 * 认证业务接口
 *
 * @author WainZeng
 * @since 2022/12/21 21:48
 */
public interface AuthService {

    /**
     * 登录
     *
     * @param req     请求参数
     * @param request 请求对象
     * @return 登录响应参数
     */
    LoginResp login(LoginReq req, HttpServletRequest request);

    /**
     * 构建路由树
     *
     * @param userId 用户 ID
     * @return 路由树
     */
    List<RouteResp> buildRouteTree(Long userId);
}
