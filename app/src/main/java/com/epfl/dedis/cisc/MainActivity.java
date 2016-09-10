package com.epfl.dedis.cisc;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.epfl.dedis.api.ConfigUpdate;
import com.epfl.dedis.crypto.Utils;
import com.epfl.dedis.net.Identity;
import com.google.zxing.WriterException;

public class MainActivity extends AppCompatActivity implements Activity {

    private ImageView mQrImageView;
    private TextView mStatusLabel;

    private Identity mIdentity;

    public void taskJoin() {
        float px = Utils.dpToPixel(mQrImageView.getWidth(), getResources().getDisplayMetrics());
        String identityBase64 = Utils.encodeBase64(mIdentity.getId());

        try {
            mQrImageView.setImageBitmap(Utils.encodeQR(identityBase64, (int) px));
            mStatusLabel.setText(R.string.info_connection);
        } catch (WriterException e){
            mStatusLabel.setText(e.getMessage());
        }
    }

    public void taskFail(int error) {
        mStatusLabel.setText(error);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mStatusLabel = (TextView) findViewById(R.id.main_status_label);
        assert mStatusLabel != null;

        mQrImageView = (ImageView) findViewById(R.id.main_qr_image);
        assert mQrImageView != null;
        mQrImageView.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ConfigActivity.class);
                startActivity(intent);
            }
        });

        FloatingActionButton mCreateButton = (FloatingActionButton) findViewById(R.id.main_create_button);
        mCreateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, CreateActivity.class);
                startActivity(intent);
            }
        });

        FloatingActionButton mJoinButton = (FloatingActionButton) findViewById(R.id.main_join_button);
        mJoinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, JoinActivity.class);
                startActivity(intent);
            }
        });

        FloatingActionButton mRefreshButton = (FloatingActionButton) findViewById(R.id.main_refresh_button);
        mRefreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences pref = getSharedPreferences(PREF, Context.MODE_PRIVATE);
                String json = pref.getString(IDENTITY, "");
                if (!json.isEmpty()) {
                    mIdentity = Utils.fromJson(json, Identity.class);
                    new ConfigUpdate(MainActivity.this, mIdentity);
                }
            }
        });
    }
}
