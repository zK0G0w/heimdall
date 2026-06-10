package top.wain.heimdall.system.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import top.continew.starter.core.enums.BaseEnum;

/**
 * 注销模式枚举
 *
 * @author KAI
 * @since 2025/10/28 14:04
 */
@Getter
@RequiredArgsConstructor
public enum LogoutModeEnum implements BaseEnum<String> {

    /**
     * 注销下线
     */
    LOGOUT("LOGOUT", "注销下线"),

    /**
     * 踢人下线
     */
    KICKOUT("KICKOUT", "踢人下线"),

    /**
     * 顶人下线
     */
    REPLACED("REPLACED", "顶人下线");

    private final String value;
    private final String description;
}