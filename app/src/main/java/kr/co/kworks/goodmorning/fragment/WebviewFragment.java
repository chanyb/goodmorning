package kr.co.kworks.goodmorning.fragment;

import android.app.Activity;
import android.app.ProgressDialog;
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
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Locale;

import kr.co.kworks.goodmorning.R;
import kr.co.kworks.goodmorning.dialog.DialogManager;
import kr.co.kworks.goodmorning.utils.ApiConstants;
import kr.co.kworks.goodmorning.utils.GlobalApplication;
import kr.co.kworks.goodmorning.utils.PreferenceHandler;
import kr.co.kworks.goodmorning.utils.SecurityManager;
import kr.co.kworks.goodmorning.utils.WebviewInterface;
import kr.co.kworks.goodmorning.viewmodel.GlobalViewModel;
import kr.co.kworks.goodmorning.viewmodel.WebviewCommunicationViewModel;

public class WebviewFragment extends Fragment {
    private ProgressDialog mProgressDialog;
    private WebView webview, childView;
    private GlobalViewModel user;
    private SecurityManager securityManager;
    private WebviewCommunicationViewModel webviewCommunicationViewModel;
    private StringBuilder postDataBuilder;
    private String url;
    private HashMap<String, String> postData;
    private GlobalViewModel global;
    private String previousQrValue;
    private Handler mHandler;
    private PreferenceHandler preferenceHandler;

    public WebviewFragment(String url, HashMap<String, String> postData) {
        this.url = url;
        this.postData = postData;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
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
        if (mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void init() {
        /* Constance Value */
        previousQrValue = "";

        /* Object Value */
        mHandler = new Handler(Looper.getMainLooper());
        global = new ViewModelProvider(GlobalApplication.getContext()).get(GlobalViewModel.class);
        securityManager = new SecurityManager(getContext());
        preferenceHandler = new PreferenceHandler(getContext());
        webviewCommunicationViewModel = new ViewModelProvider(GlobalApplication.getContext()).get(WebviewCommunicationViewModel.class);
        webviewCommunicationViewModel.functionName.removeObservers(this);
        webviewCommunicationViewModel.functionName.observe(this, o -> {
            if(o == null) return;
            callFunction(o, null);
        });
        user = new ViewModelProvider(GlobalApplication.getContext()).get(GlobalViewModel.class);

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
                    postDataBuilder.append(URLEncoder.encode(securityManager.encryptRSA(securityManager.getServerPublicKey(), postData.get(key)), "utf-8"));
                    postDataBuilder.append("&");
                } catch (UnsupportedEncodingException e) {
                    throw new RuntimeException(e);
                }
            }
            postDataBuilder.setLength(postDataBuilder.length()-1); // remove last &
        }

        Log.i("this", "url: " + url);
        if(postDataBuilder == null) webview.loadUrl(url);
        else webview.postUrl(url, postDataBuilder.toString().getBytes());
//        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) webview.getLayoutParams();
//        params.height = GlobalApplcation.getContext().getHeight();
//        webview.setLayoutParams(params);

        mProgressDialog = new ProgressDialog(getActivity());
        mProgressDialog.setMessage(getString(R.string.sentence_please_wait));
        setWebView(webview);

        registerObservers();
    }

    private void setWebView(WebView webView) {
        webView.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);                           //웹뷰가 캐시를 사용하지 않도록 설정
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
        webView.addJavascriptInterface(new WebviewInterface(getActivity(), mProgressDialog), "HybridApp");

        webView.clearCache(true);
        webView.clearHistory();

//        webView.loadUrl(Utils.URL_BASE + sUrl + "?" + sPostData);

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

            if (url.startsWith("tel:")) {
                Intent tel = new Intent(Intent.ACTION_DIAL, Uri.parse(url));
                startActivity(tel);
            }
            else if (url.startsWith("id:")) {
                String sId = url.replace("id://", "");
                preferenceHandler.setStringPreference(PreferenceHandler.PREF_USER_ID, sId);
            }
            else {
                view.loadUrl(url);
            }
            return true;
        }

        // 로딩이 시작될 때
        @Override
        public void onPageStarted(WebView view, final String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            mProgressDialog.show();
        }

        // 로딩이 완료됬을 때 한번 호출
        @Override
        public void onPageFinished(WebView view, final String url) {
            super.onPageFinished(view, url);
            if (mProgressDialog.isShowing()) mProgressDialog.dismiss();
//            CookieManager.getInstance().flush();

            Log.i("this", "url: "+url);
        }

        @Override
        public void onReceivedError(final WebView view, int errorCode, String description, final String failingUrl) {
            super.onReceivedError(view, errorCode, description, failingUrl);
            Log.e("this", "webview-onReceivedError, code: "+errorCode + " desc: " + description);
            switch (errorCode) {
                case ERROR_AUTHENTICATION:
                    break;               // 서버에서 사용자 인증 실패
                case ERROR_BAD_URL:
                    break;                           // 잘못된 URL
                case ERROR_CONNECT:
                    break;                          // 서버로 연결 실패
                case ERROR_FAILED_SSL_HANDSHAKE:
                    break;    // SSL handshake 수행 실패
                case ERROR_FILE:
                    break;                                  // 일반 파일 오류
                case ERROR_FILE_NOT_FOUND:
                    break;               // 파일을 찾을 수 없습니다
                case ERROR_HOST_LOOKUP:
                    break;           // 서버 또는 프록시 호스트 이름 조회 실패
                case ERROR_IO:
                    break;                              // 서버에서 읽거나 서버로 쓰기 실패
                case ERROR_PROXY_AUTHENTICATION:
                    break;   // 프록시에서 사용자 인증 실패
                case ERROR_REDIRECT_LOOP:
                    break;               // 너무 많은 리디렉션
                case ERROR_TIMEOUT:
                    break;                          // 연결 시간 초과
                case ERROR_TOO_MANY_REQUESTS:
                    break;     // 페이지 로드중 너무 많은 요청 발생
                case ERROR_UNKNOWN:
                    break;                        // 일반 오류
                case ERROR_UNSUPPORTED_AUTH_SCHEME:
                    break; // 지원되지 않는 인증 체계
                case ERROR_UNSUPPORTED_SCHEME:
                    break;          // URI가 지원되지 않는 방식
            }
            view.loadUrl("about:blank");
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

            if (newProgress == 10) {
                if (view == webview) {
                    if (!mProgressDialog.isShowing()) {
                        mProgressDialog.setMessage(getContext().getString(R.string.sentence_please_wait));
                        mProgressDialog.setCancelable(false);
                        mProgressDialog.show();
                    }

                }
            }
            if (newProgress == 100) {
                if (view == webview) {
                    if (mProgressDialog.isShowing()) {
                        mProgressDialog.dismiss();
                    }
                }
                if (timeoutHandler != null) timeoutHandler.removeCallbacksAndMessages(null);
                timeout = false;
            }
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
            view.removeAllViews();
            //view.scrollTo(0,0);
            childView = new WebView(view.getContext());
            /*  웹뷰의 캐시 모드를 설정하는 속성으로써 5가지 모드가 존재합니다.
                LOAD_CACHE_ELSE_NETWORK 기간이 만료돼 캐시를 사용할 수 없을 경우 네트워크를 사용합니다.
                LOAD_CACHE_ONLY 네트워크를 사용하지 않고 캐시를 불러옵니다.
                LOAD_DEFAULT 기본적인 모드로 캐시를 사용하고 만료된 경우 네트워크를 사용해 로드합니다.
                LOAD_NORMAL 기본적인 모드로 캐시를 사용합니다.
                LOAD_NO_CACHE 캐시모드를 사용하지 않고 네트워크를 통해서만 호출합니다.*/
            childView.setY(view.getScrollY());
            childView.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);                           //웹뷰가 캐시를 사용하지 않도록 설정
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
            Handler handler = new Handler(Looper.getMainLooper());
            handler.postDelayed(() -> {
                DialogManager.getInstance().showAlertDialog(getContext(),message, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        DialogManager.getInstance().dismissAlertDialog((Activity) getContext());
                        result.confirm();
                    }
                }, R.drawable.icon_alert, getString(R.string.str_caution));
            }, 0);

            return true;
        }

        @Override
        public boolean onJsConfirm(WebView view, String url, String message, final JsResult result) {
            DialogManager.getInstance().showConfirmDialog(getContext(), message, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    DialogManager.getInstance().dismissConfirmDialog(getActivity());
                    result.cancel();
                }
            }, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    DialogManager.getInstance().dismissConfirmDialog(getActivity());
                    result.confirm();
                }
            }, DialogManager.ICON_NULL, null);

            return true;
        }

        // 자바스크립트 에러 발생 시 로그 출력부
        public boolean onConsoleMessage(ConsoleMessage cm) {
            return true;
        }

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

    public boolean canGoBack() {
        if (webview == null) return false;
        else if (webview.getUrl().equals(ApiConstants.LOGIN_URL)) {
            // 로그인
            return false;
        }
        else if (webview.canGoBack()) {
            webview.goBack();
            Log.i("this", "webView.goBack()");
            return true;
        }
//        webview.loadUrl("javascript: fn_pageBack()");
        return false;
    }

    private void registerObservers() {
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

}
