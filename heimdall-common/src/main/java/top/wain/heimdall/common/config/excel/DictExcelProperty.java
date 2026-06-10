package top.wain.heimdall.common.config.excel;

import java.lang.annotation.*;

/**
 * 字典字段注解
 *
 * @author WainZeng
 * @since 2025/4/9 20:25
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface DictExcelProperty {

    /**
     * 字典编码
     *
     * @return 字典编码
     */
    String value();
}
