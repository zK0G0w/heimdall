package top.wain.heimdall.tenant.model.resp;

import lombok.Data;

import java.util.List;

/**
 * 租户通用信息返回
 * 
 * @author 小熊
 * @since 2024/11/28 09:53
 */
@Data
public class TenantCommonResp {

    /**
     * 是否开启了租户
     */
    private Boolean isEnabled;

    /**
     * 可用租户列表
     */
    private List<TenantAvailableResp> availableList;

}
