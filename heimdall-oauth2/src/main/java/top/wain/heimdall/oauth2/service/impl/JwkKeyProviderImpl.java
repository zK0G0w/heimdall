package top.wain.heimdall.oauth2.service.impl;

import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import top.wain.heimdall.oauth2.config.OidcProperties;
import top.wain.heimdall.oauth2.service.JwkKeyProvider;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @Description: JWK 密钥提供者实现，负责从配置路径加载 RSA 密钥，并在开发环境自动生成密钥对
 * @Author: WainZeng
 * @Date: 2026/06/18
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class JwkKeyProviderImpl implements JwkKeyProvider {

    private final OidcProperties oidcProperties;
    private final Environment environment;

    /** kid -> RSAKey（含公私钥） */
    private final Map<String, RSAKey> keyMap = new LinkedHashMap<>();
    private String activeKid;
    private RSAPrivateKey activePrivateKey;

    @PostConstruct
    public void init() {
        List<OidcProperties.KeyConfig> keys = oidcProperties.getKeys();
        if (keys == null || keys.isEmpty()) {
            log.warn("OIDC 密钥配置为空，将使用默认 kid=default 自动生成密钥");
            keys = new ArrayList<>();
            OidcProperties.KeyConfig defaultKey = new OidcProperties.KeyConfig();
            defaultKey.setKid("default");
            defaultKey.setPrivateKeyPath("classpath:keys/oidc-default.pem");
            defaultKey.setActive(true);
            keys.add(defaultKey);
        }
        boolean isDevProfile = isDevProfile();
        ResourceLoader resourceLoader = new DefaultResourceLoader();
        for (OidcProperties.KeyConfig keyConfig : keys) {
            try {
                RSAKey rsaKey = loadOrGenerateKey(keyConfig, resourceLoader, isDevProfile);
                keyMap.put(keyConfig.getKid(), rsaKey);
                if (keyConfig.isActive()) {
                    activeKid = keyConfig.getKid();
                    activePrivateKey = (RSAPrivateKey)rsaKey.toPrivateKey();
                }
            } catch (Exception e) {
                throw new IllegalStateException("OIDC 密钥加载失败，kid=" + keyConfig.getKid(), e);
            }
        }
        log.info("OIDC JWK 密钥加载完成，共 {} 把，活跃 kid: {}", keyMap.size(), activeKid);
    }

    private RSAKey loadOrGenerateKey(OidcProperties.KeyConfig keyConfig,
                                     ResourceLoader resourceLoader,
                                     boolean isDevProfile) throws Exception {
        String path = keyConfig.getPrivateKeyPath();
        Resource resource = resourceLoader.getResource(path);
        if (resource.exists()) {
            // 从文件加载 PEM 私钥
            RSAPrivateKey privateKey = loadPrivateKeyFromPem(resource.getInputStream());
            RSAPublicKey publicKey = derivePublicKey(privateKey);
            return new RSAKey.Builder(publicKey).privateKey(privateKey)
                .keyID(keyConfig.getKid())
                .keyUse(KeyUse.SIGNATURE)
                .build();
        }
        // 文件不存在
        if (!isDevProfile) {
            throw new IllegalStateException("OIDC 私钥文件不存在，生产环境不允许自动生成，路径=" + path);
        }
        // dev 环境：自动生成并写入
        log.warn("OIDC 私钥文件不存在，dev 环境自动生成 RSA 2048 密钥对，kid={}, 路径={}", keyConfig.getKid(), path);
        KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
        gen.initialize(2048);
        KeyPair keyPair = gen.generateKeyPair();
        RSAPrivateKey privateKey = (RSAPrivateKey)keyPair.getPrivate();
        RSAPublicKey publicKey = (RSAPublicKey)keyPair.getPublic();
        writePrivateKeyToPem(path, privateKey);
        return new RSAKey.Builder(publicKey).privateKey(privateKey)
            .keyID(keyConfig.getKid())
            .keyUse(KeyUse.SIGNATURE)
            .build();
    }

    /**
     * 从 PEM（PKCS#8 格式）输入流中解析 RSA 私钥
     */
    private RSAPrivateKey loadPrivateKeyFromPem(InputStream inputStream) throws Exception {
        String pem = new String(inputStream.readAllBytes());
        String base64 = pem.replace("-----BEGIN PRIVATE KEY-----", "")
            .replace("-----END PRIVATE KEY-----", "")
            .replaceAll("\\s", "");
        byte[] keyBytes = Base64.getDecoder().decode(base64);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return (RSAPrivateKey)keyFactory.generatePrivate(spec);
    }

    /**
     * 从 RSA 私钥推导公钥（要求私钥实现 RSAPrivateCrtKey）
     */
    private RSAPublicKey derivePublicKey(RSAPrivateKey privateKey) throws Exception {
        if (!(privateKey instanceof RSAPrivateCrtKey)) {
            throw new IllegalArgumentException("私钥不包含 CRT 参数，无法推导公钥，请使用完整的 RSA 私钥");
        }
        RSAPrivateCrtKey crtKey = (RSAPrivateCrtKey)privateKey;
        RSAPublicKeySpec publicKeySpec = new RSAPublicKeySpec(crtKey.getModulus(), crtKey.getPublicExponent());
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return (RSAPublicKey)keyFactory.generatePublic(publicKeySpec);
    }

    /**
     * 将 RSA 私钥以 PKCS#8 PEM 格式写入文件系统
     * classpath: 前缀的路径会解析到 heimdall-server/src/main/resources/ 下
     */
    private void writePrivateKeyToPem(String configPath, RSAPrivateKey privateKey) throws IOException {
        Path filePath;
        if (configPath.startsWith("classpath:")) {
            String relativePath = configPath.substring("classpath:".length());
            filePath = Paths.get("heimdall-server/src/main/resources/", relativePath);
        } else {
            filePath = Paths.get(configPath);
        }
        Files.createDirectories(filePath.getParent());
        byte[] encoded = privateKey.getEncoded();
        String base64 = Base64.getMimeEncoder(64, "\n".getBytes()).encodeToString(encoded);
        String pem = "-----BEGIN PRIVATE KEY-----\n" + base64 + "\n-----END PRIVATE KEY-----\n";
        try (OutputStream out = Files.newOutputStream(filePath)) {
            out.write(pem.getBytes());
        }
        log.info("RSA 私钥已自动生成并写入：{}", filePath.toAbsolutePath());
    }

    /**
     * 判断当前是否为 dev 环境
     */
    private boolean isDevProfile() {
        String[] activeProfiles = environment.getActiveProfiles();
        for (String profile : activeProfiles) {
            if ("dev".equalsIgnoreCase(profile)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public RSAPrivateKey getActivePrivateKey() {
        return activePrivateKey;
    }

    @Override
    public String getActiveKid() {
        return activeKid;
    }

    @Override
    public RSAPublicKey getPublicKey(String kid) {
        RSAKey rsaKey = keyMap.get(kid);
        if (rsaKey == null) {
            return null;
        }
        try {
            return rsaKey.toRSAPublicKey();
        } catch (Exception e) {
            throw new IllegalStateException("获取公钥失败，kid=" + kid, e);
        }
    }

    @Override
    public JWKSet getJwkSet() {
        List<JWK> publicKeys = new ArrayList<>();
        for (RSAKey rsaKey : keyMap.values()) {
            publicKeys.add(rsaKey.toPublicJWK());
        }
        return new JWKSet(publicKeys);
    }
}
