package top.wain.heimdall.oauth2.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import top.wain.heimdall.oauth2.model.resp.Oauth2AppSecretCreateResp;
import top.wain.heimdall.oauth2.model.resp.Oauth2AppSecretResp;
import top.wain.heimdall.oauth2.service.Oauth2AppSecretService;

import java.util.List;

/**
 * @Description: OAuth2 应用密钥管理 Controller
 * @Author: WainZeng
 * @Date: 2026/06/10
 */
@Tag(name = "OAuth2 应用密钥管理 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/oauth2/app/{appId}/secret")
public class Oauth2AppSecretController {

    private final Oauth2AppSecretService oauth2AppSecretService;

    @Operation(summary = "创建应用密钥")
    @Parameter(name = "appId", description = "应用 ID", example = "1", in = ParameterIn.PATH)
    @SaCheckPermission("oauth2:app:update")
    @PostMapping
    public Oauth2AppSecretCreateResp create(@PathVariable("appId") Long appId) {
        return oauth2AppSecretService.create(appId);
    }

    @Operation(summary = "查询应用密钥列表")
    @Parameter(name = "appId", description = "应用 ID", example = "1", in = ParameterIn.PATH)
    @SaCheckPermission("oauth2:app:get")
    @GetMapping
    public List<Oauth2AppSecretResp> list(@PathVariable("appId") Long appId) {
        return oauth2AppSecretService.list(appId);
    }

    @Operation(summary = "删除应用密钥")
    @Parameter(name = "appId", description = "应用 ID", example = "1", in = ParameterIn.PATH)
    @Parameter(name = "secretId", description = "密钥 ID", example = "1", in = ParameterIn.PATH)
    @SaCheckPermission("oauth2:app:update")
    @DeleteMapping("/{secretId}")
    public void delete(@PathVariable("appId") Long appId, @PathVariable("secretId") Long secretId) {
        oauth2AppSecretService.delete(appId, secretId);
    }
}
