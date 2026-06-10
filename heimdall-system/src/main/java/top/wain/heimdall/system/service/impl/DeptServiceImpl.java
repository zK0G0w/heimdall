package top.wain.heimdall.system.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.tree.Tree;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.wain.heimdall.common.enums.DisEnableStatusEnum;
import top.wain.heimdall.common.util.CrudHelper;
import top.wain.heimdall.system.mapper.DeptMapper;
import top.wain.heimdall.system.model.entity.DeptDO;
import top.wain.heimdall.system.model.query.DeptQuery;
import top.wain.heimdall.system.model.req.DeptReq;
import top.wain.heimdall.system.model.resp.DeptResp;
import top.wain.heimdall.system.service.DeptService;
import top.wain.heimdall.system.service.RoleDeptService;
import top.wain.heimdall.system.service.UserService;
import top.continew.starter.core.util.validation.CheckUtils;
import top.continew.starter.data.enums.DatabaseType;
import top.continew.starter.data.service.impl.ServiceImpl;
import top.continew.starter.data.util.MetaUtils;
import top.continew.starter.data.util.QueryWrapperHelper;
import top.continew.starter.excel.util.ExcelUtils;
import top.continew.starter.extension.crud.model.query.SortQuery;

import javax.sql.DataSource;
import java.util.*;

/**
 * 部门业务实现
 *
 * @author WainZeng
 * @since 2023/1/22 17:55
 */
@Service
@RequiredArgsConstructor
public class DeptServiceImpl extends ServiceImpl<DeptMapper, DeptDO> implements DeptService {

    private final RoleDeptService roleDeptService;
    private final DataSource dataSource;
    @Lazy
    @Resource
    private UserService userService;

    @Override
    public List<Tree<Long>> tree(DeptQuery query, SortQuery sortQuery, boolean isSimple) {
        QueryWrapper<DeptDO> queryWrapper = QueryWrapperHelper.build(query);
        CrudHelper.sort(queryWrapper, sortQuery, DeptDO.class);
        List<DeptDO> entityList = baseMapper.selectList(queryWrapper);
        List<DeptResp> list = BeanUtil.copyToList(entityList, DeptResp.class);
        list.forEach(CrudHelper::fill);
        return CrudHelper.buildTree(list, DeptResp.class, isSimple);
    }

    @Override
    public DeptResp get(Long id) {
        DeptDO entity = baseMapper.selectById(id);
        CheckUtils.throwIfNull(entity, "数据不存在");
        DeptResp resp = BeanUtil.toBean(entity, DeptResp.class);
        CrudHelper.fill(resp);
        return resp;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(DeptReq req) {
        this.checkNameRepeat(req.getName(), req.getParentId(), null);
        req.setAncestors(this.getAncestors(req.getParentId()));
        DeptDO entity = BeanUtil.copyProperties(req, DeptDO.class);
        baseMapper.insert(entity);
        return entity.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(DeptReq req, Long id) {
        this.checkNameRepeat(req.getName(), req.getParentId(), id);
        DeptDO oldDept = super.getById(id);
        CheckUtils.throwIfNull(oldDept, "数据不存在");
        String oldName = oldDept.getName();
        DisEnableStatusEnum newStatus = req.getStatus();
        Long oldParentId = oldDept.getParentId();
        // 系统内置部门校验
        if (Boolean.TRUE.equals(oldDept.getIsSystem())) {
            CheckUtils.throwIfEqual(DisEnableStatusEnum.DISABLE, newStatus, "[{}] 是系统内置部门，不允许禁用", oldName);
            CheckUtils.throwIfNotEqual(req.getParentId(), oldParentId, "[{}] 是系统内置部门，不允许变更上级部门", oldName);
        }
        // 启用/禁用部门
        if (ObjectUtil.notEqual(newStatus, oldDept.getStatus())) {
            List<DeptDO> children = this.listChildren(id);
            long enabledChildrenCount = children.stream()
                .filter(d -> DisEnableStatusEnum.ENABLE.equals(d.getStatus()))
                .count();
            CheckUtils.throwIf(DisEnableStatusEnum.DISABLE
                .equals(newStatus) && enabledChildrenCount > 0, "禁用 [{}] 前，请先禁用其所有下级部门", oldName);
            DeptDO oldParentDept = this.getByParentId(oldParentId);
            CheckUtils.throwIf(DisEnableStatusEnum.ENABLE.equals(newStatus) && DisEnableStatusEnum.DISABLE
                .equals(oldParentDept.getStatus()), "启用 [{}] 前，请先启用其所有上级部门", oldName);
        }
        // 变更上级部门
        if (ObjectUtil.notEqual(req.getParentId(), oldParentId)) {
            String newAncestors = this.getAncestors(req.getParentId());
            req.setAncestors(newAncestors);
            this.updateChildrenAncestors(newAncestors, oldDept.getAncestors(), id);
        }
        BeanUtil.copyProperties(req, oldDept, CopyOptions.create().ignoreNullValue());
        baseMapper.updateById(oldDept);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(List<Long> ids) {
        List<DeptDO> list = baseMapper.lambdaQuery()
            .select(DeptDO::getName, DeptDO::getIsSystem)
            .in(DeptDO::getId, ids)
            .list();
        Optional<DeptDO> isSystemData = list.stream().filter(DeptDO::getIsSystem).findFirst();
        CheckUtils.throwIf(isSystemData::isPresent, "所选部门 [{}] 是系统内置部门，不允许删除", isSystemData.orElseGet(DeptDO::new)
            .getName());
        CheckUtils.throwIf(this.countChildren(ids) > 0, "所选部门存在下级部门，不允许删除");
        CheckUtils.throwIf(userService.countByDeptIds(ids) > 0, "所选部门存在用户关联，请解除关联后重试");
        // 删除角色和部门关联
        roleDeptService.deleteByDeptIds(ids);
        baseMapper.deleteByIds(ids);
    }

    @Override
    public void export(DeptQuery query, SortQuery sortQuery, HttpServletResponse response) {
        QueryWrapper<DeptDO> queryWrapper = QueryWrapperHelper.build(query);
        CrudHelper.sort(queryWrapper, sortQuery, DeptDO.class);
        List<DeptDO> entityList = baseMapper.selectList(queryWrapper);
        List<DeptResp> list = BeanUtil.copyToList(entityList, DeptResp.class);
        list.forEach(CrudHelper::fill);
        ExcelUtils.export(list, "导出数据", DeptResp.class, response);
    }

    @Override
    public List<DeptDO> listChildren(Long id) {
        DatabaseType databaseType = MetaUtils.getDatabaseTypeOrDefault(dataSource, DatabaseType.MYSQL);
        return baseMapper.lambdaQuery().apply(databaseType.findInSet(id, "ancestors")).list();
    }

    @Override
    public List<DeptDO> listByNames(List<String> list) {
        if (CollUtil.isEmpty(list)) {
            return Collections.emptyList();
        }
        return this.list(Wrappers.<DeptDO>lambdaQuery().in(DeptDO::getName, list));
    }

    @Override
    public int countByNames(Set<String> deptNames) {
        if (CollUtil.isEmpty(deptNames)) {
            return 0;
        }
        return (int)this.count(Wrappers.<DeptDO>lambdaQuery().in(DeptDO::getName, deptNames));
    }

    /**
     * 检查名称是否重复
     */
    private void checkNameRepeat(String name, Long parentId, Long id) {
        CheckUtils.throwIf(baseMapper.lambdaQuery()
            .eq(DeptDO::getName, name)
            .eq(DeptDO::getParentId, parentId)
            .ne(id != null, DeptDO::getId, id)
            .exists(), "名称为 [{}] 的部门已存在", name);
    }

    /**
     * 获取祖级列表
     */
    private String getAncestors(Long parentId) {
        DeptDO parentDept = this.getByParentId(parentId);
        return "%s,%s".formatted(parentDept.getAncestors(), parentId);
    }

    /**
     * 根据上级部门 ID 查询
     */
    private DeptDO getByParentId(Long parentId) {
        DeptDO parentDept = baseMapper.selectById(parentId);
        CheckUtils.throwIfNull(parentDept, "上级部门不存在");
        return parentDept;
    }

    /**
     * 查询子部门数量
     */
    private Long countChildren(List<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return 0L;
        }
        DatabaseType databaseType = MetaUtils.getDatabaseTypeOrDefault(dataSource, DatabaseType.MYSQL);
        return ids.stream()
            .mapToLong(id -> baseMapper.lambdaQuery().apply(databaseType.findInSet(id, "ancestors")).count())
            .sum();
    }

    /**
     * 更新子部门祖级列表
     */
    private void updateChildrenAncestors(String newAncestors, String oldAncestors, Long id) {
        List<DeptDO> children = this.listChildren(id);
        if (CollUtil.isEmpty(children)) {
            return;
        }
        List<DeptDO> list = new ArrayList<>(children.size());
        for (DeptDO child : children) {
            DeptDO dept = new DeptDO();
            dept.setId(child.getId());
            dept.setAncestors(child.getAncestors().replaceFirst(oldAncestors, newAncestors));
            list.add(dept);
        }
        baseMapper.updateById(list);
    }
}
