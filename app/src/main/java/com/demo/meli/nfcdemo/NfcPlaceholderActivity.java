package com.demo.meli.nfcdemo;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.IntentCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class NfcPlaceholderActivity extends AppCompatActivity {

    private static String EXTRA_PLACEHOLDER_TEXT = "extra_placeholder_text";

    // NFC-related variables
    private NfcAdapter mNfcAdapter;
    private PendingIntent mNfcPendingIntent;
    IntentFilter[] mReadTagFilters;

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

        // Catch the NFC Intent even if this activity is in foreground
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (mNfcAdapter == null){
            Toast.makeText(this, "Tu dispositivo no cuenta con el hardware necesario para pagar con NFC", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        mNfcPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        IntentFilter ndefDetected = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
        ndefDetected.addDataScheme("melinfc");
        mReadTagFilters = new IntentFilter[] { ndefDetected };
    }

    public static Intent newIntent(final Context context, String placeholderText) {
        final Intent intent = new Intent(context, NfcPlaceholderActivity.class);
        intent.putExtra(EXTRA_PLACEHOLDER_TEXT, placeholderText);
        return intent;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        Log.d("NFC", "onNewIntent: " + intent);

        // Currently in tag READING mode
        if (intent.getAction().equals(NfcAdapter.ACTION_NDEF_DISCOVERED)) {
            NdefMessage[] msgs = getNdefMessagesFromIntent(intent);

            Toast.makeText(this, "NDEF: " + msgs[0].toString(), Toast.LENGTH_LONG).show();
        } else if (intent.getAction().equals(NfcAdapter.ACTION_TAG_DISCOVERED)) {
            Toast.makeText(this, "This NFC tag has no NDEF data.", Toast.LENGTH_LONG).show();
        }
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

        if (getIntent().getAction() != null) {
            // tag received when app is not running and not in the foreground:
            if (getIntent().getAction().equals(NfcAdapter.ACTION_NDEF_DISCOVERED)) {
                NdefMessage[] msgs = getNdefMessagesFromIntent(getIntent());
                NdefRecord record = msgs[0].getRecords()[0];
                byte[] payload = record.getPayload();

                String payloadString = new String(payload);

                Toast.makeText(NfcPlaceholderActivity.this, "Payload: " + payloadString, Toast.LENGTH_LONG).show();
            }
        }

        // Enable priority for current activity to detect scanned tags
        // enableForegroundDispatch( activity, pendingIntent, intentsFiltersArray, techListsArray );
        mNfcAdapter.enableForegroundDispatch(this, mNfcPendingIntent, mReadTagFilters, null);

    }

    @Override
    protected void onPause() {
        super.onPause();

        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);

        mNfcAdapter.disableForegroundDispatch(this);
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

}