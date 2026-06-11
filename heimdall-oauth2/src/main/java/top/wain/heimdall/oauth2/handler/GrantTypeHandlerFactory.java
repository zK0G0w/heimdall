package top.wain.heimdall.oauth2.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import top.wain.heimdall.oauth2.enums.GrantTypeEnum;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * @Description: OAuth2 授权类型处理器工厂，根据 grant_type 字符串路由到对应的处理器
 * @Author: WainZeng
 * @Date: 2026/06/11
 */
@Component
public class GrantTypeHandlerFactory {

    private final Map<GrantTypeEnum, GrantTypeHandler> handlerMap = new EnumMap<>(GrantTypeEnum.class);

    @Autowired
    public GrantTypeHandlerFactory(List<GrantTypeHandler> handlers) {
        for (GrantTypeHandler handler : handlers) {
            handlerMap.put(handler.getGrantType(), handler);
        }
    }

    /**
     * 根据 grant_type 字符串获取对应处理器
     *
     * @param grantType grant_type 字符串值
     * @return 对应的处理器，未匹配则返回 null
     */
    public GrantTypeHandler getHandler(String grantType) {
        for (GrantTypeEnum enumValue : GrantTypeEnum.values()) {
            if (enumValue.getValue().equals(grantType)) {
                return handlerMap.get(enumValue);
            }
        }
        return null;
    }
}
