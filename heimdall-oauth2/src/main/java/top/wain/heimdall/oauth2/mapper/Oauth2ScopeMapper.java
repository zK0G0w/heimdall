package top.wain.heimdall.oauth2.mapper;

import org.apache.ibatis.annotations.Mapper;
import top.continew.starter.data.mapper.BaseMapper;
import top.wain.heimdall.oauth2.model.entity.Oauth2ScopeDO;

/**
 * @Description: OAuth2 Scope Mapper
 * @Author: WainZeng
 * @Date: 2026/06/10
 */
@Mapper
public interface Oauth2ScopeMapper extends BaseMapper<Oauth2ScopeDO> {
}
