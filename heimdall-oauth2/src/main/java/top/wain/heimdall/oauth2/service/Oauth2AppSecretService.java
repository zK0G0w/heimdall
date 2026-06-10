package top.wain.heimdall.oauth2.service;

import top.wain.heimdall.oauth2.model.resp.Oauth2AppSecretCreateResp;
import top.wain.heimdall.oauth2.model.resp.Oauth2AppSecretResp;

import java.util.List;

/**
 * @Description: OAuth2 应用密钥 Service 接口
 * @Author: WainZeng
 * @Date: 2026/06/10
 */
public interface Oauth2AppSecretService {

    /**
     * 创建密钥
     */
    Oauth2AppSecretCreateResp create(Long appId);

    /**
     * 查询密钥列表（脱敏）
     */
    List<Oauth2AppSecretResp> list(Long appId);

    /**
     * 删除密钥
     */
    void delete(Long appId, Long secretId);
}
