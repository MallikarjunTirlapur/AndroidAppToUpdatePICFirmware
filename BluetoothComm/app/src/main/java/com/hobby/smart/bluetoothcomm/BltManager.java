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
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

/**
 * Created by Mallikarjun Tirlapur on 20-06-2016.
 */

/**
 *  BltManager class inherits the properties of class SystemStates.
 *  The class establishes the bluetooth communication link.
 *  CExchanges command with the PIC
 */
public class BltManager extends SystemStates{

    private BluetoothAdapter mBluetoothAdapter;
    private ArrayAdapter<BluetoothDevice> mAdapter;
    private ArrayAdapter<String> mAdapterForList;
    private BluetoothSocket blSocket;
    private OutputStream blOutStream;
    private InputStream blInStream;
    private Context thisCnt;
    private OTAFirmwareUpdate act;
    private Thread workerThread;
    volatile boolean stopWorker;

    /**
     * default empty constructor
     */
    public BltManager(){

    }

    /**
     * Constructor initializes all the instances decalred in the class
     */
    public BltManager(Context cnt){

        this.thisCnt = cnt;
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        act = new OTAFirmwareUpdate();
        mAdapter = new ArrayAdapter<BluetoothDevice>(thisCnt, android.R.layout.simple_list_item_1);
        mAdapterForList = new ArrayAdapter<String>(thisCnt, android.R.layout.simple_list_item_1);
    }

    /**
     * Function enables the Bluettoth
     */
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

    /**
     * Function Opens the Bluettoth port and establishes communication link
     */
    public void openBluetooth(BluetoothDevice device) throws IOException{
        if (null != blSocket) {
            closeBluetooth();
        }
        UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb"); //Standard SerialPortService ID
        blSocket = device.createInsecureRfcommSocketToServiceRecord(uuid);
        blSocket.connect();
        blOutStream = blSocket.getOutputStream();
        blInStream = blSocket.getInputStream();
        setOutputStream(blOutStream);
    }

    /**
     * Function closes the Bluettoth port and cuts the link
     */
    public void closeBluetooth(){
        try {
            blOutStream.close();
            blInStream.close();
            blSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Function defines the thread which listens data from the PIC
     */
    public void listenForData()
    {
        final Handler handler = new Handler();
        stopWorker = false;
        workerThread = new Thread(new Runnable()
        {
            public void run()
            {
                while(!Thread.currentThread().isInterrupted()&& !stopWorker)
                {
                    try
                    {
                        int bytesAvailable = blInStream.available();
                        if(bytesAvailable > 0)
                        {
                            final int b = blInStream.read();
                            runStateMachine(b);
                        }
                    }
                    catch (IOException ex)
                    {
                        stopWorker = true;
                    }
                }
            }
        });
        workerThread.start();
    }

    /**
     * Function is called to display the message
     */
    private void showToast(String message) {
        Toast.makeText(thisCnt, message, Toast.LENGTH_LONG).show();
    }


    /**
     * Function is called to send the data to PIC
     * @param data data tobe sent out to PIC
     */
    public void sendData(byte data){
        try {
            blOutStream.write(data);
        }catch (IOException ex){
            ex.printStackTrace();
        }
    }

    public ArrayAdapter<BluetoothDevice> getmAdapter(){
        return mAdapter;
    }

    public ArrayAdapter<String>getmAdapterList(){
        return mAdapterForList;
    }

    public BluetoothAdapter getmBluetoothAdapter(){
        return mBluetoothAdapter;
    }
}
