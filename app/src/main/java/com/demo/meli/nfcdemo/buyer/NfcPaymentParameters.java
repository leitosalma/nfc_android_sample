package com.demo.meli.nfcdemo.buyer;

import android.net.Uri;
import android.util.Log;

import java.util.Set;

public class NfcPaymentParameters {
    private String userId;
    private Float paymentAmount;


    public NfcPaymentParameters(String url) {
        Uri uri = Uri.parse(url);
        parseFromUri(uri);
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Float getPaymentAmount() {
        return paymentAmount;
    }

    public void setPaymentAmount(Float paymentAmount) {
        this.paymentAmount = paymentAmount;
    }

    private void parseFromUri(Uri uri) {
        /*String protocol = uri.getScheme();
        String server = uri.getAuthority();
        String path = uri.getPath();
        Set<String> args = uri.getQueryParameterNames();*/

        Log.i("NfcPaymentParameters", "Parsing: " + uri.toString());

        userId = uri.getQueryParameter("userId");

        String amount = uri.getQueryParameter("amount");
        if (amount == null) {
            paymentAmount = 0f;
        } else {
            paymentAmount = Float.parseFloat(amount);
        }
    }
}