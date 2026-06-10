package top.wain.heimdall.system.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.wain.heimdall.common.util.CrudHelper;
import top.wain.heimdall.system.mapper.DictMapper;
import top.wain.heimdall.system.model.entity.DictDO;
import top.wain.heimdall.system.model.query.DictQuery;
import top.wain.heimdall.system.model.req.DictReq;
import top.wain.heimdall.system.model.resp.DictResp;
import top.wain.heimdall.system.service.DictItemService;
import top.wain.heimdall.system.service.DictService;
import top.continew.starter.core.util.CollUtils;
import top.continew.starter.core.util.validation.CheckUtils;
import top.continew.starter.data.service.impl.ServiceImpl;
import top.continew.starter.data.util.QueryWrapperHelper;
import top.continew.starter.extension.crud.model.query.SortQuery;
import top.continew.starter.extension.crud.model.resp.LabelValueResp;

import java.util.List;
import java.util.Optional;

/**
 * 字典业务实现
 *
 * @author WainZeng
 * @since 2023/9/11 21:29
 */
@Service
@RequiredArgsConstructor
public class DictServiceImpl extends ServiceImpl<DictMapper, DictDO> implements DictService {

    private final DictItemService dictItemService;

    @Override
    public List<DictResp> list(DictQuery query, SortQuery sortQuery) {
        QueryWrapper<DictDO> queryWrapper = QueryWrapperHelper.build(query);
        CrudHelper.sort(queryWrapper, sortQuery, DictDO.class);
        List<DictDO> entityList = baseMapper.selectList(queryWrapper);
        List<DictResp> respList = BeanUtil.copyToList(entityList, DictResp.class);
        CrudHelper.fillAll(respList);
        return respList;
    }

    @Override
    public DictResp get(Long id) {
        DictDO entity = baseMapper.selectById(id);
        CheckUtils.throwIfNull(entity, "数据不存在");
        DictResp resp = BeanUtil.toBean(entity, DictResp.class);
        CrudHelper.fill(resp);
        return resp;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(DictReq req) {
        this.checkNameRepeat(req.getName(), null);
        this.checkCodeRepeat(req.getCode(), null);
        DictDO entity = BeanUtil.copyProperties(req, DictDO.class);
        baseMapper.insert(entity);
        return entity.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(DictReq req, Long id) {
        this.checkNameRepeat(req.getName(), id);
        DictDO oldDict = this.getById(id);
        CheckUtils.throwIfNotEqual(req.getCode(), oldDict.getCode(), "不允许修改字典编码");
        BeanUtil.copyProperties(req, oldDict, CopyOptions.create().ignoreNullValue());
        baseMapper.updateById(oldDict);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(List<Long> ids) {
        List<DictDO> list = baseMapper.lambdaQuery()
            .select(DictDO::getName, DictDO::getIsSystem)
            .in(DictDO::getId, ids)
            .list();
        Optional<DictDO> isSystemData = list.stream().filter(DictDO::getIsSystem).findFirst();
        CheckUtils.throwIf(isSystemData::isPresent, "所选字典 [{}] 是系统内置字典，不允许删除", isSystemData.orElseGet(DictDO::new)
            .getName());
        dictItemService.deleteByDictIds(ids);
        baseMapper.deleteByIds(ids);
    }

    @Override
    public List<LabelValueResp> dict(DictQuery query, SortQuery sortQuery) {
        QueryWrapper<DictDO> queryWrapper = QueryWrapperHelper.build(query);
        CrudHelper.sort(queryWrapper, sortQuery, DictDO.class);
        List<DictDO> entityList = baseMapper.selectList(queryWrapper);
        return CollUtils.mapToList(entityList, entity -> new LabelValueResp(entity.getName(), entity.getCode()));
    }

    @Override
    public List<LabelValueResp> listEnumDict() {
        List<String> enumDictNameList = dictItemService.listEnumDictNames();
        return CollUtils.mapToList(enumDictNameList, name -> new LabelValueResp(name, name));
    }

    /**
     * 检查名称是否重复
     *
     * @param name 名称
     * @param id   ID
     */
    private void checkNameRepeat(String name, Long id) {
        CheckUtils.throwIf(baseMapper.lambdaQuery()
            .eq(DictDO::getName, name)
            .ne(id != null, DictDO::getId, id)
            .exists(), "名称为 [{}] 的字典已存在", name);
    }

    /**
     * 检查编码是否重复
     *
     * @param code 编码
     * @param id   ID
     */
    private void checkCodeRepeat(String code, Long id) {
        CheckUtils.throwIf(baseMapper.lambdaQuery()
            .eq(DictDO::getCode, code)
            .ne(id != null, DictDO::getId, id)
            .exists(), "编码为 [{}] 的字典已存在", code);
    }
}
