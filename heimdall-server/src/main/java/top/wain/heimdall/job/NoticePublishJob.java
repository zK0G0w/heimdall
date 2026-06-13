package top.wain.heimdall.job;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.aizuda.snailjob.client.job.core.annotation.JobExecutor;
import com.aizuda.snailjob.common.log.SnailJobLog;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import top.wain.heimdall.schedule.annotation.ConditionalOnEnabledScheduleJob;
import top.wain.heimdall.system.enums.NoticeMethodEnum;
import top.wain.heimdall.system.enums.NoticeStatusEnum;
import top.wain.heimdall.system.mapper.NoticeMapper;
import top.wain.heimdall.system.model.entity.NoticeDO;
import top.wain.heimdall.system.service.NoticeService;
import top.continew.starter.core.constant.PropertiesConstants;
import top.continew.starter.core.util.CollUtils;
import top.continew.starter.extension.tenant.annotation.TenantIgnore;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 公告发布任务
 *
 * @author WainZeng
 * @since 2025/5/11 22:19
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NoticePublishJob {

    /**
     * 定时发布公告（未启用 Snail Job 则使用它）
     */
    @Component
    @ConditionalOnProperty(prefix = "snail-job", name = PropertiesConstants.ENABLED, havingValue = "false")
    public static class Scheduler {

        @TenantIgnore
        //@Scheduled(cron = "0 * * * * ?")
        @Transactional(rollbackFor = Exception.class)
        public void publishNoticeWithSchedule() {
            log.info("定时任务 [公告发布] 开始执行。");
            publishNotice();
            log.info("定时任务 [公告发布] 执行结束。");
        }
    }

    /**
     * 定时发布公告（启用 Snail Job 时）
     */
    @Component
    @ConditionalOnEnabledScheduleJob
    public static class ScheduleJob {

        @TenantIgnore
        @JobExecutor(name = "NoticePublishJob")
        @Transactional(rollbackFor = Exception.class)
        public void publishNoticeWithScheduleJob() {
            SnailJobLog.REMOTE.info("定时任务 [公告发布] 开始执行。");
            publishNotice();
            SnailJobLog.REMOTE.info("定时任务 [公告发布] 执行结束。");
        }
    }

    /**
     * 发布公告
     */
    private static void publishNotice() {
        NoticeMapper noticeMapper = SpringUtil.getBean(NoticeMapper.class);
        // 查询待发布公告
        List<NoticeDO> list = noticeMapper.lambdaQuery()
            .eq(NoticeDO::getStatus, NoticeStatusEnum.PENDING)
            .le(NoticeDO::getPublishTime, LocalDateTime.now())
            .list();
        if (CollUtil.isEmpty(list)) {
            return;
        }
        // 筛选需要发送消息的公告并发送
        List<NoticeDO> needSendMessageList = list.stream()
            .filter(notice -> CollUtil.isNotEmpty(notice.getNoticeMethods()))
            .filter(notice -> notice.getNoticeMethods().contains(NoticeMethodEnum.SYSTEM_MESSAGE.getValue()))
            .toList();
        if (CollUtil.isNotEmpty(needSendMessageList)) {
            // 发送消息
            NoticeService noticeService = SpringUtil.getBean(NoticeService.class);
            needSendMessageList.parallelStream().forEach(noticeService::publish);
        }
        // 更新状态
        noticeMapper.lambdaUpdate()
            .set(NoticeDO::getStatus, NoticeStatusEnum.PUBLISHED)
            .in(NoticeDO::getId, CollUtils.mapToList(list, NoticeDO::getId))
            .update();
    }
}
