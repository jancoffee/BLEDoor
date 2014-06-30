package bledoor.river.se.bledoor;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
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
import android.support.v13.app.FragmentPagerAdapter;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

public class IntroActivity extends Activity {

    private DeviceControlActivity.CONNECTION_STATE connectionState;
    /**
     * The fragment argument representing the section number for a fragment.
     */
    private static final String ARG_SECTION_NUMBER = "section_number";


    private static final int NR_OF_PAGES = 3;
    private static final String LOGTAG = "IntroActivity";
    private static final int REQUEST_ENABLE_BT = 1;
    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v13.app.FragmentStatePagerAdapter}.
     */
    SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;

    private String bleDeviceAddress;
    private String bleDeviceName;

    private BluetoothAdapter bluetoothAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(LOGTAG,"onCreate");
        setContentView(R.layout.intro_activity);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                Log.d(LOGTAG, "onPageScrolled position:"+position+ " positionOffset:"+positionOffset+ " positionOffsetPixels:"+positionOffsetPixels);
                //Will be called even if we don't change the Page!
            }

            @Override
            public void onPageSelected(int position) {
                Log.d(LOGTAG, "onPageSelected position:"+position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                Log.d(LOGTAG, "onPageScrollStateChanged state:"+state);
                //Will be called even if we don't change the Page

                switch(state){
                    case ViewPager.SCROLL_STATE_IDLE: Log.d(LOGTAG,"SCROLL_STATE_IDLE"); break;
                    case ViewPager.SCROLL_STATE_DRAGGING: Log.d(LOGTAG,"SCROLL_STATE_DRAGGING"); break;
                    case ViewPager.SCROLL_STATE_SETTLING: Log.d(LOGTAG,"SCROLL_STATE_SETTLING"); break;
                    default:
                        Log.e(LOGTAG, "onPageScrollStateChanged, unknown state:" + state);
                }
            }
        });

        bleInit();

    }

    private void bleInit() {
        //BLE init
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

    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(LOGTAG,"onResume");
        parseIncomingIntent(getIntent());    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(LOGTAG,"onPause");
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.d(LOGTAG,"onNewIntent intent:"+intent);
        parseIncomingIntent(intent);
    }

    private void parseIncomingIntent(Intent intent){
        Log.d(LOGTAG,"parseIncomingIntent intent:"+intent);

        if(intent!=null){
            bleDeviceAddress = intent.getStringExtra(ScanBLEActivity.BLE_DEVICE_ADRESS);
            bleDeviceName = intent.getStringExtra(ScanBLEActivity.BLE_DEVIE_NAME);
        }

    }

    //called when the open button is clicked
    public void openButtonClick(View view){
        Log.d(LOGTAG,"openButtonClick");

    }

    //called when the addDoorClick button is clicked
    public void addDoorClick(View view){
        Log.d(LOGTAG,"addDoorClick");
        Intent intent = new Intent(this,ScanBLEActivity.class);
        startActivity(intent);
    }
    //called when the addDoorClick button is clicked
    public void settingsClick(View view){
        Log.d(LOGTAG,"settingsClick");
        //Intent intent = new Intent(this,SettingsActivity.class);
        //startActivity(intent);
    }

    public String getBleDeviceAddress(){
        return bleDeviceAddress;
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            Log.d(LOGTAG,"getItem pos:"+position);
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            //TODO change?
            args.putInt(ARG_SECTION_NUMBER, position+1);
            fragment.setArguments(args);

            return fragment;
        }

        @Override
        public int getCount() {
            // Show nr total pages.
            return NR_OF_PAGES;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Log.d(LOGTAG,"getPageTitle pos:"+position);
            Locale l = Locale.getDefault();
            switch (position) {
                case 0:
                    return getString(R.string.title_section1).toUpperCase(l);
                case 1:
                    return getString(R.string.title_section2).toUpperCase(l);
                case 2:
                    return getString(R.string.title_section3).toUpperCase(l);
            }
            return null;
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public class PlaceholderFragment extends Fragment {

        private BluetoothAdapter bleAdapter;

        private ImageButton openButton;
        private BluetoothGatt bluetoothGatt;


        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        /*
        public static PlaceholderFragment newInstance(int sectionNumber) {
            Log.d(LOGTAG,"newInstance sectionNumber:"+sectionNumber);
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            //TODO change?
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }
        */
        public PlaceholderFragment() {
            Log.d(LOGTAG,"PlaceholderFragment");
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
            Log.d(LOGTAG,"onCreateView");
            int id = getArguments().getInt(ARG_SECTION_NUMBER,-1);

            View rootView = inflater.inflate(R.layout.intro_fragment, container, false);

            openButton = (ImageButton)rootView.findViewById(R.id.open_button);
            openButton.setEnabled(true);
            openButton.setAlpha(0.9f);
            openButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(LOGTAG, "onClick");
                    connect(((IntroActivity)getActivity()).getBleDeviceAddress());
                }
            });

            return rootView;

        }

        private void connect(String address) {
            Log.d(LOGTAG,"connect to address:"+address);
            // Previously connected device.  Try to reconnect.
            /*
            if (bleDeviceAddress.equals(address) && bluetoothGatt != null) {
                Log.d(LOGTAG, "Trying to use an existing bluetoothGatt for connection.");

                if (bluetoothGatt.connect()) {
                    connectionState = CONNECTION_STATE2.CONNECTING;
                    return true;
                } else {
                    return false;
                }
            }
            */
            if(bluetoothGatt != null)
                disconnect();

            if(bluetoothAdapter==null){
                Log.e(LOGTAG,"bluetoothAdapter is null in connect");
                return;
            }

            BluetoothDevice bleDevice = bluetoothAdapter.getRemoteDevice(address);
            if(bleDevice==null) {
                Log.e(LOGTAG, "Could'nt get a BluetoothDevice");
                return;
            }
            bluetoothGatt = bleDevice.connectGatt(getActivity().getBaseContext(),true,gattCallback);
            connectionState = DeviceControlActivity.CONNECTION_STATE.CONNECTING;

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
            Log.d(LOGTAG,"disconnect");
            bluetoothGatt.disconnect();
            connectionState = DeviceControlActivity.CONNECTION_STATE.DISCONNECTED;
        }



        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            Log.d(LOGTAG,"PlaceholderFragment onCreate "+savedInstanceState);

        }

        MyBluetoothGattCallback gattCallback = new MyBluetoothGattCallback();
        private class MyBluetoothGattCallback extends BluetoothGattCallback {

            private static final String LOGTAG = "DeviceControlActivity:BluetoothGattCallback";
            public boolean servicesDiscovered = false;
            private BluetoothGatt bluetoothGatt = null;

            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                Log.d(LOGTAG,"onConnectionStateChange connection state status:"+status+" newState:"+newState);

                switch (newState){
                    case BluetoothProfile.STATE_CONNECTED:
                        bluetoothGatt = gatt;
                        connectionState = DeviceControlActivity.CONNECTION_STATE.CONNECTED;
                        boolean ok = bluetoothGatt.discoverServices();

                        //Enablar beep button
                        //FIXME: update button on UI thread!
                        //beepButton.setEnabled(true);

                        Log.d(LOGTAG,"Tried to discover services:"+ok);
                        break;
                    case BluetoothProfile.STATE_DISCONNECTED:
                        bluetoothGatt = null;
                        connectionState = DeviceControlActivity.CONNECTION_STATE.DISCONNECTED;

                        //Disable beep button
                        //FIXME: update button on UI thread!
                        //beepButton.setEnabled(false);

                        break;
                    default:
                        Log.d(LOGTAG,"unknown connection state status:"+status+ " newState:"+newState);
                }

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
                beep();

                fixme: update UI open door button

            }

            @Override
            public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic,int status) {
                Log.d(LOGTAG,"onCharacteristicRead status:"+status + " UUID:"+characteristic.getUuid());
                Log.d(LOGTAG,"onCharacteristicRead read value:"+characteristic.getValue());
                readCharacteristicProperties(characteristic.getProperties(),characteristic);

            }

            @Override
            public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                Log.d(LOGTAG,"onCharacteristicWrite status:"+ status + " UUID:"+characteristic.getUuid());

            }

            @Override
            public void onCharacteristicChanged(BluetoothGatt gatt,
                                                BluetoothGattCharacteristic characteristic) {
                Log.d(LOGTAG,"onCharacteristicChanged");
            }

            @Override
            public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor,
                                         int status) {
                Log.d(LOGTAG,"onDescriptorRead");
            }

            @Override
            public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor,
                                          int status) {
                Log.d(LOGTAG,"onDescriptorWrite");
            }

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

        };

    }

}
