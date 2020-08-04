package com.beauney.httprequester;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.beauney.httprequester.http.HttpRequester;
import com.beauney.httprequester.http.LoginResponse;
import com.beauney.httprequester.http.interfaces.IDataListener;

public class MainActivity extends AppCompatActivity {
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

    private void showToast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }
}
