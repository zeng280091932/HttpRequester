package com.beauney.httprequester.http.task;

import com.beauney.httprequester.http.interfaces.IHttpService;

/**
 * @author zengjiantao
 * @since 2020-08-04
 */
public class HttpTask implements Runnable {
    private IHttpService mHttpService;

    public HttpTask(IHttpService httpService) {
        this.mHttpService = httpService;
    }

    @Override
    public void run() {
        mHttpService.execute();
    }
}
