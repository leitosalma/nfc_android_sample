package com.demo.meli.nfcdemo.buyer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.demo.meli.nfcdemo.EnterAmountActivity;
import com.demo.meli.nfcdemo.MainActivity;
import com.demo.meli.nfcdemo.NfcApplication;
import com.demo.meli.nfcdemo.R;
import com.demo.meli.nfcdemo.seller.NfcCardEmulationService;

public class NfcWaitForChargeActivity extends AppCompatActivity implements NfcCustomCardReader.TagReadCallback {
    public static final String TAG = "NfcWaitForChargeActivity";
    private static int PAY_REQUEST_CODE = 1;

    // Recommend NfcAdapter flags for reading from other Android devices. Indicates that this
    // activity is interested in NFC-A devices (including other Android devices), and that the
    // system should not check for the presence of NDEF-formatted data (e.g. Android Beam).
    public static int READER_FLAGS = NfcAdapter.FLAG_READER_NFC_A | NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK;
    public NfcCustomCardReader mNfcCardReader;
    private StringBuilder payloadReceived = new StringBuilder();

    public static Intent newIntent(final Context context) {
        final Intent intent = new Intent(context, NfcWaitForChargeActivity.class);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nfc_wait_for_charge);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        ///////////////////////////////////////////////////////////////////////////////////////////////////
        // NFC
        mNfcCardReader = new NfcCustomCardReader(this);
    }

    // Handling the received Intents from NfcCardEmulationService
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //navigateToMainScreen();

            Toast.makeText(NfcWaitForChargeActivity.this, "Tag Read", Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    protected void onResume() {
        super.onResume();

        enableReaderMode();

        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, new IntentFilter(NfcCardEmulationService.INTENT_TAG_READ));
    }

    @Override
    protected void onPause() {
        disableReaderMode();

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

    @Override
    public void onTagReceived(final String payload) {
        // This callback is run on a background thread, but updates to UI elements must be performed
        // on the UI thread.

        if (payload != null) {
            payloadReceived.append(payload);
            if (payload.contains("END")) {
            }
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String tagContent = "";
                if (payload != null) {
                    tagContent = payload;
                }

                NfcPaymentParameters params = new NfcPaymentParameters(tagContent);

                if (params.getPaymentAmount() > 0) {
                    startActivity(NfcConfirmPayment.newIntent(NfcWaitForChargeActivity.this, params.getPaymentAmount()));
                } else {
                    startActivityForResult(EnterAmountActivity.newIntent(NfcWaitForChargeActivity.this), PAY_REQUEST_CODE);
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PAY_REQUEST_CODE) {
            if (data != null) {
                Float paymentAmount = data.getFloatExtra(EnterAmountActivity.EXTRA_AMOUNT, 0);
                Intent intent = NfcConfirmPayment.newIntent(NfcWaitForChargeActivity.this, paymentAmount);
                startActivity(intent);
            }
        }
    }

    // Disable Android Beam and register our card reader callback
    private void enableReaderMode() {
        Log.i(TAG, "Enabling reader mode");
        NfcAdapter nfc = NfcAdapter.getDefaultAdapter(this);
        if (nfc != null) {
            nfc.enableReaderMode(this, mNfcCardReader, READER_FLAGS, null);
        }
    }

    private void disableReaderMode() {
        Log.i(TAG, "Disabling reader mode");
        NfcAdapter nfc = NfcAdapter.getDefaultAdapter(this);
        if (nfc != null) {
            nfc.disableReaderMode(this);
        }
    }

}