package top.wain.heimdall.open.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.codec.Base64;
import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.wain.heimdall.common.util.CrudHelper;
import top.wain.heimdall.open.mapper.AppMapper;
import top.wain.heimdall.open.model.entity.AppDO;
import top.wain.heimdall.open.model.query.AppQuery;
import top.wain.heimdall.open.model.req.AppReq;
import top.wain.heimdall.open.model.resp.AppDetailResp;
import top.wain.heimdall.open.model.resp.AppResp;
import top.wain.heimdall.open.model.resp.AppSecretResp;
import top.wain.heimdall.open.service.AppService;
import top.continew.starter.core.constant.StringConstants;
import top.continew.starter.core.util.validation.CheckUtils;
import top.continew.starter.data.service.impl.ServiceImpl;
import top.continew.starter.data.util.QueryWrapperHelper;
import top.continew.starter.excel.util.ExcelUtils;
import top.continew.starter.extension.crud.model.query.PageQuery;
import top.continew.starter.extension.crud.model.query.SortQuery;
import top.continew.starter.extension.crud.model.resp.BasePageResp;

import java.util.List;

/**
 * 应用业务实现
 *
 * @author chengzi
 * @author WainZeng
 * @since 2024/10/17 16:03
 */
@Service
public class AppServiceImpl extends ServiceImpl<AppMapper, AppDO> implements AppService {

    @Override
    public BasePageResp<AppResp> page(AppQuery query, PageQuery pageQuery) {
        QueryWrapper<AppDO> queryWrapper = QueryWrapperHelper.build(query);
        CrudHelper.sort(queryWrapper, pageQuery, AppDO.class);
        IPage<AppDO> page = baseMapper.selectPage(new Page<>(pageQuery.getPage(), pageQuery.getSize()), queryWrapper);
        List<AppResp> list = BeanUtil.copyToList(page.getRecords(), AppResp.class);
        CrudHelper.fillAll(list);
        return new BasePageResp<>(list, page.getTotal());
    }

    @Override
    public AppDetailResp get(Long id) {
        AppDO entity = baseMapper.selectById(id);
        CheckUtils.throwIfNull(entity, "数据不存在");
        AppDetailResp resp = BeanUtil.toBean(entity, AppDetailResp.class);
        CrudHelper.fill(resp);
        return resp;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(AppReq req) {
        // 生成 accessKey 和 secretKey
        req.setAccessKey(Base64.encode(IdUtil.fastSimpleUUID())
            .replace(StringConstants.SLASH, StringConstants.EMPTY)
            .replace(StringConstants.PLUS, StringConstants.EMPTY)
            .substring(0, 30));
        req.setSecretKey(this.generateSecret());
        AppDO entity = BeanUtil.copyProperties(req, AppDO.class);
        baseMapper.insert(entity);
        return entity.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(AppReq req, Long id) {
        AppDO oldEntity = this.getById(id);
        CheckUtils.throwIfNull(oldEntity, "数据不存在");
        BeanUtil.copyProperties(req, oldEntity, CopyOptions.create().ignoreNullValue());
        baseMapper.updateById(oldEntity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(List<Long> ids) {
        baseMapper.deleteByIds(ids);
    }

    @Override
    public void export(AppQuery query, SortQuery sortQuery, HttpServletResponse response) {
        QueryWrapper<AppDO> queryWrapper = QueryWrapperHelper.build(query);
        CrudHelper.sort(queryWrapper, sortQuery, AppDO.class);
        List<AppDO> entityList = baseMapper.selectList(queryWrapper);
        List<AppDetailResp> list = BeanUtil.copyToList(entityList, AppDetailResp.class);
        list.forEach(CrudHelper::fill);
        ExcelUtils.export(list, "导出数据", AppDetailResp.class, response);
    }

    @Override
    public AppSecretResp getSecret(Long id) {
        AppDO app = this.getById(id);
        CheckUtils.throwIfNull(app, "数据不存在");
        AppSecretResp appSecretResp = new AppSecretResp();
        appSecretResp.setAccessKey(app.getAccessKey());
        appSecretResp.setSecretKey(app.getSecretKey());
        return appSecretResp;
    }

    @Override
    public void resetSecret(Long id) {
        AppDO existing = this.getById(id);
        CheckUtils.throwIfNull(existing, "数据不存在");
        AppDO app = new AppDO();
        app.setSecretKey(this.generateSecret());
        baseMapper.update(app, Wrappers.lambdaQuery(AppDO.class).eq(AppDO::getId, id));
    }

    @Override
    public AppDO getByAccessKey(String accessKey) {
        return baseMapper.selectByAccessKey(accessKey);
    }

    /**
     * 生成密钥
     *
     * @return 密钥
     */
    private String generateSecret() {
        return Base64.encode(IdUtil.fastSimpleUUID())
            .replace(StringConstants.SLASH, StringConstants.EMPTY)
            .replace(StringConstants.PLUS, StringConstants.EMPTY);
    }
}
