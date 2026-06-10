package top.wain.heimdall.system.mapper;

import org.apache.ibatis.annotations.Mapper;
import top.continew.starter.data.mapper.BaseMapper;
import top.wain.heimdall.system.model.entity.SmsLogDO;

/**
 * 短信日志 Mapper
 *
 * @author luoqiz
 * @since 2025/03/15 22:15
 */
@Mapper
public interface SmsLogMapper extends BaseMapper<SmsLogDO> {}