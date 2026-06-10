package top.wain.heimdall.tenant.service.impl;

import cn.hutool.core.collection.CollUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.wain.heimdall.tenant.mapper.PackageMenuMapper;
import top.wain.heimdall.tenant.model.entity.PackageMenuDO;
import top.wain.heimdall.tenant.service.PackageMenuService;
import top.continew.starter.core.util.CollUtils;

import java.util.List;

/**
 * 套餐和菜单关联业务实现
 *
 * @author WainZeng
 * @since 2025/7/13 20:45
 */
@Service
@RequiredArgsConstructor
public class PackageMenuServiceImpl implements PackageMenuService {

    private final PackageMenuMapper baseMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean add(List<Long> menuIds, Long packageId) {
        // 检查是否有变更
        List<Long> oldMenuIdList = baseMapper.lambdaQuery()
            .select(PackageMenuDO::getMenuId)
            .eq(PackageMenuDO::getPackageId, packageId)
            .list()
            .stream()
            .map(PackageMenuDO::getMenuId)
            .toList();
        if (CollUtil.isEmpty(CollUtil.disjunction(menuIds, oldMenuIdList))) {
            return false;
        }
        // 删除原有关联
        baseMapper.lambdaUpdate().eq(PackageMenuDO::getPackageId, packageId).remove();
        // 保存最新关联
        List<PackageMenuDO> newList = CollUtils.mapToList(menuIds, menuId -> new PackageMenuDO(packageId, menuId));
        return baseMapper.insertBatch(newList);
    }

    @Override
    public List<Long> listMenuIdsByPackageId(Long packageId) {
        return baseMapper.lambdaQuery()
            .select(PackageMenuDO::getMenuId)
            .eq(PackageMenuDO::getPackageId, packageId)
            .list()
            .stream()
            .map(PackageMenuDO::getMenuId)
            .toList();
    }
}
