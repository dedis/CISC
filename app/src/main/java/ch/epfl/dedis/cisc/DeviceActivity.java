package ch.epfl.dedis.cisc;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import ch.epfl.dedis.crypto.Utils;
import ch.epfl.dedis.net.Config;
import ch.epfl.dedis.net.Identity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DeviceActivity extends AppCompatActivity implements Activity {

    private static final String[] FROM = {"name", "data"};
    private static final int[] TO = {android.R.id.text1, android.R.id.text2};

    public void taskJoin() {}
    public void taskFail(int error) {}

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device);

        ListView listView = (ListView) findViewById(R.id.device_device_list);

        SharedPreferences sharedPreferences = getSharedPreferences(PREF, Context.MODE_PRIVATE);
        Config config = Utils.fromJson(sharedPreferences.getString(IDENTITY, ""), Identity.class).getConfig();

        Map<String, String> devices = config.getDevice();
        Map<String, String> ssh = config.getData();
        List<Map<String, String>> data = new ArrayList<>();

        for (Map.Entry<String, String> d : devices.entrySet()) {
            Map<String, String> datum = new HashMap<>(2);

            String sshKey = "none";
            for (Map.Entry<String, String> s : ssh.entrySet()) {
                if (s.getKey().contains(d.getKey())) {
                    sshKey = s.getValue();
                }
            }

            datum.put("name", d.getKey());
            datum.put("data", "PubKey: " + d.getValue() + "\nSSH: " + sshKey);
            data.add(datum);
        }

        SimpleAdapter adapter = new SimpleAdapter(this, data, android.R.layout.simple_list_item_2, FROM, TO);
        listView.setAdapter(adapter);
    }
}