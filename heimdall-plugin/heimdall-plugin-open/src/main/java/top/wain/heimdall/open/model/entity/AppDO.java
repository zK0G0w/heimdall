package top.wain.heimdall.open.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import top.wain.heimdall.common.base.model.entity.BaseDO;
import top.wain.heimdall.common.enums.DisEnableStatusEnum;
import top.continew.starter.encrypt.field.annotation.FieldEncrypt;

import java.io.Serial;
import java.time.LocalDateTime;

/**
 * 应用实体
 *
 * @author chengzi
 * @author WainZeng
 * @since 2024/10/17 16:03
 */
@Data
@TableName("sys_app")
public class AppDO extends BaseDO {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 名称
     */
    private String name;

    /**
     * Access Key（访问密钥）
     */
    @FieldEncrypt
    private String accessKey;

    /**
     * Secret Key（私有密钥）
     */
    @FieldEncrypt
    private String secretKey;

    /**
     * 失效时间
     */
    private LocalDateTime expireTime;

    /**
     * 描述
     */
    private String description;

    /**
     * 状态
     */
    private DisEnableStatusEnum status;

    /**
     * 是否已过期
     *
     * @return true：已过期；false：未过期
     */
    public boolean isExpired() {
        if (expireTime == null) {
            return false;
        }
        return LocalDateTime.now().isAfter(expireTime);
    }
}