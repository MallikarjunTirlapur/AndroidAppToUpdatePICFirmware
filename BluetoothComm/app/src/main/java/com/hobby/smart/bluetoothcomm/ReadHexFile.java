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
import com.hobby.hex.to.bin.LineParser;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Set;

/**
 * Created by Mallikarjun Tirlapur on 20-06-2016.
 */

/**
 *  ReadHexFile class.
 *  The class reads the binary file and devides the binary data into packets.
 *  holds the start address of the embedded application code.
 */
public class ReadHexFile {
    long  picFlashStartAddress, appStrtAdd;
    long  picFlashEndAddress, appEndAdd;
    byte[] binDataBytes;
    LinkedHashMap<Long, String> dataAddressHashMap;
    /**
     * Constructor reads binary file and store data into an array of bytes
     * @param path carries the path to the binary file on the storage of the android device.
     */
    public void readHex(String path) throws IOException {
        String line;
        LineParser lnPrsr = new LineParser();
        LinkedHashMap<Integer, String> linTbl = new LinkedHashMap<Integer, String>();

        BufferedReader readFile = new BufferedReader(new FileReader(path));
        for (int i = 0; (line = readFile.readLine()) != null; i++) {
            linTbl.put(i, line);
        }

        dataAddressHashMap = lnPrsr.processRecord(linTbl);
   //     craeteBinData();
    }

    private byte[] getBytes(String data) {
        byte[] bytes = new byte[data.length() / 2];
        for (int i = 0; i < data.length(); i += 2) {
            bytes[i / 2] = (byte) (Integer.parseInt(data.substring(i, i + 2), 16) & 0xff);
        }
        return bytes;
    }

    /**
     * Function sets start and end address of the embedded application
     * @param strt start address.
     * @param end end address.
     */
    public void setStartEndPICFlashAddress(long strt, long end){
        picFlashStartAddress = strt;
        picFlashEndAddress = end;
    }

    /**
     * Function returns the start index of the embedded application code in the binary data array
     * @return start index and is always 0.
     */
    public int getFileStartIndex(){
        return 0;
    }

    /**
     * Function returns the end index of the embedded application code in the binary data array
     * @return integer.
     */
    public int getFileEndIndex(){
        return binDataBytes.length;
    }

    /**
     * Function returns the start address of the embedded application code
     * @return start addess.
     */
    public long getStartAddress(){
        return appStrtAdd;
    }


    /**
     * Function returns the total number of packets formed out of the binary data
     * @return integer.
     */
    public int getTotalNumberOfPackets(){

        return (int)(binDataBytes.length / 64);
    }

    public void craeteBinData(int NVM_PAGE_SIZE){
        Set<Long> keys = dataAddressHashMap.keySet();
        appStrtAdd = Collections.min(keys);

        long buffSize = calcBufferSize(keys);
        binDataBytes = new byte[(int)((buffSize % NVM_PAGE_SIZE == 0) ? buffSize : (buffSize + (NVM_PAGE_SIZE - (buffSize % NVM_PAGE_SIZE))))];
        Arrays.fill(binDataBytes, (byte) 0xff);

        for (long recAddrs : keys) {
            if((recAddrs <= picFlashEndAddress) && (recAddrs >= picFlashStartAddress)){
                byte[] data = getBytes(dataAddressHashMap.get(recAddrs));
                System.arraycopy(data, 0, binDataBytes, (int) (recAddrs - (Long) appStrtAdd), data.length);
            }
        }
    }

    private long calcBufferSize(Set<Long> keys){
        Object[] arr = keys.toArray();
        for (long recAddrs : keys) {
            if((recAddrs <= picFlashEndAddress) && (recAddrs >= picFlashStartAddress)){
                appEndAdd = recAddrs;
            }
        }
        return ((Long) appEndAdd - (Long) appStrtAdd
                + (dataAddressHashMap.get(appEndAdd).length() / 2));
    }


    /**
     * Function returns the binary data array
     * @return array.
     */
    public byte[] getFileData(){
        return binDataBytes;
    }
}
