package com.beauney.httprequester.http;

import android.util.Log;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 线程池管理
 *
 * @author zengjiantao
 * @since 2020-08-04
 */
public class ThreadPoolManager {
    private static ThreadPoolManager mInstance = new ThreadPoolManager();

    private LinkedBlockingDeque<FutureTask<?>> mTaskQueue = new LinkedBlockingDeque<>();

    private ThreadPoolExecutor mThreadPoolExecutor;

    private ThreadPoolManager() {
        mThreadPoolExecutor = new ThreadPoolExecutor(4, 10, 10, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(4), mHandler);
        mThreadPoolExecutor.execute(mRunnable);
    }

    public static ThreadPoolManager getInstance() {
        return mInstance;
    }

    public <T> void execute(FutureTask<T> futureTask) throws InterruptedException {
        mTaskQueue.put(futureTask);
    }

    public <T> boolean removeTask(FutureTask futureTask) {
        boolean result = false;
        /**
         * 阻塞式队列是否含有线程
         */
        if (mTaskQueue.contains(futureTask)) {
            mTaskQueue.remove(futureTask);
        } else {
            result = mThreadPoolExecutor.remove(futureTask);
        }
        return result;
    }

    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            while (true) {
                FutureTask<?> futureTask = null;
                try {
                    Log.d(ThreadPoolManager.class.getSimpleName(), "等待队列数：" + mTaskQueue.size());
                    futureTask = mTaskQueue.take();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                if (futureTask != null) {
                    mThreadPoolExecutor.execute(futureTask);
                }
                Log.d(ThreadPoolManager.class.getSimpleName(), "线程池大小：" + mThreadPoolExecutor.getPoolSize());
            }
        }
    };

    private RejectedExecutionHandler mHandler = new RejectedExecutionHandler() {
        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
            try {
                mTaskQueue.put(new FutureTask<>(r, null));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    };
}
