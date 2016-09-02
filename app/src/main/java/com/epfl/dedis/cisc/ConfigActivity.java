package com.epfl.dedis.cisc;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

public class ConfigActivity extends AppCompatActivity implements Activity {

    private TextView mIdTextView;
    private TextView mAddressTextView;

    private SharedPreferences sharedPreferences;

    public void callbackSuccess() {}
    public void callbackError(int error) {}

    private void populate() {
        mIdTextView.setText(sharedPreferences.getString(ID, ""));
        String address = sharedPreferences.getString(HOST, "") + ":" + sharedPreferences.getString(PORT, "");
        mAddressTextView.setText(address);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config);

        sharedPreferences = getSharedPreferences(PREF, Context.MODE_PRIVATE);

        mIdTextView = (TextView) findViewById(R.id.config_identity_value);
        assert mIdTextView != null;

        mAddressTextView = (TextView) findViewById(R.id.config_address_value);
        assert mAddressTextView != null;

        populate();
    }
}
