package com.hacaller.warningapp;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.List;
import java.util.UUID;

/**
 * Created by Herbert Caller on 21/07/2018.
 */
public class BLEService extends Service {

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothGatt mBluetoothGatt;
    private List<BluetoothGattService> mGattServices;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        UUID uuid = UUID.randomUUID();
        UUID[] uuids = new UUID[]{
                UUID.fromString("be49bd0d-0869-4558-9d88-82be600fe165")
        };
        BluetoothAdapter.LeScanCallback mLeScanCallback = new MyLeScanCallback();
        mBluetoothAdapter.startLeScan(uuids, mLeScanCallback);
        Handler mHandler = new Handler();
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                //mBluetoothAdapter.stopLeScan(mLeScanCallback);
            }
        }, 50000);
    }

    private class MyLeScanCallback implements BluetoothAdapter.LeScanCallback {
        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            Log.d("LEBluetoothDevice", "onLeScan");
            Log.d("LEBluetoothDevice", device.getName());
            Log.d("LEBluetoothDevice", device.getAddress());
            mBluetoothGatt = device.connectGatt(BLEService.this, true, new MyBluetoothGattCallback());
            mBluetoothGatt.discoverServices();
        }
    }

    private class MyBluetoothGattCallback extends BluetoothGattCallback {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.i("BluetoothGattCallback", "Attempting to start service discovery:" +
                        mBluetoothGatt.discoverServices());
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            mGattServices = gatt.getServices();
            for (BluetoothGattService gattService : mGattServices) {
                List<BluetoothGattCharacteristic> gattCharacteristics = gattService.getCharacteristics();
                for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                    Log.d("BluetoothGatt", "ServiceUUID::"+gattService.getUuid().toString());
                    Log.d("BluetoothGattService", "CharacteristicUUID::"+gattCharacteristic.getUuid().toString());
                }
            }

        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            final byte[] data = characteristic.getValue();
            if (data != null && data.length > 0) {
                final StringBuilder stringBuilder = new StringBuilder(data.length);
                for(byte byteChar : data)
                    stringBuilder.append(String.format("%02X ", byteChar));
                Log.d(new String(data), stringBuilder.toString());
            }
        }
    }



}
