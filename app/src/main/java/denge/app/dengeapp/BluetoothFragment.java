package denge.app.dengeapp;

import android.Manifest;
import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.companion.BluetoothLeDeviceFilter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelUuid;
import android.provider.SyncStateContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.ArraySet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

import static android.bluetooth.BluetoothAdapter.STATE_CONNECTED;

public class BluetoothFragment extends Fragment {
    BluetoothAdapter myBluetooth;
    //eşleşmiş cihazları gösteriyoruz.etraftaki cihazları atıyoruz.
    private Set<BluetoothDevice> pairedDevices;
    ArrayList<String> leDevices;
    ArrayList<BluetoothDevice> bleAllDevices;


    Button toggleButton;
    Button pairedButton;
    Button getBatteryButton;
    private boolean mScanning;
    private Handler handler;
    private String TAG="DengeApp_BLUETOOTH_FRAGMENT";
    private BluetoothLeScanner bluetoothLeScanner;
    BluetoothDevice bluetoothDevice;
    boolean leScanEnabled = false;
    ArrayAdapter adapter;
    ListView pairList;
    BluetoothGatt dengeGatt;
    BluetoothDevice myDevice;
    BluetoothDevice dengeDevice;
    private static final UUID Battery_Service_UUID = UUID.fromString("0000180F-0000-1000-8000-00805f9b34fb");
    private static final UUID Battery_Level_UUID = UUID.fromString("00002a19-0000-1000-8000-00805f9b34fb");
    private static final UUID Ivme_Servisi_UUID = UUID.fromString("0000a888-0000-1000-8000-00805f9b34fb");
    private static final UUID Ivme_Verisi_UUID = UUID.fromString("0000b888-0000-1000-8000-00805f9b34fb");

    public BluetoothGattService batteryService;
    public BluetoothGattCharacteristic batteryLevel;
    public BluetoothGattService ivmeService;
    public BluetoothGattCharacteristic ivmeDeger;

    private BluetoothAdapter.LeScanCallback leScanCallback =
            new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.i(TAG, "onLeScan: "+device.getName()+" - "+device.getAddress());
                            myDevice = device;
                            //Toast.makeText(getActivity(),device.getName()+" "+device.getAddress(),Toast.LENGTH_LONG).show();

                        }
                    });
                }
            };
    private ScanCallback scanCallback = new ScanCallback() {



        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            bluetoothDevice = result.getDevice();

            //Toast.makeText(getActivity(),bluetoothDevice.getName()+" "+bluetoothDevice.getAddress(),Toast.LENGTH_LONG).show();
            if (bluetoothDevice.getName() != null){
                if (bluetoothDevice.getName().startsWith("Denge")){
                    bluetoothDevice = result.getDevice();
                    dengeDevice = myBluetooth
                            .getRemoteDevice(bluetoothDevice.getAddress());

                   //Toast.makeText(getActivity(),dengeDevice.toString(),Toast.LENGTH_LONG).show();

                    Log.i(TAG, "onScanResult: "+"doğru "+bluetoothDevice.getName());
                    if (!leDevices.contains(bluetoothDevice.getName())){
                        leDevices.add(bluetoothDevice.getName());
                        adapter = new ArrayAdapter(getActivity(),android.R.layout.simple_list_item_1, (List) leDevices);
                        pairList.setAdapter(adapter);

                        //listede değişiklik olursa güncelle
                        adapter.notifyDataSetChanged();
                    }

                }

            }

            Log.i(TAG, "onLeScan: "+bluetoothDevice.getName()+" - "+bluetoothDevice.getAddress());
            //deviceAddress.setText(bluetoothDevice.getAddress());
            //deviceName.setText(bluetoothDevice.getName());
            //progressBar.setVisibility(View.INVISIBLE);
            /////////////
          /*  handler.postDelayed(new Runnable() {
                @Override
                public void run() {

                    if (bluetoothDevice.getName().startsWith("Denge")){
                        leDevices.add(bluetoothDevice.getName());
                        adapter = new ArrayAdapter(getActivity(),android.R.layout.simple_list_item_1, (List) leDevices);
                        pairList.setAdapter(adapter);
                        //listede değişiklik olursa güncelle
                        adapter.notifyDataSetChanged();
                    }
                    mScanning = true;
                }
            }, 10000);
            if (bluetoothDevice.getName().startsWith("Denge")){
                leDevices.add(bluetoothDevice.getName());
                adapter = new ArrayAdapter(getActivity(),android.R.layout.simple_list_item_1, (List) leDevices);
                pairList.setAdapter(adapter);
                //listede değişiklik olursa güncelle
                adapter.notifyDataSetChanged();
            }*/
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            Log.d(TAG, "Scanning Failed " + errorCode);
           // progressBar.setVisibility(View.INVISIBLE);
        }
    };

    BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if(status == BluetoothGatt.GATT_SUCCESS) {
                byte[] gattCharacteristic = characteristic.getValue();
                String ivmeString = characteristic.getStringValue(0);
                Log.d(TAG,"İvme Geldi"+ivmeString);

                Log.d(TAG,"Pil Geldi"+gattCharacteristic[0]);

                Toast.makeText(getActivity(),gattCharacteristic[0],Toast.LENGTH_LONG).show();
            }

        }

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == STATE_CONNECTED){
                Log.d(TAG,"Bağlantı tamamlandı."+ gatt.getDevice().getName() );
                bluetoothLeScanner.stopScan( scanCallback);
                gatt.discoverServices();


//                batteryService = gatt.getService(Battery_Service_UUID);
//                Log.d(TAG,"Batt service uuid: "+ batteryService.getUuid());
//
//                if(batteryService == null) {
//                    Log.d(TAG, "Battery service not found!");
//                    return;
//                }
//                batteryLevel = batteryService.getCharacteristic(Battery_Level_UUID);
//                Log.d(TAG, "battery level: "+batteryLevel);
//                if(batteryLevel == null) {
//                    Log.d(TAG, "Battery level not found!");
//                    return;
//                }

                //gatt.discoverServices();
                //gatt.readCharacteristic(batteryLevel);
                //Log.d(TAG,"Pil tamamlandı.");



            } else {
                Log.d(TAG, "failed");
            }

        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            Log.i(TAG, "onServicesDiscovered: " + gatt.getServices().size());
        }
    };


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View fragmentView =  inflater.inflate(R.layout.fragment_bluetooth,container,false);
        //fragment üzerinden çağırmak gerekiyor.
        //myBluetooth = BluetoothAdapter.getDefaultAdapter();
        // Initializes Bluetooth adapter.
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getActivity().getSystemService(Context.BLUETOOTH_SERVICE);
        myBluetooth = bluetoothManager.getAdapter();
        toggleButton = fragmentView.findViewById(R.id.toggleButton);
        pairedButton = fragmentView.findViewById(R.id.pairedButton);
        pairList = fragmentView.findViewById(R.id.pairedList);
        getBatteryButton = fragmentView.findViewById(R.id.getBatteryButton);
        bluetoothLeScanner = myBluetooth.getBluetoothLeScanner();
        leDevices = new ArrayList<>();
        toggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toogleBluetooth();
            }
        });
        pairedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //listDevice();
                //discoverDevice();
                //discoverBLEDevice();
                //scanLeDevice(true);
                if (leScanEnabled){
                    //true ise kapatıyoz
                }else {
                    //false ise açıyoruz.
                    //myBluetooth.startLeScan(leScanCallback);
                   // bluetoothLeScanner.startScan( scanCallback);
                    if (ContextCompat.checkSelfPermission(
                            getActivity(),Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                        ActivityCompat.requestPermissions(
                                getActivity(), // Activity
                                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                1);
                    }else {
                        bluetoothLeScanner.startScan( scanCallback);


                     /*   if (mScanning){
                            //eğer mScan yani tarama anı 10 saniye geçerse durdur
                            bluetoothLeScanner.stopScan(scanCallback);
                        }*/

                    }

                }

            }
        });
        pairList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                // gatt = bluetoothDevice.connectGatt(getContext(),true,gattCallback);
                dengeGatt = dengeDevice.connectGatt(getContext(),false, gattCallback);



                //Toast.makeText(getContext(),"Connection request"+dengeDevice.getAddress(),Toast.LENGTH_LONG).show();
                Log.i(TAG, "Bağlanılan: " + dengeGatt.getDevice().getAddress() );
                view.setEnabled(false);
                return true;
            }
        });


        getBatteryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(),"Pil Butonu",Toast.LENGTH_LONG).show();
                 batteryService = dengeGatt.getService(Battery_Service_UUID);
                 ivmeService = dengeGatt.getService(Ivme_Servisi_UUID);
                if(batteryService == null && ivmeService == null) {
                    Log.d(TAG, "Battery service not found!");
                    return;
                }
                batteryLevel = batteryService.getCharacteristic(Battery_Level_UUID);
                ivmeDeger= ivmeService.getCharacteristic(Ivme_Verisi_UUID);

                if(batteryLevel == null) {
                    Log.d(TAG, "Battery level not found!");
                    return;
                }
                //dengeGatt.readCharacteristic(batteryLevel);
                dengeGatt.readCharacteristic(ivmeDeger);

                Toast.makeText(getContext(),"Pil Butonu",Toast.LENGTH_LONG).show();

            }
        });


        return fragmentView;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (grantResults.length>0){
            if (requestCode == 1){
                if (ContextCompat.checkSelfPermission(getActivity(),Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                    bluetoothLeScanner.startScan( scanCallback);

                  /*  final ArrayAdapter adapter = new ArrayAdapter(getActivity(),android.R.layout.simple_list_item_1, (List) leDevices);
                    pairList.setAdapter(adapter);
                    //listede değişiklik olursa güncelle
                    adapter.notifyDataSetChanged();
                    if (mScanning){
                        //eğer mScan yani tarama anı 10 saniye geçerse durdur
                        bluetoothLeScanner.stopScan(scanCallback);
                    }*/

                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    /*public void pairedButton(View view){
            listDevice();
        }
        public void toogleButton(View view){
            toogleBluetooth();
        }*/
    private void toogleBluetooth() {
        //eğer bluetooth adaptörüm yoksa
        if (myBluetooth == null){
            Toast.makeText(getActivity(),"You Have No Bluetooth Device",Toast.LENGTH_LONG).show();
        }
        //eğer bluettoth adaptörüm açık değilse açtırcaz
        if (!myBluetooth.isEnabled()){
            Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(enableBTIntent);
            toggleButton.setText("BT DEACTİVE");

        }
        //eğer bluetooth aktifse kapatıyoruz.
        if (myBluetooth.isEnabled()){
            myBluetooth.disable();
            toggleButton.setText("BT ACTİVE");


        }
    }

    private void listDevice() {
        //eşleşmiş cihazları listenin içine koyduk 1 sefer göstercek setin özelliği küme gibi :)
        pairedDevices = myBluetooth.getBondedDevices();
        //listview e eklicez
        ArrayList list = new ArrayList();
        //eğer eşleşmiş cihaz varsa bulduğu eşleşmiş cihazları listeye eklicez.
        if (pairedDevices.size() >0){
            for (BluetoothDevice bt : pairedDevices){
                list.add(bt.getName() +"\n"+bt.getAddress());
                //list.add(bt.getAddress());
                //Toast.makeText(getApplicationContext(),bt.getName().toString(),Toast.LENGTH_SHORT).show();

            }

        }
        else{
            Toast.makeText(getActivity(),"No Paired Devices",Toast.LENGTH_LONG).show();

        }
        final ArrayAdapter adapter = new ArrayAdapter(getActivity(),android.R.layout.simple_list_item_1,list);
        pairList.setAdapter(adapter);
        //listede değişiklik olursa güncelle
        adapter.notifyDataSetChanged();
    }


/*    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                //Toast.makeText(getContext(),"Discover Başladı ",Toast.LENGTH_LONG).show();
                Log.i(TAG, "onReceive: " + action);
                //discovery starts, we can show progress dialog or perform other tasks
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                //BluetoothDevice device = (BluetoothDevice) intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                //Toast.makeText(getContext(),"Discover Bitti ",Toast.LENGTH_LONG).show();

                //System.out.println("action found"+action);
                Log.i(TAG, "onReceive: "+ action);
                //discovery finishes, dismis progress dialog
            } else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                Log.d(TAG, "onReceive: " + action);
                //bluetooth device found
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                Toast.makeText(getContext(),"Found device " + device.getName(),Toast.LENGTH_LONG).show();
                Log.i(TAG, "onReceive: " + device.getName() + " Mac : " + device.getAddress());
            }
        }

    };*/

    private void discoverBLEDevice(){
/**
 * Activity for scanning and displaying available BLE devices.
 */


    }


/*
    private void startScanning(final boolean enable) {
        bluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
        Handler mHandler = new Handler();
        if (enable) {
            //filter for battery service.
            List<ScanFilter> scanFilters = new ArrayList<>();
            //default setting.
            final ScanSettings settings = new ScanSettings.Builder().build();
            ScanFilter scanFilter = new ScanFilter.Builder().setServiceUuid(ParcelUuid.fromString(SampleGattAttributes.UUID_BATTERY_SERVICE)).build();
            scanFilters.add(scanFilter);
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    progressBar.setVisibility(View.INVISIBLE);
                    bluetoothLeScanner.stopScan(scanCallback);
                }
            }, 10000);
            mScanning = true;
            myBluetooth.startScan(scanFilters, settings, scanCallback);
        } else {
            mScanning = false;
            bluetoothLeScanner.stopScan(scanCallback);
        }
    }
*/
//    private ScanCallback scanCallback = new ScanCallback() {
//        @Override
//        public void onScanResult(int callbackType, ScanResult result) {
//            super.onScanResult(callbackType, result);
//           BluetoothDevice bluetoothDevice = result.getDevice();
//            //deviceAddress.setText(bluetoothDevice.getAddress());
//            //deviceName.setText(bluetoothDevice.getName());
//            //progressBar.setVisibility(View.INVISIBLE);
//            Toast.makeText(getContext(),"My Device"+bluetoothDevice.getName(),Toast.LENGTH_LONG).show();
//        }
//        @Override
//        public void onBatchScanResults(List<ScanResult> results) {
//            super.onBatchScanResults(results);
//        }
//        @Override
//        public void onScanFailed(int errorCode) {
//            super.onScanFailed(errorCode);
//            Log.d(TAG, "Scanning Failed " + errorCode);
//            //progressBar.setVisibility(View.INVISIBLE);
//        }
//    };



/*    private void discoverDevice(){

        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);

        getActivity().registerReceiver(mReceiver, filter);
        myBluetooth.startDiscovery();
    }*/

    @Override
    public void onDestroy() {
        //getActivity().unregisterReceiver(mReceiver);

        super.onDestroy();
    }
}
