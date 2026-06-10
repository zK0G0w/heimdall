package top.wain.heimdall.oauth2.mapper;

import org.apache.ibatis.annotations.Mapper;
import top.continew.starter.data.mapper.BaseMapper;
import top.wain.heimdall.oauth2.model.entity.Oauth2AppSecretDO;

/**
 * @Description: OAuth2 应用密钥 Mapper
 * @Author: WainZeng
 * @Date: 2026/06/10
 */
@Mapper
public interface Oauth2AppSecretMapper extends BaseMapper<Oauth2AppSecretDO> {
}
