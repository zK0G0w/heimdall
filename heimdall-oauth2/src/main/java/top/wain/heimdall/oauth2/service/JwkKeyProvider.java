package top.wain.heimdall.oauth2.service;

import com.nimbusds.jose.jwk.JWKSet;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

/**
 * @Description: JWK 密钥管理，负责 RSA 密钥加载和 JWKS 导出
 * @Author: WainZeng
 * @Date: 2026/06/18
 */
public interface JwkKeyProvider {

    /**
     * 获取当前活跃的 RSA 私钥，用于 JWT 签名
     */
    RSAPrivateKey getActivePrivateKey();

    /**
     * 获取当前活跃密钥的 kid
     */
    String getActiveKid();

    /**
     * 根据 kid 获取对应的 RSA 公钥，用于 token 验签
     */
    RSAPublicKey getPublicKey(String kid);

    /**
     * 获取 JWKS（仅包含公钥部分），用于 /.well-known/jwks.json 端点
     */
    JWKSet getJwkSet();
}
