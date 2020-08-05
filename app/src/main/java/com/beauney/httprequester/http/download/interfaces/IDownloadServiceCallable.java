package com.beauney.httprequester.http.download.interfaces;

import com.beauney.httprequester.http.download.DownloadItemInfo;

/**
 * @author zengjiantao
 * @since 2020-08-05
 */
public interface IDownloadServiceCallable {

    void onDownloadStatusChanged(DownloadItemInfo downloadItemInfo);

    void onTotalLengthReceived(DownloadItemInfo downloadItemInfo);

    void onCurrentSizeChanged(DownloadItemInfo downloadItemInfo, double downLength, double speed);

    void onDownloadSuccess(DownloadItemInfo downloadItemInfo);

    void onDownloadPaused(DownloadItemInfo downloadItemInfo);

    void onDownloadError(DownloadItemInfo downloadItemInfo, int code, String reason);
}
