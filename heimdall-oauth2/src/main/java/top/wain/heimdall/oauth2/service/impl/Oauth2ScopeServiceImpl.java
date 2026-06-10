package top.wain.heimdall.oauth2.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.continew.starter.core.util.validation.CheckUtils;
import top.continew.starter.core.util.validation.ValidationUtils;
import top.wain.heimdall.oauth2.mapper.Oauth2AppScopeMapper;
import top.wain.heimdall.oauth2.mapper.Oauth2ScopeMapper;
import top.wain.heimdall.oauth2.model.entity.Oauth2AppScopeDO;
import top.wain.heimdall.oauth2.model.entity.Oauth2ScopeDO;
import top.wain.heimdall.oauth2.model.req.Oauth2ScopeReq;
import top.wain.heimdall.oauth2.model.resp.Oauth2ScopeResp;
import top.wain.heimdall.oauth2.service.Oauth2ScopeService;

import java.util.List;

/**
 * @Description: OAuth2 Scope Service 实现
 * @Author: WainZeng
 * @Date: 2026/06/10
 */
@Service
@RequiredArgsConstructor
public class Oauth2ScopeServiceImpl implements Oauth2ScopeService {

    private final Oauth2ScopeMapper scopeMapper;
    private final Oauth2AppScopeMapper appScopeMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(Oauth2ScopeReq req) {
        // 校验 scopeCode 唯一性
        long count = scopeMapper.lambdaQuery().eq(Oauth2ScopeDO::getScopeCode, req.getScopeCode()).count();
        ValidationUtils.throwIf(count > 0, "Scope 标识 [{}] 已存在", req.getScopeCode());

        Oauth2ScopeDO entity = BeanUtil.copyProperties(req, Oauth2ScopeDO.class);
        scopeMapper.insert(entity);
        return entity.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Oauth2ScopeReq req, Long id) {
        Oauth2ScopeDO entity = scopeMapper.selectById(id);
        CheckUtils.throwIfNull(entity, "Scope 不存在");

        // scopeCode 有变动时校验唯一性
        if (!entity.getScopeCode().equals(req.getScopeCode())) {
            long count = scopeMapper.lambdaQuery().eq(Oauth2ScopeDO::getScopeCode, req.getScopeCode()).count();
            ValidationUtils.throwIf(count > 0, "Scope 标识 [{}] 已存在", req.getScopeCode());
        }

        BeanUtil.copyProperties(req, entity, CopyOptions.create().ignoreNullValue());
        scopeMapper.updateById(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(List<Long> ids) {
        // 校验每个 Scope 未被应用使用
        for (Long id : ids) {
            long usageCount = appScopeMapper.lambdaQuery().eq(Oauth2AppScopeDO::getScopeId, id).count();
            ValidationUtils.throwIf(usageCount > 0, "Scope 正在被应用使用，无法删除");
        }
        scopeMapper.deleteByIds(ids);
    }

    @Override
    public List<Oauth2ScopeResp> list() {
        List<Oauth2ScopeDO> scopes = scopeMapper.lambdaQuery().list();
        return BeanUtil.copyToList(scopes, Oauth2ScopeResp.class);
    }
}
