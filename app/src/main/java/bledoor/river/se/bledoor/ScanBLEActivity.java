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
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
/**
 * This Activity can scan device and show them in a select able list,
 * user can select a device to save it.
 * */
public class ScanBLEActivity extends Activity {

    public static final String BLE_ADRESS = "BLE_ADRESS";
    public static final String BLE_NAME = "BLE_NAME";
    final String LOGTAG = "ScanBLEActivity";
    private BluetoothAdapter bluetoothAdapter = null;
    private Handler handler;
    private static final long SCAN_PERIOD = 10000;
    private boolean activeBleScanning;
    private Button scanBleButton = null;
    private ListView bleListView;
    private static final int REQUEST_ENABLE_BT = 1;
    private ArrayList<BLEDeviceInfo> bleDeviceses;
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
        bleDeviceses = new ArrayList<BLEDeviceInfo>();
        myListAdapter = new MyListAdapter(bleDeviceses);
        bleListView.setAdapter(myListAdapter);

        bleListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d(LOGTAG,"onItemClick pos:"+position+ " id:"+id);

                //disable scanning
                scanLeDevice(false);

                //Get info from the BLE device
                BLEDeviceInfo bleDeviceInfo = bleDeviceses.get(position);
                String address = bleDeviceInfo.address;
                String name = bleDeviceInfo.bluetoothDevice.getName();

                //send the info in a Intent to a start a activity
                //fixme: make this a application local intent!
                Intent startControlActivity = new Intent(getApplicationContext(),DeviceControlActivity.class);
                startControlActivity.putExtra(BLE_ADRESS, address);
                startControlActivity.putExtra(BLE_NAME, name);
                startActivity(startControlActivity);
            }
        });
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
     * Start/stop the scanning of BLE devices
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
                            Log.d(LOGTAG,deviceStr);
                            updateAdapter(device, rssi, scanRecord);
                        }
                    });
                }
            };

    private void updateAdapter(final BluetoothDevice device, final int rssi, final byte[] scanRecord){
        Log.d(LOGTAG,"updateAdapter BluetoothDevice");
        if(device==null) {
            Log.d(LOGTAG,"No information about BluetoothDevice, can't update adapter");
            return;
        }
        //Create a new BLE Device object and update information in the Data source
        BLEDeviceInfo tmp = new BLEDeviceInfo(device.getAddress(),rssi,device,scanRecord);
        Log.i(LOGTAG,"checking contains");
        if(bleDeviceses.contains(tmp)){
            //check whether we should update the rssi value
            BLEDeviceInfo bleDeviceInfo = bleDeviceses.get(bleDeviceses.indexOf(tmp));
            if(bleDeviceInfo.rssi != rssi) {
                bleDeviceInfo.rssi = rssi;
            }
        }else{
            bleDeviceses.add(tmp);
        }
        myListAdapter.notifyDataSetChanged();

    }

    //Adapter for displaying BLE devices
    class MyListAdapter extends BaseAdapter {
        private ArrayList<BLEDeviceInfo> bleDeviceses;
        private LayoutInflater inflater;

        MyListAdapter(ArrayList<BLEDeviceInfo> bleDeviceses){
            this.bleDeviceses = bleDeviceses;
            inflater = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return bleDeviceses == null ? 0 : bleDeviceses.size();
        }

        @Override
        public BLEDeviceInfo getItem(int position) {
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
                //bleDevice = (BLEDeviceInfo) convertView.getTag();
            }

            //Getting the field in the listview obj
            TextView address = (TextView) convertView.findViewById(R.id.ble_item_address);
            TextView rssi = (TextView) convertView.findViewById(R.id.ble_item_rssi_level);

            //get the device_info from "datasource"
            BLEDeviceInfo device = bleDeviceses.get(position);

            //setting the device_info to the listview obj
            final String deviceName = device.address;
            if (deviceName != null && deviceName.length() > 0)
                address.setText("Proofdoor\n(" +deviceName+")");
            else
                address.setText(R.string.unknown_device);

            rssi.setText(String.valueOf(device.rssi));

            return convertView;

        }
    }
}
