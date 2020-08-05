package com.beauney.httprequester.http.download.interfaces;

import com.beauney.httprequester.http.interfaces.IHttpListener;
import com.beauney.httprequester.http.interfaces.IHttpService;

import java.util.Map;

/**
 * @author zengjiantao
 * @since 2020-08-05
 */
public interface IDownloadListener extends IHttpListener {
    void setHttpService(IHttpService httpService);

    void setCancelCallable();

    void setPauseCallable();

    void addHttpHeader(Map<String, String> headerMap);
}
