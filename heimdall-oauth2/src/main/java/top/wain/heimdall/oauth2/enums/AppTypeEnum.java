package top.wain.heimdall.oauth2.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import top.continew.starter.core.enums.BaseEnum;

/**
 * @Description: OAuth2 应用类型枚举
 * @Author: WainZeng
 * @Date: 2026/06/10
 */
@Getter
@RequiredArgsConstructor
public enum AppTypeEnum implements BaseEnum<Integer> {

    WEB(1, "Web应用"), MOBILE(2, "移动应用"), SERVER(3, "服务端应用");

    private final Integer value;
    private final String description;
}
