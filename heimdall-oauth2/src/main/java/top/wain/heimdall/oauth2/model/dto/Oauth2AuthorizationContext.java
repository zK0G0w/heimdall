package top.wain.heimdall.oauth2.model.dto;

import lombok.Data;

/**
 * @Description: OAuth2 授权请求上下文
 * @Author: WainZeng
 * @Date: 2026/06/11
 */
@Data
public class Oauth2AuthorizationContext {

    private String clientId;
    private String redirectUri;
    private String scope;
    private String state;
    private String responseType;
    private String codeChallenge;
    private String codeChallengeMethod;
    private Long userId;
}
