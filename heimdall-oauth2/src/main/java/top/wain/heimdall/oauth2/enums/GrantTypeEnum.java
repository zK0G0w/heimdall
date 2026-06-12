package top.wain.heimdall.oauth2.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @Description: OAuth2 授权类型枚举
 * @Author: WainZeng
 * @Date: 2026/06/10
 */
@Getter
@RequiredArgsConstructor
public enum GrantTypeEnum {

    AUTHORIZATION_CODE("authorization_code", "授权码"), CLIENT_CREDENTIALS("client_credentials", "客户端凭证"),
    REFRESH_TOKEN("refresh_token", "刷新令牌");

    private final String value;
    private final String description;
}
