package com.beauney.httprequester.http.download;

import android.util.Log;

import com.beauney.httprequester.http.download.interfaces.IDownloadService;
import com.beauney.httprequester.http.exception.HttpRequestFailedException;
import com.beauney.httprequester.http.interfaces.IHttpListener;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author zengjiantao
 * @since 2020-08-05
 */
public class FileDownloadHttpService implements IDownloadService {
    private static final String TAG = "Debug";

    /**
     * 即将添加到请求头的信息
     */
    private Map<String, String> mHeaderMap = Collections.synchronizedMap(new HashMap<String, String>());

    private IHttpListener mHttpListener;

    private HttpClient mHttpClient = new DefaultHttpClient();

    private HttpGet mHttpGet;

    private String mUrl;

    private HttpResponseHandler mHttpResponseHandler = new HttpResponseHandler();

    /**
     * 判断是否暂停
     */
    private AtomicBoolean mPause = new AtomicBoolean(false);

    @Override
    public void pause() {
        mPause.compareAndSet(false, true);
    }

    @Override
    public Map<String, String> getHttpHeaderMap() {
        return mHeaderMap;
    }

    @Override
    public boolean cancel() {
        return false;
    }

    @Override
    public boolean isCancel() {
        return false;
    }

    @Override
    public boolean isPause() {
        return mPause.get();
    }

    @Override
    public void setUrl(String url) {
        mUrl = url;
    }

    @Override
    public void setRequestData(byte[] requestData) {

    }

    @Override
    public void execute() {
        mHttpGet = new HttpGet(mUrl);
        constructHeader();
        try {
            mHttpClient.execute(mHttpGet, mHttpResponseHandler);
        } catch (IOException e) {
            e.printStackTrace();
            mHttpListener.onFail(e);
        }
    }

    @Override
    public void setHttpListener(IHttpListener httpListener) {
        mHttpListener = httpListener;
    }

    @Override
    public void abortRequest(){
        mHttpGet.abort();
    }

    private void constructHeader() {
        Set<String> set = mHeaderMap.keySet();
        for (String key : set) {
            String value = mHeaderMap.get(key);
            Log.d(TAG, "请求头信息：key---" + key + "value---" + value);
            mHttpGet.addHeader(key, value);
        }
    }

    private class HttpResponseHandler extends BasicResponseHandler {
        @Override
        public String handleResponse(HttpResponse response) throws IOException {
            int code = response.getStatusLine().getStatusCode();
            if (code == 200 || code == 206) {
                mHttpListener.onSuccess(response.getEntity());
            } else {
                mHttpListener.onFail(new HttpRequestFailedException(code));
            }
            return null;
        }
    }
}
