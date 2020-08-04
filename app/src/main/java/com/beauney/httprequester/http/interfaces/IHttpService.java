package com.beauney.httprequester.http.interfaces;

import org.apache.http.HttpEntity;

/**
 * 获取网络
 *
 * @author zengjiantao
 * @since 2020-08-04
 */
public interface IHttpService {
    /**
     * 设置访问链接
     *
     * @param url
     */
    void setUrl(String url);

    /**
     * 设置网络请求参数
     *
     * @param requestData
     */
    void setRequestData(byte[] requestData);

    /**
     * 执行网络请求
     */
    void execute();

    /**
     * 设置处理接口
     *
     * @param httpListener
     */
    void setHttpListener(IHttpListener httpListener);
}
