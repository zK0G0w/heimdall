package top.wain.heimdall.oauth2.model.resp;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

/**
 * @Description: OAuth2 用户信息响应
 * @Author: WainZeng
 * @Date: 2026/06/11
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Oauth2UserInfoResp {

    private String sub;
    private String nickname;
    private String avatar;
    private String email;
}
