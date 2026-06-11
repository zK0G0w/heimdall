package top.wain.heimdall.oauth2.model.resp;

import lombok.Data;

import java.util.List;

/**
 * @Description: OAuth2 授权确认页数据响应
 * @Author: WainZeng
 * @Date: 2026/06/11
 */
@Data
public class Oauth2ConsentResp {

    private String appName;
    private String logo;
    private String authReqId;
    private List<ScopeItem> scopes;

    @Data
    public static class ScopeItem {
        private String code;
        private String name;
        private String description;
    }
}
