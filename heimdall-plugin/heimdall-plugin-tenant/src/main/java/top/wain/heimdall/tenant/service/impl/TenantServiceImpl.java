package top.wain.heimdall.tenant.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.alicp.jetcache.anno.Cached;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import me.ahoo.cosid.provider.IdGeneratorProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.wain.heimdall.common.api.system.RoleApi;
import top.wain.heimdall.common.api.system.RoleMenuApi;
import top.wain.heimdall.common.api.tenant.TenantDataApi;
import top.wain.heimdall.common.config.TenantExtensionProperties;
import top.wain.heimdall.common.constant.CacheConstants;
import top.wain.heimdall.common.enums.DisEnableStatusEnum;
import top.wain.heimdall.common.enums.RoleCodeEnum;
import top.wain.heimdall.common.model.dto.TenantDTO;
import top.wain.heimdall.common.util.CrudHelper;
import top.wain.heimdall.tenant.constant.TenantCacheConstants;
import top.wain.heimdall.tenant.constant.TenantConstants;
import top.wain.heimdall.tenant.mapper.TenantMapper;
import top.wain.heimdall.tenant.model.entity.TenantDO;
import top.wain.heimdall.tenant.model.query.TenantQuery;
import top.wain.heimdall.tenant.model.req.TenantReq;
import top.wain.heimdall.tenant.model.resp.TenantDetailResp;
import top.wain.heimdall.tenant.model.resp.TenantResp;
import top.wain.heimdall.tenant.service.PackageService;
import top.wain.heimdall.tenant.service.TenantService;
import top.continew.starter.cache.redisson.util.RedisUtils;
import top.continew.starter.core.constant.StringConstants;
import top.continew.starter.core.util.validation.CheckUtils;
import top.continew.starter.data.service.impl.ServiceImpl;
import top.continew.starter.data.util.QueryWrapperHelper;
import top.continew.starter.extension.crud.model.query.PageQuery;
import top.continew.starter.extension.crud.model.resp.BasePageResp;
import top.continew.starter.extension.tenant.util.TenantUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 租户业务实现
 *
 * @author 小熊
 * @author WainZeng
 * @since 2024/11/26 17:20
 */
@Service
@RequiredArgsConstructor
public class TenantServiceImpl extends ServiceImpl<TenantMapper, TenantDO> implements TenantService {

    private final Map<String, TenantDataApi> tenantDataApiMap = SpringUtil.getBeansOfType(TenantDataApi.class);
    private final TenantExtensionProperties tenantExtensionProperties;
    private final PackageService packageService;
    private final IdGeneratorProvider idGeneratorProvider;
    private final RoleMenuApi roleMenuApi;
    private final RoleApi roleApi;

    @Override
    public BasePageResp<TenantResp> page(TenantQuery query, PageQuery pageQuery) {
        QueryWrapper<TenantDO> queryWrapper = QueryWrapperHelper.build(query);
        CrudHelper.sort(queryWrapper, pageQuery, TenantDO.class);
        IPage<TenantDO> page = baseMapper.selectPage(new Page<>(pageQuery.getPage(), pageQuery
            .getSize()), queryWrapper);
        List<TenantResp> list = BeanUtil.copyToList(page.getRecords(), TenantResp.class);
        CrudHelper.fillAll(list);
        return new BasePageResp<>(list, page.getTotal());
    }

    @Override
    public TenantDetailResp get(Long id) {
        TenantDO entity = baseMapper.selectById(id);
        CheckUtils.throwIfNull(entity, "数据不存在");
        TenantDetailResp resp = BeanUtil.toBean(entity, TenantDetailResp.class);
        CrudHelper.fill(resp);
        return resp;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(TenantReq req) {
        this.checkNameRepeat(req.getName(), null);
        this.checkDomainRepeat(req.getDomain(), null);
        // 检查套餐
        packageService.checkStatus(req.getPackageId());
        // 生成租户编码
        req.setCode(this.generateCode());
        // 新增信息
        TenantDO entity = BeanUtil.copyProperties(req, TenantDO.class);
        baseMapper.insert(entity);
        Long id = entity.getId();
        // 初始化租户数据
        req.setId(id);
        tenantDataApiMap.forEach((key, value) -> value.init(BeanUtil.copyProperties(req, TenantDTO.class)));
        return id;
    }

    @Override
    public void update(TenantReq req, Long id) {
        this.checkNameRepeat(req.getName(), id);
        this.checkDomainRepeat(req.getDomain(), id);
        TenantDO tenant = this.getById(id);
        CheckUtils.throwIfNull(tenant, "数据不存在");
        // 变更套餐
        if (!tenant.getPackageId().equals(req.getPackageId())) {
            packageService.checkStatus(req.getPackageId());
        }
        BeanUtil.copyProperties(req, tenant, CopyOptions.create().ignoreNullValue());
        baseMapper.updateById(tenant);
        RedisUtils.deleteByPattern(TenantCacheConstants.TENANT_KEY_PREFIX + StringConstants.ASTERISK);
    }

    @Override
    public void delete(Long id) {
        // 在租户中执行数据清除
        TenantUtils.execute(id, () -> tenantDataApiMap.forEach((key, value) -> value.clear()));
        baseMapper.deleteById(id);
        RedisUtils.deleteByPattern(TenantCacheConstants.TENANT_KEY_PREFIX + StringConstants.ASTERISK);
    }

    @Override
    @Cached(name = TenantCacheConstants.TENANT_KEY_PREFIX, key = "#domain")
    public Long getIdByDomain(String domain) {
        return baseMapper.lambdaQuery()
            .select(TenantDO::getId)
            .eq(TenantDO::getDomain, domain)
            .oneOpt()
            .map(TenantDO::getId)
            .orElse(null);
    }

    @Override
    @Cached(name = TenantCacheConstants.TENANT_KEY_PREFIX, key = "#code")
    public Long getIdByCode(String code) {
        return baseMapper.lambdaQuery()
            .select(TenantDO::getId)
            .eq(TenantDO::getCode, code)
            .oneOpt()
            .map(TenantDO::getId)
            .orElse(null);
    }

    @Override
    public void checkStatus(Long id) {
        // 默认租户
        if (tenantExtensionProperties.getDefaultTenantId().equals(id)) {
            return;
        }
        TenantDO tenant = this.getById(id);
        CheckUtils.throwIfEqual(DisEnableStatusEnum.DISABLE, tenant.getStatus(), "租户已被禁用");
        CheckUtils.throwIf(tenant.getExpireTime() != null && tenant.getExpireTime()
            .isBefore(LocalDateTime.now()), "租户已过期");
        // 检查套餐
        packageService.checkStatus(tenant.getPackageId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateTenantMenu(List<Long> newMenuIds, Long packageId) {
        List<Long> tenantIdList = this.listIdByPackageId(packageId);
        if (CollUtil.isEmpty(tenantIdList)) {
            return;
        }
        // 所有租户角色：删除旧菜单
        tenantIdList.forEach(tenantId -> TenantUtils.execute(tenantId, () -> {
            // 删除旧菜单
            roleMenuApi.deleteByNotInMenuIds(newMenuIds);
            // 更新在线用户上下文
            Set<Long> roleIdSet = roleMenuApi.listRoleIdByNotInMenuIds(newMenuIds);
            roleIdSet.forEach(roleApi::updateUserContext);
        }));
        // 租户管理员：新增菜单
        tenantIdList.forEach(tenantId -> TenantUtils.execute(tenantId, () -> {
            Long roleId = roleApi.getIdByCode(RoleCodeEnum.TENANT_ADMIN.getCode());
            roleMenuApi.add(newMenuIds, roleId);
            // 更新在线用户上下文
            roleApi.updateUserContext(roleId);
        }));
        // 删除缓存
        RedisUtils.deleteByPattern(CacheConstants.ROLE_MENU_KEY_PREFIX + StringConstants.ASTERISK);
    }

    @Override
    public Long countByPackageIds(List<Long> packageIds) {
        return baseMapper.lambdaQuery().in(TenantDO::getPackageId, packageIds).count();
    }

    /**
     * 检查名称是否重复
     *
     * @param name 名称
     * @param id   ID
     */
    private void checkNameRepeat(String name, Long id) {
        CheckUtils.throwIf(baseMapper.lambdaQuery()
            .eq(TenantDO::getName, name)
            .ne(id != null, TenantDO::getId, id)
            .exists(), "名称为 [{}] 的租户已存在", name);
    }

    /**
     * 检查域名是否重复
     *
     * @param domain 域名
     * @param id     ID
     */
    private void checkDomainRepeat(String domain, Long id) {
        CheckUtils.throwIf(baseMapper.lambdaQuery()
            .eq(TenantDO::getDomain, domain)
            .ne(id != null, TenantDO::getId, id)
            .exists(), "域名为 [{}] 的租户已存在", domain);
    }

    /**
     * 生成租户编码
     *
     * @return 租户编码
     */
    private String generateCode() {
        String code;
        do {
            code = idGeneratorProvider.getRequired(TenantConstants.CODE_GENERATOR_KEY).generateAsString();
        } while (baseMapper.lambdaQuery().eq(TenantDO::getCode, code).exists());
        return code;
    }

    /**
     * 根据套餐 ID 查询租户 ID 列表
     *
     * @param id 套餐 ID
     * @return 租户 ID 列表
     */
    private List<Long> listIdByPackageId(Long id) {
        return baseMapper.lambdaQuery()
            .select(TenantDO::getId)
            .eq(TenantDO::getPackageId, id)
            .list()
            .stream()
            .map(TenantDO::getId)
            .toList();
    }
}
