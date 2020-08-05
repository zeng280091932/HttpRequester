package com.beauney.httprequester.http.download;

import com.beauney.httprequester.http.task.HttpTask;

/**
 * @author zengjiantao
 * @since 2020-08-05
 */
public class DownloadItemInfo extends BaseEntity<DownloadItemInfo> {
    private long currentLength;

    private long totalLength;

    private String url;

    private String filePath;

    private transient HttpTask httpTask;

    private DownloadStatus downloadStatus;

    public DownloadItemInfo() {
    }

    public DownloadItemInfo(String url, String filePath) {
        this.url = url;
        this.filePath = filePath;
    }

    public long getCurrentLength() {
        return currentLength;
    }

    public void setCurrentLength(long currentLength) {
        this.currentLength = currentLength;
    }

    public long getTotalLength() {
        return totalLength;
    }

    public void setTotalLength(long totalLength) {
        this.totalLength = totalLength;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public HttpTask getHttpTask() {
        return httpTask;
    }

    public void setHttpTask(HttpTask httpTask) {
        this.httpTask = httpTask;
    }

    public DownloadStatus getDownloadStatus() {
        return downloadStatus;
    }

    public void setDownloadStatus(DownloadStatus downloadStatus) {
        this.downloadStatus = downloadStatus;
    }
}
