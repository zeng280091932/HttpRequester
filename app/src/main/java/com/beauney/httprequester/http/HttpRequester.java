package com.beauney.httprequester.http;

import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.beauney.httprequester.http.deal.JsonDealListener;
import com.beauney.httprequester.http.interfaces.IDataListener;
import com.beauney.httprequester.http.interfaces.IHttpListener;
import com.beauney.httprequester.http.interfaces.IHttpService;
import com.beauney.httprequester.http.service.JsonHttpService;
import com.beauney.httprequester.http.task.HttpTask;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.FutureTask;

/**
 * @author zengjiantao
 * @since 2020-08-04
 */
public class HttpRequester {
    public static <T, M> void sendRequest(String url, T requestInfo, Class<M> response, IDataListener<M> dataListener) {
        IHttpService httpService = new JsonHttpService();
        httpService.setUrl(url);
        IHttpListener httpListener = new JsonDealListener<M>(response, dataListener);
        httpService.setHttpListener(httpListener);
        String requestInfoString = JSON.toJSONString(requestInfo);
        Log.d("Debug", requestInfoString);
        httpService.setRequestData(requestInfoString.getBytes(StandardCharsets.UTF_8));
        HttpTask httpTask = new HttpTask(httpService);
        try {
            ThreadPoolManager.getInstance().execute(new FutureTask<>(httpTask, null));
        } catch (InterruptedException e) {
            e.printStackTrace();
            dataListener.onFail(e);
        }

    }
}
