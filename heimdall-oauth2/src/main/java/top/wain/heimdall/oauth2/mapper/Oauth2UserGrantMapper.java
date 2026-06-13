package top.wain.heimdall.oauth2.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import top.wain.heimdall.oauth2.model.entity.Oauth2UserGrantDO;

/**
 * @Description: 用户 OAuth2 授权记录 Mapper
 * @Author: WainZeng
 * @Date: 2026/06/13
 */
@Mapper
public interface Oauth2UserGrantMapper extends BaseMapper<Oauth2UserGrantDO> {
}
