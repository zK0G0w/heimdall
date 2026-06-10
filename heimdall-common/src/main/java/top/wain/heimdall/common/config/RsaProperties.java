package top.wain.heimdall.common.config;

import cn.hutool.extra.spring.SpringUtil;

/**
 * RSA 配置属性
 *
 * @author Zheng Jie（ELADMIN）
 * @author WainZeng
 * @since 2022/12/21 20:21
 */
public class RsaProperties {

    /**
     * 私钥
     */
    public static final String PRIVATE_KEY;
    public static final String PUBLIC_KEY;

    static {
        PRIVATE_KEY = SpringUtil.getProperty("continew-starter.encrypt.field.private-key");
        PUBLIC_KEY = SpringUtil.getProperty("continew-starter.encrypt.field.public-key");
    }

    private RsaProperties() {
    }
}
