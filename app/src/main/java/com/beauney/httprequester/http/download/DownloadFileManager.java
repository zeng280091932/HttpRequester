package com.beauney.httprequester.http.download;

import android.os.Environment;

import com.beauney.httprequester.http.ThreadPoolManager;
import com.beauney.httprequester.http.download.interfaces.IDownloadListener;
import com.beauney.httprequester.http.download.interfaces.IDownloadService;
import com.beauney.httprequester.http.download.interfaces.IDownloadServiceCallable;
import com.beauney.httprequester.http.task.HttpTask;

import java.io.File;
import java.util.Map;
import java.util.concurrent.FutureTask;

/**
 * @author zengjiantao
 * @since 2020-08-05
 */
public class DownloadFileManager {
    private IDownloadServiceCallable mDownloadServiceCallable;

    private final byte[] mLock = new byte[0];

    public DownloadFileManager(IDownloadServiceCallable mDownloadServiceCallable) {
        this.mDownloadServiceCallable = mDownloadServiceCallable;
    }

    public void download(String url) {
        synchronized (mLock) {
            String[] preFix = url.split("/");
            String afterFix = preFix[preFix.length - 1];

            File file = new File(Environment.getExternalStorageDirectory(), afterFix);
            //实例化DownloadItemInfo
            DownloadItemInfo downloadItemInfo = new DownloadItemInfo(url, file.getAbsolutePath());

            //设置请求下载策略
            IDownloadService downloadService = new FileDownloadHttpService();
            //得到请求头
            Map<String, String> map = downloadService.getHttpHeaderMap();
            //处理结果策略
            IDownloadListener downloadListener = new DownloadListener(downloadItemInfo, mDownloadServiceCallable, downloadService);
            downloadListener.addHttpHeader(map);

            downloadService.setHttpListener(downloadListener);
            downloadService.setUrl(url);

            HttpTask httpTask = new HttpTask(downloadService);
            try {
                ThreadPoolManager.getInstance().execute(new FutureTask<>(httpTask, null));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
