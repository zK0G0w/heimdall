package top.wain.heimdall.system.model.resp;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import top.wain.heimdall.common.base.model.resp.BaseDetailResp;
import top.wain.heimdall.common.enums.DisEnableStatusEnum;
import top.wain.heimdall.system.enums.StorageTypeEnum;

import java.io.Serial;

/**
 * 存储响应参数
 *
 * @author WainZeng
 * @since 2023/12/26 22:09
 */
@Data
@Schema(description = "存储响应参数")
public class StorageResp extends BaseDetailResp {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 名称
     */
    @Schema(description = "名称", example = "存储1")
    private String name;

    /**
     * 编码
     */
    @Schema(description = "编码", example = "local")
    private String code;

    /**
     * 状态
     */
    @Schema(description = "状态", example = "1")
    private DisEnableStatusEnum status;

    /**
     * 类型
     */
    @Schema(description = "类型", example = "2")
    private StorageTypeEnum type;

    /**
     * Access Key
     */
    @Schema(description = "Access Key", example = "")
    private String accessKey;

    /**
     * Endpoint
     */
    @Schema(description = "Endpoint", example = "")
    private String endpoint;

    /**
     * Bucket/存储路径
     */
    @Schema(description = "Bucket/存储路径", example = "C:/heimdall/data/file/")
    private String bucketName;

    /**
     * 域名/访问路径
     */
    @Schema(description = "域名", example = "http://localhost:8000/file")
    private String domain;

    /**
     * 启用回收站
     */
    @Schema(description = "启用回收站", example = "true")
    private Boolean recycleBinEnabled;

    /**
     * 回收站路径
     */
    @Schema(description = "回收站路径", example = ".RECYCLE.BIN/")
    private String recycleBinPath;

    /**
     * 描述
     */
    @Schema(description = "描述", example = "存储描述")
    private String description;

    /**
     * 是否为默认存储
     */
    @Schema(description = "是否为默认存储", example = "true")
    private Boolean isDefault;

    /**
     * 排序
     */
    @Schema(description = "排序", example = "1")
    private Integer sort;

    @Override
    public Boolean getDisabled() {
        return this.getIsDefault();
    }

}