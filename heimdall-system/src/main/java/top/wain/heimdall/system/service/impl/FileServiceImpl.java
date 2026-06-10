package top.wain.heimdall.system.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.file.FileNameUtil;
import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.x.file.storage.core.FileInfo;
import org.dromara.x.file.storage.core.FileStorageService;
import org.dromara.x.file.storage.core.ProgressListener;
import org.dromara.x.file.storage.core.upload.UploadPretreatment;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import top.wain.heimdall.common.context.UserContextHolder;
import top.wain.heimdall.common.enums.DisEnableStatusEnum;
import top.wain.heimdall.common.util.CrudHelper;
import top.wain.heimdall.system.enums.FileTypeEnum;
import top.wain.heimdall.system.mapper.FileMapper;
import top.wain.heimdall.system.model.entity.FileDO;
import top.wain.heimdall.system.model.entity.StorageDO;
import top.wain.heimdall.system.model.query.FileQuery;
import top.wain.heimdall.system.model.req.FileReq;
import top.wain.heimdall.system.model.resp.file.FileResp;
import top.wain.heimdall.system.model.resp.file.FileStatisticsResp;
import top.wain.heimdall.system.service.FileService;
import top.wain.heimdall.system.service.StorageService;
import top.wain.heimdall.system.util.FileNameGenerator;
import top.continew.starter.cache.redisson.util.RedisLockUtils;
import top.continew.starter.core.constant.StringConstants;
import top.continew.starter.core.util.CollUtils;
import top.continew.starter.core.util.StrUtils;
import top.continew.starter.core.util.validation.CheckUtils;
import top.continew.starter.core.util.validation.ValidationUtils;
import top.continew.starter.data.service.impl.ServiceImpl;
import top.continew.starter.data.util.QueryWrapperHelper;
import top.continew.starter.extension.crud.model.query.PageQuery;
import top.continew.starter.extension.crud.model.resp.BasePageResp;
import top.continew.starter.extension.crud.model.resp.PageResp;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 文件业务实现
 *
 * @author WainZeng
 * @since 2023/12/23 10:38
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileServiceImpl extends ServiceImpl<FileMapper, FileDO> implements FileService {

    private final FileStorageService fileStorageService;
    @Lazy
    @Resource
    private StorageService storageService;

    @Override
    public BasePageResp<FileResp> page(FileQuery query, PageQuery pageQuery) {
        QueryWrapper<FileDO> queryWrapper = QueryWrapperHelper.build(query);
        CrudHelper.sort(queryWrapper, pageQuery, FileDO.class);
        IPage<FileDO> page = baseMapper.selectPage(new Page<>(pageQuery.getPage(), pageQuery.getSize()), queryWrapper);
        PageResp<FileResp> pageResp = PageResp.build(page, FileResp.class);
        pageResp.getList().forEach(this::fill);
        return pageResp;
    }

    @Override
    public FileResp get(Long id) {
        FileDO entity = baseMapper.selectById(id);
        CheckUtils.throwIfNull(entity, "数据不存在");
        FileResp resp = BeanUtil.toBean(entity, FileResp.class);
        this.fill(resp);
        return resp;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(FileReq req, Long id) {
        FileDO entity = this.getById(id);
        CheckUtils.throwIfNull(entity, "数据不存在");
        BeanUtil.copyProperties(req, entity, CopyOptions.create().ignoreNullValue());
        baseMapper.updateById(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(List<Long> ids) {
        List<FileDO> fileList = baseMapper.lambdaQuery().in(FileDO::getId, ids).list();
        if (CollUtil.isEmpty(fileList)) {
            return;
        }
        // 批量获取存储配置
        Map<Long, List<FileDO>> fileListGroup = fileList.stream().collect(Collectors.groupingBy(FileDO::getStorageId));
        List<StorageDO> storageList = storageService.listByIds(fileListGroup.keySet());
        Map<Long, StorageDO> storageGroup = storageList.stream()
            .collect(Collectors.toMap(StorageDO::getId, Function.identity(), (existing, replacement) -> existing));
        // 删除记录
        for (Map.Entry<Long, List<FileDO>> entry : fileListGroup.entrySet()) {
            StorageDO storage = storageGroup.get(entry.getKey());
            List<Long> idList = CollUtils.mapToList(entry.getValue(), FileDO::getId);
            if (Boolean.TRUE.equals(storage.getRecycleBinEnabled())) {
                baseMapper.deleteByIds(idList);
            } else {
                baseMapper.deleteWithoutRecycleBin(idList, UserContextHolder.getUserId());
            }
        }
        // 删除实际文件
        for (Map.Entry<Long, List<FileDO>> entry : fileListGroup.entrySet()) {
            StorageDO storage = storageGroup.get(entry.getKey());
            entry.getValue().forEach(file -> this.deleteFile(file, storage));
        }
    }

    @Override
    public FileInfo upload(MultipartFile file, String parentPath, String storageCode) {
        return this.upload(file, parentPath, storageCode, FileNameUtil.extName(file.getOriginalFilename()));
    }

    @Override
    public FileInfo upload(File file, String parentPath, String storageCode) {
        return this.upload(file, parentPath, storageCode, FileNameUtil.extName(file.getName()));
    }

    @Override
    public Long createDir(FileReq req) {
        String parentPath = req.getParentPath();
        FileDO file = baseMapper.lambdaQuery()
            .eq(FileDO::getParentPath, parentPath)
            .eq(FileDO::getName, req.getOriginalName())
            .eq(FileDO::getType, FileTypeEnum.DIR)
            .one();
        CheckUtils.throwIfNotNull(file, "文件夹已存在");
        // 存储引擎需要一致
        StorageDO storage = storageService.getDefaultStorage();
        if (!StringConstants.SLASH.equals(parentPath)) {
            FileDO parentFile = baseMapper.lambdaQuery()
                .eq(FileDO::getPath, parentPath)
                .eq(FileDO::getType, FileTypeEnum.DIR)
                .one();
            CheckUtils.throwIfNull(parentFile, "父级文件夹不存在");
            CheckUtils.throwIfNotEqual(parentFile.getStorageId(), storage.getId(), "文件夹和父级文件夹存储引擎不一致");
        }
        // 创建文件夹
        FileDO dirFile = new FileDO();
        String originalName = req.getOriginalName();
        dirFile.setName(originalName);
        dirFile.setOriginalName(originalName);
        dirFile.setParentPath(parentPath);
        dirFile.setType(FileTypeEnum.DIR);
        dirFile.setStorageId(storage.getId());
        baseMapper.insert(dirFile);
        return dirFile.getId();
    }

    @Override
    public FileStatisticsResp statistics() {
        FileStatisticsResp resp = new FileStatisticsResp();
        List<FileStatisticsResp> statisticsList = baseMapper.statistics();
        if (CollUtil.isEmpty(statisticsList)) {
            return resp;
        }
        resp.setData(statisticsList);
        resp.setSize(statisticsList.stream().mapToLong(FileStatisticsResp::getSize).sum());
        resp.setNumber(statisticsList.stream().mapToLong(FileStatisticsResp::getNumber).sum());
        return resp;
    }

    @Override
    public FileResp check(String fileHash) {
        FileDO file = baseMapper.lambdaQuery().eq(FileDO::getSha256, fileHash).one();
        if (file != null) {
            return get(file.getId());
        }
        return null;
    }

    @Override
    public Long calcDirSize(Long id) {
        FileDO dirFile = this.getById(id);
        ValidationUtils.throwIfNotEqual(dirFile.getType(), FileTypeEnum.DIR, "ID 为 [{}] 的不是文件夹，不支持计算大小", id);
        // 查询当前文件夹下的所有子文件和子文件夹
        List<FileDO> children = baseMapper.lambdaQuery().eq(FileDO::getParentPath, dirFile.getPath()).list();
        if (CollUtil.isEmpty(children)) {
            return 0L;
        }
        // 累加子文件大小和递归计算子文件夹大小
        return children.stream().mapToLong(child -> {
            if (FileTypeEnum.DIR.equals(child.getType())) {
                return calcDirSize(child.getId());
            } else {
                return child.getSize();
            }
        }).sum();
    }

    @Override
    public Long countByStorageIds(List<Long> storageIds) {
        if (CollUtil.isEmpty(storageIds)) {
            return 0L;
        }
        return baseMapper.lambdaQuery().in(FileDO::getStorageId, storageIds).count();
    }

    @Override
    public void createParentDir(String parentPath, StorageDO storage) {
        String lockKey = StrUtil.format("Lock:{}:{}", storage.getCode(), parentPath);
        try (RedisLockUtils lock = RedisLockUtils.tryLock(lockKey)) {
            if (!lock.isLocked()) {
                return;
            }
            if (StrUtil.isBlank(parentPath) || StringConstants.SLASH.equals(parentPath)) {
                return;
            }
            // user/avatar/ => user、avatar
            String[] parentPathParts = StrUtil.split(parentPath, StringConstants.SLASH, false, true)
                .toArray(String[]::new);
            String lastPath = StringConstants.SLASH;
            StringBuilder currentPathBuilder = new StringBuilder();
            for (int i = 0; i < parentPathParts.length; i++) {
                String parentPathPart = parentPathParts[i];
                if (i > 0) {
                    lastPath = currentPathBuilder.toString();
                }
                currentPathBuilder.append(StringConstants.SLASH).append(parentPathPart);
                String currentPath = currentPathBuilder.toString();
                // 文件夹和文件存储引擎需要一致
                FileDO dirFile = baseMapper.lambdaQuery()
                    .eq(FileDO::getPath, currentPath)
                    .eq(FileDO::getType, FileTypeEnum.DIR)
                    .one();
                if (dirFile != null) {
                    CheckUtils.throwIfNotEqual(dirFile.getStorageId(), storage.getId(), "文件夹和上传文件存储引擎不一致");
                    continue;
                }
                FileDO newFile = new FileDO();
                newFile.setName(parentPathPart);
                newFile.setOriginalName(parentPathPart);
                newFile.setPath(currentPath);
                newFile.setParentPath(lastPath);
                newFile.setType(FileTypeEnum.DIR);
                newFile.setStorageId(storage.getId());
                baseMapper.insert(newFile);
            }
        }
    }

    /**
     * 数据填充（Crane4j + 自定义 URL 填充）
     *
     * @param obj 待填充对象
     */
    private void fill(Object obj) {
        CrudHelper.fill(obj);
        if (obj instanceof FileResp fileResp) {
            StorageDO storage = storageService.getById(fileResp.getStorageId());
            String prefix = storage.getUrlPrefix();
            String url = URLUtil.normalize(prefix + fileResp.getPath(), false, true);
            fileResp.setUrl(url);
            String parentPath = StringConstants.SLASH.equals(fileResp.getParentPath())
                ? StringConstants.EMPTY
                : fileResp.getParentPath();
            String thumbnailUrl = StrUtils.blankToDefault(fileResp.getThumbnailName(), url, thName -> URLUtil
                .normalize(prefix + parentPath + StringConstants.SLASH + thName, false, true));
            fileResp.setThumbnailUrl(thumbnailUrl);
            fileResp.setStorageName("%s (%s)".formatted(storage.getName(), storage.getCode()));
        }
    }

    /**
     * 上传文件并返回上传后的文件信息
     *
     * @param file        文件
     * @param parentPath  上级目录
     * @param storageCode 存储引擎编码
     * @param extName     文件扩展名
     * @return 文件信息
     */
    private FileInfo upload(Object file, String parentPath, String storageCode, String extName) {
        List<String> allExtensions = FileTypeEnum.getAllExtensions();
        CheckUtils.throwIf(!allExtensions.contains(extName), "不支持的文件类型，仅支持 {} 格式的文件", String
            .join(StringConstants.COMMA, allExtensions));
        // 构建上传预处理对象
        StorageDO storage = storageService.getByCode(storageCode);
        CheckUtils.throwIf(DisEnableStatusEnum.DISABLE.equals(storage.getStatus()), "请先启用存储 [{}]", storage.getCode());

        // 创建父级目录
        this.createParentDir(parentPath, storage);

        // 生成唯一文件名（处理重名情况）
        String originalFileName = getOriginalFileName(file);
        String uniqueFileName = FileNameGenerator.generateUniqueName(originalFileName, parentPath, storage
            .getId(), baseMapper);

        UploadPretreatment uploadPretreatment = fileStorageService.of(file)
            .setPlatform(storage.getCode())
            .setHashCalculatorSha256(true)
            .putAttr(ClassUtil.getClassName(StorageDO.class, false), storage)
            .setPath(this.pretreatmentPath(parentPath))
            .setSaveFilename(uniqueFileName)
            .setOriginalFilename(uniqueFileName);
        // 图片文件生成缩略图
        if (FileTypeEnum.IMAGE.getExtensions().contains(extName)) {
            uploadPretreatment.setIgnoreThumbnailException(true, true);
            uploadPretreatment.thumbnail(img -> img.size(100, 100));
        }
        uploadPretreatment.setProgressMonitor(new ProgressListener() {
            @Override
            public void start() {
                log.info("开始上传文件: {}", uniqueFileName);
            }

            @Override
            public void progress(long progressSize, Long allSize) {
                log.info("文件 [{}] 已上传 [{}]，总大小 [{}]", uniqueFileName, progressSize, allSize);
            }

            @Override
            public void finish() {
                log.info("文件 [{}] 上传完成", uniqueFileName);
            }
        });
        return uploadPretreatment.upload();
    }

    /**
     * 获取原始文件名
     *
     * @param file 文件对象（MultipartFile 或 File）
     * @return 原始文件名
     */
    private String getOriginalFileName(Object file) {
        if (file instanceof MultipartFile multipartFile) {
            return multipartFile.getOriginalFilename();
        } else if (file instanceof File ioFile) {
            return ioFile.getName();
        }
        return "unknown";
    }

    /**
     * 处理路径
     *
     * @param path 路径
     * @return 处理路径
     */
    private String pretreatmentPath(String path) {
        if (StringConstants.SLASH.equals(path)) {
            return StringConstants.EMPTY;
        }
        return StrUtil.appendIfMissing(StrUtil.removePrefix(path, StringConstants.SLASH), StringConstants.SLASH);
    }

    /**
     * 删除实际文件
     *
     * @param file    文件
     * @param storage 存储配置
     */
    private void deleteFile(FileDO file, StorageDO storage) {
        Long storageId = storage.getId();
        if (FileTypeEnum.DIR.equals(file.getType())) {
            boolean exists = baseMapper.lambdaQuery()
                .eq(FileDO::getParentPath, file.getPath())
                .eq(FileDO::getStorageId, storageId)
                .exists();
            CheckUtils.throwIf(exists, "文件夹 [{}] 不为空，请先删除文件夹下的内容", file.getName());
            return;
        }
        FileInfo fileInfo = file.toFileInfo(storage);
        if (Boolean.TRUE.equals(storage.getRecycleBinEnabled())) {
            fileInfo.setId(file.getId().toString());
            fileStorageService.move(fileInfo).setPath(storage.getRecycleBinPath() + fileInfo.getPath()).move();
        } else {
            fileStorageService.delete(fileInfo);
        }
    }
}
