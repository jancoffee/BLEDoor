package bledoor.river.se.bledoor;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

/**
 * Activity to connect and show information about a BLE device
 * http://developer.android.com/samples/BluetoothLeGatt/src/com.example.android.bluetoothlegatt/BluetoothLeService.html#l317
 */
public class DeviceControlActivity extends Activity {

    private static final String LOGTAG = "DeviceControlActivity";
    private static final int REQUEST_ENABLE_BT = 1;
    private BluetoothAdapter bluetoothAdapter;

    private TextView bleAddressTextView;
    private TextView bleNameTextView;
    private String address;
    private String name;
    private BluetoothGatt bluetoothGatt;
    private Button beepButton;


    public enum CONNECTION_STATE {DISCONNECTED,CONNECTING, CONNECTION_STATE, CONNECTED};
    private CONNECTION_STATE connectionState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_controle_ble);
        Log.d(LOGTAG, "onCreate");

        //BLE init
        // Initializes Bluetooth adapter.
        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
        // Ensures Bluetooth is available on the device and it is enabled. If not,
        // displays a dialog requesting user permission to enable Bluetooth.
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            Log.d(LOGTAG, "Enablar bluetooth");
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        //view init
        bleAddressTextView = (TextView)findViewById(R.id.ble_address);
        bleNameTextView = (TextView)findViewById(R.id.ble_name);

        beepButton = (Button)findViewById(R.id.beep_button);
        beepButton.setEnabled(true);

        address = getIntent().getStringExtra(ScanBLEActivity.BLE_DEVICE_ADRESS);
        name = getIntent().getStringExtra(ScanBLEActivity.BLE_DEVIE_NAME);

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.d(LOGTAG, "onNewIntent intent:"+intent.toString());

        address = intent.getStringExtra(ScanBLEActivity.BLE_DEVICE_ADRESS);
        name = intent.getStringExtra(ScanBLEActivity.BLE_DEVIE_NAME);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(LOGTAG, "onResume");

        bleAddressTextView.setText(address);
        bleNameTextView.setText(name);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(LOGTAG, "onPause");
        close();
    }

    /**
     * close current ble connection
     * */
    private void close(){
//        if(bluetoothGatt != null) {
            disconnect();
            bluetoothGatt.close();
            bluetoothGatt = null;
//        }
    }

    /**
     * disconnect && close current ble connection
     * */
    private void disconnect(){
        bluetoothGatt.disconnect();
        connectionState = CONNECTION_STATE.DISCONNECTED;
    }

    //called when connect button pressed
    public void connectButtonPressed(View view){
        Log.d(LOGTAG, "connectButtonPressed");

        boolean connectedSuccess = connect(address);
        Log.d(LOGTAG,"Connected ok?"+connectedSuccess);

    }

    //called when beep button pressed
    public void beepButtonPressed(View view) {
       gattCallback.beep();
    }

    //called when beep button pressed
    public void batteryButtonPressed(View view) {
        gattCallback.readBatteryLevel();
    }

    /**
     * Connect using a new or old bluetoothGatt & address
     * */
    private boolean connect(String address){
        // Previously connected device.  Try to reconnect.
        if (this.address.equals(address) && bluetoothGatt != null) {
            Log.d(LOGTAG, "Trying to use an existing bluetoothGatt for connection.");

            if (bluetoothGatt.connect()) {
                connectionState = CONNECTION_STATE.CONNECTING;
                return true;
            } else {
                return false;
            }
        }

        BluetoothDevice bleDevice = bluetoothAdapter.getRemoteDevice(address);
        bluetoothGatt = bleDevice.connectGatt(this,true,gattCallback);
        connectionState = CONNECTION_STATE.CONNECTING;
        return true;
    }

    //called when disconnect button pressed
    public void disconnectButtonPressed(View view){
        Log.d(LOGTAG, "connectButtonPressed");
        disconnect();
    }

    MyBluetoothGattCallback gattCallback = new MyBluetoothGattCallback();
    private class MyBluetoothGattCallback extends BluetoothGattCallback{

        private static final String LOGTAG = "DeviceControlActivity:BluetoothGattCallback";
        public boolean servicesDiscovered = false;
        private BluetoothGatt bluetoothGatt = null;
        /**
         * Callback indicating when GATT client has connected/disconnected to/from a remote
         * GATT server.
         *
         * @param gatt GATT client
         * @param status Status of the connect or disconnect operation.
         *               {@link BluetoothGatt#GATT_SUCCESS} if the operation succeeds.
         * @param newState Returns the new connection state. Can be one of
         *                  {@link android.bluetooth.BluetoothProfile#STATE_DISCONNECTED} or
         *                  {@link android.bluetooth.BluetoothProfile#STATE_CONNECTED}
         */
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            Log.d(LOGTAG,"onConnectionStateChange connection state status:"+status+" newState:"+newState);

            switch (newState){
                case BluetoothProfile.STATE_CONNECTED:
                    bluetoothGatt = gatt;
                    connectionState = CONNECTION_STATE.CONNECTED;
                    boolean ok = bluetoothGatt.discoverServices();

                    //Enablar beep button
                    //FIXME: update button on UI thread!
                    //beepButton.setEnabled(true);

                    Log.d(LOGTAG,"Tried to discover services:"+ok);
                    break;
                case BluetoothProfile.STATE_DISCONNECTED:
                    bluetoothGatt = null;
                    connectionState = CONNECTION_STATE.DISCONNECTED;

                    //Disable beep button
                    //FIXME: update button on UI thread!
                    //beepButton.setEnabled(false);

                    break;
                default:
                    Log.d(LOGTAG,"unknown connection state status:"+status+ " newState:"+newState);
            }

        }

        private String byte2String(byte[] arr, int format){
            String retVal;
            switch (format){
                case BluetoothGattCharacteristic.FORMAT_FLOAT:
                    retVal = "-1";
                    break;
                case BluetoothGattCharacteristic.FORMAT_SFLOAT:
                    retVal = "-1";
                    break;
                case BluetoothGattCharacteristic.FORMAT_SINT16:
                    retVal = "-1";
                    break;
                case BluetoothGattCharacteristic.FORMAT_SINT32:
                    retVal = "-1";
                    break;
                case BluetoothGattCharacteristic.FORMAT_SINT8:
                    retVal = "-1";
                    break;
                case BluetoothGattCharacteristic.FORMAT_UINT16:
                    //TODO fix a better way to konv.
                    final String  digits = "0123456789abcdef";
                    StringBuffer buf = new StringBuffer();

                    for (int i = 0; i != arr.length; i++)
                    {
                        int v = arr[i] & 0xff;

                        buf.append(digits.charAt(v >> 4));
                        buf.append(digits.charAt(v & 0xf));
                    }
                    retVal = buf.toString();

                    break;
                case BluetoothGattCharacteristic.FORMAT_UINT32:
                    retVal = "-1";
                    break;
                case BluetoothGattCharacteristic.FORMAT_UINT8:
                    retVal = "-1";
                    break;
                default:
                    retVal = "-2";
                    break;
            }
            return retVal;

        }


        //read a BluetoothGattCharacteristic Properties
        private void readCharacteristicProperties(final int properties, BluetoothGattCharacteristic characteristic){
            Log.d(LOGTAG,"Characteristic properties:"+ properties );
            int format = 0;

            switch (properties){
                case BluetoothGattCharacteristic.FORMAT_FLOAT:
                    Log.d(LOGTAG,"FORMAT_FLOAT");
                    break;
                case BluetoothGattCharacteristic.FORMAT_SFLOAT:
                    Log.d(LOGTAG,"FORMAT_SFLOAT");
                    break;
                case BluetoothGattCharacteristic.FORMAT_SINT16:
                    Log.d(LOGTAG,"FORMAT_SINT16");
                    break;
                case BluetoothGattCharacteristic.FORMAT_SINT32:
                    Log.d(LOGTAG,"FORMAT_SINT32");
                    break;
                case BluetoothGattCharacteristic.FORMAT_SINT8:
                    Log.d(LOGTAG,"FORMAT_SINT8");
                    break;
                case BluetoothGattCharacteristic.FORMAT_UINT16:
                    Log.d(LOGTAG,"FORMAT_UINT16");
                    //Log.d(LOGTAG,"characteristic value is:"+byte2String(characteristic.getValue(),BluetoothGattCharacteristic.FORMAT_UINT16));
                    byte value[] = characteristic.getValue();
                    Log.d(LOGTAG,"characteristic value is:"+value[0]);
                    break;
                case BluetoothGattCharacteristic.FORMAT_UINT32:
                    Log.d(LOGTAG,"FORMAT_UINT32");
                    break;
                case BluetoothGattCharacteristic.FORMAT_UINT8:
                    Log.d(LOGTAG,"FORMAT_UINT8");
                    break;
                default:
                    Log.d(LOGTAG,"unknown properties:"+properties);
                    break;
            }
            /*
//            if ((properties & 0x01) != 0) {
            if ((properties & BluetoothGattCharacteristic.FORMAT_FLOAT) != 0) {
                format = BluetoothGattCharacteristic.FORMAT_FLOAT;
                Log.d(LOGTAG, "Heart rate format FORMAT_FLOAT.");
                final float heartRate = characteristic.getFloatValue(format, 1);
                Log.d(LOGTAG, String.format("Received heart rate: %f", heartRate));
            }
            */
        }

        /**
         * Callback invoked when the list of remote services, characteristics and descriptors
         * for the remote device have been updated, ie new services have been discovered.
         *
         * @param gatt GATT client invoked {@link BluetoothGatt#discoverServices}
         * @param status {@link BluetoothGatt#GATT_SUCCESS} if the remote device
         *               has been explored successfully.
         */
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            Log.d(LOGTAG,"onServicesDiscovered status:"+status);
            HashMap<String,String> dd = new HashMap<String,String>();
            dd.put("","");
            if(status == BluetoothGatt.GATT_SUCCESS){
                //All ok, do stuff
                servicesDiscovered = true;
                for(BluetoothGattService service : gatt.getServices()){
                    Log.d(LOGTAG,"Service found Uuid:"+UUIDParser.Parse(service.getUuid()) + " of type:"+service.getType());

                    for( BluetoothGattCharacteristic characteristic : service.getCharacteristics()){
                        Log.d(LOGTAG,"Characteristic found Uuid:"+UUIDParser.Parse(characteristic.getUuid()));
                        Log.d(LOGTAG,"Characteristic permissions:"+ characteristic.getPermissions());

                        //TODO enable? services
                        //TODO enable? notifications services
                        for(BluetoothGattDescriptor descriptor : characteristic.getDescriptors() ){
                            Log.d(LOGTAG,"BluetoothGattDescriptor found Uuid:"+UUIDParser.Parse(descriptor.getUuid()));
                            Log.d(LOGTAG,"BluetoothGattDescriptor permissions:"+ descriptor.getPermissions());

                            if(descriptor.getValue()!=null)
                                Log.d(LOGTAG,"BluetoothGattDescriptor getvalue: 0x00");
                            else
                                Log.d(LOGTAG,"BluetoothGattDescriptor getvalue:" + descriptor.getValue());

                        }
                    }
                }
            }else{
                //uhho, no good
                servicesDiscovered = false;
                Log.w(LOGTAG,"onServicesDiscovered got status:"+status);
            }

        }

        public void beep(){
            readBeep(bluetoothGatt);
            writeBeep(bluetoothGatt);
            readBeep(bluetoothGatt);
        }

        public void readBatteryLevel(){
            readBatteryLevel(bluetoothGatt);
        }


        private void readBatteryLevel(BluetoothGatt bluetoothGatt) {
            Log.d(LOGTAG,"readBatteryLevel");
            //00001802-0000-1000-8000-00805f9b34fb
            final String DEFAULT = "-0000-1000-8000-00805f9b34fb";
            final String BATTERY_SERVICE_UUID = "0000180f"+DEFAULT;
            final String BATTERY_LEVEL_UUID = "00002a19"+DEFAULT;
            BluetoothGattService service = bluetoothGatt.getService(UUID.fromString(BATTERY_SERVICE_UUID));
            BluetoothGattCharacteristic characteristic = service.getCharacteristic(UUID.fromString(BATTERY_LEVEL_UUID));
            boolean couldRead = bluetoothGatt.readCharacteristic(characteristic);
            Log.d(LOGTAG,"readBatteryLevel couldRead:"+couldRead);
        }

        private void readBeep(BluetoothGatt bluetoothGatt) {
            Log.d(LOGTAG,"readBeep");
            //00001802-0000-1000-8000-00805f9b34fb
            final String DEFAULT = "-0000-1000-8000-00805f9b34fb";
            final String IMMEDIATE_ALERT_SERVICE_UUID = "00001802"+DEFAULT;
            final String PROXIMITY_ALERT_LEVEL_UUID = "00002a06"+DEFAULT;
            BluetoothGattService service = bluetoothGatt.getService(UUID.fromString(IMMEDIATE_ALERT_SERVICE_UUID));
            BluetoothGattCharacteristic characteristic = service.getCharacteristic(UUID.fromString(PROXIMITY_ALERT_LEVEL_UUID));
            boolean couldRead = bluetoothGatt.readCharacteristic(characteristic);
            Log.d(LOGTAG,"readBeep couldRead:"+couldRead);
        }

        private void writeBeep(BluetoothGatt gatt) {
            Log.d(LOGTAG,"writeBeep");
            //00001802-0000-1000-8000-00805f9b34fb
            final String DEFAULT = "-0000-1000-8000-00805f9b34fb";
            final String IMMEDIATE_ALERT_SERVICE_UUID = "00001802"+DEFAULT;
            final String PROXIMITY_ALERT_LEVEL_UUID = "00002a06"+DEFAULT;
            BluetoothGattService service = gatt.getService(UUID.fromString(IMMEDIATE_ALERT_SERVICE_UUID));
            BluetoothGattCharacteristic characteristic = service.getCharacteristic(UUID.fromString(PROXIMITY_ALERT_LEVEL_UUID));
            //descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            characteristic.setValue(new byte[] {0x01} );
            gatt.writeCharacteristic(characteristic);
        }

        /**
         * Callback reporting the result of a characteristic read operation.
         *
         * @param gatt GATT client invoked {@link BluetoothGatt#readCharacteristic}
         * @param characteristic Characteristic that was read from the associated
         *                       remote device.
         * @param status {@link BluetoothGatt#GATT_SUCCESS} if the read operation
         *               was completed successfully.
         */
        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic,int status) {
            Log.d(LOGTAG,"onCharacteristicRead status:"+status + " UUID:"+characteristic.getUuid());
            Log.d(LOGTAG,"onCharacteristicRead read value:"+characteristic.getValue());
            readCharacteristicProperties(characteristic.getProperties(),characteristic);

        }

        /**
         * Callback indicating the result of a characteristic writeBeep operation.
         *
         * <p>If this callback is invoked while a reliable writeBeep transaction is
         * in progress, the value of the characteristic represents the value
         * reported by the remote device. An application should compare this
         * value to the desired value to be written. If the values don't match,
         * the application must abort the reliable writeBeep transaction.
         *
         * @param gatt GATT client invoked {@link BluetoothGatt#writeCharacteristic}
         * @param characteristic Characteristic that was written to the associated
         *                       remote device.
         * @param status The result of the writeBeep operation
         *               {@link BluetoothGatt#GATT_SUCCESS} if the operation succeeds.
         */
        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            Log.d(LOGTAG,"onCharacteristicWrite status:"+ status + " UUID:"+characteristic.getUuid());

        }

        /**
         * Callback triggered as a result of a remote characteristic notification.
         *
         * @param gatt GATT client the characteristic is associated with
         * @param characteristic Characteristic that has been updated as a result
         *                       of a remote notification event.
         */
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            Log.d(LOGTAG,"onCharacteristicChanged");
        }

        /**
         * Callback reporting the result of a descriptor read operation.
         *
         * @param gatt GATT client invoked {@link BluetoothGatt#readDescriptor}
         * @param descriptor Descriptor that was read from the associated
         *                   remote device.
         * @param status {@link BluetoothGatt#GATT_SUCCESS} if the read operation
         *               was completed successfully
         */
        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor,
                                     int status) {
            Log.d(LOGTAG,"onDescriptorRead");
        }

        /**
         * Callback indicating the result of a descriptor writeBeep operation.
         *
         * @param gatt GATT client invoked {@link BluetoothGatt#writeDescriptor}
         * @param descriptor Descriptor that was writte to the associated
         *                   remote device.
         * @param status The result of the writeBeep operation
         *               {@link BluetoothGatt#GATT_SUCCESS} if the operation succeeds.
         */
        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor,
                                      int status) {
            Log.d(LOGTAG,"onDescriptorWrite");
        }

        /**
         * Callback invoked when a reliable writeBeep transaction has been completed.
         *
         * @param gatt GATT client invoked {@link BluetoothGatt#executeReliableWrite}
         * @param status {@link BluetoothGatt#GATT_SUCCESS} if the reliable writeBeep
         *               transaction was executed successfully
         */
        @Override
        public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
            Log.d(LOGTAG,"onReliableWriteCompleted");
        }

        /**
         * Callback reporting the RSSI for a remote device connection.
         *
         * This callback is triggered in response to the
         * {@link BluetoothGatt#readRemoteRssi} function.
         *
         * @param gatt GATT client invoked {@link BluetoothGatt#readRemoteRssi}
         * @param rssi The RSSI value for the remote device
         * @param status {@link BluetoothGatt#GATT_SUCCESS} if the RSSI was read successfully
         */
        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            Log.d(LOGTAG,"onReadRemoteRssi");
        }

        /**
         * Retrieves a list of supported GATT services on the connected device. This should be
         * invoked only after {@code BluetoothGatt#discoverServices()} completes successfully.
         * @return A {@code List} of supported services OR NULL.
         */
        public List<BluetoothGattService> getServices(){
            Log.d(LOGTAG,"getServices");
            if( servicesDiscovered /*&& bluetoothGatt != null */ ){
               return bluetoothGatt.getServices();
            }
            return null;
        }


    };

    private class BLEGenericService{
        UUID uuid;
        BLEValue value;
        List<BLECharacteristic> bleCharacteristics;
        public String getName(){ return null; };
    }
    private class BLEValue{
        UUID uuid;
        public String getName(){ return null; };
    }
    private class BLECharacteristic{
        UUID uuid;
        public String getName(){ return null; };
    }
}
