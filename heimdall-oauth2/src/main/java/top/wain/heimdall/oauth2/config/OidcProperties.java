package top.wain.heimdall.oauth2.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @Description: OIDC 配置属性
 * @Author: WainZeng
 * @Date: 2026/06/18
 */
@Data
@Component
@ConfigurationProperties(prefix = "heimdall.oidc")
public class OidcProperties {

    /** 签发者标识（必须与对外暴露的基础 URL 一致） */
    private String issuer = "http://localhost:8000";

    /** RSA 签名密钥列表（支持多 key 实现 rotation） */
    private List<KeyConfig> keys = new ArrayList<>();

    @Data
    public static class KeyConfig {
        /** 密钥标识（对应 JWT header 中的 kid） */
        private String kid;
        /** 私钥文件路径（支持 classpath: 和绝对路径） */
        private String privateKeyPath;
        /** 是否为当前活跃签发密钥 */
        private boolean active = true;
    }
}
