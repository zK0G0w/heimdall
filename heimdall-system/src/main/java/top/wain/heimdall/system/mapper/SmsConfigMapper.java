package top.wain.heimdall.system.mapper;

import org.apache.ibatis.annotations.Mapper;
import top.continew.starter.data.mapper.BaseMapper;
import top.wain.heimdall.system.model.entity.SmsConfigDO;

/**
 * 短信配置 Mapper
 *
 * @author luoqiz
 * @since 2025/03/15 18:41
 */
@Mapper
public interface SmsConfigMapper extends BaseMapper<SmsConfigDO> {}