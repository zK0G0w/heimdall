package top.wain.heimdall.system.mapper;

import org.apache.ibatis.annotations.Mapper;
import top.wain.heimdall.system.model.entity.NoticeLogDO;
import top.continew.starter.data.mapper.BaseMapper;

/**
 * 公告日志 Mapper
 *
 * @author WainZeng
 * @since 2025/5/18 19:17
 */
@Mapper
public interface NoticeLogMapper extends BaseMapper<NoticeLogDO> {
}
