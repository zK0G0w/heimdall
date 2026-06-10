package top.wain.heimdall.common.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @Description: 统一响应信息
 * @Author: WainZeng
 * @Date: 2026/04/16
 */
@Data
@Schema(description = "响应信息")
public class R<T> {

    private static final String DEFAULT_SUCCESS_CODE = "0";
    private static final String DEFAULT_SUCCESS_MSG = "ok";
    private static final String DEFAULT_ERROR_CODE = "1";
    private static final String DEFAULT_ERROR_MSG = "error";

    @Schema(description = "状态码", example = "0")
    private String code;

    @Schema(description = "状态信息", example = "ok")
    private String msg;

    @Schema(description = "是否成功", example = "true")
    private boolean success;

    @Schema(description = "时间戳", example = "1691453288000")
    private Long timestamp;

    @Schema(description = "响应数据")
    private T data;

    /**
     * 获取时间戳（每次序列化时返回当前时间）
     */
    public Long getTimestamp() {
        return System.currentTimeMillis();
    }

    public R() {
    }

    public R(String code, String msg) {
        this.code = code;
        this.msg = msg;
        this.success = DEFAULT_SUCCESS_CODE.equals(code);
    }

    public R(String code, String msg, T data) {
        this(code, msg);
        this.data = data;
    }

    /**
     * 操作成功
     */
    public static <T> R<T> ok() {
        return new R<>(DEFAULT_SUCCESS_CODE, DEFAULT_SUCCESS_MSG);
    }

    /**
     * 操作成功
     *
     * @param data 响应数据
     */
    public static <T> R<T> ok(T data) {
        R<T> r = new R<>(DEFAULT_SUCCESS_CODE, DEFAULT_SUCCESS_MSG);
        r.setData(data);
        return r;
    }

    /**
     * 操作成功
     *
     * @param msg  业务状态信息
     * @param data 响应数据
     */
    public static <T> R<T> ok(String msg, T data) {
        R<T> r = new R<>(DEFAULT_SUCCESS_CODE, msg);
        r.setData(data);
        return r;
    }

    /**
     * 操作失败
     */
    public static R fail() {
        return new R<>(DEFAULT_ERROR_CODE, DEFAULT_ERROR_MSG);
    }

    /**
     * 操作失败
     *
     * @param code 业务状态码
     * @param msg  业务状态信息
     */
    public static R fail(String code, String msg) {
        return new R<>(code, msg);
    }
}
