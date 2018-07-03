package com.demo.meli.nfcdemo;

import android.app.Application;

public class NfcApplication extends Application {
    private boolean waitingForPayment = false;


    public boolean amIWaitingForPayment() {
        return waitingForPayment;
    }

    public void setWaitingForPayment(boolean waitingForPayment) {
        this.waitingForPayment = waitingForPayment;
    }
}