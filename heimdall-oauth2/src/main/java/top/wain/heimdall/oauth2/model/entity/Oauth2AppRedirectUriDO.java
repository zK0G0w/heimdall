package top.wain.heimdall.oauth2.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import top.continew.starter.extension.crud.model.entity.BaseIdDO;

import java.io.Serial;

/**
 * @Description: OAuth2 应用回调地址实体
 * @Author: WainZeng
 * @Date: 2026/06/10
 */
@Data
@TableName("oauth2_app_redirect_uri")
public class Oauth2AppRedirectUriDO extends BaseIdDO {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long appId;
    private String uri;
}
