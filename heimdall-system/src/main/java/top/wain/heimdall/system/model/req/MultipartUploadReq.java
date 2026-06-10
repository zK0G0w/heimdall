package top.wain.heimdall.system.model.req;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 分片上传请求参数
 *
 * @author KAI
 * @since 2025/7/30 16:40
 */
@Data
@Schema(description = "分片上传请求参数")
public class MultipartUploadReq {

    /**
     * 上传ID
     */
    @Schema(description = "上传ID")
    private String uploadId;

    /**
     * 分片序号
     */
    @Schema(description = "分片序号")
    private Integer partNumber;

    /**
     * 分片ETag
     */
    @Schema(description = "分片ETag")
    private String eTag;

    /**
     * 存储编码
     */
    @Schema(description = "存储编码")
    private String storageCode;
}