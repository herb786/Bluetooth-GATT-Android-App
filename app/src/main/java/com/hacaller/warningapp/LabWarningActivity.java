package com.hacaller.warningapp;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothSocket;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelUuid;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;




public class LabWarningActivity extends AppCompatActivity {

    private final String TAG = "LogLabWarning";
    private final String TAG2 = "LogLabWarning2";

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothSocket mBluetoothSocket;
    private BluetoothDevice mBluetoothDevice;
    private BluetoothGatt mBluetoothGatt;
    private BluetoothGattService mBluetoothGattService;
    private BluetoothGattCharacteristic mBluetoothGattCharacteristic;
    private final String BluetoothDeviceName = "RDL51822";
    private final String BluetoothDeviceMAC = "EA:81:41:11:64:15";
    public static final ParcelUuid Service_UUID = ParcelUuid.fromString("0000b81d-0000-1000-8000-00805f9b34fb");

    TextView txtMessage;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

        txtMessage = findViewById(R.id.txtMessage);

        Button btnReconnect = findViewById(R.id.btnReconnect);
        btnReconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                txtMessage.setText("Biohazard");
                initBluetoothAdapter();
            }
        });

        Button btnWrite = findViewById(R.id.btnWrite);
        btnWrite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = "WarningBeacon";
                UUID mUUID = UUID.fromString("00001803-494c-4f47-4943-544543480000");
                if (mBluetoothGatt != null) {
                    mBluetoothGattService = mBluetoothGatt.getService(mUUID);
                    mUUID = UUID.fromString("00001804-494c-4f47-4943-544543480000");
                    mBluetoothGattCharacteristic = mBluetoothGattService.getCharacteristic(mUUID);
                    mBluetoothGattCharacteristic.setValue(message.getBytes());
                    mBluetoothGattCharacteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
                    mBluetoothGatt.writeCharacteristic(mBluetoothGattCharacteristic);
                    mBluetoothGatt.setCharacteristicNotification(mBluetoothGattCharacteristic, true);
                    mUUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
                    BluetoothGattDescriptor descriptor = mBluetoothGattCharacteristic.getDescriptor(mUUID);
                    descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                    mBluetoothGatt.writeDescriptor(descriptor);
                    mUUID = UUID.fromString("00001805-494c-4f47-4943-544543480000");
                    mBluetoothGattCharacteristic = mBluetoothGattService.getCharacteristic(mUUID);
                    mBluetoothGattCharacteristic.setValue(message.getBytes());
                    mBluetoothGatt.writeCharacteristic(mBluetoothGattCharacteristic);
                }
            }
        });

        Button btnRead = findViewById(R.id.btnRead);
        btnRead.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UUID mUUID = UUID.fromString("00001803-494c-4f47-4943-544543480000");
                if (mBluetoothGatt != null) {
                    mBluetoothGattService = mBluetoothGatt.getService(mUUID);
                    mUUID = UUID.fromString("00001804-494c-4f47-4943-544543480000");
                    mBluetoothGattCharacteristic = mBluetoothGattService.getCharacteristic(mUUID);
                    mBluetoothGatt.readCharacteristic(mBluetoothGattCharacteristic);
                    mUUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
                    BluetoothGattDescriptor descriptor = mBluetoothGattCharacteristic.getDescriptor(mUUID);
                    mBluetoothGatt.readDescriptor(descriptor);
                }
            }
        });

        Button btnAdvertiser = findViewById(R.id.btnAdvertiser);
        btnAdvertiser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mBluetoothAdapter != null && mBluetoothAdapter.isMultipleAdvertisementSupported()) {
                    BluetoothLeAdvertiser advertiser = mBluetoothAdapter.getBluetoothLeAdvertiser();
                    AdvertiseSettings settings = new AdvertiseSettings.Builder()
                            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_POWER)
                            .setTimeout(0)
                            .build();
                    AdvertiseData advertiseData = new AdvertiseData.Builder()
                            .addServiceUuid(Service_UUID)
                            .setIncludeDeviceName(true)
                            .build();
                    advertiser.startAdvertising(settings, advertiseData, bleAdvertiserCallback);
                }
            }
        });

        Button btnScanner = findViewById(R.id.btnScanner);
        btnScanner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mBluetoothAdapter != null){
                    BluetoothLeScanner bluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
                    List<ScanFilter> scanFilters = new ArrayList<>();
                    ScanFilter scanFilter = new ScanFilter.Builder()
                            .setDeviceAddress(BluetoothDeviceMAC)
                            .build();
                    scanFilters.add(scanFilter);
                    ScanSettings settings = new ScanSettings.Builder()
                            .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
                            .build();
                    bluetoothLeScanner.startScan(scanFilters, settings, bleScanCallback);
                }
            }
        });
    }

    private ScanCallback bleScanCallback = new ScanCallback() {
        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            Log.d(TAG, result.getScanRecord().toString());
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
        }
    };

    private AdvertiseCallback bleAdvertiserCallback = new AdvertiseCallback() {
        @Override
        public void onStartSuccess(AdvertiseSettings settingsInEffect) {
            super.onStartSuccess(settingsInEffect);
        }

        @Override
        public void onStartFailure(int errorCode) {
            super.onStartFailure(errorCode);
        }
    };

    private void initBluetoothManager(){
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, 999);
        } else {
            initBluetoothAdapter();
        }

    }

    private void initBluetoothAdapter(){
        mBluetoothAdapter.startDiscovery();
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, filter);
    }


    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Discovery has found a device. Get the BluetoothDevice
                // object and its info from the Intent.
                mBluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // Received Signal Strength Indication
                short rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, (short) 0);
                if (mBluetoothDevice.getAddress().equals(BluetoothDeviceMAC)) {
                    mBluetoothDevice.createBond();
                    mBluetoothGatt = mBluetoothDevice.connectGatt(LabWarningActivity.this, true, labBluetoothGattCallback);
                    mBluetoothGatt.connect();
                }
                mBluetoothAdapter.cancelDiscovery();
            }
        }
    };

    private BluetoothGattCallback labBluetoothGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                mBluetoothGatt.discoverServices();
            }
        }
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    txtMessage.setText("Connected!!!");
                }
            });
            mBluetoothGatt = gatt;
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
            Log.d(TAG, String.valueOf(characteristic.getValue()));
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            Log.d(TAG, String.valueOf(status));
        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorRead(gatt, descriptor, status);
            Log.d(TAG, String.valueOf(status));
            Log.d(TAG, String.valueOf(descriptor.getValue()));
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorWrite(gatt, descriptor, status);
            Log.d(TAG, String.valueOf(status));
        }
    };


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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }

}
