package com.beauney.httprequester.http.download;

/**
 * @author zengjiantao
 * @since 2020-08-05
 */
public enum DownloadStatus {
    waiting(0),

    starting(1),

    downloading(2),

    paused(3),

    finish(4),

    failed(5);

    private int value;

    DownloadStatus(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }
}
