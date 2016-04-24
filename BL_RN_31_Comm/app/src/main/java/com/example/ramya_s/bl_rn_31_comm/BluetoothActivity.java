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
    TextView txtVw;
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
        setContentView(R.layout.activity_bluetooth);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        btnSend = (Button) findViewById(R.id.send);
        btnScan = (Button) findViewById(R.id.scan);
        textbox = (EditText) findViewById(R.id.editText);
        txtVw = (TextView) findViewById(R.id.textView);
        mAdapter = new ArrayAdapter<BluetoothDevice>(BluetoothActivity.this, android.R.layout.simple_list_item_1);
        mAdapterForList = new ArrayAdapter<String>(BluetoothActivity.this, android.R.layout.simple_list_item_1);
        // Set the adapter
        mListView = (ListView)findViewById(R.id.listView);

        mListView.setAdapter(mAdapterForList);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(mBluetoothAdapter == null)
        {
            //Show a mensag. that thedevice has no bluetooth adapter
            Toast.makeText(getApplicationContext(), "Bluetooth Device Not Available", Toast.LENGTH_LONG).show();
            //finish apk
            finish();
        }
        else
        {
            if (!mBluetoothAdapter.isEnabled())

            {
                //Ask to the user turn the bluetooth on
                Intent turnBTon = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(turnBTon,1);
            }
        }


        mListView.setOnItemClickListener(new android.widget.AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //  BluetoothDevice device = (BluetoothDevice)mListView.getItemAtPosition(position);
                BluetoothDevice device = mAdapter.getItem(position);

                showToast(device.getName());

                try {
                    Log.v(TAG, "in send data...");
                    UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb"); //Standard SerialPortService ID
                    mmSocket = device.createInsecureRfcommSocketToServiceRecord(uuid);
                    mmSocket.connect();
                    mmOutputStream = mmSocket.getOutputStream();
                    mmInputStream = mmSocket.getInputStream();
                    txtVw.setText("Bluetooth Opened");
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
                try {
                    mmOutputStream.write(msg.getBytes());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

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
            Toast.makeText(getApplicationContext(), "No Paired Bluetooth Devices Found.", Toast.LENGTH_LONG).show();
        }

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

    public void scanDevices(View v){
        mAdapter.clear();
        pairedDevicesList(); //method that will be called
    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
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



