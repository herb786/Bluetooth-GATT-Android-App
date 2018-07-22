package com.hacaller.warningapp;

import android.Manifest;
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
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 iBeacon / Android 4.3 / iOS 7.0
 Bluetooth Low Energy NRF51822
 Input 2V-3.3V
 Frequency 2.4 GHz
 Battery CR2032 3V / Life 20 Month
 Range 60-80m
 Power On: LED blinks three times / Power Off: LED blinks once

 00001800-0000-1000-8000-00805f9b34fb
 mobile->ibeacon | 00002a01-0000-1000-8000-00805f9b34fb | read               | Length 20
 mobile->ibeacon | 00002a04-0000-1000-8000-00805f9b34fb | read               | Length 20
 ibeacon->mobile | 00002a00-0000-1000-8000-00805f9b34fb | write              | Length 20

 00001801-0000-1000-8000-00805f9b34fb

 00001803-494c-4f47-4943-544543480000
 ibeacon->mobile | 00001804-494c-4f47-4943-544543480000 | notify             | Length 20
 ibeacon->mobile | 00001805-494c-4f47-4943-544543480000 | write              | Length 20
 */

public class ExploreBluetoothActivity extends AppCompatActivity {

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothGatt mBluetoothGatt;
    private List<BluetoothGattService> mGattServices;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_explore_bluetooth);

        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "BLE NOT SUPPORTED", Toast.LENGTH_SHORT).show();
            finish();
        }

        boolean hasCoarseLocation =
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED;

        if (hasCoarseLocation){
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 998);
        } else {
            initBluetoothManager();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 999 && resultCode == RESULT_OK) {
            initBluetoothAdapter();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 998 && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            initBluetoothManager();
        }
    }

    private void initBluetoothAdapter(){
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();

        if (pairedDevices.size() > 0) {
            // There are paired devices. Get the name and address of each paired device.
            for (BluetoothDevice device : pairedDevices) {
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
                Log.d("AppBluetoothDevice", deviceName);
                Log.d("AppBluetoothDevice", deviceHardwareAddress);
            }
        }

        mBluetoothAdapter.startDiscovery();
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, filter);
    }

    private void initBluetoothManager(){
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        //mBluetoothAdapter = bluetoothManager.getAdapter();

        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, 999);
        } else {
            initBluetoothAdapter();
        }



    }


    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Discovery has found a device. Get the BluetoothDevice
                // object and its info from the Intent.
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // Received Signal Strength Indication
                short rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, (short) 0);
                UUID extraUuid = intent.getParcelableExtra(BluetoothDevice.EXTRA_UUID);
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
                Log.d("AppBluetoothDevice", deviceName);
                Log.d("AppBluetoothDevice", deviceHardwareAddress);
                Log.d("AppBluetoothDevice", String.valueOf(rssi));
                //Log.d("AppBluetoothDevice", extraUuid.toString());
                mBluetoothGatt = device.connectGatt(ExploreBluetoothActivity.this, true, new MyBluetoothGattCallback());
                unregisterReceiver(mReceiver);
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mReceiver != null)
            unregisterReceiver(mReceiver);
        if (mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }



    private class MyBluetoothGattCallback extends BluetoothGattCallback {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                mBluetoothGatt.discoverServices();
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            mGattServices = gatt.getServices();
            for (BluetoothGattService gattService : mGattServices) {
                Log.d("AppBluetoothGatt", "ServiceUUID::"+gattService.getUuid().toString());
                Log.d("AppBluetoothGatt", String.format("InstanceID::%d",gattService.getInstanceId()));
                List<BluetoothGattCharacteristic> gattChars = gattService.getCharacteristics();
                for (BluetoothGattCharacteristic gattChar : gattChars) {
                    Log.d("AppBluetoothGatt", "--->CharUUID::"+gattChar.getUuid().toString());
                    if (gattChar.getProperties() == BluetoothGattCharacteristic.PROPERTY_WRITE){
                        Log.d("AppBluetoothGatt", "Write 0x08 Property");
                    } else if (gattChar.getProperties() == BluetoothGattCharacteristic.PROPERTY_NOTIFY){
                        Log.d("AppBluetoothGatt", "Notify 0x10 Property");
                    } else if (gattChar.getProperties() == BluetoothGattCharacteristic.PROPERTY_READ){
                        Log.d("AppBluetoothGatt", "Read 0x02 Property");
                    } else if (gattChar.getProperties() == 0x0A){
                        Log.d("AppBluetoothGatt", "Read 0x02 /Write 0x08 Property");
                    } else if (gattChar.getProperties() == 0x0C){
                        Log.d("AppBluetoothGatt", "Write 0x04 /Write 0x08 Property");
                    } else {
                        Log.d("AppBluetoothGatt", String.format("Unknown Property:%d",gattChar.getProperties()));
                    }
                    Log.d("AppBluetoothGatt", String.format("CharPermission::%d",gattChar.getPermissions()));
                    Log.d("AppBluetoothGatt", "CharValue::"+ Arrays.toString(gattChar.getValue()));
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
