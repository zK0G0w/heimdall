package top.wain.heimdall.oauth2.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import top.wain.heimdall.oauth2.config.OidcProperties;
import top.wain.heimdall.oauth2.service.JwkKeyProvider;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @Description: OIDC Discovery 和 JWKS 端点
 * @Author: WainZeng
 * @Date: 2026/06/18
 */
@RestController
@RequiredArgsConstructor
public class OidcDiscoveryController {

    private final OidcProperties oidcProperties;
    private final JwkKeyProvider jwkKeyProvider;

    @GetMapping("/.well-known/openid-configuration")
    public ResponseEntity<Map<String, Object>> discovery() {
        String issuer = oidcProperties.getIssuer();
        Map<String, Object> config = new LinkedHashMap<>();
        config.put("issuer", issuer);
        config.put("authorization_endpoint", issuer + "/oauth2/authorize");
        config.put("token_endpoint", issuer + "/oauth2/token");
        config.put("userinfo_endpoint", issuer + "/oauth2/userinfo");
        config.put("jwks_uri", issuer + "/oauth2/jwks");
        config.put("revocation_endpoint", issuer + "/oauth2/revoke");
        config.put("introspection_endpoint", issuer + "/oauth2/introspect");
        config.put("scopes_supported", List.of("openid", "profile", "email"));
        config.put("response_types_supported", List.of("code"));
        config.put("grant_types_supported", List.of("authorization_code", "refresh_token", "client_credentials"));
        config.put("subject_types_supported", List.of("public"));
        config.put("id_token_signing_alg_values_supported", List.of("RS256"));
        config.put("token_endpoint_auth_methods_supported", List.of("client_secret_basic", "client_secret_post"));
        config.put("code_challenge_methods_supported", List.of("S256", "plain"));

        return ResponseEntity.ok().cacheControl(CacheControl.maxAge(24, TimeUnit.HOURS).cachePublic()).body(config);
    }

    @GetMapping(value = "/oauth2/jwks", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> jwks() {
        String jwksJson = jwkKeyProvider.getJwkSet().toString();
        return ResponseEntity.ok().cacheControl(CacheControl.maxAge(1, TimeUnit.HOURS).cachePublic()).body(jwksJson);
    }
}
