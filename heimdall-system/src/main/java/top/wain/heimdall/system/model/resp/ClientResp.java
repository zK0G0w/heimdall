package top.wain.heimdall.system.model.resp;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import top.wain.heimdall.common.base.model.resp.BaseDetailResp;
import top.wain.heimdall.common.config.excel.DictExcelProperty;
import top.wain.heimdall.common.config.excel.ExcelDictConverter;
import top.wain.heimdall.common.enums.DisEnableStatusEnum;
import top.wain.heimdall.system.enums.LogoutModeEnum;
import top.wain.heimdall.system.enums.ReplacedRangeEnum;
import top.continew.starter.excel.converter.ExcelBaseEnumConverter;
import top.continew.starter.excel.converter.ExcelListConverter;

import java.io.Serial;
import java.util.List;

/**
 * 客户端响应参数
 *
 * @author KAI
 * @author WainZeng
 * @since 2024/12/03 16:04
 */
@Data
@ExcelIgnoreUnannotated
@Schema(description = "客户端响应参数")
public class ClientResp extends BaseDetailResp {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 客户端 ID
     */
    @Schema(description = "客户端 ID", example = "ef51c9a3e9046c4f2ea45142c8a8344a")
    @ExcelProperty(value = "客户端 ID", order = 2)
    private String clientId;

    /**
     * 客户端类型（取值于字典 client_type
     */
    @Schema(description = "客户端类型（取值于字典 client_type）", example = "PC")
    @ExcelProperty(value = "客户端类型", converter = ExcelDictConverter.class, order = 5)
    @DictExcelProperty("client_type")
    private String clientType;

    /**
     * 认证类型
     */
    @Schema(description = "认证类型", example = "ACCOUNT")
    @ExcelProperty(value = "认证类型", converter = ExcelListConverter.class, order = 4)
    private List<String> authType;

    /**
     * Token 最低活跃频率（单位：秒，-1：不限制，永不冻结）
     */
    @Schema(description = "Token 最低活跃频率（单位：秒，-1：不限制，永不冻结）", example = "1800")
    @ExcelProperty(value = "Token 最低活跃频率", order = 6)
    private Long activeTimeout;

    /**
     * Token 有效期（单位：秒，-1：永不过期）
     */
    @Schema(description = "Token 有效期（单位：秒，-1：永不过期）", example = "86400")
    @ExcelProperty(value = "Token 有效期", order = 7)
    private Long timeout;

    /**
     * 是否允许同一账号多地同时登录（true：允许；false：新登录挤掉旧登录）
     */
    @Schema(description = "是否允许同一账号多地同时登录", example = "true")
    @ExcelProperty(value = "是否允许同一账号多地同时登录", order = 8)
    private Boolean isConcurrent;

    /**
     * 顶人下线的范围
     */
    @Schema(description = "顶人下线的范围", example = "ALL_DEVICE_TYPE")
    @ExcelProperty(value = "顶人下线的范围", converter = ExcelBaseEnumConverter.class, order = 9)
    private ReplacedRangeEnum replacedRange;

    /**
     * 同一账号最大登录数量（-1：不限制，只有在 isConcurrent=true，isShare=false 时才有效）
     */
    @Schema(description = "同一账号最大登录数量", example = "-1")
    @ExcelProperty(value = "同一账号最大登录数量", order = 10)
    private Integer maxLoginCount;

    /**
     * 溢出人数的下线方式
     */
    @Schema(description = "溢出人数的下线方式", example = "KICKOUT")
    @ExcelProperty(value = "溢出人数的下线方式", converter = ExcelBaseEnumConverter.class, order = 11)
    private LogoutModeEnum overflowLogoutMode;

    /**
     * 状态
     */
    @Schema(description = "状态", example = "1")
    @ExcelProperty(value = "状态", converter = ExcelBaseEnumConverter.class, order = 12)
    private DisEnableStatusEnum status;
}