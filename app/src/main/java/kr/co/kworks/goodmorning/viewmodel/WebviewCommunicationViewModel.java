package kr.co.kworks.goodmorning.viewmodel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class WebviewCommunicationViewModel extends ViewModel {
    public MutableLiveData<String> functionName;


    public WebviewCommunicationViewModel() {
        functionName = new MutableLiveData<>(); // function 이름 값이 작성 되면 바로 호출 해 줌 (android → Web)
    }
}
