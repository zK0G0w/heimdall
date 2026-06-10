package top.wain.heimdall.system.model.resp.file;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 分片上传结果
 *
 * @author echo
 * @since 2.14.0
 */
@Data
@Schema(description = "分片上传响应参数")
public class MultipartUploadResp implements Serializable {
    /**
     * 分片编号
     */
    @Schema(description = "分片编号")
    private Integer partNumber;

    /**
     * 分片ETag
     */
    @Schema(description = "分片ETag")
    private String partETag;

    /**
     * 分片大小
     */
    @Schema(description = "分片大小")
    private Long partSize;

    /**
     * 是否成功
     */
    @Schema(description = "是否成功")
    private boolean success;

    /**
     * 错误信息
     */
    @Schema(description = "错误信息")
    private String errorMessage;

}
