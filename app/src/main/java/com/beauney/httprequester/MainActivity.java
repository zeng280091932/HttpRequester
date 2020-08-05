package com.beauney.httprequester;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.beauney.httprequester.http.HttpRequester;
import com.beauney.httprequester.http.LoginResponse;
import com.beauney.httprequester.http.download.DownloadFileManager;
import com.beauney.httprequester.http.download.DownloadItemInfo;
import com.beauney.httprequester.http.download.interfaces.IDownloadServiceCallable;
import com.beauney.httprequester.http.interfaces.IDataListener;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "Debug";

    private static final String URL = "http://192.168.1.9:3000/dologin";

    private TextView mDataTxt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDataTxt = findViewById(R.id.data);
    }

    public void fetchData(View view) {
        showToast("获取数据");
//        mDataTxt.setText("获取数据");
        for (int i = 0; i < 30; i++) {
            User user = new User();
            user.setName("zengxiaotao");
            user.setPassword("123456");
            HttpRequester.sendRequest(URL, user, LoginResponse.class, new IDataListener<LoginResponse>() {
                @Override
                public void onSuccess(LoginResponse loginResponse) {
                    Log.d("Debug", loginResponse.toString());
                }

                @Override
                public void onFail(Throwable throwable) {
                    throwable.printStackTrace();
                }
            });
        }
    }

    public void downloadFile(View view) {
        DownloadFileManager downloadFileManager = new DownloadFileManager(new IDownloadServiceCallable() {


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
            public void onDownloadError(DownloadItemInfo downloadItemInfo, int var2, String var3) {

            }
        });
        downloadFileManager.download("http://eqcdn.beauney.net/src/app/13/Beauney_V1.0.5_201912181552_Hawaii.apk");
    }

    private void showToast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }
}
