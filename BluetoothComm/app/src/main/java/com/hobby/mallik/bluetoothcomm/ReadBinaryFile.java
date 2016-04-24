package com.hobby.mallik.bluetoothcomm;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;

/**
 * Created by Mallik on 20-06-2016.
 */
public class ReadBinaryFile {

    private byte[] fileData;
    private int binLength;
    private int fileStartIndex, fileEndIndex;

    public byte[] readBinary() {
        File file = new File("/storage/emulated/0/crtBin.bin");
        fileData = new byte[(int) file.length()];
        boolean startIndexState = false;
        fileEndIndex = 0;
        int cnt = 0;
        int index;
        try {
            DataInputStream dis = new DataInputStream(new FileInputStream(file));
            dis.readFully(fileData);
            dis.close();

            fileStartIndex = 0;
            fileEndIndex = (0x800 - 0x400);
            binLength = ((fileEndIndex - fileStartIndex) / 64); //must be optimized
        } catch (Exception ex) {
            System.console();
        }
        return fileData;
    }

    public int getBinLength(){
        return binLength;
    }

    public int getFileStartIndex(){
        return fileStartIndex;
    }

    public int getFileEndIndex(){
        return fileEndIndex;
    }
}
