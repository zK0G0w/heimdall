package top.wain.heimdall.system.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.wain.heimdall.common.constant.CacheConstants;
import top.wain.heimdall.common.util.CrudHelper;
import top.wain.heimdall.system.mapper.DictItemMapper;
import top.wain.heimdall.system.model.entity.DictItemDO;
import top.wain.heimdall.system.model.query.DictItemQuery;
import top.wain.heimdall.system.model.req.DictItemReq;
import top.wain.heimdall.system.model.resp.DictItemResp;
import top.wain.heimdall.system.service.DictItemService;
import top.continew.starter.cache.redisson.util.RedisUtils;
import top.continew.starter.core.autoconfigure.application.ApplicationProperties;
import top.continew.starter.core.constant.StringConstants;
import top.continew.starter.core.enums.BaseEnum;
import top.continew.starter.core.util.validation.CheckUtils;
import top.continew.starter.data.service.impl.ServiceImpl;
import top.continew.starter.data.util.QueryWrapperHelper;
import top.continew.starter.extension.crud.model.query.PageQuery;
import top.continew.starter.extension.crud.model.resp.BasePageResp;
import top.continew.starter.extension.crud.model.resp.LabelValueResp;
import top.continew.starter.extension.crud.model.resp.PageResp;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 字典项业务实现
 *
 * @author WainZeng
 * @since 2023/9/11 21:29
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DictItemServiceImpl extends ServiceImpl<DictItemMapper, DictItemDO> implements DictItemService {

    private final ApplicationProperties applicationProperties;
    private static final Map<String, List<LabelValueResp>> ENUM_DICT_CACHE = new ConcurrentHashMap<>();

    @Override
    public BasePageResp<DictItemResp> page(DictItemQuery query, PageQuery pageQuery) {
        QueryWrapper<DictItemDO> queryWrapper = QueryWrapperHelper.build(query);
        CrudHelper.sort(queryWrapper, pageQuery, DictItemDO.class);
        IPage<DictItemDO> page = baseMapper.selectPage(new Page<>(pageQuery.getPage(), pageQuery
            .getSize()), queryWrapper);
        PageResp<DictItemResp> pageResp = PageResp.build(page, DictItemResp.class);
        pageResp.getList().forEach(CrudHelper::fill);
        return pageResp;
    }

    @Override
    public DictItemResp get(Long id) {
        DictItemDO entity = baseMapper.selectById(id);
        CheckUtils.throwIfNull(entity, "数据不存在");
        DictItemResp resp = BeanUtil.toBean(entity, DictItemResp.class);
        CrudHelper.fill(resp);
        return resp;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(DictItemReq req) {
        this.checkValueRepeat(req.getValue(), null, req.getDictId());
        RedisUtils.deleteByPattern(CacheConstants.DICT_KEY_PREFIX + StringConstants.ASTERISK);
        DictItemDO entity = BeanUtil.copyProperties(req, DictItemDO.class);
        baseMapper.insert(entity);
        return entity.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(DictItemReq req, Long id) {
        this.checkValueRepeat(req.getValue(), id, req.getDictId());
        RedisUtils.deleteByPattern(CacheConstants.DICT_KEY_PREFIX + StringConstants.ASTERISK);
        DictItemDO entity = this.getById(id);
        CheckUtils.throwIfNull(entity, "数据不存在");
        BeanUtil.copyProperties(req, entity, CopyOptions.create().ignoreNullValue());
        baseMapper.updateById(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(List<Long> ids) {
        baseMapper.deleteByIds(ids);
    }

    @Override
    public List<LabelValueResp> listByDictCode(String dictCode) {
        return Optional.ofNullable(ENUM_DICT_CACHE.get(dictCode.toLowerCase()))
            .orElseGet(() -> baseMapper.listByDictCode(dictCode));
    }

    @Override
    public void deleteByDictIds(List<Long> dictIds) {
        if (CollUtil.isEmpty(dictIds)) {
            return;
        }
        baseMapper.lambdaUpdate().in(DictItemDO::getDictId, dictIds).remove();
        RedisUtils.deleteByPattern(CacheConstants.DICT_KEY_PREFIX + StringConstants.ASTERISK);
    }

    @Override
    public List<String> listEnumDictNames() {
        return ENUM_DICT_CACHE.keySet().stream().toList();
    }

    private void checkValueRepeat(String value, Long id, Long dictId) {
        CheckUtils.throwIf(baseMapper.lambdaQuery()
            .eq(DictItemDO::getValue, value)
            .eq(DictItemDO::getDictId, dictId)
            .ne(id != null, DictItemDO::getId, id)
            .exists(), "字典值为 [{}] 的字典项已存在", value);
    }

    private List<LabelValueResp> toEnumDict(Class<?> enumClass) {
        Object[] enumConstants = enumClass.getEnumConstants();
        if (ArrayUtil.isEmpty(enumConstants)) {
            return List.of();
        }
        return Arrays.stream(enumConstants).map(e -> {
            BaseEnum baseEnum = (BaseEnum)e;
            return new LabelValueResp(baseEnum.getDescription(), baseEnum.getValue(), baseEnum.getColor());
        }).toList();
    }

    @PostConstruct
    public void init() {
        Set<Class<?>> classSet = ClassUtil.scanPackageBySuper(applicationProperties.getBasePackage(), BaseEnum.class);
        for (Class<?> cls : classSet) {
            List<LabelValueResp> value = this.toEnumDict(cls);
            if (CollUtil.isEmpty(value)) {
                continue;
            }
            String key = StrUtil.toUnderlineCase(cls.getSimpleName()).toLowerCase();
            ENUM_DICT_CACHE.put(key, value);
        }
        log.debug("枚举字典已缓存到内存：{}", ENUM_DICT_CACHE.keySet());
    }
}
