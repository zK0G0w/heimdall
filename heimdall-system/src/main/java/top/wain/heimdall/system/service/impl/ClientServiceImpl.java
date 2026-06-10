package top.wain.heimdall.system.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.codec.Base64;
import cn.hutool.core.util.IdUtil;
import cn.hutool.crypto.SecureUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.wain.heimdall.auth.model.query.OnlineUserQuery;
import top.wain.heimdall.auth.service.OnlineUserService;
import top.wain.heimdall.common.util.CrudHelper;
import top.wain.heimdall.system.mapper.ClientMapper;
import top.wain.heimdall.system.model.entity.ClientDO;
import top.wain.heimdall.system.model.query.ClientQuery;
import top.wain.heimdall.system.model.req.ClientReq;
import top.wain.heimdall.system.model.resp.ClientResp;
import top.wain.heimdall.system.service.ClientService;
import top.continew.starter.core.constant.StringConstants;
import top.continew.starter.core.util.validation.CheckUtils;
import top.continew.starter.data.service.impl.ServiceImpl;
import top.continew.starter.data.util.QueryWrapperHelper;
import top.continew.starter.extension.crud.model.query.PageQuery;
import top.continew.starter.extension.crud.model.resp.BasePageResp;
import top.continew.starter.extension.crud.model.resp.PageResp;

import java.util.List;

/**
 * 客户端业务实现
 *
 * @author KAI
 * @author WainZeng
 * @since 2024/12/03 16:04
 */
@Service
@RequiredArgsConstructor
public class ClientServiceImpl extends ServiceImpl<ClientMapper, ClientDO> implements ClientService {

    private final OnlineUserService onlineUserService;

    @Override
    public BasePageResp<ClientResp> page(ClientQuery query, PageQuery pageQuery) {
        QueryWrapper<ClientDO> queryWrapper = QueryWrapperHelper.build(query);
        CrudHelper.sort(queryWrapper, pageQuery, ClientDO.class);
        IPage<ClientDO> page = baseMapper.selectPage(new Page<>(pageQuery.getPage(), pageQuery
            .getSize()), queryWrapper);
        PageResp<ClientResp> pageResp = PageResp.build(page, ClientResp.class);
        pageResp.getList().forEach(CrudHelper::fill);
        return pageResp;
    }

    @Override
    public ClientResp get(Long id) {
        ClientDO entity = baseMapper.selectById(id);
        CheckUtils.throwIfNull(entity, "数据不存在");
        ClientResp resp = BeanUtil.toBean(entity, ClientResp.class);
        CrudHelper.fill(resp);
        return resp;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(ClientReq req) {
        // 自动生成 clientId
        req.setClientId(SecureUtil.md5(Base64.encode(IdUtil.fastSimpleUUID())
            .replace(StringConstants.SLASH, StringConstants.EMPTY)
            .replace(StringConstants.PLUS, StringConstants.EMPTY)));
        ClientDO entity = BeanUtil.copyProperties(req, ClientDO.class);
        baseMapper.insert(entity);
        return entity.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(ClientReq req, Long id) {
        ClientDO entity = this.getById(id);
        CheckUtils.throwIfNull(entity, "数据不存在");
        BeanUtil.copyProperties(req, entity, CopyOptions.create().ignoreNullValue());
        baseMapper.updateById(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(List<Long> ids) {
        // 如果还存在在线用户，则不能删除
        OnlineUserQuery query = new OnlineUserQuery();
        for (Long id : ids) {
            ClientDO client = this.getById(id);
            query.setClientId(client.getClientId());
            CheckUtils.throwIfNotEmpty(onlineUserService.list(query), "客户端 [{}] 还存在在线用户，不允许删除", client.getClientId());
        }
        baseMapper.deleteByIds(ids);
    }

    @Override
    public ClientResp getByClientId(String clientId) {
        return baseMapper.lambdaQuery()
            .eq(ClientDO::getClientId, clientId)
            .oneOpt()
            .map(client -> BeanUtil.copyProperties(client, ClientResp.class))
            .orElse(null);
    }
}
