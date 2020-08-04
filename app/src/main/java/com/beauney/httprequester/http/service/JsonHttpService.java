package com.beauney.httprequester.http.service;

import com.beauney.httprequester.http.exception.HttpRequestFailedException;
import com.beauney.httprequester.http.interfaces.IHttpListener;
import com.beauney.httprequester.http.interfaces.IHttpService;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;


/**
 * Json Http处理
 * @author zengjiantao
 * @since 2020-08-04
 */
public class JsonHttpService implements IHttpService {
    private String mUrl;

    private IHttpListener mHttpListener;

    private byte[] mRequestData;

    private HttpClient mHttpClient = new DefaultHttpClient();

    private HttpPost mHttpPost;

    private HttpResponseHandler mHttpResponseHandler = new HttpResponseHandler();

    @Override
    public void execute() {
        mHttpPost = new HttpPost(mUrl);
        ByteArrayEntity byteArrayEntity = new ByteArrayEntity(mRequestData);
        mHttpPost.setEntity(byteArrayEntity);

        try {
            mHttpClient.execute(mHttpPost, mHttpResponseHandler);
        } catch (IOException e) {
            e.printStackTrace();
            mHttpListener.onFail(e);
        }
    }

    @Override
    public void setUrl(String url) {
        mUrl = url;
    }

    @Override
    public void setRequestData(byte[] requestData) {
        mRequestData = requestData;
    }

    @Override
    public void setHttpListener(IHttpListener httpListener) {
        mHttpListener = httpListener;
    }

    private class HttpResponseHandler extends BasicResponseHandler {
        @Override
        public String handleResponse(HttpResponse response) throws IOException {
            int code = response.getStatusLine().getStatusCode();
            if (code == 200) {
                mHttpListener.onSuccess(response.getEntity());
            } else {
                mHttpListener.onFail(new HttpRequestFailedException(code));
            }
            return null;
        }
    }
}
