package top.wain.heimdall.auth.mfa.service;

import top.wain.heimdall.auth.mfa.model.resp.MfaSetupResp;
import top.wain.heimdall.auth.mfa.model.resp.MfaStatusResp;

import java.util.List;

/**
 * @Description: MFA 业务接口
 * @Author: WainZeng
 * @Date: 2026/06/13
 */
public interface MfaService {

    MfaSetupResp initSetup(Long userId);

    List<String> confirmSetup(Long userId, String code);

    void disable(Long userId, String code);

    List<String> regenerateBackupCodes(Long userId, String code);

    MfaStatusResp getStatus(Long userId);
}
