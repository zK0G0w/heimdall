package top.wain.heimdall.oauth2.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import top.wain.heimdall.oauth2.mapper.Oauth2AppMapper;
import top.wain.heimdall.oauth2.mapper.Oauth2UserGrantMapper;
import top.wain.heimdall.oauth2.model.entity.Oauth2AppDO;
import top.wain.heimdall.oauth2.model.entity.Oauth2UserGrantDO;
import top.wain.heimdall.oauth2.model.resp.Oauth2UserGrantResp;
import top.wain.heimdall.oauth2.service.Oauth2UserGrantService;
import top.wain.heimdall.oauth2.store.RedisOauth2TokenStore;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @Description: 用户 OAuth2 授权记录管理实现
 * @Author: WainZeng
 * @Date: 2026/06/13
 */
@Service
@RequiredArgsConstructor
public class Oauth2UserGrantServiceImpl extends ServiceImpl<Oauth2UserGrantMapper, Oauth2UserGrantDO>
    implements Oauth2UserGrantService {

    private final RedisOauth2TokenStore tokenStore;
    private final Oauth2AppMapper appMapper;

    @Override
    public List<Oauth2UserGrantResp> listByUserId(Long userId) {
        List<Oauth2UserGrantDO> grants = lambdaQuery()
            .eq(Oauth2UserGrantDO::getUserId, userId)
            .orderByDesc(Oauth2UserGrantDO::getUpdatedAt)
            .list();
        if (grants.isEmpty()) {
            return List.of();
        }
        // 批量查应用信息
        Set<Long> appIds = grants.stream().map(Oauth2UserGrantDO::getAppId).collect(Collectors.toSet());
        List<Oauth2AppDO> apps = appMapper.selectBatchIds(appIds);
        Map<Long, Oauth2AppDO> appMap = apps.stream().collect(Collectors.toMap(Oauth2AppDO::getId, a -> a));

        return grants.stream().map(g -> {
            Oauth2UserGrantResp resp = new Oauth2UserGrantResp();
            resp.setAppId(g.getAppId());
            resp.setClientId(g.getClientId());
            resp.setScope(g.getScope());
            resp.setGrantedAt(g.getGrantedAt());
            resp.setUpdatedAt(g.getUpdatedAt());
            Oauth2AppDO app = appMap.get(g.getAppId());
            if (app != null) {
                resp.setAppName(app.getAppName());
                resp.setLogo(app.getLogo());
            }
            return resp;
        }).collect(Collectors.toList());
    }

    @Override
    public void saveOrUpdateGrant(Long userId, Long appId, String clientId, String scope) {
        Oauth2UserGrantDO existing = lambdaQuery()
            .eq(Oauth2UserGrantDO::getUserId, userId)
            .eq(Oauth2UserGrantDO::getAppId, appId)
            .one();
        LocalDateTime now = LocalDateTime.now();
        if (existing != null) {
            existing.setScope(scope);
            existing.setUpdatedAt(now);
            updateById(existing);
        } else {
            Oauth2UserGrantDO grant = new Oauth2UserGrantDO();
            grant.setUserId(userId);
            grant.setAppId(appId);
            grant.setClientId(clientId);
            grant.setScope(scope);
            grant.setGrantedAt(now);
            grant.setUpdatedAt(now);
            save(grant);
        }
    }

    @Override
    public void revokeGrant(Long userId, Long appId) {
        Oauth2UserGrantDO grant = lambdaQuery()
            .eq(Oauth2UserGrantDO::getUserId, userId)
            .eq(Oauth2UserGrantDO::getAppId, appId)
            .one();
        if (grant == null) {
            return;
        }
        removeById(grant.getId());
        tokenStore.removeConsent(userId, grant.getClientId());
        tokenStore.revokeTokensByClientId(userId, grant.getClientId());
    }

    @Override
    public void revokeAllGrants(Long userId) {
        List<Oauth2UserGrantDO> grants = lambdaQuery()
            .eq(Oauth2UserGrantDO::getUserId, userId)
            .list();
        if (grants.isEmpty()) {
            return;
        }
        removeBatchByIds(grants.stream().map(Oauth2UserGrantDO::getId).collect(Collectors.toList()));
        for (Oauth2UserGrantDO grant : grants) {
            tokenStore.removeConsent(userId, grant.getClientId());
        }
        tokenStore.revokeAllTokens(userId);
    }
}
