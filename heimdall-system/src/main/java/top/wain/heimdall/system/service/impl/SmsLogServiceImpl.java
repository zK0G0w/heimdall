package top.wain.heimdall.system.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.wain.heimdall.common.util.CrudHelper;
import top.wain.heimdall.system.mapper.SmsLogMapper;
import top.wain.heimdall.system.model.entity.SmsLogDO;
import top.wain.heimdall.system.model.query.SmsLogQuery;
import top.wain.heimdall.system.model.req.SmsLogReq;
import top.wain.heimdall.system.model.resp.SmsLogResp;
import top.wain.heimdall.system.service.SmsLogService;
import top.continew.starter.core.util.validation.CheckUtils;
import top.continew.starter.data.service.impl.ServiceImpl;
import top.continew.starter.data.util.QueryWrapperHelper;
import top.continew.starter.excel.util.ExcelUtils;
import top.continew.starter.extension.crud.model.query.PageQuery;
import top.continew.starter.extension.crud.model.query.SortQuery;
import top.continew.starter.extension.crud.model.resp.BasePageResp;
import top.continew.starter.extension.crud.model.resp.PageResp;

import java.util.List;

/**
 * 短信日志业务实现
 *
 * @author luoqiz
 * @since 2025/03/15 22:15
 */
@Service
@RequiredArgsConstructor
public class SmsLogServiceImpl extends ServiceImpl<SmsLogMapper, SmsLogDO> implements SmsLogService {

    @Override
    public BasePageResp<SmsLogResp> page(SmsLogQuery query, PageQuery pageQuery) {
        QueryWrapper<SmsLogDO> queryWrapper = QueryWrapperHelper.build(query);
        CrudHelper.sort(queryWrapper, pageQuery, SmsLogDO.class);
        IPage<SmsLogDO> page = baseMapper.selectPage(new Page<>(pageQuery.getPage(), pageQuery
            .getSize()), queryWrapper);
        PageResp<SmsLogResp> pageResp = PageResp.build(page, SmsLogResp.class);
        pageResp.getList().forEach(CrudHelper::fill);
        return pageResp;
    }

    @Override
    public SmsLogResp get(Long id) {
        SmsLogDO entity = baseMapper.selectById(id);
        CheckUtils.throwIfNull(entity, "数据不存在");
        SmsLogResp resp = BeanUtil.toBean(entity, SmsLogResp.class);
        CrudHelper.fill(resp);
        return resp;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(SmsLogReq req) {
        SmsLogDO entity = BeanUtil.copyProperties(req, SmsLogDO.class);
        baseMapper.insert(entity);
        return entity.getId();
    }

    @Override
    public void delete(List<Long> ids) {
        baseMapper.deleteByIds(ids);
    }

    @Override
    public void export(SmsLogQuery query, SortQuery sortQuery, HttpServletResponse response) {
        QueryWrapper<SmsLogDO> queryWrapper = QueryWrapperHelper.build(query);
        CrudHelper.sort(queryWrapper, sortQuery, SmsLogDO.class);
        List<SmsLogDO> entityList = baseMapper.selectList(queryWrapper);
        List<SmsLogResp> list = BeanUtil.copyToList(entityList, SmsLogResp.class);
        list.forEach(CrudHelper::fill);
        ExcelUtils.export(list, "导出数据", SmsLogResp.class, response);
    }
}
