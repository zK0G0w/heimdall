package top.wain.heimdall.oauth2.handler;

import top.wain.heimdall.oauth2.enums.GrantTypeEnum;
import top.wain.heimdall.oauth2.model.dto.Oauth2TokenDTO;
import top.wain.heimdall.oauth2.model.req.Oauth2TokenReq;

/**
 * @Description: OAuth2 授权类型处理器策略接口
 * @Author: WainZeng
 * @Date: 2026/06/11
 */
public interface GrantTypeHandler {

    /**
     * 处理令牌颁发请求
     *
     * @param req 令牌请求参数
     * @return 令牌 DTO
     */
    Oauth2TokenDTO handle(Oauth2TokenReq req);

    /**
     * 返回当前处理器对应的授权类型
     *
     * @return 授权类型枚举
     */
    GrantTypeEnum getGrantType();
}
