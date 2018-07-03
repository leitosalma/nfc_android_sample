package com.demo.meli.nfcdemo;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

public class NfcPlaceholderActivity extends AppCompatActivity {

    private static String EXTRA_PLACEHOLDER_TEXT = "extra_placeholder_text";

    // NFC-related variables
    private NfcAdapter mNfcAdapter;
    private PendingIntent mNfcPendingIntent;
    IntentFilter[] mReadTagFilters;

    public static Intent newIntent(final Context context, String placeholderText) {
        final Intent intent = new Intent(context, NfcPlaceholderActivity.class);
        intent.putExtra(EXTRA_PLACEHOLDER_TEXT, placeholderText);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nfc_placeholder);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        String placeholderText = getIntent().getStringExtra(EXTRA_PLACEHOLDER_TEXT);
        if (placeholderText != null) {
            TextView placeholderTextView = findViewById(R.id.placeholder_text);
            placeholderTextView.setText(placeholderText);
        }

        setupNfcAdapter();

        handleIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        handleIntent(intent);
    }

    NdefMessage[] getNdefMessagesFromIntent(Intent intent) {
        // Parse the intent
        NdefMessage[] msgs = null;
        String action = intent.getAction();
        if (action.equals(NfcAdapter.ACTION_TAG_DISCOVERED) || action.equals(NfcAdapter.ACTION_NDEF_DISCOVERED)) {
            Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            if (rawMsgs != null){
                msgs = new NdefMessage[rawMsgs.length];
                for (int i = 0; i < rawMsgs.length; i++) {
                    msgs[i] = (NdefMessage) rawMsgs[i];
                }

            } else {
                // Unknown tag type
                byte[] empty = new byte[] {};
                NdefRecord record = new NdefRecord(NdefRecord.TNF_UNKNOWN, empty, empty, empty);
                NdefMessage msg = new NdefMessage(new NdefRecord[] { record });
                msgs = new NdefMessage[] { msg };
            }

        } else {
            Log.e("NFC", "Unknown intent.");
            finish();
        }
        return msgs;
    }

    // Handling the received Intents from NfcCardEmulationService
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            navigateToMainScreen();
        }
    };

    @Override
    protected void onResume() {
        super.onResume();

        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, new IntentFilter(NfcCardEmulationService.INTENT_TAG_READ));

        setupNfcForegroundDispatch(this, mNfcAdapter);

    }

    @Override
    protected void onPause() {
        stopNfcForegroundDispatch(this, mNfcAdapter);

        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);

        super.onPause();
    }

    private void navigateToMainScreen() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    private void setupNfcAdapter() {
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);

        if (mNfcAdapter == null) {
            // Stop here, we definitely need NFC
            Toast.makeText(this, "Tu teléfono no soporta NFC", Toast.LENGTH_LONG).show();
            finish();
            return;

        }

        if (!mNfcAdapter.isEnabled()) {
            Toast.makeText(this, "Tenés que habilitar NFC en tu teléfono para poder enviar y recibir pagos", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleIntent(Intent intent) {
        Log.d("NFC", "onNewIntent: " + intent);

        if (intent != null && intent.getAction() != null) {
            if (intent.getAction().equals(NfcAdapter.ACTION_NDEF_DISCOVERED)) {
                NdefMessage[] msgs = getNdefMessagesFromIntent(intent);
                Toast.makeText(this, "NDEF: " + msgs[0].getRecords()[0].getPayload().toString(), Toast.LENGTH_LONG).show();
            } else if (intent.getAction().equals(NfcAdapter.ACTION_TAG_DISCOVERED)) {
                Toast.makeText(this, "This NFC tag has no NDEF data.", Toast.LENGTH_LONG).show();
            }
        }
    }

    public static void setupNfcForegroundDispatch(final Activity activity, NfcAdapter adapter) {
        final Intent intent = new Intent(activity.getApplicationContext(), activity.getClass());
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        final PendingIntent pendingIntent = PendingIntent.getActivity(activity.getApplicationContext(), 0, intent, 0);

        IntentFilter[] filters = new IntentFilter[1];
        String[][] techList = new String[][]{};

        // Notice that this is the same filter as in our manifest.
        filters[0] = new IntentFilter();
        filters[0].addAction(NfcAdapter.ACTION_NDEF_DISCOVERED);
        filters[0].addCategory(Intent.CATEGORY_DEFAULT);
        filters[0].addDataScheme("melinfc");

        adapter.enableForegroundDispatch(activity, pendingIntent, filters, techList);
    }

    public static void stopNfcForegroundDispatch(final Activity activity, NfcAdapter adapter) {
        adapter.disableForegroundDispatch(activity);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

}