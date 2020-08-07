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
import com.beauney.httprequester.http.download.enums.DownloadStatus;
import com.beauney.httprequester.http.download.enums.DownloadStopMode;
import com.beauney.httprequester.http.download.interfaces.IDownloadCallable;
import com.beauney.httprequester.http.download.interfaces.IDownloadServiceCallable;
import com.beauney.httprequester.http.interfaces.IDataListener;

public class MainActivity extends AppCompatActivity {
    private static final String URL = "http://192.168.1.9:3000/dologin";

    private TextView mDataTxt;

    private DownloadFileManager downloadFileManager = new DownloadFileManager();

    private int mRecordId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDataTxt = findViewById(R.id.data);
        downloadFileManager.setDownCallable(new IDownloadCallable() {
            @Override
            public void onDownloadInfoAdd(int downloadId) {

            }

            @Override
            public void onDownloadInfoRemove(int downloadId) {

            }

            @Override
            public void onDownloadStatusChanged(int downloadId, DownloadStatus status) {
                Log.d("Debug", "status:" + status);
            }

            @Override
            public void onTotalLengthReceived(int downloadId, long totalLength) {
                Log.d("Debug", "totalLength:" + totalLength);
            }

            @Override
            public void onCurrentSizeChanged(int downloadId, double downloadpercent, long speed) {
                Log.d("Debug", "downloadpercent:" + downloadpercent + "\nspeed:" + speed);
            }

            @Override
            public void onDownloadSuccess(int downloadId) {

            }

            @Override
            public void onDownloadError(int downloadId, int errorCode, String errorMsg) {
                Log.d("Debug", "errorCode:" + errorCode + "\nerrorMsg:" + errorMsg);
            }
        });
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
//        mRecordId = downloadFileManager.download("http://eqcdn.beauney.net/src/app/13/Beauney_V1.0.5_201912181552_Hawaii.apk");
        mRecordId = downloadFileManager.download("http://eqcdn.beauney.net/src/app/14/Beauney_V1.0.7_202007061351_Colourful.apk");
    }

    public void pause(View view) {
        if (mRecordId != 0) {
            downloadFileManager.pause(mRecordId, DownloadStopMode.hand);
        }
    }

    private void showToast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }


}
