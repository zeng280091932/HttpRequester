package com.beauney.httprequester.http.interfaces;

/**
 * 调用层结果放回
 *
 * @author zengjiantao
 * @since 2020-08-04
 */
public interface IDataListener<M> {
    /**
     * 处理成功返回结果
     *
     * @param m
     */
    void onSuccess(M m);

    /**
     * 处理失败结果
     */
    void onFail(Throwable throwable);

}
