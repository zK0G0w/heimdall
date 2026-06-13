package top.wain.heimdall.auth.mfa.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import top.wain.heimdall.auth.mfa.model.entity.UserMfaDO;

/**
 * @Description: 用户 MFA Mapper
 * @Author: WainZeng
 * @Date: 2026/06/13
 */
@Mapper
public interface UserMfaMapper extends BaseMapper<UserMfaDO> {
}
