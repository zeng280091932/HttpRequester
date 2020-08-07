package com.beauney.httprequester.http.task;

import com.beauney.httprequester.http.ThreadPoolManager;
import com.beauney.httprequester.http.download.interfaces.IDownloadService;
import com.beauney.httprequester.http.interfaces.IHttpService;

import java.util.concurrent.FutureTask;

/**
 * @author zengjiantao
 * @since 2020-08-04
 */
public class HttpTask implements Runnable {
    private IHttpService mHttpService;
    private FutureTask mFutureTask;

    public HttpTask(IHttpService httpService) {
        this.mHttpService = httpService;
    }

    @Override
    public void run() {
        mHttpService.execute();
    }

    /**
     * 新增方法
     */
    public void start() {
        mFutureTask = new FutureTask(this, null);
        try {
            ThreadPoolManager.getInstance().execute(mFutureTask);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    /**
     * 新增方法
     */
    public void pause() {
        if (mHttpService instanceof IDownloadService) {
            ((IDownloadService) mHttpService).pause();
        }
        if (mFutureTask != null) {
            ThreadPoolManager.getInstance().removeTask(mFutureTask);
        }
    }
}
