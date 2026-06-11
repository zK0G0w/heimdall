package top.wain.heimdall.oauth2.model.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @Description: OAuth2 令牌聚合信息
 * @Author: WainZeng
 * @Date: 2026/06/11
 */
@Data
public class Oauth2TokenDTO {

    private String accessToken;
    private String refreshToken;
    private String clientId;
    private Long userId;
    private String scope;
    private String grantType;
    private Integer expiresIn;
    private LocalDateTime issuedAt;
    private LocalDateTime expiresAt;
}
