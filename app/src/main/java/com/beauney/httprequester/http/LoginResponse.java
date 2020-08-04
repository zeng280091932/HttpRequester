package com.beauney.httprequester.http;

import java.io.Serializable;

/**
 * @author zengjiantao
 * @since 2020-08-04
 */
public class LoginResponse implements Serializable {
    private int code;
    private String msg;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    @Override
    public String toString() {
        return "LoginResponse{" +
                "code=" + code +
                ", msg='" + msg + '\'' +
                '}';
    }
}
