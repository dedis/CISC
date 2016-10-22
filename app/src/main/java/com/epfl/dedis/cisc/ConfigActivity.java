package com.epfl.dedis.cisc;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.epfl.dedis.api.ConfigUpdate;
import com.epfl.dedis.api.ProposeUpdate;
import com.epfl.dedis.api.ProposeVote;
import com.epfl.dedis.crypto.Utils;
import com.epfl.dedis.net.Identity;

import java.util.HashMap;
import java.util.Map;

public class ConfigActivity extends AppCompatActivity implements Activity {

    private TextView mStatusTextView;

    private Identity mIdentity;
    private boolean mUpdate;
    private boolean mProposed;
    private boolean mVote;

    private enum States {
      IDLE, PRE_VOTE, POST_VOTE_SUCC, POST_VOTE_WAIT
    };

    // TODO: IMPORTANT! Find simpler way to detect changes in Config; Currently very messy
    public void taskJoin() {
        Map<String, String> configDevice = new HashMap<>(mIdentity.getConfig().getDevice());
        Map<String, String> configData = new HashMap<>(mIdentity.getConfig().getData());

        Map<String, String> proposedDevice = mIdentity.getProposed() == null ?
                                                new HashMap<String, String>() :
                                                new HashMap<>(mIdentity.getProposed().getDevice());

        Map<String, String> proposedData = mIdentity.getProposed() == null ?
                                                new HashMap<String, String>() :
                                                new HashMap<>(mIdentity.getProposed().getData());
        // TODO: More robust state machine; also in other Activities that have to deal with several requests
        if (mUpdate) {
            if (proposedDevice.isEmpty()) {
                mStatusTextView.setText(R.string.info_uptodate);
                mProposed = true;
            }
            if (configDevice.keySet().equals(proposedDevice.keySet()) && configData.keySet().equals(proposedData.keySet())) {
                mStatusTextView.setText(R.string.info_acceptedchange);
                mIdentity.setProposed(null);
                mProposed = true;
            }

            SharedPreferences.Editor editor = getSharedPreferences(PREF, Context.MODE_PRIVATE).edit();
            editor.putString(IDENTITY, Utils.toJson(mIdentity));
            editor.apply();
        } else if (mProposed){
            proposedDevice.keySet().removeAll(configDevice.keySet());
            proposedData.keySet().removeAll(configData.keySet());
            if (proposedDevice.size() != 0) {
                mStatusTextView.setText(proposedDevice.toString());
            } else {
                mStatusTextView.setText(proposedData.toString());
            }
            mVote = true;
        } else {

        }
    }

    public void taskFail(int error) {
        mStatusTextView.setText(error);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config);

        TextView idTextView = (TextView) findViewById(R.id.config_identity_value);
        TextView addressTextView = (TextView) findViewById(R.id.config_address_value);

        mStatusTextView = (TextView) findViewById(R.id.config_status_value);
        mStatusTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mVote) {
                    new ProposeVote(ConfigActivity.this, mIdentity);
                    mStatusTextView.setText(R.string.info_voted);
                    mVote = false;
                    mProposed = false;
                }
            }
        });

        FloatingActionButton changeButton = (FloatingActionButton) findViewById(R.id.config_change_button);
        changeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ConfigActivity.this, DataActivity.class);
                startActivity(intent);
                finish();
            }
        });

        FloatingActionButton deviceButton = (FloatingActionButton) findViewById(R.id.config_devices_button);
        deviceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ConfigActivity.this, DeviceActivity.class);
                startActivity(intent);
            }
        });

        FloatingActionButton refreshButton = (FloatingActionButton) findViewById(R.id.config_refresh_button);
        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new ConfigUpdate(ConfigActivity.this, mIdentity);
                mUpdate = true;
            }
        });

        FloatingActionButton fetchButton = (FloatingActionButton) findViewById(R.id.config_fetch_button);
        fetchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mProposed) {
                    new ProposeUpdate(ConfigActivity.this, mIdentity);
                    mUpdate = false;
                }
            }
        });

        SharedPreferences sharedPreferences = getSharedPreferences(PREF, Context.MODE_PRIVATE);
        mIdentity = Utils.fromJson(sharedPreferences.getString(IDENTITY, ""), Identity.class);

        idTextView.setText(Utils.encodeBase64(mIdentity.getId()));
        addressTextView.setText(mIdentity.getCothority().getHost());

        if (getIntent().hasExtra("wait"))
            mStatusTextView.setText(getIntent().getStringExtra("wait"));
        mProposed = getIntent().getBooleanExtra("pro", true);
        mVote = getIntent().getBooleanExtra("vote", false);
    }
}