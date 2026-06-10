package top.wain.heimdall.system.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.wain.heimdall.common.context.UserContextHolder;
import top.wain.heimdall.common.util.CrudHelper;
import top.wain.heimdall.system.enums.*;
import top.wain.heimdall.system.mapper.NoticeMapper;
import top.wain.heimdall.system.model.entity.NoticeDO;
import top.wain.heimdall.system.model.query.NoticeQuery;
import top.wain.heimdall.system.model.req.MessageReq;
import top.wain.heimdall.system.model.req.NoticeReq;
import top.wain.heimdall.system.model.resp.dashboard.DashboardNoticeResp;
import top.wain.heimdall.system.model.resp.notice.NoticeDetailResp;
import top.wain.heimdall.system.model.resp.notice.NoticeResp;
import top.wain.heimdall.system.service.MessageService;
import top.wain.heimdall.system.service.NoticeLogService;
import top.wain.heimdall.system.service.NoticeService;
import top.continew.starter.core.util.validation.CheckUtils;
import top.continew.starter.data.service.impl.ServiceImpl;
import top.continew.starter.extension.crud.model.query.PageQuery;
import top.continew.starter.extension.crud.model.resp.PageResp;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 公告业务实现
 *
 * @author WainZeng
 * @since 2023/8/20 10:55
 */
@Service
@RequiredArgsConstructor
public class NoticeServiceImpl extends ServiceImpl<NoticeMapper, NoticeDO> implements NoticeService {

    private final NoticeLogService noticeLogService;
    private final MessageService messageService;

    @Override
    public PageResp<NoticeResp> page(NoticeQuery query, PageQuery pageQuery) {
        IPage<NoticeResp> page = baseMapper.selectNoticePage(new Page<>(pageQuery.getPage(), pageQuery
            .getSize()), query);
        PageResp<NoticeResp> pageResp = PageResp.build(page);
        pageResp.getList().forEach(CrudHelper::fill);
        return pageResp;
    }

    @Override
    public NoticeDetailResp get(Long id) {
        NoticeDO entity = baseMapper.selectById(id);
        CheckUtils.throwIfNull(entity, "数据不存在");
        NoticeDetailResp resp = BeanUtil.toBean(entity, NoticeDetailResp.class);
        CrudHelper.fill(resp);
        return resp;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(NoticeReq req) {
        // 设置发布状态
        if (!NoticeStatusEnum.DRAFT.equals(req.getStatus())) {
            if (Boolean.TRUE.equals(req.getIsTiming())) {
                req.setStatus(NoticeStatusEnum.PENDING);
            } else {
                req.setStatus(NoticeStatusEnum.PUBLISHED);
                req.setPublishTime(LocalDateTime.now());
            }
        }
        NoticeDO entity = BeanUtil.copyProperties(req, NoticeDO.class);
        baseMapper.insert(entity);
        // 发送消息
        if (NoticeStatusEnum.PUBLISHED.equals(entity.getStatus())) {
            this.publish(entity);
        }
        return entity.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(NoticeReq req, Long id) {
        NoticeDO oldNotice = super.getById(id);
        CheckUtils.throwIfNull(oldNotice, "数据不存在");
        // 根据原状态校验
        switch (oldNotice.getStatus()) {
            case PUBLISHED -> {
                CheckUtils.throwIfNotEqual(req.getStatus(), oldNotice.getStatus(), "公告已发布，不允许修改状态");
                CheckUtils.throwIfNotEqual(req.getIsTiming(), oldNotice.getIsTiming(), "公告已发布，不允许修改定时发布信息");
                CheckUtils.throwIfNotEqual(req.getNoticeScope(), oldNotice.getNoticeScope(), "公告已发布，不允许修改通知范围");
                if (NoticeScopeEnum.USER.equals(oldNotice.getNoticeScope())) {
                    CheckUtils.throwIfNotEmpty(CollUtil.disjunction(req.getNoticeUsers(), oldNotice
                        .getNoticeUsers()), "公告已发布，不允许修改通知用户");
                }
                CheckUtils.throwIf(!CollUtil.isEqualList(req.getNoticeMethods(), oldNotice
                    .getNoticeMethods()), "公告已发布，不允许修改通知方式");
                if (Boolean.TRUE.equals(oldNotice.getIsTiming())) {
                    CheckUtils.throwIfNotEqual(req.getPublishTime(), oldNotice.getPublishTime(), "公告已发布，不允许修改定时发布信息");
                }
                req.setPublishTime(oldNotice.getPublishTime());
            }
            case DRAFT, PENDING -> {
                if (NoticeStatusEnum.PUBLISHED.equals(req.getStatus())) {
                    if (Boolean.TRUE.equals(req.getIsTiming())) {
                        req.setStatus(NoticeStatusEnum.PENDING);
                    } else {
                        req.setStatus(NoticeStatusEnum.PUBLISHED);
                        req.setPublishTime(LocalDateTime.now());
                    }
                }
            }
            default -> throw new IllegalArgumentException("状态无效");
        }
        BeanUtil.copyProperties(req, oldNotice, CopyOptions.create().ignoreNullValue());
        baseMapper.updateById(oldNotice);
        // 重置定时发布时间
        if (!NoticeStatusEnum.PUBLISHED.equals(oldNotice.getStatus()) && Boolean.FALSE.equals(oldNotice
            .getIsTiming()) && oldNotice.getPublishTime() != null) {
            baseMapper.lambdaUpdate()
                .set(NoticeDO::getPublishTime, null)
                .eq(NoticeDO::getId, oldNotice.getId())
                .update();
        }
        // 发送消息
        if (Boolean.FALSE.equals(oldNotice.getIsTiming()) && NoticeStatusEnum.PUBLISHED.equals(oldNotice.getStatus())) {
            this.publish(oldNotice);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(List<Long> ids) {
        baseMapper.deleteByIds(ids);
        // 删除公告日志
        noticeLogService.deleteByNoticeIds(ids);
    }

    @Override
    public void publish(NoticeDO notice) {
        List<Integer> noticeMethods = notice.getNoticeMethods();
        if (CollUtil.isNotEmpty(noticeMethods) && noticeMethods.contains(NoticeMethodEnum.SYSTEM_MESSAGE.getValue())) {
            MessageTemplateEnum template = MessageTemplateEnum.NOTICE_PUBLISH;
            MessageReq req = new MessageReq(MessageTypeEnum.SYSTEM);
            req.setTitle(template.getTitle());
            req.setContent(template.getContent().formatted(notice.getTitle()));
            req.setPath(template.getPath().formatted(notice.getId()));
            messageService.add(req, notice.getNoticeUsers());
        }
    }

    @Override
    public List<Long> listUnreadIdsByUserId(NoticeMethodEnum method, Long userId) {
        return baseMapper.selectUnreadIdsByUserId(method != null ? method.getValue() : null, userId);
    }

    @Override
    public void readNotice(Long id, Long userId) {
        noticeLogService.add(List.of(userId), id);
    }

    @Override
    public List<DashboardNoticeResp> listDashboard() {
        Long userId = UserContextHolder.getUserId();
        return baseMapper.selectDashboardList(userId);
    }
}
