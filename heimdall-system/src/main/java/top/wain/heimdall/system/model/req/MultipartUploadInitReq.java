package top.wain.heimdall.system.model.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;

/**
 * 分片初始化请求参数
 *
 * @author KAI
 * @since 2025/7/30 16:38
 */
@Data
@Schema(description = "分片初始化请求参数")
public class MultipartUploadInitReq implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 文件名
     */
    @Schema(description = "文件名", example = "example.zip")
    @NotBlank(message = "文件名不能为空")
    private String fileName;

    /**
     * 文件大小（字节）
     */
    @Schema(description = "文件大小", example = "1048576")
    @NotNull(message = "文件大小不能为空")
    @Min(value = 1, message = "文件大小必须大于0")
    private Long fileSize;

    /**
     * 文件MD5值
     */
    @Schema(description = "文件MD5值", example = "5d41402abc4b2a76b9719d911017c592")
    @NotBlank(message = "文件MD5值不能为空")
    private String fileMd5;

    /**
     * 文件MIME类型
     */
    @Schema(description = "文件MIME类型", example = "application/zip")
    private String contentType;

    /**
     * 存储路径
     */
    @Schema(description = "存储父路径", example = "/upload/files/")
    private String parentPath;

    /**
     * 文件元信息
     */
    @Schema(description = "文件元信息")
    private Map<String, String> metaData;
}