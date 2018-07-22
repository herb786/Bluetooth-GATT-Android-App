package com.hacaller.warningapp;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.UUID;

/**
 * Created by Herbert Caller on 15/07/2018.
 */
public class BluetoothThread extends Thread {

    private final String TAG = getClass().getSimpleName();

    private final BluetoothSocket mSocket;
    private final BluetoothAdapter mAdapter;


    public BluetoothThread(BluetoothAdapter adapter, BluetoothDevice device, UUID mUUID) {
        BluetoothSocket tmp = null;
        mAdapter = adapter;
        try {
            // Get a BluetoothSocket to connect with the given BluetoothDevice.
            // MY_UUID is the app's UUID string, also used in the server code.
            tmp = device.createRfcommSocketToServiceRecord(mUUID);
        } catch (IOException e) {
            Log.e(TAG, "Socket's create() method failed", e);
        }
        mSocket = tmp;
    }

    public void run() {
        // Cancel discovery because it otherwise slows down the connection.
        mAdapter.cancelDiscovery();

        try {
            // Connect to the remote device through the socket. This call blocks
            // until it succeeds or throws an exception.
            mSocket.connect();
            InputStream in  = mSocket.getInputStream();
            OutputStream out = mSocket.getOutputStream();
            String message = "WarningBeacon";
            byte[] mData = message.getBytes();
            //out.write(mData);
            byte[] mBuffer = new byte[1024];
            int numBytes;
            while (true) {
                numBytes = in.read(mBuffer);
                Log.d(TAG, Arrays.toString(mBuffer));
            }
        } catch (IOException connectException) {
            // Unable to connect; close the socket and return.
            try {
                mSocket.close();
            } catch (IOException closeException) {
                Log.e(TAG, "Could not close the client socket", closeException);
            }
            return;
        }
        // The connection attempt succeeded. Perform work associated with
        // the connection in a separate thread.
        //manageMyConnectedSocket(mmSocket);
    }

    // Closes the client socket and causes the thread to finish.
    public void cancel() {
        try {
            mSocket.close();
        } catch (IOException e) {
            Log.e(TAG, "Could not close the client socket", e);
        }
    }
}
