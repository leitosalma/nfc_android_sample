package com.demo.meli.nfcdemo.buyer;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.demo.meli.nfcdemo.EnterAmountActivity;
import com.demo.meli.nfcdemo.R;

import org.w3c.dom.Text;

public class NfcConfirmPayment extends AppCompatActivity {

    private static String EXTRA_AMOUNT_TO_PAY = "extra_amount_to_pay";

    public static Intent newIntent(final Context context, Float amount) {
        final Intent intent = new Intent(context, NfcConfirmPayment.class);
        intent.putExtra(EXTRA_AMOUNT_TO_PAY, amount);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nfc_confirm_payment);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        TextView amountTextView = findViewById(R.id.payment_amount);
        Float amountToPay = getIntent().getFloatExtra(EXTRA_AMOUNT_TO_PAY, 0f);
        amountTextView.setText("$ " + amountToPay.toString());
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
