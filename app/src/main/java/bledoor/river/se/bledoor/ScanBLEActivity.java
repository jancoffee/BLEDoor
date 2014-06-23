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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;


public class ScanBLEActivity extends Activity {

    final String LOGTAG = "ScanBLEActivity";
    private BluetoothAdapter bluetoothAdapter = null;
    private Handler handler;
    private static final long SCAN_PERIOD = 10000;
    private boolean activeBleScanning;
    private Button scanBleButton = null;
    private static final int REQUEST_ENABLE_BT = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(LOGTAG,"onCreate");
        setContentView(R.layout.activity_scan_ble);
        scanBleButton = (Button)findViewById(R.id.start_scan_ble_button);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(LOGTAG, "onResume");
        //Check if ble is availble
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }
        // Initializes Bluetooth adapter.
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
        // Ensures Bluetooth is available on the device and it is enabled. If not,
        // displays a dialog requesting user permission to enable Bluetooth.
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(LOGTAG,"onPause");
        scanLeDevice(false);
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
                public void onLeScan(final BluetoothDevice device, final int rssi, byte[] scanRecord) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            String deviceStr = "Found a device named:"+device.getName()+
                                    "\ntoString:"+device.toString()+
                                    "\nBluetoothClass:"+device.getBluetoothClass().toString()+
                                    "\nAddress:"+device.getAddress()+
                                    "\nrssi:"+rssi;
                            Toast.makeText(getBaseContext(),deviceStr,Toast.LENGTH_SHORT).show();
                            //mLeDeviceListAdapter.addDevice(device);
                            //mLeDeviceListAdapter.notifyDataSetChanged();
                        }
                    });
                }
            };
}
