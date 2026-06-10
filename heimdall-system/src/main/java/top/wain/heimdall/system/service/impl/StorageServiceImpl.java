package top.wain.heimdall.system.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.dromara.x.file.storage.core.FileStorageProperties;
import org.dromara.x.file.storage.core.FileStorageService;
import org.dromara.x.file.storage.core.FileStorageServiceBuilder;
import org.dromara.x.file.storage.core.platform.FileStorage;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.wain.heimdall.common.enums.DisEnableStatusEnum;
import top.wain.heimdall.common.model.req.CommonStatusUpdateReq;
import top.wain.heimdall.common.util.CrudHelper;
import top.wain.heimdall.common.util.SecureUtils;
import top.wain.heimdall.system.enums.StorageTypeEnum;
import top.wain.heimdall.system.mapper.StorageMapper;
import top.wain.heimdall.system.model.entity.StorageDO;
import top.wain.heimdall.system.model.query.StorageQuery;
import top.wain.heimdall.system.model.req.StorageReq;
import top.wain.heimdall.system.model.resp.StorageResp;
import top.wain.heimdall.system.service.FileService;
import top.wain.heimdall.system.service.StorageService;
import top.continew.starter.core.util.ExceptionUtils;
import top.continew.starter.core.util.SpringWebUtils;
import top.continew.starter.core.util.validation.CheckUtils;
import top.continew.starter.core.util.validation.ValidationUtils;
import top.continew.starter.data.service.impl.ServiceImpl;
import top.continew.starter.data.util.QueryWrapperHelper;
import top.continew.starter.extension.crud.model.query.SortQuery;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 存储业务实现
 *
 * @author WainZeng
 * @since 2023/12/26 22:09
 */
@Service
@RequiredArgsConstructor
public class StorageServiceImpl extends ServiceImpl<StorageMapper, StorageDO> implements StorageService {

    private final FileStorageService fileStorageService;
    @Resource
    private FileService fileService;

    @Override
    public List<StorageResp> list(StorageQuery query, SortQuery sortQuery) {
        QueryWrapper<StorageDO> queryWrapper = QueryWrapperHelper.build(query);
        CrudHelper.sort(queryWrapper, sortQuery, StorageDO.class);
        List<StorageDO> entityList = baseMapper.selectList(queryWrapper);
        List<StorageResp> respList = BeanUtil.copyToList(entityList, StorageResp.class);
        CrudHelper.fillAll(respList);
        return respList;
    }

    @Override
    public StorageResp get(Long id) {
        StorageDO entity = baseMapper.selectById(id);
        CheckUtils.throwIfNull(entity, "数据不存在");
        StorageResp resp = BeanUtil.toBean(entity, StorageResp.class);
        CrudHelper.fill(resp);
        return resp;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(StorageReq req) {
        // 解密密钥
        if (StorageTypeEnum.OSS.equals(req.getType())) {
            ValidationUtils.throwIfBlank(req.getSecretKey(), "Secret Key不能为空");
            req.setSecretKey(this.decryptSecretKey(req.getSecretKey(), null));
        }
        // 指定配置参数校验及预处理
        StorageTypeEnum storageType = req.getType();
        storageType.validate(req);
        storageType.pretreatment(req);
        // 校验存储编码
        this.checkCodeRepeat(req.getCode(), null);
        // 需要独立操作来指定默认存储
        req.setIsDefault(false);
        // 加载存储引擎
        if (DisEnableStatusEnum.ENABLE.equals(req.getStatus())) {
            this.load(BeanUtil.copyProperties(req, StorageDO.class));
        }
        StorageDO entity = BeanUtil.copyProperties(req, StorageDO.class);
        baseMapper.insert(entity);
        return entity.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(StorageReq req, Long id) {
        // 解密密钥
        StorageDO oldStorage = this.getById(id);
        if (StorageTypeEnum.OSS.equals(req.getType())) {
            req.setSecretKey(this.decryptSecretKey(req.getSecretKey(), oldStorage));
        }
        // 校验存储类型、存储编码、回收站配置、状态
        CheckUtils.throwIfNotEqual(req.getType(), oldStorage.getType(), "不允许修改存储类型");
        CheckUtils.throwIfNotEqual(req.getCode(), oldStorage.getCode(), "不允许修改存储编码");
        CheckUtils.throwIfNotEqual(req.getRecycleBinEnabled(), oldStorage.getRecycleBinEnabled(), "不允许修改回收站配置");
        CheckUtils.throwIfNotEqual(req.getRecycleBinPath(), oldStorage.getRecycleBinPath(), "不允许修改回收站配置");
        DisEnableStatusEnum newStatus = req.getStatus();
        CheckUtils.throwIf(Boolean.TRUE.equals(oldStorage.getIsDefault()) && DisEnableStatusEnum.DISABLE
            .equals(newStatus), "[{}] 是默认存储，不允许禁用", oldStorage.getName());
        // 指定配置参数校验及预处理
        StorageTypeEnum storageType = req.getType();
        storageType.validate(req);
        storageType.pretreatment(req);
        // 卸载存储引擎
        this.unload(oldStorage);
        // 加载存储引擎
        if (DisEnableStatusEnum.ENABLE.equals(newStatus)) {
            BeanUtil.copyProperties(req, oldStorage);
            this.load(oldStorage);
        }
        BeanUtil.copyProperties(req, oldStorage, CopyOptions.create().ignoreNullValue());
        baseMapper.updateById(oldStorage);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(List<Long> ids) {
        CheckUtils.throwIf(fileService.countByStorageIds(ids) > 0, "所选存储存在文件或文件夹关联，请删除后重试");
        List<StorageDO> storageList = baseMapper.lambdaQuery().in(StorageDO::getId, ids).list();
        storageList.forEach(storage -> {
            CheckUtils.throwIfEqual(Boolean.TRUE, storage.getIsDefault(), "[{}] 是默认存储，不允许删除", storage.getName());
            // 卸载存储引擎
            this.unload(storage);
        });
        baseMapper.deleteByIds(ids);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(CommonStatusUpdateReq req, Long id) {
        StorageDO storage = this.getById(id);
        // 状态未改变
        DisEnableStatusEnum newStatus = req.getStatus();
        if (storage.getStatus().equals(newStatus)) {
            return;
        }
        // 修改状态
        baseMapper.lambdaUpdate().eq(StorageDO::getId, id).set(StorageDO::getStatus, newStatus).update();
        // 加载、卸载存储引擎
        switch (newStatus) {
            case ENABLE:
                this.load(storage);
                break;
            case DISABLE:
                CheckUtils.throwIfEqual(Boolean.TRUE, storage.getIsDefault(), "[{}] 是默认存储，不允许禁用", storage.getName());
                this.unload(storage);
                break;
            default:
                break;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setDefaultStorage(Long id) {
        StorageDO storage = this.getById(id);
        if (Boolean.TRUE.equals(storage.getIsDefault())) {
            return;
        }
        // 启用状态才能设为默认存储
        CheckUtils.throwIfEqual(DisEnableStatusEnum.DISABLE, storage.getStatus(), "请先启用所选存储");
        baseMapper.lambdaUpdate().eq(StorageDO::getIsDefault, true).set(StorageDO::getIsDefault, false).update();
        baseMapper.lambdaUpdate().eq(StorageDO::getId, id).set(StorageDO::getIsDefault, true).update();
    }

    @Override
    public StorageDO getDefaultStorage() {
        StorageDO storage = baseMapper.lambdaQuery().eq(StorageDO::getIsDefault, true).one();
        CheckUtils.throwIfNull(storage, "请先指定默认存储");
        return storage;
    }

    @Override
    public StorageDO getByCode(String code) {
        if (StrUtil.isBlank(code)) {
            return this.getDefaultStorage();
        }
        StorageDO storage = baseMapper.lambdaQuery().eq(StorageDO::getCode, code).one();
        CheckUtils.throwIfNotExists(storage, "存储", "code", code);
        return storage;
    }

    @Override
    public void load(StorageDO storage) {
        CopyOnWriteArrayList<FileStorage> fileStorageList = fileStorageService.getFileStorageList();
        switch (storage.getType()) {
            case LOCAL -> {
                FileStorageProperties.LocalPlusConfig config = new FileStorageProperties.LocalPlusConfig();
                config.setPlatform(storage.getCode());
                config.setStoragePath(storage.getBucketName());
                fileStorageList.addAll(FileStorageServiceBuilder.buildLocalPlusFileStorage(Collections
                    .singletonList(config)));
                // 注册资源映射
                SpringWebUtils.registerResourceHandler(MapUtil.of(URLUtil.url(storage.getDomain()).getPath(), storage
                    .getBucketName()));
            }
            case OSS -> {
                FileStorageProperties.AmazonS3Config config = new FileStorageProperties.AmazonS3Config();
                config.setPlatform(storage.getCode());
                config.setAccessKey(storage.getAccessKey());
                config.setSecretKey(storage.getSecretKey());
                config.setEndPoint(storage.getEndpoint());
                config.setBucketName(storage.getBucketName());
                fileStorageList.addAll(FileStorageServiceBuilder.buildAmazonS3FileStorage(Collections
                    .singletonList(config), null));
            }
            default -> throw new IllegalArgumentException("不支持的存储类型：%s".formatted(storage.getType()));
        }
    }

    @Override
    public void unload(StorageDO storage) {
        FileStorage fileStorage = fileStorageService.getFileStorage(storage.getCode());
        if (fileStorage == null) {
            return;
        }
        CopyOnWriteArrayList<FileStorage> fileStorageList = fileStorageService.getFileStorageList();
        fileStorageList.remove(fileStorage);
        fileStorage.close();
        // 本地存储引擎需要移除资源映射
        if (StorageTypeEnum.LOCAL.equals(storage.getType())) {
            SpringWebUtils.deRegisterResourceHandler(MapUtil.of(URLUtil.url(storage.getDomain()).getPath(), storage
                .getBucketName()));
        }
    }

    /**
     * 解密 SecretKey
     *
     * @param encryptSecretKey 加密的 SecretKey
     * @param oldStorage       旧存储配置
     * @return 解密后的 SecretKey
     */
    private String decryptSecretKey(String encryptSecretKey, StorageDO oldStorage) {
        // 修改时，SecretKey 为空将不更改
        if (oldStorage != null && StrUtil.isBlank(encryptSecretKey)) {
            return oldStorage.getSecretKey();
        }
        // 解密
        String secretKey = ExceptionUtils.exToNull(() -> SecureUtils.decryptByRsaPrivateKey(encryptSecretKey));
        ValidationUtils.throwIfNull(secretKey, "私有密钥解密失败");
        ValidationUtils.throwIf(secretKey.length() > 255, "私有密钥长度不能超过 255 个字符");
        return secretKey;
    }

    /**
     * 检查编码是否重复
     *
     * @param code 编码
     * @param id   ID
     */
    private void checkCodeRepeat(String code, Long id) {
        CheckUtils.throwIf(baseMapper.lambdaQuery()
            .eq(StorageDO::getCode, code)
            .ne(id != null, StorageDO::getId, id)
            .exists(), "编码为 [{}] 的存储配置已存在", code);
    }
}
