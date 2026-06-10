package top.wain.heimdall.oauth2.mapper;

import org.apache.ibatis.annotations.Mapper;
import top.continew.starter.data.mapper.BaseMapper;
import top.wain.heimdall.oauth2.model.entity.Oauth2AppScopeDO;

/**
 * @Description: OAuth2 应用与 Scope 关联 Mapper
 * @Author: WainZeng
 * @Date: 2026/06/10
 */
@Mapper
public interface Oauth2AppScopeMapper extends BaseMapper<Oauth2AppScopeDO> {
}
