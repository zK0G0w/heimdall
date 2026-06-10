package top.wain.heimdall.oauth2.mapper;

import org.apache.ibatis.annotations.Mapper;
import top.continew.starter.data.mapper.BaseMapper;
import top.wain.heimdall.oauth2.model.entity.Oauth2AppRedirectUriDO;

/**
 * @Description: OAuth2 应用回调地址 Mapper
 * @Author: WainZeng
 * @Date: 2026/06/10
 */
@Mapper
public interface Oauth2AppRedirectUriMapper extends BaseMapper<Oauth2AppRedirectUriDO> {
}
