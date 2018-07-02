package com.demo.meli.nfcdemo;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    private static int PAY_REQUEST_CODE = 1;
    private static int RECEIVE_PAYMENT_REQUEST_CODE = 2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Button receivePaymentButton = findViewById(R.id.receive_payment_button);
        receivePaymentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(EnterAmountActivity.newIntent(MainActivity.this), 1);
            }
        });

        Button sendPaymentButton = findViewById(R.id.send_payment_button);
        sendPaymentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(NfcPlaceholderActivity.newIntent(MainActivity.this, "Acercá tu teléfono al del vendedor para realizar el pago"));
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PAY_REQUEST_CODE) {
            Float paymentAmount = data.getFloatExtra(EnterAmountActivity.EXTRA_AMOUNT, 0);

            // Send the amount to the CardEmulationService
            startService(NfcCardEmulationService.newIntent(this, paymentAmount));

            // Show the placeholder activity
            startActivity(NfcPlaceholderActivity.newIntent(this, "Acercá tu teléfono al del comprador para terminar el pago"));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
