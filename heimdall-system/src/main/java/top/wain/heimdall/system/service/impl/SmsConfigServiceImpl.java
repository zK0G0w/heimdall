package top.wain.heimdall.system.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.dromara.sms4j.core.factory.SmsFactory;
import org.dromara.sms4j.provider.config.BaseConfig;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.wain.heimdall.common.enums.DisEnableStatusEnum;
import top.wain.heimdall.common.util.CrudHelper;
import top.wain.heimdall.system.config.sms.SmsConfigUtil;
import top.wain.heimdall.system.mapper.SmsConfigMapper;
import top.wain.heimdall.system.model.entity.SmsConfigDO;
import top.wain.heimdall.system.model.query.SmsConfigQuery;
import top.wain.heimdall.system.model.req.SmsConfigReq;
import top.wain.heimdall.system.model.resp.SmsConfigResp;
import top.wain.heimdall.system.service.SmsConfigService;
import top.continew.starter.core.util.validation.CheckUtils;
import top.continew.starter.data.service.impl.ServiceImpl;
import top.continew.starter.data.util.QueryWrapperHelper;
import top.continew.starter.extension.crud.model.query.PageQuery;
import top.continew.starter.extension.crud.model.resp.BasePageResp;
import top.continew.starter.extension.crud.model.resp.PageResp;

import java.util.List;

/**
 * 短信配置业务实现
 *
 * @author luoqiz
 * @since 2025/03/15 18:41
 */
@Service
@RequiredArgsConstructor
public class SmsConfigServiceImpl extends ServiceImpl<SmsConfigMapper, SmsConfigDO> implements SmsConfigService {

    @Override
    public BasePageResp<SmsConfigResp> page(SmsConfigQuery query, PageQuery pageQuery) {
        QueryWrapper<SmsConfigDO> queryWrapper = QueryWrapperHelper.build(query);
        CrudHelper.sort(queryWrapper, pageQuery, SmsConfigDO.class);
        IPage<SmsConfigDO> page = baseMapper.selectPage(new Page<>(pageQuery.getPage(), pageQuery
            .getSize()), queryWrapper);
        PageResp<SmsConfigResp> pageResp = PageResp.build(page, SmsConfigResp.class);
        pageResp.getList().forEach(CrudHelper::fill);
        return pageResp;
    }

    @Override
    public SmsConfigResp get(Long id) {
        SmsConfigDO entity = baseMapper.selectById(id);
        CheckUtils.throwIfNull(entity, "数据不存在");
        SmsConfigResp resp = BeanUtil.toBean(entity, SmsConfigResp.class);
        CrudHelper.fill(resp);
        return resp;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(SmsConfigReq req) {
        SmsConfigDO entity = BeanUtil.copyProperties(req, SmsConfigDO.class);
        baseMapper.insert(entity);
        // 创建后加载短信配置
        this.load(entity);
        return entity.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(SmsConfigReq req, Long id) {
        SmsConfigDO oldEntity = this.getById(id);
        CheckUtils.throwIfNull(oldEntity, "数据不存在");
        BeanUtil.copyProperties(req, oldEntity, CopyOptions.create().ignoreNullValue());
        baseMapper.updateById(oldEntity);
        // 更新后重新加载短信配置：先卸载再加载
        this.unload(id.toString());
        this.load(oldEntity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(List<Long> ids) {
        baseMapper.deleteByIds(ids);
        // 删除后卸载短信配置
        for (Long id : ids) {
            this.unload(id.toString());
        }
    }

    @Override
    public List<SmsConfigResp> list(SmsConfigQuery query) {
        QueryWrapper<SmsConfigDO> queryWrapper = QueryWrapperHelper.build(query);
        List<SmsConfigDO> entityList = baseMapper.selectList(queryWrapper);
        List<SmsConfigResp> list = BeanUtil.copyToList(entityList, SmsConfigResp.class);
        list.forEach(CrudHelper::fill);
        return list;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setDefaultConfig(Long id) {
        SmsConfigDO smsConfig = this.getById(id);
        CheckUtils.throwIfNull(smsConfig, "数据不存在");
        if (Boolean.TRUE.equals(smsConfig.getIsDefault())) {
            return;
        }
        // 启用状态才能设为默认配置
        CheckUtils.throwIfEqual(DisEnableStatusEnum.DISABLE, smsConfig.getStatus(), "请先启用所选配置");
        baseMapper.lambdaUpdate().eq(SmsConfigDO::getIsDefault, true).set(SmsConfigDO::getIsDefault, false).update();
        baseMapper.lambdaUpdate().eq(SmsConfigDO::getId, id).set(SmsConfigDO::getIsDefault, true).update();
    }

    @Override
    public SmsConfigDO getDefaultConfig() {
        return baseMapper.lambdaQuery()
            .eq(SmsConfigDO::getIsDefault, true)
            .eq(SmsConfigDO::getStatus, DisEnableStatusEnum.ENABLE)
            .one();
    }

    /**
     * 加载配置
     *
     * @param entity 配置信息
     */
    private void load(SmsConfigDO entity) {
        // 手动查询完整实体并转换为响应对象
        SmsConfigDO fullEntity = baseMapper.selectById(entity.getId());
        SmsConfigResp smsConfig = BeanUtil.toBean(fullEntity, SmsConfigResp.class);
        BaseConfig config = SmsConfigUtil.from(smsConfig);
        if (config != null) {
            SmsFactory.createSmsBlend(config);
        }
    }

    /**
     * 卸载配置
     *
     * @param configId 配置 ID
     */
    private void unload(String configId) {
        if (SmsFactory.getSmsBlend(configId) != null) {
            SmsFactory.unregister(configId);
        }
    }
}
