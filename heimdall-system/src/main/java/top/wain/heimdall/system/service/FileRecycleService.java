package top.wain.heimdall.system.service;

import jakarta.validation.Valid;
import top.wain.heimdall.system.model.query.FileQuery;
import top.wain.heimdall.system.model.resp.file.FileResp;
import top.continew.starter.extension.crud.model.query.PageQuery;
import top.continew.starter.extension.crud.model.resp.PageResp;

/**
 * 文件回收站业务接口
 *
 * @author WainZeng
 * @since 2025/11/11 21:28
 */
public interface FileRecycleService {

    /**
     * 分页查询列表
     *
     * @param query     查询参数
     * @param pageQuery 分页参数
     * @return 文件列表
     */
    PageResp<FileResp> page(@Valid FileQuery query, @Valid PageQuery pageQuery);

    /**
     * 还原文件
     *
     * @param id ID
     */
    void restore(Long id);

    /**
     * 删除文件
     *
     * @param id ID
     */
    void delete(Long id);

    /**
     * 清空回收站
     */
    void clean();
}
