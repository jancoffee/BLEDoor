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
import android.os.Build;
import android.os.Handler;
import android.support.v13.app.FragmentPagerAdapter;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

/**
 *
 * WARNING, there is a lot of dependencies between the Activity that ARE NOT THREAD SAFE and they
 * are run in separate threads! == NOT GOOD
 * */

public class IntroActivity extends Activity {

    private DeviceControlActivity.CONNECTION_STATE connectionState;
    /**
     * The fragment argument representing the section number for a fragment.
     */
    private static final String ARG_SECTION_NUMBER = "section_number";


    private static final int NR_OF_PAGES = 3;
    private final String LOGTAG = "IntroActivity";
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

        logDeviceInformation();
    }

    private void logDeviceInformation(){
        //adding application info to log
        Log.d(LOGTAG,"Application Version code:"+getResources().getString(R.string.version_code));
        Log.d(LOGTAG,"Application Version release name:"+getResources().getString(R.string.version_name));
        Log.d(LOGTAG,"Build.DEVICE:"+ Build.DEVICE);
        Log.d(LOGTAG,"Build.BRAND:"+ Build.BRAND);
        Log.d(LOGTAG,"Build.BOARD:"+ Build.BOARD);
        Log.d(LOGTAG,"Build.DISPLAY:"+ Build.DISPLAY);
        Log.d(LOGTAG,"Build.FINGERPRINT:"+ Build.FINGERPRINT);
        Log.d(LOGTAG,"Build.HARDWARE:"+ Build.HARDWARE);
        Log.d(LOGTAG,"Build.ID:"+ Build.ID);
        Log.d(LOGTAG,"Build.MANUFACTURER:"+ Build.MANUFACTURER);
        Log.d(LOGTAG,"Build.MODEL:"+ Build.MODEL);
        Log.d(LOGTAG,"Build.PRODUCT:"+ Build.PRODUCT);
        Log.d(LOGTAG,"Build.SERIAL:"+ Build.SERIAL);
        Log.d(LOGTAG,"Build.TAGS:"+ Build.TAGS);
        Log.d(LOGTAG,"Build.TIME:"+ Build.TIME);
        Log.d(LOGTAG,"Build.TYPE:"+ Build.TYPE);
        Log.d(LOGTAG,"Build.USER:"+ Build.USER);
        Log.d(LOGTAG,"Build.VERSION.RELEASE:"+ Build.VERSION.RELEASE);
        Log.d(LOGTAG,"Build.VERSION.CODENAME:"+ Build.VERSION.CODENAME);
        Log.d(LOGTAG,"Build.VERSION.INCREMENTAL:"+ Build.VERSION.INCREMENTAL);
        Log.d(LOGTAG,"Build.VERSION.SDK_INT:"+ Build.VERSION.SDK_INT);
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
        //finish();

    }
    @Override
    public void onBackPressed()
    {
        //todo: Kolla på start parameterar till StartActivity för att återanvända en gammal activity
        //    alt död hela appen vid onBackPress
        Log.d(LOGTAG,"onBackPressed");
        //super.onBackPressed();
        finish();
    }
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.d(LOGTAG,"onNewIntent intent:"+intent);
        parseIncomingIntent(intent);
    }

    private void parseIncomingIntent(Intent intent){
        Log.d(LOGTAG,"parseIncomingIntent intent:"+intent);

        if(intent==null)
            return;

        String tmpAddress = intent.getStringExtra(ScanBLEActivity.BLE_DEVICE_ADRESS);
        String tmpName = intent.getStringExtra(ScanBLEActivity.BLE_DEVIE_NAME);


        if(tmpAddress != null && tmpAddress.length() > 0  && tmpName != null && tmpName.length() > 0 ) {
            bleDeviceAddress = tmpAddress;
            bleDeviceName = tmpName;
        }
        Log.d(LOGTAG,"tmpAddress:"+tmpAddress+ " tmpName:"+tmpName);
        Log.d(LOGTAG,"bleDeviceAddress:"+bleDeviceAddress+ " bleDeviceName:"+bleDeviceName);
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
        Toast.makeText(this,"Sorry, Not implemented yet",Toast.LENGTH_SHORT).show();
        //Intent intent = new Intent(this,SettingsActivity.class);
        //startActivity(intent);
    }

    public String getBleDeviceAddress(){
        Log.d(LOGTAG,"getBleDeviceAddress:"+bleDeviceAddress);
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

        private static final long CONNECTION_TIMEOUT = 1000 * 3;
        private final String LOGTAG = "IntroActivity:PlaceholderFragment";
        private BluetoothAdapter bleAdapter;

        private ImageButton openButton;
        private BluetoothGatt bluetoothGatt;
        Handler handler;
        private int id;
        private View rootView;
        private BluetoothAdapter bluetoothAdapter;

        //headline/title of the page, visible name of the Garage door.
        private TextView introFragmentHeadline;

        //progressbar wheel showing connection
        private ProgressBar connectionProgressbar;

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
            handler = new Handler();
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
            Log.d(LOGTAG,"onCreateView");
            id = getArguments().getInt(ARG_SECTION_NUMBER,-1);

            rootView = inflater.inflate(R.layout.intro_fragment, container, false);

            openButton = (ImageButton)rootView.findViewById(R.id.open_button);
            openButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(LOGTAG, "onClick isActivated"+v.isEnabled());
                    if( !v.isEnabled() )
                        return;

                    if(gattCallback.getServicesDiscovered())
                        openDoor();
                }
            });

            introFragmentHeadline = (TextView)rootView.findViewById(R.id.intro_fragment_headline);

            connectionProgressbar = (ProgressBar)rootView.findViewById(R.id.progressBar1);

            return rootView;

        }

        @Override
        public void onDestroyView() {
            super.onDestroyView();
            Log.d(LOGTAG,"onDestroyView");
        }

        @Override
        public void onPause() {
            super.onDestroy();
            Log.d(LOGTAG,"onPause");
            disconnect();
        }

        @Override
        public void onResume() {
            super.onResume();
            Log.d(LOGTAG,"onResume");
            String address = ((IntroActivity)getActivity()).getBleDeviceAddress();
            if(address != null && address.length() > 0) {
                foundDoor(true);
                connect(address);
            }else {
                foundDoor(false);
            }
        }

        private void foundDoor(boolean foundDoor) {
            if (foundDoor) {
                //todo: set title to "Garage Door, Mum"
                introFragmentHeadline.setText(getResources().getString(R.string.intro_fragment_headline_example_door)+" "+String.valueOf(id));
                //todo: change bg picture
            } else {
                //todo: set title to "No door found"
                introFragmentHeadline.setText(getResources().getString(R.string.intro_fragment_headline_no_door));
                //todo: change bg picture
            }
        }

        private void openDoor() {
            Log.d(LOGTAG, "openDoor");
            gattCallback.beep();
            //set disconnect timer

//            handler.postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    disconnectAndStartScan();
//                }
//            },CONNECTION_TIMEOUT);

        }

        //Enable or disable the connect button, Thread safe
        private void enableConnectButton(boolean enable){
            Log.d(LOGTAG,"enableConnectButton enable:"+enable+" isActive:"+openButton.isActivated());
            if(enable) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        openButton.setEnabled(false);
                        openButton.setBackgroundResource(R.drawable.open_btn_connecting);
                        openButton.setVisibility(View.VISIBLE);
                        openButton.invalidate();
                    }
                });
            }
            //disable button
            else{
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        openButton.setEnabled(false);
                        openButton.setBackgroundResource(R.drawable.open_btn_not_connected);
                        openButton.setVisibility(View.VISIBLE);
                        openButton.invalidate();
                    }
                });
            }
        }

        //Enable or disable the connect button, Thread safe
        private void hideConnectButton(boolean hide){
            Log.d(LOGTAG,"hideConnectButton enable:"+hide+" isActive:"+openButton.isActivated());
            //hide button
            if(hide) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        openButton.setEnabled(false);
                        openButton.setVisibility(View.INVISIBLE);
                        openButton.invalidate();

                    }
                });
            }
            //unhide button
            else{
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        openButton.setEnabled(true);
                        openButton.setVisibility(View.VISIBLE);
                        openButton.invalidate();
                    }
                });
            }
        }

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
                        //Log.d(LOGTAG, deviceStr);
                        if(bleDeviceAddress != null && bleDeviceAddress.equals(device.getAddress())){
                            Log.d(LOGTAG,"MATCHED ADDRESS rssiValue:"+rssi +" for device:"+bleDeviceAddress + " incoming device:"+device.getAddress());
//                            connect(bleDeviceAddress);
                        }
                    }
                });
            }
        };


        public void connect(final String address) {
            Log.d(LOGTAG, "connect to id "+id+" address:" + address);
            //disable ble scan
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(bluetoothAdapter!=null)
                        bluetoothAdapter.stopLeScan(mLeScanCallback);
                }
            });

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
                disconnectAndStartScan();

            if(bluetoothAdapter==null){
                Log.e(LOGTAG,"bluetoothAdapter is null in connect");
                return;
            }

            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    final BluetoothDevice bleDevice = bluetoothAdapter.getRemoteDevice(address);
                    if(bleDevice==null) {
                        Log.e(LOGTAG, "Could'nt get a BluetoothDevice");
                        return;
                    }

                    bluetoothGatt = bleDevice.connectGatt(getActivity().getBaseContext(),true,gattCallback);
                    connectionProgressbar.setVisibility(View.VISIBLE);
                }
            });
            connectionState = DeviceControlActivity.CONNECTION_STATE.CONNECTING;

        }



        /**
         * disconnect && close current ble connection
         * */
        private void disconnect(){
            Log.d(LOGTAG,"disconnect id"+id);
            if(bluetoothGatt != null) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        bluetoothGatt.close();
                        bluetoothGatt.disconnect();
                        bluetoothGatt = null;
                        connectionState = DeviceControlActivity.CONNECTION_STATE.DISCONNECTED;
                    }
                });
            }else{
                //TODO: check this
                Log.w(LOGTAG,"disconnect while not connected, investigate why!!");
            }
            enableConnectButton(false);
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(bluetoothAdapter!=null)
                        bluetoothAdapter.stopLeScan(mLeScanCallback);
                }
            });

        }
        private void disconnectAndStartScan() {
            Log.d(LOGTAG,"disconnectAndStartScan id"+id);
            disconnect();
            //start ble scan
            if(bluetoothAdapter!=null)
                bluetoothAdapter.startLeScan(mLeScanCallback);
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            Log.d(LOGTAG,"PlaceholderFragment onCreate "+savedInstanceState);
            //FIXME harcode so only active in FIRST view
            int idt = getArguments().getInt(ARG_SECTION_NUMBER,-1);
            Log.d(LOGTAG,"Idt:"+idt);
            if(idt==1)
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
            if (bleAdapter == null || !bluetoothAdapter.isEnabled()) {
                Log.d(LOGTAG, "Enablar bluetooth");
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }

        }


        MyBluetoothGattCallback gattCallback = new MyBluetoothGattCallback();
        private class MyBluetoothGattCallback extends BluetoothGattCallback {

            private static final String LOGTAG = "IntroActivity:BluetoothGattCallback";
            public boolean servicesDiscovered = false;
            private BluetoothGatt bluetoothGatt = null;

            public boolean getServicesDiscovered(){
                return servicesDiscovered;
            }

            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                Log.d(LOGTAG,"onConnectionStateChange connection state status:"+status+" newState:"+newState);

                Runnable getServicesRunnable = new Runnable() {
                    @Override
                    public void run() {
                        //hack: have to LOCK the bluetoothGatt
                        if(bluetoothGatt != null);
                        boolean ok = bluetoothGatt.discoverServices();
                        Log.d(LOGTAG,"Doing a bluetoothGatt.discoverServices"+ok);
                    }
                };

                switch (newState){
                    case BluetoothProfile.STATE_CONNECTED:
                        Log.d(LOGTAG,"STATE_CONNECTED");
                        bluetoothGatt = gatt;
                        connectionState = DeviceControlActivity.CONNECTION_STATE.CONNECTED;

                        getActivity().runOnUiThread(getServicesRunnable);

                        //FIXME: update button on UI thread!
                        enableConnectButton(true);

                        break;
                    case BluetoothProfile.STATE_DISCONNECTED:
                        Log.d(LOGTAG,"STATE_DISCONNECTED");

                        bluetoothGatt = null;
                        connectionState = DeviceControlActivity.CONNECTION_STATE.DISCONNECTED;
                        enableConnectButton(false);
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                connectionProgressbar.setVisibility(View.VISIBLE);
                            }
                        });
                        break;
                    default:
                        enableConnectButton(false);
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

                //always reset this as a precuation
                servicesDiscovered = false;

                if(status == BluetoothGatt.GATT_SUCCESS){
                    //All ok, do stuff

                    if(gatt!=null){
                        //immediate alert server
                        //hack: will only give the FIRST INSTANCE of the Service
                        BluetoothGattService btleService = gatt.getService(UUID.fromString("00001802-0000-1000-8000-00805f9b34fb"));
                        Log.d(LOGTAG, "BluetoothGattService type:" + (btleService != null ? btleService.getType() : "null"));
                        BluetoothGattCharacteristic characteristic2 = null;
                        //hack: will only give the FIRST INSTANCE of the Characteristic matching the UUID, might be several on the unit
                        if(btleService!=null) {
                            final BluetoothGattCharacteristic characteristic = btleService.getCharacteristic(UUID.fromString("00002a06-0000-1000-8000-00805f9b34fb"));
                            characteristic2 = characteristic;
                            Log.d(LOGTAG, "BluetoothGattService getProperties:" + (characteristic != null ? characteristic.getProperties() : "null"));
                            if(characteristic!=null) {
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Log.d(LOGTAG, "Setting the GREEN button, propterties:" + (characteristic != null ? characteristic.getProperties() : "null"));
                                        openButton.setBackgroundResource(R.drawable.open_btn_connected);
                                        openButton.invalidate();
                                        openButton.setEnabled(true);
                                        connectionProgressbar.setVisibility(View.INVISIBLE);
                                    }
                                });
                                servicesDiscovered = true;
                            }
                        }

                        if(characteristic2 == null){
                                //didn't find the expected characteristic, disconnect and connect to try again
                                disconnect();
                                String address = ((IntroActivity)getActivity()).getBleDeviceAddress();
                                if(address != null && address.length() > 0) {
                                    foundDoor(true);
                                    connect(address);
                                }else {
                                    foundDoor(false);
                                }
                        }
                    }
//                    for(BluetoothGattService service : gatt.getServices()){
//                        Log.d(LOGTAG,"Service found Uuid:"+UUIDParser.Parse(service.getUuid()) + " of type:"+service.getType());
//
//                        for( BluetoothGattCharacteristic characteristic : service.getCharacteristics()){
//                            Log.d(LOGTAG,"Characteristic found Uuid:"+UUIDParser.Parse(characteristic.getUuid()));
//                            Log.d(LOGTAG,"Characteristic permissions:"+ characteristic.getPermissions());
//
//                            //TODO enable? services
//                            //TODO enable? notifications services
//                            for(BluetoothGattDescriptor descriptor : characteristic.getDescriptors() ){
//                                Log.d(LOGTAG,"BluetoothGattDescriptor found Uuid:"+UUIDParser.Parse(descriptor.getUuid()));
//                                Log.d(LOGTAG,"BluetoothGattDescriptor permissions:"+ descriptor.getPermissions());
//
//                                if(descriptor.getValue()!=null)
//                                    Log.d(LOGTAG,"BluetoothGattDescriptor getvalue: 0x00");
//                                else
//                                    Log.d(LOGTAG,"BluetoothGattDescriptor getvalue:" + descriptor.getValue());
//
//                            }
//                        }
//                    }
                }else{
                    //uhho, no good
                    servicesDiscovered = false;
                    Log.e(LOGTAG,"onServicesDiscovered got unknown status:"+status);
                }

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
            @SuppressWarnings("unused")
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
                if(service == null){
                    Log.e(LOGTAG,"ReadBeep bluetoothGatt.getService failed");
                    return;
                }

                BluetoothGattCharacteristic characteristic = service.getCharacteristic(UUID.fromString(PROXIMITY_ALERT_LEVEL_UUID));
                if(characteristic == null){
                    Log.e(LOGTAG,"ReadBeep service.getCharacteristic failed");
                    return;
                }

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
                if(service == null){
                    Log.e(LOGTAG,"WriteBeep gatt.getService failed");
                    return;
                }

                BluetoothGattCharacteristic characteristic = service.getCharacteristic(UUID.fromString(PROXIMITY_ALERT_LEVEL_UUID));
                if(characteristic == null){
                    Log.e(LOGTAG,"WriteBeep service.getCharacteristic failed");
                    return;
                }

                //descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                characteristic.setValue(new byte[] {0x01} );
                gatt.writeCharacteristic(characteristic);
            }

        }

    }

}
