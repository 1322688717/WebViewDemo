package com.example.webviewproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.webkit.ConsoleMessage;
import android.webkit.DownloadListener;
import android.webkit.SslErrorHandler;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.webviewproject.utils.OneAesUtil;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;

public class MainActivity extends AppCompatActivity {

    private long mLastClickBackTime;//上次点击back键的时间
   // private DragFloatActionButton floatingButton;
    private RelativeLayout mLayoutMain;
    private FrameLayout mLayoutPrivacy;
    private WebView mWebView;
    private RelativeLayout mLayoutError;
    private Button mBtnReload;
    private String mWebViewUrl;
    private String mFloatUrl;
    private String mJson;
    //private NTSkipView mTvSkip;

    private ValueCallback<Uri> uploadFile;
    private ValueCallback<Uri[]> uploadFiles;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
        initData();
        initWebView();
    }

    private void initData() {
        String getIpConfig = getResources().getString(R.string.getIpConfig);
        String urlMain = getResources().getString(R.string.url_main);
        mJson = OneAesUtil.decrypt(urlMain);
        String suffix = getResources().getString(R.string.suffix);
        String urlSuffix = OneAesUtil.decrypt(suffix);
        String floatUrl = getResources().getString(R.string.floatUrl);
        mFloatUrl = OneAesUtil.decrypt(floatUrl);
        if (!TextUtils.isEmpty(getIpConfig) && getIpConfig.equals("true")) {
            final OkHttpClient okHttpClient = new OkHttpClient();
           // final httpLoggingInterceptor = new HttpLoggingInterceptor().level(HttpLoggingInterceptor.Level.BODY);
            okHttpClient.newBuilder().addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY));
            Request.Builder builder = new Request.Builder();
            final Request request = builder.url(mJson)
                    .get()
                    .build();

            final Call call = okHttpClient.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    skipError();
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    try {
                        String json = response.body().string();
                        JSONObject jsonObject = new JSONObject(json);
                        mWebViewUrl = jsonObject.getString("query");
                        if (!mWebViewUrl.contains("http")) {
                            mWebViewUrl = ("http://" + mWebViewUrl);
                        }
                        if (!TextUtils.isEmpty(urlSuffix)) {
                            mWebViewUrl = mWebViewUrl + urlSuffix;
                        }
                        if (TextUtils.isEmpty(mWebViewUrl)) {
                            skipError();
                            Toast.makeText(MainActivity.this, "配置异常，请检查", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        loadUrl(mWebViewUrl);
                    } catch (Exception e) {
                        skipError();
                    }
                }
            });
        } else {
            loadUrl(mJson);
        }

    }


    private void loadUrl(String webUrl) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mWebView.loadUrl(webUrl);
            }
        });
    }

    /**
     * 跳转错误页
     */
    public void skipError() {
        if (null == mLayoutError || null == mBtnReload) {
            return;
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mLayoutError.setVisibility(View.VISIBLE);
                mBtnReload.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        startActivity(new Intent(MainActivity.this, MainActivity.class));
                        finish();
                    }
                });
            }
        });
    }

    private void initView() {
//        mLayoutMain = findViewById(R.id.layout_main);
//        mLayoutPrivacy = findViewById(R.id.layout_privacy);
        mWebView = findViewById(R.id.web_view);
//        floatingButton = findViewById(R.id.floatingButton);
//        mLayoutError = findViewById(R.id.layout_error);
//        mBtnReload = findViewById(R.id.btn_reload);
//        mTvSkip = findViewById(R.id.tv_skip);
    }

    /**
     * 初始化webView
     */
    public void initWebView() {
        try {
            WebSettings settings = mWebView.getSettings();
            settings.setAllowFileAccess(true);
            settings.setLoadWithOverviewMode(true);
            // 设置WebView是否允许执行JavaScript脚本，默认false，不允许。
            settings.setJavaScriptEnabled(true);
            // 设置脚本是否允许自动打开弹窗，默认false，不允许。
            settings.setJavaScriptCanOpenWindowsAutomatically(true);
            // 设置WebView是否使用其内置的变焦机制，该机制结合屏幕缩放控件使用，默认是false，不使用内置变焦机制。
            settings.setAllowContentAccess(true);
            // 设置在WebView内部是否允许访问文件，默认允许访问。
            settings.setAllowFileAccess(true);
            // 设置WebView运行中的脚本可以是否访问任何原始起点内容，默认true
            settings.setAllowUniversalAccessFromFileURLs(true);
            // 设置WebView运行中的一个文件方案被允许访问其他文件方案中的内容，默认值true
            settings.setAllowFileAccessFromFileURLs(true);
            // 设置Application缓存API是否开启，默认false，设置有效的缓存路径参考setAppCachePath(String path)方法
            settings.setAppCacheEnabled(false);
            // 设置是否开启数据库存储API权限，默认false，未开启，可以参考setDatabasePath(String path)
            settings.setDatabaseEnabled(true);
            // 设置是否开启DOM存储API权限，默认false，未开启，设置为true，WebView能够使用DOM
            settings.setDomStorageEnabled(true);
            // 设置WebView是否使用viewport，当该属性被设置为false时，加载页面的宽度总是适应WebView控件宽度；
            // 当被设置为true，当前页面包含viewport属性标签，在标签中指定宽度值生效，如果页面不包含viewport标签，无法提供一个宽度值，这个时候该方法将被使用。
            settings.setUseWideViewPort(true);
            // 设置WebView是否支持多屏窗口，参考WebChromeClient#onCreateWindow，默认false，不支持。
            settings.setSupportMultipleWindows(false);
            // 设置WebView是否支持使用屏幕控件或手势进行缩放，默认是true，支持缩放。
            settings.setSupportZoom(false);
            // 设置WebView是否使用其内置的变焦机制，该机制集合屏幕缩放控件使用，默认是false，不使用内置变焦机制。
            settings.setBuiltInZoomControls(true);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
            }
            // 设置WebView加载页面文本内容的编码，默认“UTF-8”。
            settings.setDefaultTextEncodingName("UTF-8");
            mWebView.setWebViewClient(new MyWebViewClient());
            mWebView.setWebChromeClient(new MyWebChromeClient());
            mWebView.setDownloadListener(new DownloadListener() {
                @Override
                public void onDownloadStart(String str, String str2, String str3, String str4, long j) {
                    startActivity(new Intent("android.intent.action.VIEW", Uri.parse(str)));
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public class MyWebViewClient extends WebViewClient {
        String currentUrl;

        @Override
        public boolean shouldOverrideUrlLoading(WebView webView, String str) {
            this.currentUrl = str;
            if (str.startsWith("http") || str.startsWith("https")) {
                return false;
            }
            try {
                Uri.parse(str);
                Intent parseUri = Intent.parseUri(str, Intent.URI_INTENT_SCHEME);
                parseUri.addCategory("android.intent.category.BROWSABLE");
                parseUri.setComponent(null);
                startActivity(parseUri);
            } catch (Exception unused) {
            }
            return true;
        }

        @Override
        public void onReceivedSslError(WebView webView, SslErrorHandler sslErrorHandler, SslError sslError) {
            sslErrorHandler.proceed();
        }

        @Override
        public void onPageStarted(WebView webView, String str, Bitmap bitmap) {
            super.onPageStarted(webView, str, bitmap);
        }

        @Override
        public void onPageFinished(final WebView webView, String str) {
            super.onPageFinished(webView, str);
        }

        @Override
        public void onReceivedError(WebView webView, int i, String str, String str2) {
            AlertDialog create = new AlertDialog.Builder(MainActivity.this).create();
            create.setTitle("网络异常");
            create.setMessage("请切换到其他可用网络重新打开, 如果不行, 则清理应用数据后重试!");
            create.show();
        }
    }

    class MyWebChromeClient extends WebChromeClient {

        @Override
        public void onProgressChanged(WebView webView, int i) {
            super.onProgressChanged(webView, i);
        }

        @Override
        public void onReceivedTitle(WebView webView, String str) {
            super.onReceivedTitle(webView, str);
        }

        @Override
        public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
            return super.onConsoleMessage(consoleMessage);
        }

        @Override
        public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> valueCallback, WebChromeClient.FileChooserParams fileChooserParams) {
            uploadFiles = valueCallback;
            openFileChooseProcess();
            return true;
        }
    }

    public void openFileChooseProcess() {
        Intent intent = new Intent("android.intent.action.GET_CONTENT");
        intent.addCategory("android.intent.category.OPENABLE");
        intent.setType("*/*");
        startActivityForResult(Intent.createChooser(intent, "选择文件"), 0);
    }
}