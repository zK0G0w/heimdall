package top.wain.heimdall.system.service;

import cn.hutool.core.util.StrUtil;
import org.dromara.x.file.storage.core.FileInfo;
import org.springframework.web.multipart.MultipartFile;
import top.wain.heimdall.system.model.entity.FileDO;
import top.wain.heimdall.system.model.entity.StorageDO;
import top.wain.heimdall.system.model.query.FileQuery;
import top.wain.heimdall.system.model.req.FileReq;
import top.wain.heimdall.system.model.resp.file.FileResp;
import top.wain.heimdall.system.model.resp.file.FileStatisticsResp;
import top.continew.starter.core.constant.StringConstants;
import top.continew.starter.data.service.IService;
import top.continew.starter.extension.crud.model.query.PageQuery;
import top.continew.starter.extension.crud.model.resp.BasePageResp;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

/**
 * 文件业务接口
 *
 * @author WainZeng
 * @since 2023/12/23 10:38
 */
public interface FileService extends IService<FileDO> {

    /**
     * 分页查询
     *
     * @param query     查询条件
     * @param pageQuery 分页查询条件
     * @return 分页列表信息
     */
    BasePageResp<FileResp> page(FileQuery query, PageQuery pageQuery);

    /**
     * 查询详情
     *
     * @param id ID
     * @return 详情信息
     */
    FileResp get(Long id);

    /**
     * 修改数据
     *
     * @param req 修改请求参数
     * @param id  ID
     */
    void update(FileReq req, Long id);

    /**
     * 删除数据
     *
     * @param ids ID 列表
     */
    void delete(List<Long> ids);

    /**
     * 上传到默认存储
     *
     * @param file 文件信息
     * @return 文件信息
     * @throws IOException /
     */
    default FileInfo upload(MultipartFile file) throws IOException {
        return upload(file, getDefaultParentPath(), null);
    }

    /**
     * 上传到默认存储
     *
     * @param file       文件信息
     * @param parentPath 上级目录
     * @return 文件信息
     * @throws IOException /
     */
    default FileInfo upload(MultipartFile file, String parentPath) throws IOException {
        return upload(file, StrUtil.blankToDefault(parentPath, getDefaultParentPath()), null);
    }

    /**
     * 上传到指定存储
     *
     * @param file        文件信息
     * @param parentPath  上级目录
     * @param storageCode 存储编码
     * @return 文件信息
     * @throws IOException /
     */
    FileInfo upload(MultipartFile file, String parentPath, String storageCode) throws IOException;

    /**
     * 上传到默认存储
     *
     * @param file 文件信息
     * @return 文件信息
     * @throws IOException /
     */
    default FileInfo upload(File file) throws IOException {
        return upload(file, getDefaultParentPath(), null);
    }

    /**
     * 上传到默认存储
     *
     * @param file       文件信息
     * @param parentPath 上级目录
     * @return 文件信息
     * @throws IOException /
     */
    default FileInfo upload(File file, String parentPath) throws IOException {
        return upload(file, StrUtil.blankToDefault(parentPath, getDefaultParentPath()), null);
    }

    /**
     * 上传到指定存储
     *
     * @param file        文件信息
     * @param parentPath  上级目录
     * @param storageCode 存储编码
     * @return 文件信息
     * @throws IOException /
     */
    FileInfo upload(File file, String parentPath, String storageCode) throws IOException;

    /**
     * 创建目录
     *
     * @param req 请求参数
     * @return ID
     */
    Long createDir(FileReq req);

    /**
     * 查询文件资源统计信息
     *
     * @return 资源统计信息
     */
    FileStatisticsResp statistics();

    /**
     * 检查文件是否存在
     *
     * @param fileHash 文件 Hash
     * @return 响应参数
     */
    FileResp check(String fileHash);

    /**
     * 计算文件夹大小
     *
     * @param id ID
     * @return 文件夹大小（字节）
     */
    Long calcDirSize(Long id);

    /**
     * 根据存储 ID 列表查询
     *
     * @param storageIds 存储 ID 列表
     * @return 文件数量
     */
    Long countByStorageIds(List<Long> storageIds);

    /**
     * 创建上级文件夹（支持多级）
     *
     * <p>
     * user/avatar/ => user（path：/user）、avatar（path：/user/avatar）
     * </p>
     *
     * @param parentPath 上级目录
     * @param storage    存储配置
     */
    void createParentDir(String parentPath, StorageDO storage);

    /**
     * 获取默认上级目录
     *
     * <p>
     * 默认上级目录：yyyy/MM/dd/
     * </p>
     *
     * @return 默认上级目录
     */
    default String getDefaultParentPath() {
        LocalDate today = LocalDate.now();
        return today.getYear() + StringConstants.SLASH + today.getMonthValue() + StringConstants.SLASH + today
            .getDayOfMonth() + StringConstants.SLASH;
    }
}
