package top.wain.heimdall.oauth2.service.impl;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import top.wain.heimdall.oauth2.config.OidcProperties;
import top.wain.heimdall.oauth2.service.JwkKeyProvider;
import top.wain.heimdall.oauth2.service.OidcIdTokenService;

import java.time.Instant;
import java.util.Date;

/**
 * @Description: OIDC id_token 生成实现，使用 RS256 签名
 * @Author: WainZeng
 * @Date: 2026/06/18
 */
@Service
@RequiredArgsConstructor
public class OidcIdTokenServiceImpl implements OidcIdTokenService {

    private final JwkKeyProvider jwkKeyProvider;
    private final OidcProperties oidcProperties;

    @Override
    public String generate(Long userId, String clientId, String nonce, int ttl) {
        Instant now = Instant.now();

        JWTClaimsSet.Builder claimsBuilder = new JWTClaimsSet.Builder().issuer(oidcProperties.getIssuer())
            .subject(userId.toString())
            .audience(clientId)
            .issueTime(Date.from(now))
            .expirationTime(Date.from(now.plusSeconds(ttl)))
            .claim("auth_time", now.getEpochSecond());

        if (nonce != null && !nonce.isEmpty()) {
            claimsBuilder.claim("nonce", nonce);
        }

        JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.RS256).keyID(jwkKeyProvider.getActiveKid()).build();

        SignedJWT signedJWT = new SignedJWT(header, claimsBuilder.build());
        try {
            JWSSigner signer = new RSASSASigner(jwkKeyProvider.getActivePrivateKey());
            signedJWT.sign(signer);
        } catch (JOSEException e) {
            throw new IllegalStateException("id_token 签名失败", e);
        }
        return signedJWT.serialize();
    }
}
