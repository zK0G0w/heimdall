package top.wain.heimdall.system.model.resp;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import top.wain.heimdall.common.enums.SuccessFailureStatusEnum;
import top.wain.heimdall.common.base.model.resp.BaseResp;
import top.continew.starter.excel.converter.ExcelBaseEnumConverter;

import java.io.Serial;

/**
 * 短信日志响应参数
 *
 * @author luoqiz
 * @author WainZeng
 * @since 2025/03/15 22:15
 */
@Data
@ExcelIgnoreUnannotated
@Schema(description = "短信日志响应参数")
public class SmsLogResp extends BaseResp {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 配置 ID
     */
    @Schema(description = "配置 ID", example = "")
    @ExcelProperty(value = "配置 ID")
    private Long configId;

    /**
     * 手机号
     */
    @Schema(description = "手机号", example = "18888888888")
    @ExcelProperty(value = "手机号")
    private String phone;

    /**
     * 参数配置
     */
    @Schema(description = "参数配置")
    @ExcelProperty(value = "参数配置")
    private String params;

    /**
     * 发送状态
     */
    @Schema(description = "发送状态", example = "1")
    @ExcelProperty(value = "发送状态", converter = ExcelBaseEnumConverter.class)
    private SuccessFailureStatusEnum status;

    /**
     * 返回数据
     */
    @Schema(description = "返回数据")
    @ExcelProperty(value = "返回数据")
    private String resMsg;
}