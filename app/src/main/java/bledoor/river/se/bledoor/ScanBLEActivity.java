package bledoor.river.se.bledoor;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.Inflater;


public class ScanBLEActivity extends Activity {

    final String LOGTAG = "ScanBLEActivity";
    private BluetoothAdapter bluetoothAdapter = null;
    private Handler handler;
    private static final long SCAN_PERIOD = 10000;
    private boolean activeBleScanning;
    private Button scanBleButton = null;
    private ListView bleListView;
    private static final int REQUEST_ENABLE_BT = 1;
    private ArrayList<BLEDevices> bleDeviceses;
    MyListAdapter myListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(LOGTAG, "onCreate");
        setContentView(R.layout.activity_scan_ble);
        scanBleButton = (Button)findViewById(R.id.start_scan_ble_button);
        bleListView = (ListView)findViewById(R.id.ble_device_list);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(LOGTAG, "onResume");
        //Check if ble is availble
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
	    return;
        }
        // Initializes Bluetooth adapter.
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
        // Ensures Bluetooth is available on the device and it is enabled. If not,
        // displays a dialog requesting user permission to enable Bluetooth.
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            Log.d(LOGTAG, "Enablar bluetooth");
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
        bleDeviceses = new ArrayList<BLEDevices>();
        myListAdapter = new MyListAdapter(bleDeviceses);
        bleListView.setAdapter(myListAdapter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(LOGTAG,"onPause");
        scanLeDevice(false);

        bleListView.setAdapter(null);
        bleDeviceses.clear();
        bleDeviceses = null;

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.scan_ble, menu);
        return false;//true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void startScanning(View view){
        Log.d(LOGTAG,"startScanning");
        scanLeDevice(true);
    }
    public void stopScanning(View view){
        Log.d(LOGTAG,"startScanning");
        scanLeDevice(false);
    }


    /**
     * Start the scanning of BLE devices
     * */
    private void scanLeDevice(final boolean enable) {
        Log.d(LOGTAG,"scanLeDevice "+enable);
        if (enable) {
            Toast.makeText(this,"Start scanning for devices",Toast.LENGTH_SHORT).show();
            /*
            // Stops scanning after a pre-defined scan period.
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    activeBleScanning = false;
                    bluetoothAdapter.stopLeScan(mLeScanCallback);
                }
            }, SCAN_PERIOD);
            */
            activeBleScanning = true;
            bluetoothAdapter.startLeScan(mLeScanCallback);
            scanBleButton.setEnabled(false);
        } else {
            activeBleScanning = false;
            bluetoothAdapter.stopLeScan(mLeScanCallback);
            scanBleButton.setEnabled(true);
        }

    }

    // Device scan callback.
    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(final BluetoothDevice device, final int rssi, final byte[] scanRecord) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            String deviceStr = "Found a device named:"+device.getName()+
                                    "\ntoString:"+device.toString()+
                                    "\nBluetoothClass:"+device.getBluetoothClass().toString()+
                                    "\nAddress:"+device.getAddress()+
                                    "\nrssi:"+rssi;
                            //Toast.makeText(getBaseContext(),deviceStr,Toast.LENGTH_SHORT).show();

                            //Create a new BLE Device object and update information in the Data source
                            BLEDevices tmp = new BLEDevices(device.getAddress(),rssi,device,scanRecord);
                            Log.i(LOGTAG,"checking contains");
                            if(bleDeviceses.contains(tmp)){
                                //check whether we should update the rssi value
                                BLEDevices device = bleDeviceses.get(bleDeviceses.indexOf(tmp));
                                if(device.rssi != rssi) {
                                    device.rssi = rssi;
                                }
                            }else{
                                bleDeviceses.add(tmp);
                            }
                            myListAdapter.notifyDataSetChanged();

                        }
                    });
                }
            };

    //Adapter for displaying BLE devices
    class MyListAdapter extends BaseAdapter {
        private ArrayList<BLEDevices> bleDeviceses;
        private LayoutInflater inflater;

        MyListAdapter(ArrayList<BLEDevices> bleDeviceses){
            this.bleDeviceses = bleDeviceses;
            inflater = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return bleDeviceses == null ? 0 : bleDeviceses.size();
        }

        @Override
        public BLEDevices getItem(int position) {
            return bleDeviceses.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            //getting a listview object
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.ble_list_item, null);
            } else {
                //bleDevice = (BLEDevices) convertView.getTag();
            }

            //Getting the field in the listview obj
            TextView address = (TextView) convertView.findViewById(R.id.ble_item_address);
            TextView rssi = (TextView) convertView.findViewById(R.id.ble_item_rssi_level);

            //get the device_info from "datasource"
            BLEDevices device = bleDeviceses.get(position);

            //setting the device_info to the listview obj
            final String deviceName = device.address;
            if (deviceName != null && deviceName.length() > 0)
                address.setText(deviceName);
            else
                address.setText(R.string.unknown_device);

            rssi.setText(String.valueOf(device.rssi));

            return convertView;

        }
    }

    //Container class describing the BLE device
    class BLEDevices{
        //address of the bluetooth LE device
        public String address;

        //radio strength value
        public int rssi;

        //BluetoothDevice information from the BLE device
        public BluetoothDevice bluetoothDevice;

        //scanrecord from the BLE device
        byte[] scanRecord;

        BLEDevices(String address,int rssi,BluetoothDevice bluetoothDevice,byte[] scanRecord){
            this.address = address;
            this.rssi = rssi;
            this.bluetoothDevice = bluetoothDevice;
            this.scanRecord = scanRecord;
        }

        @Override
        public boolean equals(Object object){
            boolean retVal = false;
            if ( this == object ) retVal = true;
            if ( !(object instanceof BLEDevices) ) retVal = false;
            //Fixme
            retVal = address.equals(((BLEDevices) object).address);
            Log.i(LOGTAG,"equals"+retVal);
            return retVal;
        }
        @Override
        public int hashCode(){
            return address.hashCode();
        }

    }

}
