package com.beauney.httprequester.http.exception;

/**
 * 网络请求失败异常封装
 *
 * @author zengjiantao
 * @since 2020-08-04
 */
public class HttpRequestFailedException extends Exception {
    private int mCode;

    public HttpRequestFailedException(int mCode) {
        this.mCode = mCode;
    }

    public HttpRequestFailedException(String message, int mCode) {
        super(message);
        this.mCode = mCode;
    }

    public int getCode() {
        return mCode;
    }
}
