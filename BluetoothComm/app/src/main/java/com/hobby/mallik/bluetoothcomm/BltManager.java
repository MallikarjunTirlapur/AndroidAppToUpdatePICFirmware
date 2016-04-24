package com.hobby.mallik.bluetoothcomm;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

/**
 * Created by Mallik on 20-06-2016.
 */
public class BltManager extends ReadBinaryFile {

    BluetoothAdapter mBluetoothAdapter;
    private ArrayAdapter<BluetoothDevice> mAdapter;
    private ArrayAdapter<String> mAdapterForList;
    BluetoothSocket mmSocket;
    OutputStream mmOutputStream;
    InputStream mmInputStream;
    Context thisCnt;
    OTAFirmwareUpdate act;
    View mView;
    ListView mListView;
    TextView txtVw;
    Thread workerThread;
    volatile boolean stopWorker;

    ReadBinaryFile fileRead;
    SystemStates state;

    public BltManager(){

    }
    public BltManager(Context cnt){
        state = new SystemStates();
        this.thisCnt = cnt;
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        act = new OTAFirmwareUpdate();
        mAdapter = new ArrayAdapter<BluetoothDevice>(thisCnt, android.R.layout.simple_list_item_1);
        mAdapterForList = new ArrayAdapter<String>(thisCnt, android.R.layout.simple_list_item_1);
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

    public void openBluetooth(BluetoothDevice device) throws IOException{

        UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb"); //Standard SerialPortService ID
        mmSocket = device.createInsecureRfcommSocketToServiceRecord(uuid);
        mmSocket.connect();
        mmOutputStream = mmSocket.getOutputStream();
        mmInputStream = mmSocket.getInputStream();
        mmOutputStream.write(SystemStates.SYS_CONFIG_CMND);
        state.setOutputStream(mmOutputStream);
        listenForData();
    }

    public void closeBluetooth(){
        try {
            mmOutputStream.close();
            mmInputStream.close();
            mmSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    void listenForData()
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
                        int bytesAvailable = mmInputStream.available();
                        if(bytesAvailable > 0)
                        {
                            final int b = mmInputStream.read();

                            state.runStateMachine(b);

                            handler.post(new Runnable()
                            {
                                public void run()
                                {
                                    //txtVw.setText(String.valueOf(b));
                                }
                            });
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

    private void showToast(String message) {
        Toast.makeText(thisCnt, message, Toast.LENGTH_LONG).show();
    }




    public void sendData(byte[] data){
        try {
            mmOutputStream.write(data);
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
