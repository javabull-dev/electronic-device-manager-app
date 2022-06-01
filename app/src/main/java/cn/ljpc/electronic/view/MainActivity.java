package cn.ljpc.electronic.view;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.util.Log;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import cn.bingoogolapple.qrcode.core.QRCodeView;
import cn.bingoogolapple.qrcode.zxing.ZXingView;
import cn.ljpc.electronic.AppApplication;
import cn.ljpc.electronic.R;
import cn.ljpc.electronic.model.CommonResult;
import cn.ljpc.electronic.model.Constant;
import cn.ljpc.electronic.network.CustomerStringRequest;
import cn.ljpc.electronic.util.Util;

public class MainActivity extends AppCompatActivity {

    private ZXingView mQRCodeView;
    public static final Integer PERMISSION_CODE = 100;
    public static final int QRCODE_VIEW_DELAY_TIME = 2000;//2秒
    private double firstPressdTime = -2000;
    private boolean mMode = false; //false 出库，true 入库
    private SoundPool mSoundPool;
    private Context context;
    private boolean start=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        requirePermission();
        initView();
        initOther();
        context = MainActivity.this;
    }

    private void requirePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, PERMISSION_CODE);
        }
    }

    private void initView() {
        mQRCodeView = findViewById(R.id.zxingview);
        mQRCodeView.changeToScanQRCodeStyle(); //扫二维码
        mQRCodeView.setDelegate(new QRCodeView.Delegate() {
            @Override
            public void onScanQRCodeSuccess(String result) {
                if (!start) return;
                Log.d("lan", "result=" + result);
                //二维码解码出来的信息需要有 5部分信息
                /**
                 * goodsname, providername,uuid,number,inportprice
                 */
                /*******************识别数据***************************/
                String[] strs = result.split("\n");
                if (strs.length != 5) {
                    //继续扫描二维码
                    new Handler().postDelayed(() -> mQRCodeView.startSpot(), QRCODE_VIEW_DELAY_TIME);
                    return;
                }

                Map<String, String> map = new HashMap<>();
                for (String str : strs) {
                    int index = str.indexOf(':');
                    if (index < 0) {
                        //继续扫描二维码
                        new Handler().postDelayed(() -> mQRCodeView.startSpot(), QRCODE_VIEW_DELAY_TIME);
                        return;
                    } else {
                        String name = str.substring(0, index);
                        String value = str.substring(index + 1);
                        map.put(name, value);
                    }
                }
                if (!checkParams(map)) {
                    //继续扫描二维码
                    new Handler().postDelayed(() -> mQRCodeView.startSpot(), QRCODE_VIEW_DELAY_TIME);
                    return;
                }
                /*******************识别数据***************************/

                //震动，表示扫描到二维码中的数据
                vibrate();
                //显示loading效果
                final AlertDialog alertDialog = Util.showLoadingDialog(context);
                //false 出库，true 入库
                preOutOrInPort(alertDialog, map, mMode ? Constant.APP_QUERY_INFO_URL : Constant.APP_PORT_EXIST_GOODS);
                //todo 从服务器端获取该器件的信息（唯一标识），查看数据库中是否存在？是否被废弃?
            }

            @Override
            public void onScanQRCodeOpenCameraError() {
                Toast.makeText(MainActivity.this, "打开相机错误", Toast.LENGTH_SHORT).show();
            }
        });

        findViewById(R.id.spot).setOnClickListener(view -> {
            Button spot = (Button) view;
            String content = spot.getText().toString();
            String string = getApplicationContext().getString(R.string.start_spot);
            if (content.equals(string)) {//当前属于关闭状态
                start = true;
                mQRCodeView.startSpot();
                spot.setText(R.string.stop_spot);
            } else {
                start = false;
                mQRCodeView.stopSpot();
                spot.setText(R.string.start_spot);
            }
        });

        findViewById(R.id.flashlight).setOnClickListener(view -> {
            if (view instanceof TextView) {
                TextView textView = (TextView) view;
                String string = getApplicationContext().getString(R.string.open_flashlight);
                String content = textView.getText().toString();
                if (content.equals(string)) {//当前属于关闭状态
                    mQRCodeView.openFlashlight();
                    textView.setText(R.string.close_flashlight);
                } else {
                    mQRCodeView.closeFlashlight();
                    textView.setText(R.string.open_flashlight);
                }
            }
        });

        @SuppressLint("UseSwitchCompatOrMaterialCode")
        Switch mode = findViewById(R.id.mode);
        mode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            Log.d("lan", "isChecked =" + isChecked);
            mMode = isChecked;
            buttonView.setText(isChecked ? R.string.mode_in : R.string.mode_out);
        });
    }

    private void initOther() {
        SoundPool.Builder builder = new SoundPool.Builder();
        builder.setMaxStreams(1);
        AudioAttributes.Builder attrBuild = new AudioAttributes.Builder();
        attrBuild.setLegacyStreamType(AudioManager.STREAM_MUSIC);
        builder.setAudioAttributes(attrBuild.build());
        mSoundPool = builder.build();

        //加载音频
        mSoundPool.load(this, R.raw.in, 1);//1
        mSoundPool.load(this, R.raw.out, 1);//2
    }

    private void playMusic(int id) {
        mSoundPool.play(id, 1.0f, 1.0f, 1, 0, 1.2f);
    }

    private void vibrate() {
        Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        vibrator.vibrate(200);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_CODE) {
            if (grantResults.length > 0) {
                for (int i = 0; i < grantResults.length; i++) {
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(getApplicationContext(), "获取" + permissions[i] + "权限被拒绝", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
            }
        }
    }

    /**
     * 检查二维码中的key是否是我们想要的
     *
     * @param map
     * @return
     */
    private boolean checkParams(Map<String, String> map) {
        String[] names = {"goodsname", "providername", "uuid", "number", "inportprice"};
        for (String name : names) {
            if (!map.containsKey(name)) {
                return false;
            }
        }
        return true;
    }

    private String getFormatString(Map<String, String> map) {
        StringBuilder stringBuilder = new StringBuilder();
        String goodsname = map.get("goodsname");
        String providername = map.get("providername");
        String uuid = map.get("uuid");
        String number = map.get("number");
        String inportprice = map.get("inportprice");
        stringBuilder.append("元器件:").append(goodsname).append("\n");
        stringBuilder.append("供应商:").append(providername).append("\n");
        stringBuilder.append("唯一标识:").append(uuid).append("\n");
        stringBuilder.append("数量:").append(number).append("\n");
        stringBuilder.append("入库价格:").append(inportprice).append("\n");
        return stringBuilder.toString();
    }

    //入库或者出库选择对话框
    private Dialog createInOrOutportDialog(Context context, String message, String title, Map<String, String> map) {
        return new MaterialAlertDialogBuilder(context)
                .setMessage(message)
                .setPositiveButton(R.string.dialog_positive_button, (dialog, which) -> {
                    //发起出库或者入库的请求, mMode==false 出库，mMode==true入库
                    CustomerStringRequest stringRequest = new CustomerStringRequest(mMode ? Constant.APP_GOODS_INPORT_URL : Constant.APP_GOODS_OUTPORT_URL, resultStr -> {
                        Gson gson = new Gson();
                        //json字符串转化为实例
                        CommonResult commonResult = gson.fromJson(resultStr, CommonResult.class);
                        if (commonResult.getCode() != 200) {
                            //提示入库或者出库失败
                            Object msg = commonResult.getData().get("msg");
                            Util.createDialog(context, Constant.DIALOG_TITLE_ERROR, msg == null ? commonResult.getMsg() : msg.toString()).show();
                            //继续扫描二维码
                            new Handler().postDelayed(() -> mQRCodeView.startSpot(), QRCODE_VIEW_DELAY_TIME);
                        } else {
                            //入库或者出库成功
                            // 1==in 2==out
                            //false == out，true == in
                            playMusic(mMode ? 1 : 2);
                            //继续扫描二维码
                            new Handler().postDelayed(() -> mQRCodeView.startSpot(), QRCODE_VIEW_DELAY_TIME);
                        }
                    }, error -> {
                        //网络发生故障
                        Util.createDialog(context, Constant.DIALOG_TITLE_ERROR, Constant.DIALOG_CONTENT_NETWORK_ERROR);
                        //继续扫描二维码
                        new Handler().postDelayed(() -> mQRCodeView.startSpot(), QRCODE_VIEW_DELAY_TIME);
                    });
                    stringRequest.setParams(map);
                    //设置标识
                    stringRequest.setTag(Constant.STRING_INPORT_TAG);
                    AppApplication.getRequestQueue().add(stringRequest);
                })
                .setNegativeButton(R.string.dialog_negative_button, (dialog, which) -> {
                    //继续扫描二维码
                    new Handler().postDelayed(() -> mQRCodeView.startSpot(), QRCODE_VIEW_DELAY_TIME);
                })
                .setTitle(title)
                .create();
    }

    private void preOutOrInPort(AlertDialog alertDialog, Map<String, String> map, String url) {
        CustomerStringRequest stringRequest = new CustomerStringRequest(url, resultStr -> {
            Util.dismissLoadingDialog(alertDialog);
            Gson gson = new Gson();
            CommonResult commonResult = gson.fromJson(resultStr, CommonResult.class);
            if (commonResult.getCode() != 200) {
                //显示查询数据库后的错误
                Util.createDialog(context, Constant.DIALOG_TITLE_ERROR, commonResult.getData().get("msg").toString()).show();
                //继续扫描二维码
                new Handler().postDelayed(() -> mQRCodeView.startSpot(), QRCODE_VIEW_DELAY_TIME);
            } else {
                //提示是否入库或者出库
                if (mMode) {
                    //false 出库，true 入库
                    map.put("providerid", Objects.requireNonNull(commonResult.getData().get("providerid")).toString());
                    map.put("goodsid", Objects.requireNonNull(commonResult.getData().get("goodsid")).toString());
                }
                map.put("operateperson", AppApplication.username);
                Dialog dialog = createInOrOutportDialog(context, getFormatString(map), "是否确认" + (mMode ? "入库" : "出库") + "?", map);
                dialog.setCancelable(false);//接触对话框之外的位置，不可使对话框消失
                dialog.show();
            }
        }, error -> {
            Util.dismissLoadingDialog(alertDialog);
            Util.createDialog(context, Constant.DIALOG_TITLE_ERROR, Constant.DIALOG_CONTENT_NETWORK_ERROR).show();
            //继续扫描二维码
            new Handler().postDelayed(() -> mQRCodeView.startSpot(), QRCODE_VIEW_DELAY_TIME);
        });

        Map<String, String> params = new HashMap<>();
        if (mMode) {
            //入库
            params.put("goodsname", map.get("goodsname"));
            params.put("providername", map.get("providername"));
            params.put("uuid", map.get("uuid"));
        } else {
            //出库
            params.put("uuid", map.get("uuid"));
        }
        //设置请求参数
        stringRequest.setParams(params);
        //设置标记
        stringRequest.setTag(Constant.STRING_REQUEST_QUERY_GOODS_INFO_TAG);
        AppApplication.getRequestQueue().add(stringRequest);
    }

    @Override
    public void onBackPressed() {
        if (System.currentTimeMillis() - firstPressdTime < 2000) {//2s
            //彻底关闭整个APP
            Intent startMain = new Intent(Intent.ACTION_MAIN);
            startMain.addCategory(Intent.CATEGORY_HOME);
            startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(startMain);
            System.exit(0);
        } else {
            firstPressdTime = System.currentTimeMillis();
            Toast.makeText(this, "再按一次退出", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mQRCodeView.startCamera();
        mQRCodeView.showScanRect(); //显示扫描方框
    }

    @Override
    protected void onStop() {
        mQRCodeView.stopCamera();
        AppApplication.getRequestQueue().cancelAll(Constant.APP_LOGIN_URL);
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        mQRCodeView.onDestroy();
        super.onDestroy();
    }
}