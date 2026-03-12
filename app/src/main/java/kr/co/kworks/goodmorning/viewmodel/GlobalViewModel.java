package kr.co.kworks.goodmorning.viewmodel;

import android.webkit.JsResult;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import kr.co.kworks.goodmorning.model.business_logic.Alert;
import kr.co.kworks.goodmorning.model.business_logic.Confirm;
import kr.co.kworks.goodmorning.model.business_logic.ProgressDialog;

@HiltViewModel
public class GlobalViewModel extends ViewModel {

    private final Executor io = Executors.newSingleThreadExecutor();

    public MutableLiveData<Event<String>> _webViewFragment, _popBackStack, _confirm, _alert, _progress;
    public JsResult jsResult;
    public ProgressDialog progressDialog;
    public MutableLiveData<Alert> alertContent;
    public MutableLiveData<Confirm> confirmContent;
    public MutableLiveData<String> _progressText1, _progressText2;


    @Inject
    public GlobalViewModel(
    ) {
        init();
    }

    @NonNull
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        return sb.toString();
    }

    public void init() {
        _popBackStack = new MutableLiveData<>();
        _webViewFragment = new MutableLiveData<>();
        _confirm = new MutableLiveData<>();
        _alert = new MutableLiveData<>();
        _progress = new MutableLiveData<>();
        jsResult = null;
        progressDialog = new ProgressDialog();
        alertContent = new MutableLiveData<>(new Alert());
        confirmContent = new MutableLiveData<>(new Confirm());
        _progressText1 = new MutableLiveData<>();
        _progressText2 = new MutableLiveData<>();

    }
}
