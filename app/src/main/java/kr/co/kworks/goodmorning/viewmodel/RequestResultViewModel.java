package kr.co.kworks.goodmorning.viewmodel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class RequestResultViewModel extends ViewModel {
    public MutableLiveData<String> qrScanResult, addressRequestResult;

    public RequestResultViewModel() {
        qrScanResult = new MutableLiveData<>(); // QR 코드 인식 결과
        addressRequestResult = new MutableLiveData<>(); // Address 요청 결과
    }
}
