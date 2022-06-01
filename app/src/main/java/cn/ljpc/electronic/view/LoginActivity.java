package cn.ljpc.electronic.view;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.RequestQueue;
import com.google.gson.Gson;

import java.util.Map;
import java.util.Objects;

import cn.ljpc.electronic.AppApplication;
import cn.ljpc.electronic.R;
import cn.ljpc.electronic.model.CommonResult;
import cn.ljpc.electronic.model.Constant;
import cn.ljpc.electronic.network.CustomerStringRequest;
import cn.ljpc.electronic.util.MapUtil;
import cn.ljpc.electronic.util.Util;

public class LoginActivity extends AppCompatActivity {

    private EditText etUserName;
    private String userName = "";
    private String userPassword = "";
    private AlertDialog alertDialog;
    private Context context;

    private static final String TAG = LoginActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        context = LoginActivity.this;
        initView();
    }

    private void initView() {
        etUserName = findViewById(R.id.et_userName);
        EditText etUserPassword = findViewById(R.id.et_password);
        ImageView unameClear = findViewById(R.id.iv_unameClear);
        ImageView pwdClear = findViewById(R.id.iv_pwdClear);
        Button btnLogin = findViewById(R.id.btn_login);
        btnLogin.setOnClickListener(v -> {
            if (userName.trim().isEmpty()) {
                Util.createDialog(context, Constant.DIALOG_TITLE_TIP, Constant.DIALOG_CONTENT_USERNAME_EMPTY).show();
                return;
            }

            if (userPassword.isEmpty()) {
                Util.createDialog(context, Constant.DIALOG_TITLE_TIP, Constant.DIALOG_CONTENT_PWD_EMPTY).show();
                return;
            }
            //展示loading效果
            alertDialog = Util.showLoadingDialog(context);
            CustomerStringRequest stringRequest = new CustomerStringRequest(Constant.APP_LOGIN_URL, resultStr -> {
                Gson gson = new Gson();
                CommonResult commonResult = gson.fromJson(resultStr, CommonResult.class);
                Log.d(TAG, commonResult.toString());
                Util.dismissLoadingDialog(alertDialog);
                if (commonResult.getCode() != 200) {
                    Util.createDialog(context, "错误", Objects.requireNonNull(commonResult.getData().get("msg")).toString()).show();
                } else {
                    Toast.makeText(context, Constant.TOAST_TEXT_LOGIN_SUCCESS, Toast.LENGTH_SHORT).show();
                    //登录成功
                    AppApplication.username = userName;
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                }
            }, error -> {
                Log.d(TAG, error.toString());
                Util.dismissLoadingDialog(alertDialog);
                Util.createDialog(context, Constant.DIALOG_TITLE_ERROR, Constant.DIALOG_CONTENT_NETWORK_ERROR).show();

            });
            Map<String, String> map = MapUtil.crateMap().data("loginname", userName).data("pwd", userPassword).getMap();
            stringRequest.setParams(map);
            stringRequest.setTag(Constant.STRING_REQUEST_LOGIN_TAG);
            AppApplication.getRequestQueue().add(stringRequest);
        });

        addClearListener(etUserName, unameClear);
        addClearListener(etUserPassword, pwdClear);
    }

    private void addClearListener(final EditText et, final ImageView iv) {
        et.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                //如果有输入内容长度大于0那么显示clear按钮
                if (et == etUserName) {
                    userName = s + "";
                } else {
                    userPassword = s + "";
                }
                if (s.length() > 0) {
                    iv.setVisibility(View.VISIBLE);
                } else {
                    iv.setVisibility(View.INVISIBLE);
                }
            }
        });

        iv.setOnClickListener(v -> {
            et.setText("");
            if (et == etUserName) {
                userName = "";
            } else {
                userPassword = "";
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        RequestQueue requestQueue = AppApplication.getRequestQueue();
        if (requestQueue != null) {
            requestQueue.cancelAll(Constant.STRING_REQUEST_LOGIN_TAG);
        }
    }
}