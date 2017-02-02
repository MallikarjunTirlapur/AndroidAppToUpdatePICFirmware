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

import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by Mallikarjun Tirlapur on 30-06-2016.
 */

/**
 *  SystemStates class inherits the properties of class ReadHexFile.
 *  The class handles various state of the system.
 *  Based on the command received from the PIC, Class responds with a command.
 */

public class SystemStates extends ReadHexFile {
    public static final int SYS_CONFIG_CMND = 0x01;
    public static final int SYS_SEND_FIRMWARE_HEADER = 0x02;
    public static final int SYS_FIRMWARE_HEADER_POS_ACK = 0x03;
    public static final int SYS_FIRMWARE_HEADER_NEG_ACK = 0x04;
    public static final int SYS_SEND_CHUNK = 0x05;
    public static final int SYS_ADDRESS_RANGE_RCV = 0x06;
    public static final int SYS_ADDRESS_RANGE_RQT = 0x07;

    private byte[] fileData;
    private int binLength;
    private int fileStartIndex, fileEndIndex, fileStartAddress;
    OutputStream blOutStream;


    boolean flagRcvCntrlHdr;
    int contr;
    byte[] data;
    int NVM_PAGE_SIZE;


    /**
     * empty constructor
     */
    SystemStates(){
        flagRcvCntrlHdr = false;
        contr = 0;
        data = new byte[15];
    }
    /**
     * Function executes different states of the system
     * @param currentState state to be executed
     */
    public void runStateMachine(int currentState){

        if((flagRcvCntrlHdr == true) && (contr < 15)){
            data[contr++] = (byte) currentState;
            if(contr >= 9){
                NVM_PAGE_SIZE = data[0];
                setStartEndPICFlashAddress(bytesToLong(data, 5), bytesToLong(data, 1));
                craeteBinData();
                contr = 0;
                flagRcvCntrlHdr = false;
                sendData(SYS_CONFIG_CMND);
            }
            return;
        }

        try {
            switch(currentState){
                case SYS_ADDRESS_RANGE_RCV:
                    flagRcvCntrlHdr = true;
                    break;

                case SYS_SEND_FIRMWARE_HEADER:
                    resetBinaryFileData();
                    sendHeader();
                    break;
                case SYS_FIRMWARE_HEADER_POS_ACK:
                    if(fileStartIndex <= fileEndIndex){
                        sendBinary();
                    }
                    break;
                case SYS_FIRMWARE_HEADER_NEG_ACK:
   //                 sendHeader();
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

    private long bytesToLong(byte[] arr, int offst){
        long var = 0;
        var += arr[offst];
        var = var << 24;
        var += arr[offst + 1];
        var = var << 16;
        var += arr[offst + 2];
        var = var << 8;
        var += arr[offst + 3];

        return var;
    }

    /**
     * Function initializes the variables filestartindex, fileEndIndex, binLength, fileData, fileStartAddress
     */
    public void resetBinaryFileData() {
        fileStartIndex = getFileStartIndex();
        fileEndIndex = getFileEndIndex();
        binLength = getTotalNumberOfPackets();
        fileData = getFileData();
        fileStartAddress = (int)getStartAddress();
    }

    /**
     * Function sends out the firmware header o the PIC
     */
    public void sendHeader() throws IOException{
        int strtAdd;
        sendData(binLength);

        strtAdd = (fileStartAddress & 0x00ff);
        sendData(strtAdd);
        strtAdd = ((fileStartAddress & 0xff00) >> 8);
        sendData(strtAdd);
    }

    /**
     * Function sends the binary data out in a packet, each of size 64 bytes
     */
    public void sendBinary() throws IOException{
        for (int i = 0; ((i < NVM_PAGE_SIZE) && (fileStartIndex < fileEndIndex)); i++) {
            sendData(fileData[fileStartIndex++]);
        }
    }

    /**
     * Function initializes an instance of OutputStream
     * @param mmOutStream instance of OutputStream
     */
    public void setOutputStream (OutputStream mmOutStream){
        this.blOutStream = mmOutStream;
    }

    /**
     * Function sends out the data byte
     * @param data data to be sent
     */
    public void sendData(int data){
        try {
            blOutStream.write(data);
        }catch (IOException ex){
            ex.printStackTrace();
        }
    }

}
