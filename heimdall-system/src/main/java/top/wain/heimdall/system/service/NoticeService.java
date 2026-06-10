package top.wain.heimdall.system.service;

import top.wain.heimdall.system.enums.NoticeMethodEnum;
import top.wain.heimdall.system.model.entity.NoticeDO;
import top.wain.heimdall.system.model.query.NoticeQuery;
import top.wain.heimdall.system.model.req.NoticeReq;
import top.wain.heimdall.system.model.resp.dashboard.DashboardNoticeResp;
import top.wain.heimdall.system.model.resp.notice.NoticeDetailResp;
import top.wain.heimdall.system.model.resp.notice.NoticeResp;
import top.continew.starter.data.service.IService;
import top.continew.starter.extension.crud.model.query.PageQuery;
import top.continew.starter.extension.crud.model.resp.BasePageResp;

import java.util.List;

/**
 * 公告业务接口
 *
 * @author WainZeng
 * @since 2023/8/20 10:55
 */
public interface NoticeService extends IService<NoticeDO> {

    BasePageResp<NoticeResp> page(NoticeQuery query, PageQuery pageQuery);

    NoticeDetailResp get(Long id);

    Long create(NoticeReq req);

    void update(NoticeReq req, Long id);

    void delete(List<Long> ids);

    /**
     * 发布公告
     *
     * @param notice 公告信息
     */
    void publish(NoticeDO notice);

    /**
     * 查询未读公告 ID 列表
     *
     * @param method 通知方式
     * @param userId 用户 ID
     * @return 未读公告 ID 响应参数
     */
    List<Long> listUnreadIdsByUserId(NoticeMethodEnum method, Long userId);

    /**
     * 阅读公告
     *
     * @param id     公告 ID
     * @param userId 用户 ID
     */
    void readNotice(Long id, Long userId);

    /**
     * 查询仪表盘公告列表
     *
     * @return 仪表盘公告列表
     */
    List<DashboardNoticeResp> listDashboard();
}
