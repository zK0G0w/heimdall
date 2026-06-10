package top.wain.heimdall.schedule.model;

import com.aizuda.snailjob.common.core.model.Result;
import lombok.Data;

/**
 * 任务调度服务端分页响应参数
 *
 * @author KAI
 * @author WainZeng
 * @since 2024/6/26 22:27
 */
@Data
public class JobPageResult<T> extends Result<T> {

    /**
     * 页码
     */
    private long page;

    /**
     * 每页条数
     */
    private long size;

    /**
     * 总条数
     */
    private long total;
}
