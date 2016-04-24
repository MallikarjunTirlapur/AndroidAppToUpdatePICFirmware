package com.hobby.mallik.bluetoothcomm;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.UUID;

public class BluetoothActivity extends AppCompatActivity {

    BluetoothAdapter mBluetoothAdapter;
    EditText textbox;
    Button btnSend;
    Button btnScan;
    ScrollView scrlView;
    TextView txtVw, updateState;
    boolean endLess = false;
    private ArrayAdapter<BluetoothDevice> mAdapter;
    private ArrayAdapter<String> mAdapterForList;
    ListView mListView;

    protected static final String TAG = "Mallik";
    TextView myLabel;
    BluetoothSocket mmSocket;
    OutputStream mmOutputStream;
    InputStream mmInputStream;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View v = getLayoutInflater().inflate(R.layout.activity_bluetooth, null);
        setContentView(v);
        BltManager bltMng = new BltManager(BluetoothActivity.this, v);

        setContentView(R.layout.activity_bluetooth);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        btnSend = (Button) findViewById(R.id.send);
        btnScan = (Button) findViewById(R.id.scan);
        textbox = (EditText) findViewById(R.id.editText);
        txtVw = (TextView) findViewById(R.id.textView);
        updateState = (TextView) findViewById(R.id.updateStatus);
        mAdapter = new ArrayAdapter<BluetoothDevice>(BluetoothActivity.this, android.R.layout.simple_list_item_1);
        mAdapterForList = new ArrayAdapter<String>(BluetoothActivity.this, android.R.layout.simple_list_item_1);
        // Set the adapter
        mListView = (ListView)findViewById(R.id.listView);

        mListView.setAdapter(mAdapterForList);

        bltMng.enableBluetooth();
        bltMng.openBluetooth();


        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String msg = textbox.getText().toString();
                try {
                    int byt = mmInputStream.read();
                    updateState.setText(String.valueOf(byt));
                    //mmOutputStream.write(msg.getBytes());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

    }



    @Override
    protected void onStart() {
        super.onStart();
        // TODO onResume() is better?
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (null == mBluetoothAdapter) {
            txtVw.setText("No bluetooth adapter available");
        }
        if (!mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.enable();
        }
    }



    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }



    private void readBinary(){
        File file = new File("/storage/emulated/0/crtBin.bin");
        byte[] fileData = new byte[(int) file.length()];
        try {
            DataInputStream dis = new DataInputStream(new FileInputStream(file));
            dis.readFully(fileData);
            dis.close();
        }catch (Exception ex){
            System.console();
        }
    }
}



