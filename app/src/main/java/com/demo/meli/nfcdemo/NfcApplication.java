package com.demo.meli.nfcdemo;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.nfc.tech.NfcA;
import android.widget.Toast;

import com.demo.meli.nfcdemo.seller.NfcCardEmulationService;

public class NfcApplication extends Application {
    private boolean waitingForPayment = false;
    private Float amountInQueue = 0f;
    private String paymentUrl = "melinfc://mp.com/processNFCPayment?userId=999";

    public boolean amIWaitingForPayment() {
        return waitingForPayment;
    }

    public void setWaitingForPayment(boolean waitingForPayment) {
        this.waitingForPayment = waitingForPayment;

        if (waitingForPayment == true) {
            // It's the seller, enable HostApduService
            Toast.makeText(getApplicationContext(), "NFC Card Emulation Enabled", Toast.LENGTH_SHORT).show();
            getApplicationContext().getPackageManager().setComponentEnabledSetting(new ComponentName(getApplicationContext(), NfcCardEmulationService.class), PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
        } else {
            // It's the buyer, disable HostApduService
            Toast.makeText(getApplicationContext(), "NFC Card Emulation Disabled", Toast.LENGTH_SHORT).show();
            getApplicationContext().getPackageManager().setComponentEnabledSetting(new ComponentName(getApplicationContext(), NfcCardEmulationService.class), PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
        }
    }

    public String getPaymentUrl() {
        String url = amountInQueue > 0 ? (paymentUrl + "&amount=" + amountInQueue) : paymentUrl;
        return url;
    }

    public void setAmountInQueue(Float amountInQueue) {
        this.amountInQueue = amountInQueue;
    }
}