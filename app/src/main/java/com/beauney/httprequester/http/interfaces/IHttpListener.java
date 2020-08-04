package com.beauney.httprequester.http.interfaces;

import org.apache.http.HttpEntity;

/**
 * 处理结果(框架层)
 * @author zengjiantao
 * @since 2020-08-04
 */
public interface IHttpListener {
    /**
     * 处理成功结果
     * @param httpEntity
     */
    void onSuccess(HttpEntity httpEntity);

    /**
     * 处理失败结果
     */
    void onFail(Throwable throwable);
}
