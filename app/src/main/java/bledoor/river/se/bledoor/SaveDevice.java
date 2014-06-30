package bledoor.river.se.bledoor;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
/**
 * This class is activaded from selecting a device in the list and
 * shows the information about a door that was selected in the list and give the user the
 * ability to save the door.
 *
 * TODO: impl. premanent storage of the device, right now we sent it in a intent
 * TODO: impl. dialog boxex for changing the code, execute before any information is shown!
 * */
public class SaveDevice extends Activity {

    private static final String LOGTAG = "SaveDevice";
    private String deviceAddress;
    private String deviceName;

    //Display the information from the device, eg addr
    private TextView deviceInfoText;

    //Display user descriptive text save on the device eg a street address
    private EditText deviceDesc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(LOGTAG,"onCreate");
        setContentView(R.layout.activity_save_device);
        deviceInfoText =  (TextView)findViewById(R.id.device_info_text);
        deviceDesc = (EditText)findViewById(R.id.device_desc);
        Intent intent = getIntent();
        if(intent != null){
            deviceName = intent.getStringExtra(ScanBLEActivity.BLE_DEVIE_NAME);
            deviceAddress = intent.getStringExtra(ScanBLEActivity.BLE_DEVICE_ADRESS);
        }else
            Log.w(LOGTAG,"couldn't find a start intent information");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(LOGTAG,"onResume");
        deviceInfoText.setText("Device Name:" +deviceName+ "\nDevice addr:"+deviceAddress);
        deviceDesc.setText("Garage door 22");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.save_device, menu);
        return false;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.d(LOGTAG, "onNewIntent intent:" + intent.toString());
        deviceAddress = intent.getStringExtra(ScanBLEActivity.BLE_DEVICE_ADRESS);
        deviceName = intent.getStringExtra(ScanBLEActivity.BLE_DEVIE_NAME);
    }

    //executes when the save button is pressed
    public void saveDeviceButtonClicked(View view) {
        Log.d(LOGTAG,"saveDeviceButtonClicked");
        //send an intent that start the swipe view
        Intent startActivity = new Intent(this,IntroActivity.class);
        startActivity.putExtra(ScanBLEActivity.BLE_DEVIE_NAME,deviceName);
        startActivity.putExtra(ScanBLEActivity.BLE_DEVICE_ADRESS,deviceAddress);

        startActivity(startActivity);
    }
}
