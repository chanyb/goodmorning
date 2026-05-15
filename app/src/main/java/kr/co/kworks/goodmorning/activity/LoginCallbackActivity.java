package kr.co.kworks.goodmorning.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;


import kr.co.kworks.goodmorning.utils.Logger;

public class LoginCallbackActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Uri uri = getIntent().getData();
        if(uri != null) {
            String result = uri.getQueryParameter("result");
            String token = uri.getQueryParameter("join".equalsIgnoreCase(result) ? "join_token":"login_token");

            setCallbackResult(this, result);
            setToken(this, token);
        }

        Intent intent = new Intent(this, SinglePageActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);

        finish();
    }

    private void setCallbackResult(Context context, String value) {
        SharedPreferences prefs = context.getSharedPreferences("result_prefs", Context.MODE_PRIVATE);
        prefs.edit()
            .putString("result_prefs", value)
            .apply();
    }

    private void setToken(Context context, String token) {
        SharedPreferences prefs = context.getSharedPreferences("token", Context.MODE_PRIVATE);
        prefs.edit()
            .putString("token", token)
            .apply();
    }
}