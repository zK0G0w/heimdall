package top.wain.heimdall.system.config.file;

import cn.hutool.core.collection.CollUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import top.wain.heimdall.common.enums.DisEnableStatusEnum;
import top.wain.heimdall.system.model.entity.StorageDO;
import top.wain.heimdall.system.service.StorageService;

import java.util.List;

/**
 * 文件存储配置加载器
 *
 * @author WainZeng
 * @since 2023/12/24 22:31
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FileStorageConfigLoader implements ApplicationRunner {

    private final StorageService storageService;

    @Override
    public void run(ApplicationArguments args) {
        List<StorageDO> list = storageService.lambdaQuery().eq(StorageDO::getStatus, DisEnableStatusEnum.ENABLE).list();
        if (CollUtil.isEmpty(list)) {
            return;
        }
        list.forEach(storageService::load);
    }
}
