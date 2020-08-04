package com.beauney.httprequester.http.deal;

import android.os.Handler;
import android.os.Looper;

import com.alibaba.fastjson.JSON;
import com.beauney.httprequester.http.interfaces.IDataListener;
import com.beauney.httprequester.http.interfaces.IHttpListener;

import org.apache.http.HttpEntity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;


/**
 * Json转化处理类
 *
 * @author zengjiantao
 * @since 2020-08-04
 */
public class JsonDealListener<M> implements IHttpListener {
    private Class<M> mResponse;

    private IDataListener<M> mDataListener;

    private Handler mHandler = new Handler(Looper.getMainLooper());

    public JsonDealListener(Class<M> response, IDataListener<M> dataListener) {
        this.mResponse = response;
        this.mDataListener = dataListener;
    }

    @Override
    public void onSuccess(HttpEntity httpEntity) {
        InputStream inputStream = null;
        try {
            inputStream = httpEntity.getContent();

            //得到网络放回的数据，运行在子线程中
            String content = getContent(inputStream);
            final M m = JSON.parseObject(content, mResponse);
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mDataListener.onSuccess(m);
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
            handleError(e);
        }
    }

    @Override
    public void onFail(Throwable throwable) {
        handleError(throwable);
    }

    private void handleError(final Throwable throwable) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mDataListener.onFail(throwable);
            }
        });
    }

    private String getContent(InputStream inputStream) {
        BufferedReader reader = null;
        InputStreamReader inputStreamReader = null;
        StringBuilder builder = new StringBuilder();
        String line = null;
        try {
            inputStreamReader = new InputStreamReader(inputStream);
            reader = new BufferedReader(inputStreamReader);
            while ((line = reader.readLine()) != null) {
                builder.append(line + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
            handleError(e);
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
                if (inputStreamReader != null) {
                    inputStreamReader.close();
                }
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return builder.toString();
    }
}
