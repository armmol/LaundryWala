package com.example.laundry2;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.example.laundry2.Contract.PaymentsContract;
import com.example.laundry2.DataClasses.AuthState;
import com.example.laundry2.Repositories.ApplicationRepository;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.wallet.PaymentData;

public class PaymentsViewModel extends AndroidViewModel implements PaymentsContract {

    private final ApplicationRepository applicationRepository;
    private final MutableLiveData<AuthState> authStateMutableLiveData;
    private final MutableLiveData<Boolean> _canUseGooglePay;
    private final MutableLiveData<Task<PaymentData>> paymentDataTaskMutableLiveData;

    public PaymentsViewModel (Application application) {
        super (application);
        this.applicationRepository = new ApplicationRepository (application);
        authStateMutableLiveData = applicationRepository.getAuthStateMutableLiveData ();
        _canUseGooglePay = applicationRepository.get_canUseGooglePay ();
        paymentDataTaskMutableLiveData = applicationRepository.getpaymentDataTaskMutableLiveData ();
    }

    @Override
    public void canPayWithGooglePay () {
        applicationRepository.fetchCanUseGooglePay ();
    }

    @Override
    public void loadPaymentDataForGPay (long cents) {
        applicationRepository.getLoadPaymentDataTask (cents);
    }

    @Override
    public MutableLiveData<AuthState> getAuthStateMutableLiveData () {
        return authStateMutableLiveData;
    }

    public MutableLiveData<Boolean> get_canUseGooglePay () {
        return _canUseGooglePay;
    }

    public MutableLiveData<Task<PaymentData>> getPaymentDataTaskMutableLiveData () {
        return paymentDataTaskMutableLiveData;
    }
}
