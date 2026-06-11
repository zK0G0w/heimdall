package top.wain.heimdall.oauth2.exception;

import lombok.Getter;

/**
 * @Description: OAuth2 协议异常
 * @Author: WainZeng
 * @Date: 2026/06/11
 */
@Getter
public class Oauth2Exception extends RuntimeException {

    private final String error;
    private final String errorDescription;
    private final int httpStatus;

    public Oauth2Exception(String error, String errorDescription) {
        this(error, errorDescription, 400);
    }

    public Oauth2Exception(String error, String errorDescription, int httpStatus) {
        super(error + ": " + errorDescription);
        this.error = error;
        this.errorDescription = errorDescription;
        this.httpStatus = httpStatus;
    }
}
