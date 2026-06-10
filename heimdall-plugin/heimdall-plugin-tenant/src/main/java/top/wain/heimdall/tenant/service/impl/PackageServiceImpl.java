package top.wain.heimdall.tenant.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import top.wain.heimdall.common.enums.DisEnableStatusEnum;
import top.wain.heimdall.common.util.CrudHelper;
import top.wain.heimdall.tenant.mapper.PackageMapper;
import top.wain.heimdall.tenant.model.entity.PackageDO;
import top.wain.heimdall.tenant.model.query.PackageQuery;
import top.wain.heimdall.tenant.model.req.PackageReq;
import top.wain.heimdall.tenant.model.resp.PackageDetailResp;
import top.wain.heimdall.tenant.model.resp.PackageResp;
import top.wain.heimdall.tenant.service.PackageMenuService;
import top.wain.heimdall.tenant.service.PackageService;
import top.wain.heimdall.tenant.service.TenantService;
import top.continew.starter.core.util.validation.CheckUtils;
import top.continew.starter.data.service.impl.ServiceImpl;
import top.continew.starter.data.util.QueryWrapperHelper;
import top.continew.starter.extension.crud.model.query.PageQuery;
import top.continew.starter.extension.crud.model.query.SortQuery;
import top.continew.starter.extension.crud.model.resp.BasePageResp;
import top.continew.starter.extension.crud.model.resp.LabelValueResp;
import top.continew.starter.extension.crud.model.resp.PageResp;

import java.util.List;

/**
 * 套餐业务实现
 *
 * @author 小熊
 * @author WainZeng
 * @since 2024/11/26 11:25
 */
@Service
@RequiredArgsConstructor
public class PackageServiceImpl extends ServiceImpl<PackageMapper, PackageDO> implements PackageService {

    private final PackageMenuService packageMenuService;
    @Lazy
    @Resource
    private TenantService tenantService;

    @Override
    public BasePageResp<PackageResp> page(PackageQuery query, PageQuery pageQuery) {
        QueryWrapper<PackageDO> queryWrapper = QueryWrapperHelper.build(query);
        CrudHelper.sort(queryWrapper, pageQuery, PackageDO.class);
        IPage<PackageDO> page = baseMapper.selectPage(new Page<>(pageQuery.getPage(), pageQuery
            .getSize()), queryWrapper);
        PageResp<PackageResp> pageResp = PageResp.build(page, PackageResp.class);
        pageResp.getList().forEach(CrudHelper::fill);
        return pageResp;
    }

    @Override
    public List<PackageResp> list(PackageQuery query, SortQuery sortQuery) {
        QueryWrapper<PackageDO> queryWrapper = QueryWrapperHelper.build(query);
        CrudHelper.sort(queryWrapper, sortQuery, PackageDO.class);
        List<PackageDO> entityList = baseMapper.selectList(queryWrapper);
        List<PackageResp> list = BeanUtil.copyToList(entityList, PackageResp.class);
        list.forEach(CrudHelper::fill);
        return list;
    }

    @Override
    public PackageDetailResp get(Long id) {
        PackageDO entity = baseMapper.selectById(id);
        CheckUtils.throwIfNull(entity, "数据不存在");
        PackageDetailResp resp = BeanUtil.toBean(entity, PackageDetailResp.class);
        CrudHelper.fill(resp);
        return resp;
    }

    @Override
    public Long create(PackageReq req) {
        this.checkNameRepeat(req.getName(), null);
        // 新增信息
        PackageDO entity = BeanUtil.copyProperties(req, PackageDO.class);
        baseMapper.insert(entity);
        Long id = entity.getId();
        // 保存套餐和菜单关联
        packageMenuService.add(req.getMenuIds(), id);
        return id;
    }

    @Override
    public void update(PackageReq req, Long id) {
        this.checkNameRepeat(req.getName(), id);
        // 更新信息
        PackageDO oldEntity = this.getById(id);
        CheckUtils.throwIfNull(oldEntity, "数据不存在");
        BeanUtil.copyProperties(req, oldEntity, CopyOptions.create().ignoreNullValue());
        baseMapper.updateById(oldEntity);
        // 保存套餐和菜单关联
        boolean isSaveMenuSuccess = packageMenuService.add(req.getMenuIds(), id);
        if (!isSaveMenuSuccess) {
            return;
        }
        // 更新租户菜单
        tenantService.updateTenantMenu(req.getMenuIds(), id);
    }

    @Override
    public void delete(Long id) {
        CheckUtils.throwIf(tenantService.countByPackageIds(List.of(id)) > 0, "所选套餐存在关联租户，不允许删除");
        baseMapper.deleteById(id);
    }

    @Override
    public List<LabelValueResp> dict(PackageQuery query, SortQuery sortQuery) {
        QueryWrapper<PackageDO> queryWrapper = QueryWrapperHelper.build(query);
        CrudHelper.sort(queryWrapper, sortQuery, PackageDO.class);
        List<PackageDO> entityList = baseMapper.selectList(queryWrapper);
        return CrudHelper.buildDict(entityList, PackageDO.class);
    }

    @Override
    public void checkStatus(Long id) {
        PackageDO entity = this.getById(id);
        CheckUtils.throwIfEqual(DisEnableStatusEnum.DISABLE, entity.getStatus(), "租户套餐已被禁用");
    }

    /**
     * 名称是否存在
     *
     * @param name 名称
     * @param id   ID
     */
    private void checkNameRepeat(String name, Long id) {
        CheckUtils.throwIf(baseMapper.lambdaQuery()
            .eq(PackageDO::getName, name)
            .ne(id != null, PackageDO::getId, id)
            .exists(), "名称为 [{}] 的套餐已存在", name);
    }
}
