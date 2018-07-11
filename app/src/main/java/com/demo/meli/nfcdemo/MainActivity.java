package com.demo.meli.nfcdemo;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.demo.meli.nfcdemo.buyer.NfcWaitForChargeActivity;
import com.demo.meli.nfcdemo.seller.NfcCardEmulationService;
import com.demo.meli.nfcdemo.seller.NfcCustomCardEmulationService;
import com.demo.meli.nfcdemo.seller.NfcWaitForPaymentActivity;

public class MainActivity extends AppCompatActivity {

    private static int PAY_REQUEST_CODE = 1;
    private static int RECEIVE_PAYMENT_REQUEST_CODE = 2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        CardView receivePaymentButton = findViewById(R.id.receive_payment_button);
        receivePaymentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(EnterAmountActivity.newIntent(MainActivity.this), PAY_REQUEST_CODE);
            }
        });

        CardView sendPaymentButton = findViewById(R.id.send_payment_button);
        sendPaymentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(NfcWaitForChargeActivity.newIntent(MainActivity.this));
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PAY_REQUEST_CODE) {
            if (data != null) {
                Float paymentAmount = data.getFloatExtra(EnterAmountActivity.EXTRA_AMOUNT, 0);
                prepareNfcForPayment(paymentAmount);
            }
        }
    }

    private void prepareNfcForPayment(Float amount) {
        // Enqueue the amount to be consumed by HostApduService's
        ((NfcApplication) getApplication()).setAmountInQueue(amount);
        ((NfcApplication) getApplication()).setWaitingForPayment(true);
        startService(NfcCardEmulationService.newIntent(this));
        startService(NfcCustomCardEmulationService.newIntent(this));

        // Show the placeholder activity
        startActivity(NfcWaitForPaymentActivity.newIntent(this));
    }
}