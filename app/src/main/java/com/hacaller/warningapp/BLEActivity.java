package com.hacaller.warningapp;

import android.Manifest;
import android.app.IntentService;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public class BLEActivity extends AppCompatActivity {

    private BluetoothAdapter mBluetoothAdapter;
    BluetoothAdapter.LeScanCallback mLeScanCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ble);

        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "BLE NOT SUPPORTED", Toast.LENGTH_SHORT).show();
            finish();
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 998);
        } else {
            initBleManager();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 999 && resultCode == RESULT_OK) {
            BluetoothAdapter.LeScanCallback mLeScanCallback = new MyLeScanCallback();
            mBluetoothAdapter.startLeScan(mLeScanCallback);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 998 && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            initBleManager();
        }
    }


    private void initBleManager(){
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, 999);
        } else {
            mLeScanCallback = new MyLeScanCallback();
            mBluetoothAdapter.startLeScan(mLeScanCallback);
        }
    }

    private class MyLeScanCallback implements BluetoothAdapter.LeScanCallback {
        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            Log.d("LEBluetoothDevice", "onLeScan");
            Log.d("LEBluetoothDevice", device.getName());
            Log.d("LEBluetoothDevice", device.getAddress());
            Log.d("LEBRSSI", String.valueOf(rssi));
            StringBuilder builder = new StringBuilder();
            StringBuilder proxUUID = new StringBuilder();
            int i=0;
            for (byte b : scanRecord) {
                if (i>8 && i<25){
                    proxUUID.append(String.format("%02X", b));
                }
                builder.append(String.format("%02X ", b));
                i++;
            }
            proxUUID.insert(20,"-");
            proxUUID.insert(16,"-");
            proxUUID.insert(12,"-");
            proxUUID.insert(8,"-");
            Log.d("LEBDATA", builder.toString());
            Log.d("LEBPROXUUID", proxUUID.toString());
            //mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
    }


}
