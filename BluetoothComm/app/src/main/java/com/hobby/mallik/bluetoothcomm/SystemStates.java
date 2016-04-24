package com.hobby.mallik.bluetoothcomm;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by Ramya_S on 30-06-2016.
 */
public class SystemStates{
    public static final int SYS_CONFIG_CMND = 0x01;
    public static final int SYS_SEND_FIRMWARE_HEADER = 0x02;
    public static final int SYS_FIRMWARE_HEADER_POS_ACK = 0x03;
    public static final int SYS_FIRMWARE_HEADER_NEG_ACK = 0x04;
    public static final int SYS_SEND_CHUNK = 0x05;

    private byte[] fileData;
    private int binLength;
    private int fileStartIndex, fileEndIndex;
    ReadBinaryFile fileMng;
    OutputStream mmOutputStream;

    SystemStates(){
        fileMng = new ReadBinaryFile();
        fileData = fileMng.readBinary();
        fileStartIndex = fileMng.getFileStartIndex();
        fileEndIndex = fileMng.getFileEndIndex();
        binLength = fileMng.getBinLength();
    }

    public void runStateMachine(int currentState){
        try {
            switch(currentState){
                case SYS_SEND_FIRMWARE_HEADER:
                    sendHeader();
                    break;
                case SYS_FIRMWARE_HEADER_POS_ACK:
                    if(fileStartIndex <= fileEndIndex){
                        sendBinary();
                    }
                    break;
                case SYS_FIRMWARE_HEADER_NEG_ACK:
                    sendHeader();
                    break;
                case SYS_SEND_CHUNK:
                    if(fileStartIndex <= fileEndIndex){
                        sendBinary();
                    }
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void sendHeader() throws IOException{

        for (int i = 0; i < 4; i++) {
            sendData(0x55);
        }
        sendData(binLength);
    }

    public void sendBinary() throws IOException{
        for (int i = 0; i < 64; i++) {
            sendData(fileData[fileStartIndex++]);
        }
    }

    public void setOutputStream (OutputStream mmOutStream){
        this.mmOutputStream = mmOutStream;
    }

    public void sendData(int data){
        try {
            mmOutputStream.write(data);
        }catch (IOException ex){
            ex.printStackTrace();
        }
    }

}
