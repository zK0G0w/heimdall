package top.wain.heimdall.system.model.req;

import lombok.Data;
import top.wain.heimdall.common.enums.SuccessFailureStatusEnum;

import java.io.Serial;
import java.io.Serializable;

/**
 * 短信日志创建或修改请求参数
 *
 * @author luoqiz
 * @author WainZeng
 * @since 2025/03/15 22:15
 */
@Data
public class SmsLogReq implements Serializable {

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