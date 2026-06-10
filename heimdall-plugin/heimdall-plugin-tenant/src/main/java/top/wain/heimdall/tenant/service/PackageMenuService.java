package top.wain.heimdall.tenant.service;

import java.util.List;

/**
 * 套餐和菜单关联业务接口
 *
 * @author WainZeng
 * @since 2025/7/13 20:44
 */
public interface PackageMenuService {

    /**
     * 新增
     *
     * @param menuIds   菜单 ID 列表
     * @param packageId 套餐 ID
     * @return 是否成功（true：成功；false：无变更/失败）
     */
    boolean add(List<Long> menuIds, Long packageId);

    /**
     * 根据套餐 ID 查询
     *
     * @param packageId 套餐 ID
     * @return 菜单 ID 列表
     */
    List<Long> listMenuIdsByPackageId(Long packageId);
}
