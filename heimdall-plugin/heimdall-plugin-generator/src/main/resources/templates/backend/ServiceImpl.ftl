package ${packageName}.${subPackageName};

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.wain.heimdall.common.util.CrudHelper;
import top.continew.starter.core.util.validation.CheckUtils;
import top.continew.starter.data.service.impl.ServiceImpl;
import top.continew.starter.data.util.QueryWrapperHelper;
import top.continew.starter.extension.crud.model.query.PageQuery;
import top.continew.starter.extension.crud.model.resp.PageResp;
import ${packageName}.mapper.${classNamePrefix}Mapper;
import ${packageName}.model.entity.${classNamePrefix}DO;
import ${packageName}.model.query.${classNamePrefix}Query;
import ${packageName}.model.req.${classNamePrefix}Req;
import ${packageName}.model.resp.${classNamePrefix}DetailResp;
import ${packageName}.model.resp.${classNamePrefix}Resp;
import ${packageName}.service.${classNamePrefix}Service;

import java.util.List;

/**
 * ${businessName}业务实现
 *
 * @author ${author}
 * @since ${datetime}
 */
@Service
@RequiredArgsConstructor
public class ${className} extends ServiceImpl<${classNamePrefix}Mapper, ${classNamePrefix}DO> implements ${classNamePrefix}Service {

    @Override
    public PageResp<${classNamePrefix}Resp> page(${classNamePrefix}Query query, PageQuery pageQuery) {
        QueryWrapper<${classNamePrefix}DO> queryWrapper = QueryWrapperHelper.build(query);
        CrudHelper.sort(queryWrapper, pageQuery, ${classNamePrefix}DO.class);
        IPage<${classNamePrefix}DO> page = baseMapper.selectPage(new Page<>(pageQuery.getPage(), pageQuery.getSize()), queryWrapper);
        PageResp<${classNamePrefix}Resp> pageResp = PageResp.build(page, ${classNamePrefix}Resp.class);
        pageResp.getList().forEach(CrudHelper::fill);
        return pageResp;
    }

    @Override
    public ${classNamePrefix}DetailResp get(Long id) {
        ${classNamePrefix}DO entity = baseMapper.selectById(id);
        CheckUtils.throwIfNull(entity, "数据不存在");
        ${classNamePrefix}DetailResp resp = BeanUtil.toBean(entity, ${classNamePrefix}DetailResp.class);
        CrudHelper.fill(resp);
        return resp;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(${classNamePrefix}Req req) {
        ${classNamePrefix}DO entity = BeanUtil.copyProperties(req, ${classNamePrefix}DO.class);
        baseMapper.insert(entity);
        return entity.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(${classNamePrefix}Req req, Long id) {
        ${classNamePrefix}DO oldEntity = super.getById(id);
        CheckUtils.throwIfNull(oldEntity, "数据不存在");
        BeanUtil.copyProperties(req, oldEntity, CopyOptions.create().ignoreNullValue());
        baseMapper.updateById(oldEntity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(List<Long> ids) {
        baseMapper.deleteByIds(ids);
    }
}
