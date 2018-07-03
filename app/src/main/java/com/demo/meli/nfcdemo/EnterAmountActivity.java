package com.demo.meli.nfcdemo;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import java.text.ParseException;

import faranjit.currency.edittext.CurrencyEditText;

public class EnterAmountActivity extends AppCompatActivity {

    public static String EXTRA_AMOUNT = "extra_amount";

    private CurrencyEditText amountEditText;
    private Button confirmButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enter_amount);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        amountEditText = findViewById(R.id.amount_edit_text);
        amountEditText.setText("0");
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);

        confirmButton = findViewById(R.id.wait_for_payment);
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent returnIntent = new Intent();
                Float amount = 0f;
                try {
                    amount = Float.parseFloat(amountEditText.getText().toString().replace("$", "").replace(",", "."));
                } catch (Exception e) {
                    e.printStackTrace();
                }

                returnIntent.putExtra(EXTRA_AMOUNT, amount);
                setResult(RESULT_OK, returnIntent);
                finish();
            }
        });
    }

    public static Intent newIntent(final Context context) {
        final Intent intent = new Intent(context, EnterAmountActivity.class);
        return intent;
    }

    @Override
    protected void onPause() {
        super.onPause();

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getWindow().getCurrentFocus().getWindowToken(), 0);
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
