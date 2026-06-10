package top.wain.heimdall.generator.service;

import jakarta.servlet.http.HttpServletResponse;
import top.wain.heimdall.generator.model.entity.FieldConfigDO;
import top.wain.heimdall.generator.model.entity.GenConfigDO;
import top.wain.heimdall.generator.model.query.GenConfigQuery;
import top.wain.heimdall.generator.model.req.GenConfigReq;
import top.wain.heimdall.generator.model.resp.GeneratePreviewResp;
import top.continew.starter.extension.crud.model.query.PageQuery;
import top.continew.starter.extension.crud.model.resp.PageResp;

import java.sql.SQLException;
import java.util.List;

/**
 * 代码生成业务接口
 *
 * @author WainZeng
 * @since 2023/4/12 23:57
 */
public interface GeneratorService {

    /**
     * 分页查询生成配置列表
     *
     * @param query     查询条件
     * @param pageQuery 分页查询条件
     * @return 分页列表信息
     */
    PageResp<GenConfigDO> pageGenConfig(GenConfigQuery query, PageQuery pageQuery);

    /**
     * 查询生成配置信息
     *
     * @param tableName 表名称
     * @return 生成配置信息
     * @throws SQLException /
     */
    GenConfigDO getGenConfig(String tableName) throws SQLException;

    /**
     * 查询字段配置列表
     *
     * @param tableName   表名称
     * @param requireSync 是否需要同步
     * @return 字段配置列表
     */
    List<FieldConfigDO> listFieldConfig(String tableName, Boolean requireSync);

    /**
     * 保存代码生成配置信息
     *
     * @param req       请求参数
     * @param tableName 表名称
     */
    void saveConfig(GenConfigReq req, String tableName);

    /**
     * 生成预览
     *
     * @param tableNames 表名称列表
     * @return 预览信息
     */
    List<GeneratePreviewResp> preview(List<String> tableNames);

    /**
     * 生成下载代码
     *
     * @param tableNames 表名称列表
     * @param response   响应对象
     */
    void downloadCode(List<String> tableNames, HttpServletResponse response);

    /**
     * 生成下载代码
     *
     * @param tableNames 表名称列表
     */
    void generateCode(List<String> tableNames);
}
