package com.hobby.mallik.bluetoothcomm;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
import java.util.Arrays;
import java.util.Set;
import java.util.UUID;

public class OTAFirmwareUpdate extends AppCompatActivity {

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
    byte[] fileData;

    int fileStartIndex, fileEndIndex;


    BltManager bltMan;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);

        bltMan = new BltManager(this);
        mBluetoothAdapter = bltMan.getmBluetoothAdapter();
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        btnSend = (Button) findViewById(R.id.send);
        btnScan = (Button) findViewById(R.id.scan);
        textbox = (EditText) findViewById(R.id.editText);
        txtVw = (TextView) findViewById(R.id.textView);
        updateState = (TextView) findViewById(R.id.updateStatus);
        mAdapter = bltMan.getmAdapter();
        mAdapterForList = bltMan.getmAdapterList();
        // Set the adapter
        mListView = (ListView) findViewById(R.id.listView);

        mListView.setAdapter(mAdapterForList);

        bltMan.enableBluetooth();


        mListView.setOnItemClickListener(new android.widget.AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //  BluetoothDevice device = (BluetoothDevice)mListView.getItemAtPosition(position);
                BluetoothDevice device = mAdapter.getItem(position);
                showToast(device.getName());
                try {
                    bltMan.openBluetooth(device);
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    showToast("Unbale to establish bluetooth com link");
                }
            }

        });


        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String msg = textbox.getText().toString();

                   bltMan.sendData(msg.getBytes());

            }
        });

    }

    @Override
    protected void onStop() {
        super.onStop();
        bltMan.closeBluetooth();
    }


    @Override
    protected void onStart() {
        super.onStart();
    }


    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void pairedDevicesList()
    {
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();

        if (pairedDevices.size()>0)
        {
            for(BluetoothDevice bt : pairedDevices)
            {
                mAdapterForList.add(bt.getName() + "\n" + bt.getAddress()); //Get the device's name and the address
                mAdapter.add(bt);
                mAdapter.notifyDataSetChanged();
            }
        }
        else
        {
            showToast("No Paired Bluetooth Devices Found.");
        }

    }

    public void scanDevices(View v){
        mAdapter.clear();
        pairedDevicesList(); //method that will be called
    }

    private void pairDevice(BluetoothDevice device) {
        try {
            Method method = device.getClass().getMethod("createBond", (Class[]) null);
            method.invoke(device, (Object[]) null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}