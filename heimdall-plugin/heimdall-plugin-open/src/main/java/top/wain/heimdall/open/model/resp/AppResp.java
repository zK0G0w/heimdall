package top.wain.heimdall.open.model.resp;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import top.wain.heimdall.common.base.model.resp.BaseDetailResp;
import top.wain.heimdall.common.enums.DisEnableStatusEnum;

import java.io.Serial;
import java.time.LocalDateTime;

/**
 * 应用响应参数
 *
 * @author chengzi
 * @author WainZeng
 * @since 2024/10/17 16:03
 */
@Data
@Schema(description = "应用响应参数")
public class AppResp extends BaseDetailResp {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 名称
     */
    @Schema(description = "名称", example = "应用1")
    private String name;

    /**
     * Access Key（访问密钥）
     */
    @Schema(description = "Access Key（访问密钥）", example = "YjUyMGJjYjIxNTE0NDAxMWE1NmRiY2")
    private String accessKey;

    /**
     * 失效时间
     */
    @Schema(description = "失效时间", example = "2023-08-08 08:08:08", type = "string")
    private LocalDateTime expireTime;

    /**
     * 状态
     */
    @Schema(description = "状态", example = "1")
    private DisEnableStatusEnum status;

    /**
     * 描述
     */
    @Schema(description = "描述", example = "应用1描述信息")
    private String description;
}