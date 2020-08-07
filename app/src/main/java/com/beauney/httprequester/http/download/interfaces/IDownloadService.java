package com.beauney.httprequester.http.download.interfaces;

import com.beauney.httprequester.http.interfaces.IHttpService;

import java.util.Map;

/**
 * @author zengjiantao
 * @since 2020-08-05
 */
public interface IDownloadService extends IHttpService {
    void pause();

    Map<String, String> getHttpHeaderMap();

    boolean cancel();

    boolean isCancel();

    boolean isPause();

    void abortRequest();
}
