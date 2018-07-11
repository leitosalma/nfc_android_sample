package com.demo.meli.nfcdemo.seller;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.Toast;

import com.demo.meli.nfcdemo.MainActivity;
import com.demo.meli.nfcdemo.NfcApplication;
import com.demo.meli.nfcdemo.R;
import com.demo.meli.nfcdemo.buyer.NfcCustomCardReader;

public class NfcWaitForPaymentActivity extends AppCompatActivity {
    public static final String TAG = "NfcWaitForPaymentActivity";

    public static Intent newIntent(final Context context) {
        final Intent intent = new Intent(context, NfcWaitForPaymentActivity.class);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nfc_wait_for_payment);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    // Handling the received Intents from NfcCardEmulationService
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //navigateToMainScreen();

            Toast.makeText(NfcWaitForPaymentActivity.this, "Tag Read", Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    protected void onResume() {
        super.onResume();

        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, new IntentFilter(NfcCardEmulationService.INTENT_TAG_READ));
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);

        super.onPause();
    }

    private void navigateToMainScreen() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void finish() {
        ((NfcApplication) getApplication()).setWaitingForPayment(false);

        super.finish();
    }

}