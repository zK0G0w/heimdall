package top.wain.heimdall.oauth2.service;

import top.continew.starter.data.service.IService;
import top.continew.starter.extension.crud.model.query.PageQuery;
import top.continew.starter.extension.crud.model.resp.BasePageResp;
import top.wain.heimdall.oauth2.model.entity.Oauth2AppDO;
import top.wain.heimdall.oauth2.model.query.Oauth2AppQuery;
import top.wain.heimdall.oauth2.model.req.Oauth2AppRedirectUriReq;
import top.wain.heimdall.oauth2.model.req.Oauth2AppReq;
import top.wain.heimdall.oauth2.model.req.Oauth2AppScopeReq;
import top.wain.heimdall.oauth2.model.resp.Oauth2AppDetailResp;
import top.wain.heimdall.oauth2.model.resp.Oauth2AppResp;

import java.util.List;

/**
 * @Description: OAuth2 应用 Service 接口
 * @Author: WainZeng
 * @Date: 2026/06/10
 */
public interface Oauth2AppService extends IService<Oauth2AppDO> {

    /**
     * 分页查询应用列表
     */
    BasePageResp<Oauth2AppResp> page(Oauth2AppQuery query, PageQuery pageQuery);

    /**
     * 查询应用详情
     */
    Oauth2AppDetailResp get(Long id);

    /**
     * 创建应用
     */
    Long create(Oauth2AppReq req);

    /**
     * 更新应用
     */
    void update(Oauth2AppReq req, Long id);

    /**
     * 删除应用
     */
    void delete(List<Long> ids);

    /**
     * 更新应用状态
     */
    void updateStatus(Long id, Integer status);

    /**
     * 更新应用回调地址
     */
    void updateRedirectUris(Long appId, Oauth2AppRedirectUriReq req);

    /**
     * 更新应用授权 Scope
     */
    void updateScopes(Long appId, Oauth2AppScopeReq req);
}
