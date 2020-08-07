package com.beauney.httprequester.http.download;

import android.os.Handler;
import android.os.Looper;

import com.beauney.httprequester.http.download.enums.DownloadStatus;
import com.beauney.httprequester.http.download.interfaces.IDownloadListener;
import com.beauney.httprequester.http.download.interfaces.IDownloadService;
import com.beauney.httprequester.http.download.interfaces.IDownloadServiceCallable;
import com.beauney.httprequester.http.interfaces.IHttpService;

import org.apache.http.HttpEntity;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * @author zengjiantao
 * @since 2020-08-05
 */
public class DownloadListener implements IDownloadListener {

    private DownloadItemInfo mDownloadItemInfo;

    private File mFile;

    private String url;

    private long mBreakPoint;

    private IDownloadServiceCallable mDownloadServiceCallable;

    private IDownloadService mDownloadService;

    private Handler mHandler = new Handler(Looper.getMainLooper());

    private final byte[] mLock = new byte[0];

    public DownloadListener(DownloadItemInfo downloadItemInfo) {
        mDownloadItemInfo = downloadItemInfo;
    }

    public DownloadListener(DownloadItemInfo downloadItemInfo,
                            IDownloadServiceCallable downloadServiceCallable,
                            IDownloadService downloadService) {
        mDownloadItemInfo = downloadItemInfo;
        mDownloadServiceCallable = downloadServiceCallable;
        mDownloadService = downloadService;

        mFile = new File(downloadItemInfo.getFilePath());
        //得到已经下载的长度
        mBreakPoint = mFile.length();
    }

    @Override
    public void setHttpService(IHttpService httpService) {
        mDownloadService = (IDownloadService) httpService;
    }

    @Override
    public void setCancelCallable() {

    }

    @Override
    public void setPauseCallable() {

    }

    @Override
    public void addHttpHeader(Map<String, String> headerMap) {
        long length = getFile().length();
        if (length > 0L) {
            headerMap.put("RANGE", "bytes=" + length + "-");
        }
    }

    @Override
    public void onSuccess(HttpEntity httpEntity) {
        InputStream inputStream = null;
        try {
            inputStream = httpEntity.getContent();
        } catch (IOException e) {
            e.printStackTrace();
        }

        long startTime = System.currentTimeMillis();
        //用于计算每秒多少k
        long speed = 0L;
        //花费时间
        long useTime = 0L;
        //下载的长度
        long getLen = 0L;
        //接受的长度
        long receiveLen = 0L;
        boolean bufferLen = false;
        //得到下载的长度
        long dataLength = httpEntity.getContentLength();
        //单位时间下载的字节数
        long calcSpeedLen = 0L;
        //总数
        long totalLength = mBreakPoint + dataLength;
        //更新数量
        this.receiveTotalLength(totalLength);
        //更新状态
        this.downloadStatusChange(DownloadStatus.downloading);
        byte[] buffer = new byte[512];
        int count = 0;
        long currentTime = System.currentTimeMillis();
        BufferedOutputStream bos = null;
        FileOutputStream fos = null;

        try {
            if (!makeDir(this.getFile().getParentFile())) {
                mDownloadServiceCallable.onDownloadError(mDownloadItemInfo, 1, "创建文件夹失败");
            } else {
                fos = new FileOutputStream(this.getFile(), true);
                bos = new BufferedOutputStream(fos);
                int length = 1;
                while ((length = inputStream.read(buffer)) != -1) {
                    if (this.getDownloadService().isCancel()) {
                        mDownloadServiceCallable.onDownloadError(mDownloadItemInfo, 1, "用户取消了");
                        return;
                    }

                    if (this.getDownloadService().isPause()) {
                        mDownloadServiceCallable.onDownloadError(mDownloadItemInfo, 2, "用户暂停了");
                        return;
                    }

                    bos.write(buffer, 0, length);
                    getLen += (long) length;
                    receiveLen += (long) length;
                    calcSpeedLen += (long) length;
                    ++count;
                    if (receiveLen * 10L / totalLength >= 1L || count >= 5000) {
                        currentTime = System.currentTimeMillis();
                        useTime = currentTime - startTime;
                        startTime = currentTime;
                        speed = 1000L * calcSpeedLen / useTime;
                        count = 0;
                        calcSpeedLen = 0L;
                        receiveLen = 0L;
                        this.downloadLengthChange(mBreakPoint + getLen, totalLength, speed);
                    }
                }
                if (dataLength != getLen) {
                    mDownloadServiceCallable.onDownloadError(mDownloadItemInfo, 3, "下载长度不相等");
                } else {
                    this.downloadLengthChange(mBreakPoint + getLen, totalLength, speed);
                    mDownloadServiceCallable.onDownloadSuccess(mDownloadItemInfo.copy());
                }
            }
        } catch (Exception e) {
            if (this.getDownloadService() != null) {
                this.getDownloadService().abortRequest();
            }
        } finally {
            try {
                if (bos != null) {
                    bos.close();
                }
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 创建文件夹的操作
     *
     * @param parentFile
     * @return
     */
    private boolean makeDir(File parentFile) {
        return parentFile.exists() && !parentFile.isFile()
                ? parentFile.exists() && parentFile.isDirectory() :
                parentFile.mkdirs();
    }


    private void downloadLengthChange(final long downLength, final long totalLength, final long speed) {
        mDownloadItemInfo.setCurrentLen(downLength);
        if (mDownloadServiceCallable != null) {
            final DownloadItemInfo downloadItemInfo = mDownloadItemInfo.copy();
            synchronized (mLock) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mDownloadServiceCallable.onCurrentSizeChanged(downloadItemInfo, ((double) downLength) / ((double) totalLength), speed);
                    }
                });
            }
        }
    }

    /**
     * 更改下载时的状态
     *
     * @param downloading
     */
    private void downloadStatusChange(DownloadStatus downloading) {
        mDownloadItemInfo.setStatus(downloading.getValue());
        final DownloadItemInfo copyDownloadItemInfo = mDownloadItemInfo.copy();
        if (mDownloadServiceCallable != null) {
            synchronized (mLock) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mDownloadServiceCallable.onDownloadStatusChanged(copyDownloadItemInfo);
                    }
                });
            }
        }
    }

    /**
     * 回调  长度的变化
     *
     * @param totalLength
     */
    private void receiveTotalLength(long totalLength) {
        mDownloadItemInfo.setCurrentLen(totalLength);
        final DownloadItemInfo copyDownloadItemInfo = mDownloadItemInfo.copy();
        if (mDownloadServiceCallable != null) {
            synchronized (mLock) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mDownloadServiceCallable.onTotalLengthReceived(copyDownloadItemInfo);
                    }
                });
            }
        }

    }

    @Override
    public void onFail(Throwable throwable) {

    }

    public File getFile() {
        return mFile;
    }

    public IDownloadService getDownloadService() {
        return mDownloadService;
    }
}
