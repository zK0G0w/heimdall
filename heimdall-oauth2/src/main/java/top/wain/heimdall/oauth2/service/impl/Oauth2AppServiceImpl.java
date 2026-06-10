package top.wain.heimdall.oauth2.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.continew.starter.core.util.validation.CheckUtils;
import top.continew.starter.data.service.impl.ServiceImpl;
import top.continew.starter.data.util.QueryWrapperHelper;
import top.continew.starter.extension.crud.model.query.PageQuery;
import top.continew.starter.extension.crud.model.resp.BasePageResp;
import top.continew.starter.extension.crud.model.resp.PageResp;
import top.wain.heimdall.common.enums.DisEnableStatusEnum;
import top.wain.heimdall.common.util.CrudHelper;
import top.wain.heimdall.oauth2.mapper.Oauth2AppMapper;
import top.wain.heimdall.oauth2.mapper.Oauth2AppRedirectUriMapper;
import top.wain.heimdall.oauth2.mapper.Oauth2AppScopeMapper;
import top.wain.heimdall.oauth2.mapper.Oauth2ScopeMapper;
import top.wain.heimdall.oauth2.model.entity.Oauth2AppDO;
import top.wain.heimdall.oauth2.model.entity.Oauth2AppRedirectUriDO;
import top.wain.heimdall.oauth2.model.entity.Oauth2AppScopeDO;
import top.wain.heimdall.oauth2.model.entity.Oauth2ScopeDO;
import top.wain.heimdall.oauth2.model.query.Oauth2AppQuery;
import top.wain.heimdall.oauth2.model.req.Oauth2AppRedirectUriReq;
import top.wain.heimdall.oauth2.model.req.Oauth2AppReq;
import top.wain.heimdall.oauth2.model.req.Oauth2AppScopeReq;
import top.wain.heimdall.oauth2.model.resp.Oauth2AppDetailResp;
import top.wain.heimdall.oauth2.model.resp.Oauth2AppResp;
import top.wain.heimdall.oauth2.model.resp.Oauth2ScopeResp;
import top.wain.heimdall.oauth2.service.Oauth2AppService;
import top.wain.heimdall.oauth2.util.ClientCredentialGenerator;

import java.util.List;

/**
 * @Description: OAuth2 应用 Service 实现
 * @Author: WainZeng
 * @Date: 2026/06/10
 */
@Service
@RequiredArgsConstructor
public class Oauth2AppServiceImpl extends ServiceImpl<Oauth2AppMapper, Oauth2AppDO> implements Oauth2AppService {

    private final Oauth2AppRedirectUriMapper redirectUriMapper;
    private final Oauth2AppScopeMapper appScopeMapper;
    private final Oauth2ScopeMapper scopeMapper;

    @Override
    public BasePageResp<Oauth2AppResp> page(Oauth2AppQuery query, PageQuery pageQuery) {
        QueryWrapper<Oauth2AppDO> queryWrapper = QueryWrapperHelper.build(query);
        CrudHelper.sort(queryWrapper, pageQuery, Oauth2AppDO.class);
        IPage<Oauth2AppDO> page = baseMapper.selectPage(new Page<>(pageQuery.getPage(), pageQuery
            .getSize()), queryWrapper);
        PageResp<Oauth2AppResp> pageResp = PageResp.build(page, Oauth2AppResp.class);
        pageResp.getList().forEach(CrudHelper::fill);
        return pageResp;
    }

    @Override
    public Oauth2AppDetailResp get(Long id) {
        Oauth2AppDO entity = baseMapper.selectById(id);
        CheckUtils.throwIfNull(entity, "应用不存在");
        Oauth2AppDetailResp resp = BeanUtil.toBean(entity, Oauth2AppDetailResp.class);
        CrudHelper.fill(resp);

        // 查询回调地址列表
        List<String> uris = redirectUriMapper.lambdaQuery()
            .eq(Oauth2AppRedirectUriDO::getAppId, id)
            .list()
            .stream()
            .map(Oauth2AppRedirectUriDO::getUri)
            .toList();
        resp.setRedirectUris(uris);

        // 通过 appScope 关联查询 scope 详情
        List<Long> scopeIds = appScopeMapper.lambdaQuery()
            .eq(Oauth2AppScopeDO::getAppId, id)
            .list()
            .stream()
            .map(Oauth2AppScopeDO::getScopeId)
            .toList();
        if (!scopeIds.isEmpty()) {
            List<Oauth2ScopeDO> scopes = scopeMapper.selectByIds(scopeIds);
            resp.setScopes(BeanUtil.copyToList(scopes, Oauth2ScopeResp.class));
        } else {
            resp.setScopes(List.of());
        }

        return resp;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(Oauth2AppReq req) {
        Oauth2AppDO entity = BeanUtil.copyProperties(req, Oauth2AppDO.class);
        // 自动生成 clientId
        entity.setClientId(ClientCredentialGenerator.generateClientId());
        // 新建应用默认启用
        entity.setStatus(DisEnableStatusEnum.ENABLE);
        baseMapper.insert(entity);
        return entity.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Oauth2AppReq req, Long id) {
        Oauth2AppDO entity = this.getById(id);
        CheckUtils.throwIfNull(entity, "应用不存在");
        BeanUtil.copyProperties(req, entity, CopyOptions.create().ignoreNullValue());
        baseMapper.updateById(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(List<Long> ids) {
        baseMapper.deleteByIds(ids);
        // 级联删除回调地址和 scope 关联
        for (Long id : ids) {
            redirectUriMapper.lambdaUpdate().eq(Oauth2AppRedirectUriDO::getAppId, id).remove();
            appScopeMapper.lambdaUpdate().eq(Oauth2AppScopeDO::getAppId, id).remove();
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long id, Integer status) {
        Oauth2AppDO entity = this.getById(id);
        CheckUtils.throwIfNull(entity, "应用不存在");
        // 根据传入值设置启用/禁用状态
        entity.setStatus(DisEnableStatusEnum.ENABLE.getValue().equals(status)
            ? DisEnableStatusEnum.ENABLE
            : DisEnableStatusEnum.DISABLE);
        baseMapper.updateById(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateRedirectUris(Long appId, Oauth2AppRedirectUriReq req) {
        CheckUtils.throwIfNull(this.getById(appId), "应用不存在");
        // 删除旧回调地址
        redirectUriMapper.lambdaUpdate().eq(Oauth2AppRedirectUriDO::getAppId, appId).remove();
        // 插入新回调地址
        for (String uri : req.getUris()) {
            Oauth2AppRedirectUriDO uriDO = new Oauth2AppRedirectUriDO();
            uriDO.setAppId(appId);
            uriDO.setUri(uri);
            redirectUriMapper.insert(uriDO);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateScopes(Long appId, Oauth2AppScopeReq req) {
        CheckUtils.throwIfNull(this.getById(appId), "应用不存在");
        // 删除旧 scope 关联
        appScopeMapper.lambdaUpdate().eq(Oauth2AppScopeDO::getAppId, appId).remove();
        // 插入新 scope 关联
        for (Long scopeId : req.getScopeIds()) {
            appScopeMapper.insert(new Oauth2AppScopeDO(appId, scopeId));
        }
    }
}
