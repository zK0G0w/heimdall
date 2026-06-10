package top.wain.heimdall.open.util;

import cn.dev33.satoken.context.SaHolder;
import cn.dev33.satoken.context.model.SaRequest;
import cn.dev33.satoken.sign.template.SaSignTemplate;

import java.util.Collection;

/**
 * Open Api 工具类
 *
 * @author chengzi
 * @author WainZeng
 * @since 2024/10/25 15:31
 */
public class OpenApiUtils {

    private OpenApiUtils() {
    }

    /**
     * 判断请求是否包含sign参数
     *
     * @return 是否包含sign参数（true：包含；false：不包含）
     */
    public static boolean isSignParamExists() {
        SaRequest saRequest = SaHolder.getRequest();
        Collection<String> paramNames = saRequest.getParamNames();
        return paramNames.stream().anyMatch(SaSignTemplate.sign::equals);
    }
}
