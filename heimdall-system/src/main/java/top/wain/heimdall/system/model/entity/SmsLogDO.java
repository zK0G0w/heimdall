package top.wain.heimdall.system.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import top.wain.heimdall.common.enums.SuccessFailureStatusEnum;
import top.wain.heimdall.common.base.model.entity.BaseCreateDO;

import java.io.Serial;

/**
 * 短信日志实体
 *
 * @author luoqiz
 * @author WainZeng
 * @since 2025/03/15 22:15
 */
@Data
@TableName("sys_sms_log")
public class SmsLogDO extends BaseCreateDO {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 配置 ID
     */
    private Long configId;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 参数配置
     */
    private String params;

    /**
     * 发送状态
     */
    private SuccessFailureStatusEnum status;

    /**
     * 返回数据
     */
    private String resMsg;
}