package com.demo.meli.nfcdemo;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

public class AmountActivity extends AppCompatActivity {

    private EditText amountEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_amount);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        amountEditText = findViewById(R.id.amount_edit_text);
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(amountEditText, InputMethodManager.SHOW_IMPLICIT);
        amountEditText.requestFocus();

        Button waitForPaymentButton = findViewById(R.id.wait_for_payment);
        waitForPaymentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Send the amount to the CardEmulationService
                Intent intent = new Intent(view.getContext(), NfcCardEmulationService.class);
                intent.putExtra(NfcCardEmulationService.AMOUNT_TO_RECEIVE_EXTRA, Float.parseFloat(amountEditText.getText().toString()));
                startService(intent);

                Intent activityIntent = new Intent(AmountActivity.this, NfcPlaceholderActivity.class);
                startActivity(activityIntent);
            }
        });
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
