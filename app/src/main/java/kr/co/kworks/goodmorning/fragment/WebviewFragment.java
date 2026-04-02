package kr.co.kworks.goodmorning.fragment;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.ConsoleMessage;
import android.webkit.GeolocationPermissions;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Locale;

import dagger.hilt.android.AndroidEntryPoint;
import kr.co.kworks.goodmorning.R;
import kr.co.kworks.goodmorning.activity.SinglePageActivity;
import kr.co.kworks.goodmorning.model.business_logic.Alert;
import kr.co.kworks.goodmorning.model.business_logic.Confirm;
import kr.co.kworks.goodmorning.utils.Logger;
import kr.co.kworks.goodmorning.utils.WebviewInterface;
import kr.co.kworks.goodmorning.viewmodel.Event;
import kr.co.kworks.goodmorning.viewmodel.GlobalViewModel;
import kr.co.kworks.goodmorning.viewmodel.WebviewCommunicationViewModel;

@AndroidEntryPoint
public class WebviewFragment extends Fragment implements SinglePageActivity.onBackPressedListener {
    private WebView webview, childView;
    private SecurityManager securityManager;
    private WebviewCommunicationViewModel webviewCommunicationViewModel;
    private StringBuilder postDataBuilder;
    private String url;
    private HashMap<String, String> postData;
    private GlobalViewModel global;
    private String previousQrValue;
    private Handler mHandler;
    private long backKeyPressedTime = 0;
    private Toast toast;
    private WebviewInterface webviewInterface;

    public WebviewFragment(String url, HashMap<String, String> postData) {
        this.url = url;
        this.postData = postData;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        SinglePageActivity activity = (SinglePageActivity) getActivity();
        if (activity == null) return;
        activity.setOnKeyBackPressedListener(this);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        SinglePageActivity activity = (SinglePageActivity) getActivity();
        if (activity == null) return;
        activity.setOnKeyBackPressedListener(null);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_webview, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        init();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void init() {
        /* Constance Value */
        previousQrValue = "";

        /* Object Value */
        global = new ViewModelProvider(getActivity()).get(GlobalViewModel.class);
        mHandler = new Handler(Looper.getMainLooper());
        webviewInterface = new WebviewInterface(getActivity());

        if (getView() == null) throw new NullPointerException("getView is null");
        // webview init
        webview = getView().findViewById(R.id.webview);

        if (webview.getUrl() != null) return;

        if(postData != null) {
            postDataBuilder = new StringBuilder();

            for (String key: postData.keySet()) {
                try {
                    postDataBuilder.append(URLEncoder.encode(key, "utf-8"));
                    postDataBuilder.append("=");
                    postDataBuilder.append(URLEncoder.encode(postData.get(key), "utf-8"));
                    postDataBuilder.append("&");
                } catch (UnsupportedEncodingException e) {
                    throw new RuntimeException(e);
                }
            }
            postDataBuilder.setLength(postDataBuilder.length()-1); // remove last &
        }

        Log.i("this", "url: " + url);

        setWebView(webview);
        reserveTimeout();
        if(postDataBuilder == null) webview.loadUrl(url);
        else webview.postUrl(url, postDataBuilder.toString().getBytes());

        observerInit();
        testInterface();
    }

    private void testInterface() {
        mHandler.postDelayed(() -> {
            webviewInterface.doGetPhoto();
        }, 7_000);

    }

    private void observerInit() {
        global._callFunction.observe(this, event -> {
            if (event==null) return;
            String isHandled = event.getContentIfNotHandled();
            if (isHandled == null) return;
            callFunction(isHandled);
        });
    }

    private final Runnable timeoutRunnable = () -> {
        if (webview != null) {
            webview.stopLoading();
        }

        global._progress.setValue(new Event<>("gone"));

        Alert alert = global.alertContent.getValue();
        if (alert != null) {
            alert.body = "서버의 응답시간이 초과 되었습니다. 잠시후 다시 시도해 주세요.";
            global.alertContent.setValue(alert);
            global._alert.setValue(new Event<>("visible"));
        }
    };

    private void reserveTimeout() {
        mHandler.removeCallbacks(timeoutRunnable);
        mHandler.postDelayed(timeoutRunnable, 30_000);
        mHandler.post(() -> {
            global._progress.setValue(new Event<>("visible"));
        });
    }

    private void releaseTimeout() {
        mHandler.removeCallbacks(timeoutRunnable);
        mHandler.post(() -> {
            global._progress.setValue(new Event<>("gone"));
        });
    }


    private void setWebView(WebView webView) {
        webView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);                           //웹뷰가 캐시를 사용하지 않도록 설정
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setGeolocationEnabled(true);
        webView.getSettings().setAllowFileAccess(true);
        webView.getSettings().setBuiltInZoomControls(false);
        webView.getSettings().setSupportZoom(false);
        webView.getSettings().setDomStorageEnabled(true);                                            //로컬 스토리지 사용 여부를 설정하는 속성으로 팝업창등을 '하루동안 보지 않기' 기능 사용에 필요합니다.
        webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);                        //자바스크립트가 window.open()을 사용할 수 있도록 설정
        webView.getSettings().setSupportMultipleWindows(true);                                    //새창열 때 필수설정(true)
        webView.getSettings().setTextZoom(100);
        webView.getSettings().setAllowFileAccessFromFileURLs(false);
        webView.getSettings().setAllowUniversalAccessFromFileURLs(false);
        //webView.getSettings().setDefaultTextEncodingName("utf-8");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            webView.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW); //HTTPS HTTP의 연동, 서로 호출 가능하도록
        }
        webView.setWebChromeClient(new CustomWebChromeClient());
        webView.setWebViewClient(new AxaWebViewClient());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        } else {
            webView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }
        webView.addJavascriptInterface(new WebviewInterface(getActivity()), "HybridApp");

        webView.clearCache(true);
        webView.clearHistory();

        webview.setOnScrollChangeListener(new View.OnScrollChangeListener() {
            @Override
            public void onScrollChange(View view, int i, int scrollY, int i2, int i3) {
                if(scrollY == 0) {
//                    if(getActivity() != null && getActivity() instanceof MainActivity) ((MainActivity)getActivity()).setSwipeEnable(true);
                } else {
//                    if(getActivity() != null && getActivity() instanceof MainActivity) ((MainActivity)getActivity()).setSwipeEnable(false);
                }
            }
        });
    }

    public class AxaWebViewClient extends WebViewClient {

        @Override
        public void onLoadResource(WebView view, String url) {
            super.onLoadResource(view, url);
        }

        // 새로운 URL이 webview에 로드되려 할 경우 컨트롤을 대신할 기회를 줌
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            try {
                url = URLDecoder.decode(url, "utf-8");
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (url == null) {
                return false;
            }

            if (url.startsWith("tel:")) {
                Intent tel = new Intent(Intent.ACTION_DIAL, Uri.parse(url));
                startActivity(tel);
                return true;
            } else if (url.startsWith("id:")) {
                String sId = url.replace("id://", "");
                return true;
            } else if (url.startsWith("intent://")) {
                Intent intent = null;
                try {
                    intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                } catch (URISyntaxException e) {
                }
                return true;
            }

            // http/https 및 일반 URL은 WebView가 원래대로 처리하게 둠
            return false;
        }

        // 로딩이 시작될 때
        @Override
        public void onPageStarted(WebView view, final String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            mHandler.post(() -> {
                global._progress.setValue(new Event<>("visible"));
            });
        }

        // 로딩이 완료됬을 때 한번 호출
        @Override
        public void onPageFinished(WebView view, final String url) {
            super.onPageFinished(view, url);
            releaseTimeout();
        }

        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
            super.onReceivedError(view, request, error);
            view.loadUrl("about:blank");
            Alert alert = global.alertContent.getValue();
            alert.body = String.format(Locale.KOREA, "(%d) %s", error.getErrorCode(), error.getDescription());
            mHandler.post(() -> {
                global.alertContent.setValue(alert);
                global._alert.postValue(new Event<>("visible"));
            });
        }
    }

    public class CustomWebChromeClient extends WebChromeClient {
        private boolean timeout = true;
        private Handler timeoutHandler = null;

        @Override
        public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
            super.onGeolocationPermissionsShowPrompt(origin, callback);
            callback.invoke(origin, true, false);
        }


        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            super.onProgressChanged(view, newProgress);
            mHandler.post(() -> {
                global._progressText1.setValue(String.format(Locale.KOREA,"통신중 %d%%", newProgress));
            });
        }

        @Override
        public void onCloseWindow(WebView window) {
            super.onCloseWindow(window);
            if (childView != null) {
                childView.loadUrl("javascript:window.close()");
                if (webview != null) webview.removeView(childView);
                childView = null;
            }
        }

        @Override
        public boolean onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture, Message resultMsg) {
//            view.removeAllViews();
            //view.scrollTo(0,0);
            childView = new WebView(view.getContext());
            /*  웹뷰의 캐시 모드를 설정하는 속성으로써 5가지 모드가 존재합니다.
                LOAD_CACHE_ELSE_NETWORK 기간이 만료돼 캐시를 사용할 수 없을 경우 네트워크를 사용합니다.
                LOAD_CACHE_ONLY 네트워크를 사용하지 않고 캐시를 불러옵니다.
                LOAD_DEFAULT 기본적인 모드로 캐시를 사용하고 만료된 경우 네트워크를 사용해 로드합니다.
                LOAD_NORMAL 기본적인 모드로 캐시를 사용합니다.
                LOAD_NO_CACHE 캐시모드를 사용하지 않고 네트워크를 통해서만 호출합니다.*/
            childView.setY(view.getScrollY());
            childView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);                           //웹뷰가 캐시를 사용하지 않도록 설정
            childView.getSettings().setJavaScriptEnabled(true);                                          //자바스크립트로 이루어져 있는 기능을 사용하기 위한 설정
            childView.getSettings().setDomStorageEnabled(true);                                           //로컬 스토리지 사용 여부를 설정하는 속성으로 팝업창등을 '하루동안 보지 않기' 기능 사용에 필요합니다.
            childView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);                      //자바스크립트가 window.open()을 사용할 수 있도록 설정
            childView.setWebViewClient(new AxaWebViewClient());
            childView.setWebChromeClient(new CustomWebChromeClient());
            childView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));

            view.addView(childView);
            WebView.WebViewTransport transport = (WebView.WebViewTransport) resultMsg.obj;
            transport.setWebView(childView);
            resultMsg.sendToTarget();
            return true;
        }

        @Override
        public boolean onJsAlert(WebView view, String url, String message, final JsResult result) {
            Alert alert = global.alertContent.getValue();
            alert.body = message;
            mHandler.post(() -> {
                global.jsResult = result;
                global.alertContent.postValue(alert);
                global._alert.postValue(new Event<>("visible"));
            });
            return true;
        }

        @Override
        public boolean onJsConfirm(WebView view, String url, String message, final JsResult result) {
            Confirm confirm = global.confirmContent.getValue();
            confirm.body = message;
            mHandler.post(() -> {
                global.jsResult = result;
                global.confirmContent.postValue(confirm);
                global._confirm.postValue(new Event<>("visible"));
            });
            return true;
        }

        // 자바스크립트 에러 발생 시 로그 출력부
        public boolean onConsoleMessage(ConsoleMessage cm) {
            return true;
        }

    }

    public void callFunction(String str) {
        if (webview == null) return;
        Handler mHandler = new Handler(Looper.getMainLooper());
        mHandler.post(() -> {
            webview.loadUrl("javascript:" + str);
        });

        Logger.getInstance().info("javascript: " + str);
    }

    public void callFunction(String funcName, String stringData) {
        if (webview == null) return;
        Handler mHandler = new Handler(Looper.getMainLooper());
        mHandler.post(() -> {
            if(stringData != null) {
                webview.loadUrl("javascript:" + funcName + "('" + stringData + "')");
            } else {
                webview.loadUrl("javascript:" + funcName + "()");
            }
        });
    }

    public void callFunction(String funcName, int count, String phoneNumbers) {
        if (webview == null) return;
        Handler mHandler = new Handler(Looper.getMainLooper());
        mHandler.post(() -> {
            webview.loadUrl(String.format(Locale.KOREA, "javascript: %s(%d, '%s')", funcName, count, phoneNumbers));
        });
    }

    public void sendImage(String name, String mimeType, String base64string, String end, int delay) {
        if (webview == null) throw new NullPointerException("webview is null");
        Handler mHandler = new Handler(Looper.getMainLooper());
        mHandler.postDelayed(() -> {
            webview.loadUrl(String.format(Locale.KOREA, "javascript:captureResult('%s','%s','%s','%s')", name, mimeType, base64string, end));
        }, delay);
    }

    public void refresh() {
        if(webview != null && webview.getUrl() != null) webview.loadUrl(webview.getUrl());
    }

    public void onBack() {
        if (childView != null) {
            if (childView.canGoBack()) {
                childView.goBack();
            } else {
                 closeChildView();
            }
            return;
        }

        if (webview != null && webview.canGoBack()) {
            webview.goBack();
            return;
        }

        // 2초 이내 2번 클릭 시 종료
        if (System.currentTimeMillis() > backKeyPressedTime + 2000) {
            backKeyPressedTime = System.currentTimeMillis();
            showGuide();
            return;
        }
        if (System.currentTimeMillis() <= backKeyPressedTime + 2000) {
            getActivity().finish();
            getActivity().overridePendingTransition(0,0);
            toast.cancel();
        }


    }

    private void closeChildView() {
        if (childView != null) {
            if (webview != null) {
                webview.removeView(childView);
            }
            childView.destroy();
            childView = null;
        }
    }

    public void showGuide() {
        toast = Toast.makeText(getActivity(),
            "\'뒤로\'버튼을 한번 더 누르시면 종료됩니다.", Toast.LENGTH_SHORT);
        toast.show();
    }

    public void doNoticeUrl(String url) {
        Intent siteLaunch = new Intent(Intent.ACTION_VIEW);
        siteLaunch.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        siteLaunch.setData(Uri.parse(url));
        getActivity().startActivity(siteLaunch);
    }
}
