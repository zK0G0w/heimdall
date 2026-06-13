package top.wain.heimdall.oauth2.controller;

import cn.dev33.satoken.stp.StpUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.wain.heimdall.oauth2.model.resp.Oauth2UserGrantResp;
import top.wain.heimdall.oauth2.service.Oauth2UserGrantService;

import java.util.List;

/**
 * @Description: 用户已授权应用管理（用户自助操作）
 * @Author: WainZeng
 * @Date: 2026/06/13
 */
@Tag(name = "用户授权管理 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/auth/user/grants")
public class Oauth2UserGrantController {

    private final Oauth2UserGrantService userGrantService;

    @Operation(summary = "查询已授权应用列表")
    @GetMapping
    public List<Oauth2UserGrantResp> list() {
        Long userId = StpUtil.getLoginIdAsLong();
        return userGrantService.listByUserId(userId);
    }

    @Operation(summary = "撤销指定应用的授权")
    @DeleteMapping("/{appId}")
    public void revoke(@PathVariable Long appId) {
        Long userId = StpUtil.getLoginIdAsLong();
        userGrantService.revokeGrant(userId, appId);
    }

    @Operation(summary = "撤销所有应用的授权")
    @DeleteMapping
    public void revokeAll() {
        Long userId = StpUtil.getLoginIdAsLong();
        userGrantService.revokeAllGrants(userId);
    }
}
