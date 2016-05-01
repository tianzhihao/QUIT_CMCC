package com.zhihao.quit_cmcc;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Build;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.HttpAuthHandler;
import android.webkit.ValueCallback;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private Button mUnLoginButton,mRefreshButton,mAlertInfoButton;
    private WebView mWebView;
    private String sUser = "";
    private String sPass = "";
    public static String TAG = "webview";
    public static String SENT_SMS_ACTION = "SENT_SMS_ACTION";
    public static String DELIVERED_SMS_ACTION = "DELIVERED_SMS_ACTION";
    public static String RECEIVED_SMS = "android.provider.Telephony.SMS_RECEIVED";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        listener();
        if (getInfo().get(0).equals("178")){
            inputInfo();
        }
        loadWeb();
    }

    private void init() {
        mUnLoginButton = (Button) findViewById(R.id.btn_send);
        mRefreshButton = (Button) findViewById(R.id.btn_refresh);
        mAlertInfoButton = (Button) findViewById(R.id.btn_alertinfo);
        mWebView = (WebView) findViewById(R.id.web_view);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setDomStorageEnabled(true);
        mWebView.getSettings().setSupportZoom(true);//设置此属性，仅支持双击缩放，不支持触摸缩放（在android4.0是这样，其他平台没试过）
        mWebView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        //设置加载进来的页面自适应手机屏幕
        mWebView.getSettings().setUseWideViewPort(true);
        mWebView.getSettings().setLoadWithOverviewMode(true);
        mWebView.getSettings().setSavePassword(true);
        mWebView.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                Log.i(TAG, "shouldOverrideUrlLoading: ");
                view.loadUrl(url);
                return super.shouldOverrideUrlLoading(view, url);
            }
            @Override
            public void onLoadResource(WebView view, String url) {
                super.onLoadResource(view, url);
                Log.i(TAG, "onLoadResource: ");
            }
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                Log.i(TAG, "onPageStarted: ");
            }
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                Log.i(TAG, "onPageFinished: "+url);
//                  if (url.startsWith("http://172.16.40.210")) {
//                      String js = "javascript:document.getElementById('username').value = '" +fUser+ "';document.getElementById('pwd').value='" +fPass+ "';document.getElementById('net_access_type').value='internet';document.getElementById('loginLink').click();";
//                      if (Build.VERSION.SDK_INT >= 19) {
//                          view.evaluateJavascript(js, new ValueCallback<String>() {
//                              @Override
//                              public void onReceiveValue(String s) {
//
//                              }
//                          });
//                      } else {
//                          view.loadUrl(js);
//                      }
                if(url.startsWith("http://cmcc.sd.chinamobile.com:8001/redirectLogin.do?")){
                    List<String> mInfo = getInfo();
                    sUser = mInfo.get(0);
                    sPass = mInfo.get(1);
                    String js = "javascript:document.getElementById('username').value = '" +sUser+ "';document.getElementById('password').value='" +sPass+ "';document.getElementById('autosubmitid').click();";
                    if (Build.VERSION.SDK_INT >= 19) {
                        view.evaluateJavascript(js, new ValueCallback<String>() {
                            @Override
                            public void onReceiveValue(String s) {

                            }
                        });
                    } else {
                        view.loadUrl(js);
                    }
                }
            }
            @Override
            public void onReceivedHttpAuthRequest(WebView view,
                                                  HttpAuthHandler handler, String host, String realm) {
                super.onReceivedHttpAuthRequest(view, handler, host, realm);
                Log.i(TAG, "onReceivedHttpAuthRequest: ");
            }

            @Override
            public void onReceivedError(WebView view, int errorCode,
                                        String description, String failingUrl) {
                super.onReceivedError(view, errorCode, description, failingUrl);
                Log.i(TAG, "onReceivedError: ");
                Log.i(TAG, "errorCode: "+errorCode);
                Log.i(TAG, "description: "+description);
                Log.i(TAG, "failingUrl: "+failingUrl);
            }
        });
    }

    private void listener() {
        mUnLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendSMS();
            }
        });
        mRefreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadWeb();
                Toast.makeText(MainActivity.this,"已刷新，请耐心等待", Toast.LENGTH_SHORT).show();
            }
        });
        mAlertInfoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                inputInfo();
            }
        });
    }

    private void inputInfo() {
        View mView = LayoutInflater.from(MainActivity.this).inflate(R.layout.dialog_input,null);
        final EditText mUser = (EditText) mView.findViewById(R.id.username);
        final EditText mPass = (EditText) mView.findViewById(R.id.password);
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(MainActivity.this);
        mBuilder.setTitle("请输入第二道认证信息").setView(mView).setPositiveButton("保存",new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                saveInfo(mUser.getText().toString(),mPass.getText().toString());
                Toast.makeText(MainActivity.this,"第二道认证界面会自动填充该信息，并自动登录", Toast.LENGTH_LONG).show();
            }
        }).setNegativeButton("取消",null);
        mBuilder.show();
    }

    private void loadWeb() {
          mWebView.loadUrl("http://www.baidu.com/");
    }
    private void sendSMS() {
        Intent mSentIntent = new Intent(SENT_SMS_ACTION);
        PendingIntent mSentPI = PendingIntent.getBroadcast(MainActivity.this,0,mSentIntent,0);
        MainActivity.this.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
            }
        },new IntentFilter(SENT_SMS_ACTION));
        Intent mDeliverIntent = new Intent(DELIVERED_SMS_ACTION);
        PendingIntent mDeliveredPI = PendingIntent.getBroadcast(MainActivity.this,0,mDeliverIntent,0);
        MainActivity.this.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                    Toast.makeText(MainActivity.this, "短信成功发送", Toast.LENGTH_SHORT).show();
            }
        },new IntentFilter(DELIVERED_SMS_ACTION));
        SmsManager manager = SmsManager.getDefault();
        manager.sendTextMessage("10086222",null,"9",mSentPI,mDeliveredPI);
        SmsRec_BroadCastReceive mSmsR = new SmsRec_BroadCastReceive(MainActivity.this);
        IntentFilter mFilter = new IntentFilter(RECEIVED_SMS);
        mFilter.setPriority(Integer.MAX_VALUE);
        MainActivity.this.registerReceiver(mSmsR,mFilter);
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(event.getAction() == KeyEvent.KEYCODE_BACK&&mWebView.canGoBack()){
                mWebView.goBack();
        }
        return super.onKeyDown(keyCode, event);
    }
    private void saveInfo(String mUs,String mPa){
        SharedPreferences mPreferences = getSharedPreferences("EduInfo",MODE_PRIVATE);
        SharedPreferences.Editor mEditor = mPreferences.edit();
        mEditor.putString("username",mUs);
        mEditor.putString("password",mPa);
        mEditor.commit();
        Toast.makeText(MainActivity.this,"信息保存到本地", Toast.LENGTH_SHORT).show();
    }
    private List<String> getInfo(){
        SharedPreferences mPreferences = getSharedPreferences("EduInfo",MODE_PRIVATE);
        List<String> mStrings = new ArrayList<>();
        mStrings.add(mPreferences.getString("username","178"));
        mStrings.add(mPreferences.getString("password",""));
        return mStrings;
    }
}
