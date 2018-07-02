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

public class EnterAmountActivity extends AppCompatActivity {

    public static String EXTRA_AMOUNT = "extra_amount";

    private EditText amountEditText;
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
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(amountEditText, InputMethodManager.SHOW_IMPLICIT);
        amountEditText.requestFocus();

        confirmButton = findViewById(R.id.wait_for_payment);
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent returnIntent = new Intent();
                returnIntent.putExtra(EXTRA_AMOUNT, Float.parseFloat(amountEditText.getText().toString()));
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
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }
}
