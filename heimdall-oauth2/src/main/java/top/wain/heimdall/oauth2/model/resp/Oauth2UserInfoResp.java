package top.wain.heimdall.oauth2.model.resp;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @Description: OAuth2 用户信息响应
 * @Author: WainZeng
 * @Date: 2026/06/11
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Oauth2UserInfoResp {

    @Schema(description = "用户唯一标识")
    private String sub;
    @Schema(description = "昵称（scope: profile）")
    private String nickname;
    @Schema(description = "头像（scope: profile）")
    private String avatar;
    @Schema(description = "邮箱（scope: email）")
    private String email;
}
