package com.hobby.mallik.bluetoothcomm;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.UUID;

/**
 * Created by Mallik on 20-06-2016.
 */
public class BltManager extends Thread {

    BluetoothAdapter mBluetoothAdapter;
    private ArrayAdapter<BluetoothDevice> mAdapter;
    private ArrayAdapter<String> mAdapterForList;
    BluetoothSocket mmSocket;
    OutputStream mmOutputStream;
    InputStream mmInputStream;
    Context thisCnt;
    BluetoothActivity act;
    View mView;
    ListView mListView;
    TextView txtVw;

    public BltManager(Context cnt, View v){
        this.thisCnt = cnt;
        this.mView = v;
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        act = new BluetoothActivity();
        mListView = (ListView)mView.findViewById(R.id.listView);
        mAdapterForList = new ArrayAdapter<String>(thisCnt, android.R.layout.simple_list_item_1);
        mListView.setAdapter(mAdapterForList);
        txtVw = (TextView) mView.findViewById(R.id.textView);
    }


    public void enableBluetooth() {
        if (mBluetoothAdapter == null) {
            //Show a mensag. that thedevice has no bluetooth adapter
            showToast("Bluetooth Device Not Available");
            //finish apk
            act.finish();
        } else {
            if (!mBluetoothAdapter.isEnabled())

            {
                //Ask to the user turn the bluetooth on
                Intent turnBTon = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                act.startActivityForResult(turnBTon, 1);
            }
        }

    }

    public void openBluetooth(){

        mListView.setOnItemClickListener(new android.widget.AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //  BluetoothDevice device = (BluetoothDevice)mListView.getItemAtPosition(position);
                BluetoothDevice device = mAdapter.getItem(position);

                showToast(device.getName());

                try {
                //    Log.v(TAG, "in send data...");
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
    }

    private void showToast(String message) {
        Toast.makeText(thisCnt, message, Toast.LENGTH_LONG).show();
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

    @Override
    public void run() {
        super.run();
    }
}
