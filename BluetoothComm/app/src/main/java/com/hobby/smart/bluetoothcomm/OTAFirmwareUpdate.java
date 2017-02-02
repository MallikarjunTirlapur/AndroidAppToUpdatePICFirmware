
/*
 * The MIT License
 *
 * Copyright(c) 2016 Mallikarjun Tirlapur.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */


package com.hobby.smart.bluetoothcomm;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.pm.ActivityInfo;
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

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Set;

/**
 * Created by Mallikarjun Tirlapur on 20-06-2016.
 */

/**
 *  OTAFirmwareUpdate class.
 *  The class interacts with the app GUI get the inputs and responds for the query.
 *  Defines and initializes all the GUI objects
 */
public class OTAFirmwareUpdate extends AppCompatActivity {

    private BluetoothAdapter bluetoothAdapter;
    private Button btnUpdtFrmwr;
    private Button btnScan;
    private TextView txtVw;
    private BluetoothDevice device;
    private ArrayAdapter<BluetoothDevice> mAdapter;
    private ArrayAdapter<String> mAdapterForList;
    private ArrayAdapter<File> fileInstances;
    private ArrayAdapter<String> fileNames;
    private ListView listView, fileList;
    private BltManager bltMan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);
        bltMan = new BltManager(this);
        bluetoothAdapter = bltMan.getmBluetoothAdapter();
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        btnUpdtFrmwr = (Button) findViewById(R.id.update);
        btnScan = (Button) findViewById(R.id.scan);
        txtVw = (TextView) findViewById(R.id.textView);
        mAdapter = bltMan.getmAdapter();
        mAdapterForList = bltMan.getmAdapterList();
        listView = (ListView) findViewById(R.id.listView);
        fileList = (ListView) findViewById(R.id.binListView);

        /** Set the adapters */
        fileInstances = new ArrayAdapter<File>(this, android.R.layout.simple_list_item_1);
        fileNames = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);

        listView.setAdapter(mAdapterForList);
        fileList.setAdapter(fileNames);

        bltMan.enableBluetooth();

        listView.setOnItemClickListener(new android.widget.AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                device = mAdapter.getItem(position);
                showToast(device.getName());
                try {
                    bltMan.openBluetooth(device);
                    showToast("established bluetooth com link");
                } catch (IOException e) {
                    showToast("Unbale to establish bluetooth com link");
                }
            }

        });

        fileList.setOnItemClickListener(new android.widget.AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                File binFile = fileInstances.getItem(position);
                showToast(binFile.getName());
                try {
                    bltMan.readHex(binFile.getPath());
                } catch (IOException e) {
                    showToast("Unbale to read file");
                } catch (NumberFormatException ef){
                    showToast("Enter Programmable Start and End address of PIC");
                }
            }

        });



        btnUpdtFrmwr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bltMan.sendData((byte)SystemStates.SYS_ADDRESS_RANGE_RQT);
                bltMan.listenForData();
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
    }


    @Override
    protected void onStart() {
        super.onStart();
    }

    /**
     * call the funtion for the toast
     */
    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    /**
     * Get the paired device list..
     */
    private void pairedDevicesList()
    {
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();

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

    /**
     * Function publishes available files on the GUI .
     * @param fList File array.
     */
    private void publishFileList(File[] fList)
    {
        if (fList.length > 0)
        {
            for(File fl : fList)
            {
                fileNames.add(fl.getName()); //Get the device's name and the address
                fileInstances.add(fl);
                fileInstances.notifyDataSetChanged();
            }
        }
        else
        {
            showToast("No binary files Found.");
        }

    }

    /**
     * Scan the available bluetooth devices .
     * @param v instance of View.
     */
    public void scanDevices(View v){
        mAdapter.clear();
        pairedDevicesList(); //method that will be called
    }

    /**
     * get the binary files from the specified location.
     * @param v instance of View.
     */
    void listBinaryFiles(View v){
        File directory = new File("/storage/emulated/0/HexFiles");
        //get all the file list from a directory
        File[] fList = directory.listFiles();
        publishFileList(fList);
    }

    /**
     * The fauntion pairs the bluetooth devices.
     * @param device instance of BluetoothDevice.
     */
    private void pairDevice(BluetoothDevice device) {
        try {
            Method method = device.getClass().getMethod("createBond", (Class[]) null);
            method.invoke(device, (Object[]) null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}