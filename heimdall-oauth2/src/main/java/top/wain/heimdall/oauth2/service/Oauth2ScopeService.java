package top.wain.heimdall.oauth2.service;

import top.wain.heimdall.oauth2.model.req.Oauth2ScopeReq;
import top.wain.heimdall.oauth2.model.resp.Oauth2ScopeResp;

import java.util.List;

/**
 * @Description: OAuth2 Scope Service 接口
 * @Author: WainZeng
 * @Date: 2026/06/10
 */
public interface Oauth2ScopeService {

    /**
     * 创建 Scope
     */
    Long create(Oauth2ScopeReq req);

    /**
     * 更新 Scope
     */
    void update(Oauth2ScopeReq req, Long id);

    /**
     * 删除 Scope
     */
    void delete(List<Long> ids);

    /**
     * 查询全量 Scope 列表
     */
    List<Oauth2ScopeResp> list();
}
