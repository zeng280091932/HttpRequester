package com.beauney.httprequester.http.download;

import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.beauney.httprequester.http.download.dao.DownLoadDao;
import com.beauney.httprequester.http.download.enums.DownloadStatus;
import com.beauney.httprequester.http.download.enums.DownloadStopMode;
import com.beauney.httprequester.http.download.enums.Priority;
import com.beauney.httprequester.http.download.interfaces.IDownloadCallable;
import com.beauney.httprequester.http.download.interfaces.IDownloadListener;
import com.beauney.httprequester.http.download.interfaces.IDownloadService;
import com.beauney.httprequester.http.download.interfaces.IDownloadServiceCallable;
import com.beauney.httprequester.http.task.HttpTask;
import com.beauney.objectdb.BaseDaoFactory;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author zengjiantao
 * @since 2020-08-05
 */
public class DownloadFileManager implements IDownloadServiceCallable {
    private static final String TAG = "Debug";
    private final byte[] mLock = new byte[0];

    private byte[] lock = new byte[0];
    DownLoadDao mDownLoadDao = BaseDaoFactory.getInstance().
            getDataHelper(DownLoadDao.class, DownloadItemInfo.class);
    java.text.SimpleDateFormat mDateFormat = new java.text.SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
    /**
     * 观察者模式
     */
    private final List<IDownloadCallable> mDownloadCallableList = new CopyOnWriteArrayList<IDownloadCallable>();

    /**
     * 怎在下载的所有任务
     */
    private static List<DownloadItemInfo> downloadFileTaskList = new CopyOnWriteArrayList();

    Handler handler = new Handler(Looper.getMainLooper());


    public int download(String url) {
        String[] preFix = url.split("/");
        return this.download(url, Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + preFix[preFix.length - 1]);
    }

    public int download(String url, String filePath) {
        String[] preFix = url.split("/");
        String displayName = preFix[preFix.length - 1];
        return this.download(url, filePath, displayName);
    }

    public int download(String url, String filePath, String displayName) {
        return this.download(url, filePath, displayName, Priority.middle);
    }

    public int download(String url, String filePath,
                        String displayName, Priority priority) {
        if (priority == null) {
            priority = Priority.low;
        }
        File file = new File(filePath);
        DownloadItemInfo downloadItemInfo = null;

        downloadItemInfo = mDownLoadDao.findRecord(url, filePath);
        //没下载
        if (downloadItemInfo == null) {
            /**
             * 根据文件路径查找
             */
            List<DownloadItemInfo> samesFile = mDownLoadDao.findRecord(filePath);
            /**
             * 大于0  表示下载
             */
            if (samesFile.size() > 0) {
                DownloadItemInfo sameDown = samesFile.get(0);
                if (sameDown.getCurrentLen() == sameDown.getTotalLen()) {
                    synchronized (mDownloadCallableList) {
                        for (IDownloadCallable downloadCallable : mDownloadCallableList) {
                            downloadCallable.onDownloadError(sameDown.getId(), 2, "文件已经下载了");
                        }
                    }
                    return sameDown.getId();
                }
            }
            /**---------------------------------------------
             * 插入数据库
             * 可能插入失败
             * 因为filePath  和id是独一无二的  在数据库建表时已经确定了
             */
            int recordId = mDownLoadDao.addRecord(url, filePath, displayName, priority.getValue());
            if (recordId != -1) {
                //插入成功时，再次进行查找，确保能查得到
                downloadItemInfo = mDownLoadDao.findRecord(url, filePath);
                synchronized (mDownloadCallableList) {
                    for (IDownloadCallable downloadCallable : mDownloadCallableList) {
                        //通知应用层  数据库被添加了
                        downloadCallable.onDownloadInfoAdd(downloadItemInfo.getId());
                    }
                }
            } else {
                synchronized (mDownloadCallableList) {
                    for (IDownloadCallable downloadCallable : mDownloadCallableList) {
                        downloadCallable.onDownloadError(-1, -1, "插入数据库失败");
                    }
                }
            }
        }
        /**-----------------------------------------------
         * 括号写错了  放在外面
         *
         * 是否正在下载`
         */
        if (isDowning(file.getAbsolutePath())) {
            synchronized (mDownloadCallableList) {
                for (IDownloadCallable downloadCallable : mDownloadCallableList) {
                    downloadCallable.onDownloadError(downloadItemInfo.getId(), 4, "正在下载，请不要重复添加");
                }
            }
            return downloadItemInfo.getId();
        }

        if (downloadItemInfo != null) {
            downloadItemInfo.setPriority(priority.getValue());
            //添加----------------------------------------------------
            downloadItemInfo.setStopMode(DownloadStopMode.auto.getValue());

            //判断数据库存的 状态是否是完成
            if (downloadItemInfo.getStatus() != DownloadStatus.finish.getValue()) {
                if (downloadItemInfo.getTotalLen() == 0L || file.length() == 0L) {
                    Log.i(TAG, "还未开始下载");
                    //----------------------删除--------------------
                    downloadItemInfo.setStatus(DownloadStatus.failed.getValue());
                }
                //判断数据库中 总长度是否等于文件长度
                if (downloadItemInfo.getTotalLen() == file.length() && downloadItemInfo.getTotalLen() != 0) {
                    downloadItemInfo.setStatus(DownloadStatus.finish.getValue());
                    synchronized (mDownloadCallableList) {
                        for (IDownloadCallable downloadCallable : mDownloadCallableList) {
                            try {
                                downloadCallable.onDownloadError(downloadItemInfo.getId(), 4, "已经下载了");
                            } catch (Exception e) {
                            }
                        }
                    }
                }
            }
            //------------------添加--------
            else {
                if (!file.exists() || (downloadItemInfo.getTotalLen() != downloadItemInfo.getCurrentLen())) {
                    downloadItemInfo.setStatus(DownloadStatus.failed.getValue());
                }
            }
            /**
             *
             * 更新
             */
            mDownLoadDao.updateRecord(downloadItemInfo);
            //移到括号里面来----------------------------------------------------
            /**
             * 判断是否已经下载完成
             */
            if (downloadItemInfo.getStatus() == DownloadStatus.finish.getValue()) {
                Log.i(TAG, "已经下载完成  回调应用层");
                final int downId = downloadItemInfo.getId();
                synchronized (mDownloadCallableList) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            for (IDownloadCallable downloadCallable : mDownloadCallableList) {
                                downloadCallable.onDownloadStatusChanged(downId, DownloadStatus.finish);
                            }
                        }
                    });
                }
                mDownLoadDao.removeRecordFromMemory(downId);
                return downloadItemInfo.getId();
            }//之前的下载 状态为暂停状态
            List<DownloadItemInfo> allDowning = downloadFileTaskList;
            //当前下载不是最高级  则先退出下载
            if (priority != Priority.high) {
                for (DownloadItemInfo downling : allDowning) {
                    //从下载表中  获取到全部正在下载的任务
                    downling = mDownLoadDao.findSingleRecord(downling.getFilePath());

                    if (downling != null && downling.getPriority() == Priority.high.getValue()) {

                        /**
                         *     更改---------
                         *     当前下载级别不是最高级 传进来的是middle    但是在数据库中查到路径一模一样 的记录   所以他也是最高级------------------------------
                         *     比如 第一次下载是用最高级下载，app闪退后，没有下载完成，第二次传的是默认级别，这样就应该是最高级别下载

                         */
                        if (downling.getFilePath().equals(downloadItemInfo.getFilePath())) {
                            break;
                        } else {
                            return downloadItemInfo.getId();
                        }
//                        if(downloadItemInfo.getFilePath().equals(downling.getFilePath()))
//                        {
//                            return downloadItemInfo.getId();
//                        }
                    }
                }
            }
            //
            reallyDown(downloadItemInfo);
            if (priority == Priority.high || priority == Priority.middle) {
                synchronized (allDowning) {
                    for (DownloadItemInfo downloadItemInfo1 : allDowning) {
                        if (!downloadItemInfo.getFilePath().equals(downloadItemInfo1.getFilePath())) {
                            DownloadItemInfo downingInfo = mDownLoadDao.findSingleRecord(downloadItemInfo1.getFilePath());
                            if (downingInfo != null) {
                                pause(downloadItemInfo.getId(), DownloadStopMode.auto);
                            }
                        }
                    }
                }
                return downloadItemInfo.getId();
            }
        }
        return -1;
    }


    /**
     * 停止
     *
     * @param downloadId
     * @param mode
     */
    public void pause(int downloadId, DownloadStopMode mode) {
        if (mode == null) {
            mode = DownloadStopMode.auto;
        }
        final DownloadItemInfo downloadInfo = mDownLoadDao.findRecordById(downloadId);
        if (downloadInfo != null) {
            // 更新停止状态
            downloadInfo.setStopMode(mode.getValue());
            downloadInfo.setStatus(DownloadStatus.paused.getValue());
            mDownLoadDao.updateRecord(downloadInfo);
            DownloadItemInfo downingInfo = null;
            for (DownloadItemInfo downing : downloadFileTaskList) {
                if (downloadId == downing.getId()) {
                    downing.getHttpTask().pause();
                    downingInfo = downing;
                }
            }
            if (downingInfo != null) {
                downloadFileTaskList.remove(downingInfo);
            }
        }
    }

    /**
     * 判断当前是否正在下载
     *
     * @param absolutePath
     * @return
     */
    private boolean isDowning(String absolutePath) {
        for (DownloadItemInfo downloadItemInfo : downloadFileTaskList) {
            if (downloadItemInfo.getFilePath().equals(absolutePath)) {
                return true;
            }
        }
        return false;
    }


    /**
     * 添加观察者
     *
     * @param downloadCallable
     */
    public void setDownCallable(IDownloadCallable downloadCallable) {
        synchronized (mDownloadCallableList) {
            mDownloadCallableList.add(downloadCallable);
        }
    }

    /**
     * 下载
     */
    private DownloadItemInfo reallyDown(DownloadItemInfo downloadItemInfo) {
        synchronized (lock) {
            //设置请求下载的策略
            IDownloadService downloadService = new FileDownloadHttpService();
            //得到请求头的参数 map
            Map<String, String> map = downloadService.getHttpHeaderMap();
            /**
             * 处理结果的策略
             */
            //处理结果策略
            IDownloadListener downloadListener = new DownloadListener(downloadItemInfo, this, downloadService);
            downloadListener.addHttpHeader(map);

            downloadService.setHttpListener(downloadListener);
            downloadService.setUrl(downloadItemInfo.getUrl());

            HttpTask httpTask = new HttpTask(downloadService);
            downloadItemInfo.setHttpTask(httpTask);

            /**
             * 添加
             */
            downloadFileTaskList.add(downloadItemInfo);
            httpTask.start();
        }
        return downloadItemInfo;
    }

    @Override
    public void onDownloadStatusChanged(DownloadItemInfo downloadItemInfo) {

    }

    @Override
    public void onTotalLengthReceived(DownloadItemInfo downloadItemInfo) {

    }

    @Override
    public void onCurrentSizeChanged(DownloadItemInfo downloadItemInfo, double downLength, double speed) {
        Log.i(TAG, "下载速度：" + speed / 1000 + "k/s");
        Log.i(TAG, "-----路径  " + downloadItemInfo.getFilePath() + "  下载长度  " + downLength + "   速度  " + speed);
    }

    @Override
    public void onDownloadSuccess(DownloadItemInfo downloadItemInfo) {
        Log.i(TAG, "下载成功    路劲  " + downloadItemInfo.getFilePath() + "  url " + downloadItemInfo.getUrl());
    }

    @Override
    public void onDownloadPaused(DownloadItemInfo downloadItemInfo) {

    }

    @Override
    public void onDownloadError(DownloadItemInfo downloadItemInfo, int code, String reason) {

    }
}
