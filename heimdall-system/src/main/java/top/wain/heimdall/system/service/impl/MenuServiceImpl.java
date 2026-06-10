package top.wain.heimdall.system.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.tree.Tree;
import cn.hutool.core.util.StrUtil;
import com.alicp.jetcache.anno.Cached;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.wain.heimdall.common.config.TenantExtensionProperties;
import top.wain.heimdall.common.constant.CacheConstants;
import top.wain.heimdall.common.enums.DisEnableStatusEnum;
import top.wain.heimdall.common.enums.RoleCodeEnum;
import top.wain.heimdall.common.util.CrudHelper;
import top.wain.heimdall.system.constant.SystemConstants;
import top.wain.heimdall.system.enums.MenuTypeEnum;
import top.wain.heimdall.system.mapper.MenuMapper;
import top.wain.heimdall.system.model.entity.MenuDO;
import top.wain.heimdall.system.model.query.MenuQuery;
import top.wain.heimdall.system.model.req.MenuReq;
import top.wain.heimdall.system.model.resp.MenuResp;
import top.wain.heimdall.system.service.MenuService;
import top.wain.heimdall.system.service.RoleService;
import top.continew.starter.cache.redisson.util.RedisUtils;
import top.continew.starter.core.constant.StringConstants;
import top.continew.starter.core.util.CollUtils;
import top.continew.starter.core.util.validation.CheckUtils;
import top.continew.starter.data.service.impl.ServiceImpl;
import top.continew.starter.data.util.QueryWrapperHelper;
import top.continew.starter.extension.crud.model.query.SortQuery;
import top.continew.starter.extension.tenant.context.TenantContextHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 菜单业务实现
 *
 * @author WainZeng
 * @since 2023/2/15 20:30
 */
@Service
@RequiredArgsConstructor
public class MenuServiceImpl extends ServiceImpl<MenuMapper, MenuDO> implements MenuService {

    private final TenantExtensionProperties tenantExtensionProperties;
    @Lazy
    @Resource
    private RoleService roleService;

    @Override
    public List<Tree<Long>> tree(MenuQuery query, SortQuery sortQuery, boolean isSimple) {
        // 租户模式下排除非租户菜单
        if (TenantContextHolder.isTenantEnabled() && !tenantExtensionProperties.isDefaultTenant()) {
            query = query == null ? new MenuQuery() : query;
            query.setExcludeMenuIdList(this.listExcludeTenantMenu());
        }
        QueryWrapper<MenuDO> queryWrapper = QueryWrapperHelper.build(query);
        CrudHelper.sort(queryWrapper, sortQuery, MenuDO.class);
        List<MenuDO> entityList = baseMapper.selectList(queryWrapper);
        List<MenuResp> list = BeanUtil.copyToList(entityList, MenuResp.class);
        list.forEach(CrudHelper::fill);
        return CrudHelper.buildTree(list, MenuResp.class, isSimple);
    }

    @Override
    public MenuResp get(Long id) {
        MenuDO entity = baseMapper.selectById(id);
        CheckUtils.throwIfNull(entity, "数据不存在");
        MenuResp resp = BeanUtil.toBean(entity, MenuResp.class);
        CrudHelper.fill(resp);
        return resp;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(MenuReq req) {
        this.checkTitleRepeat(req.getTitle(), req.getParentId(), null);
        // 目录和菜单的组件名称不能重复
        if (!MenuTypeEnum.BUTTON.equals(req.getType())) {
            this.checkNameRepeat(req.getName(), null);
        }
        // 目录类型菜单，默认为 Layout
        if (MenuTypeEnum.DIR.equals(req.getType())) {
            req.setComponent(StrUtil.blankToDefault(req.getComponent(), "Layout"));
        }
        MenuDO entity = BeanUtil.copyProperties(req, MenuDO.class);
        baseMapper.insert(entity);
        RedisUtils.deleteByPattern(CacheConstants.ROLE_MENU_KEY_PREFIX + StringConstants.ASTERISK);
        return entity.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(MenuReq req, Long id) {
        this.checkTitleRepeat(req.getTitle(), req.getParentId(), id);
        // 目录和菜单的组件名称不能重复
        if (!MenuTypeEnum.BUTTON.equals(req.getType())) {
            this.checkNameRepeat(req.getName(), id);
        }
        MenuDO oldMenu = super.getById(id);
        CheckUtils.throwIfNull(oldMenu, "数据不存在");
        CheckUtils.throwIfNotEqual(req.getType(), oldMenu.getType(), "不允许修改菜单类型");
        BeanUtil.copyProperties(req, oldMenu, CopyOptions.create().ignoreNullValue());
        baseMapper.updateById(oldMenu);
        RedisUtils.deleteByPattern(CacheConstants.ROLE_MENU_KEY_PREFIX + StringConstants.ASTERISK);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(List<Long> ids) {
        // 级联删除菜单（包含子菜单）
        List<Long> allDeleteIdList = this.listChildMenuIds(ids);
        baseMapper.deleteByIds(allDeleteIdList);
        RedisUtils.deleteByPattern(CacheConstants.ROLE_MENU_KEY_PREFIX + StringConstants.ASTERISK);
    }

    @Override
    public Set<String> listPermissionByUserId(Long userId) {
        return baseMapper.selectPermissionByUserId(userId);
    }

    @Override
    @Cached(key = "#roleId", name = CacheConstants.ROLE_MENU_KEY_PREFIX)
    public List<MenuResp> listByRoleId(Long roleId) {
        if (SystemConstants.SUPER_ADMIN_ROLE_ID.equals(roleId)) {
            // 超级管理员查询所有启用菜单
            List<MenuDO> allMenus = baseMapper.lambdaQuery().eq(MenuDO::getStatus, DisEnableStatusEnum.ENABLE).list();
            List<MenuResp> list = BeanUtil.copyToList(allMenus, MenuResp.class);
            list.forEach(CrudHelper::fill);
            return list;
        }
        List<MenuDO> menuList = baseMapper.selectListByRoleId(roleId);
        List<MenuResp> list = BeanUtil.copyToList(menuList, MenuResp.class);
        list.forEach(CrudHelper::fill);
        return list;
    }

    @Override
    public List<Long> listExcludeTenantMenu() {
        Long roleId = roleService.getIdByCode(RoleCodeEnum.TENANT_ADMIN.getCode());
        List<Long> allMenuIdList = CollUtils.mapToList(super.list(), MenuDO::getId);
        List<Long> menuIdList = CollUtils.mapToList(baseMapper.selectListByRoleId(roleId), MenuDO::getId);
        return CollUtil.disjunction(allMenuIdList, menuIdList).stream().toList();
    }

    @Override
    public List<Long> listChildMenuIds(List<Long> menuIds) {
        if (CollUtil.isEmpty(menuIds)) {
            return new ArrayList<>();
        }
        // 自身 ID
        List<Long> resultIds = new ArrayList<>(menuIds);
        // 查询直接子菜单 ID
        List<Long> childIdList = baseMapper.lambdaQuery()
            .select(MenuDO::getId)
            .in(MenuDO::getParentId, menuIds)
            .list()
            .stream()
            .map(MenuDO::getId)
            .toList();
        // 递归向下收集所有层级子菜单
        if (!childIdList.isEmpty()) {
            resultIds.addAll(this.listChildMenuIds(childIdList));
        }
        return resultIds;
    }

    /**
     * 检查标题是否重复
     */
    private void checkTitleRepeat(String title, Long parentId, Long id) {
        CheckUtils.throwIf(baseMapper.lambdaQuery()
            .eq(MenuDO::getTitle, title)
            .eq(MenuDO::getParentId, parentId)
            .ne(id != null, MenuDO::getId, id)
            .exists(), "标题为 [{}] 的菜单已存在", title);
    }

    /**
     * 检查组件名称是否重复
     */
    private void checkNameRepeat(String name, Long id) {
        CheckUtils.throwIf(baseMapper.lambdaQuery()
            .eq(MenuDO::getName, name)
            .ne(MenuDO::getType, MenuTypeEnum.BUTTON)
            .ne(id != null, MenuDO::getId, id)
            .exists(), "组件名称为 [{}] 的菜单已存在", name);
    }

}
