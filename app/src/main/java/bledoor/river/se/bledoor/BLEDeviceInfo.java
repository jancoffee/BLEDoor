package bledoor.river.se.bledoor;

import android.bluetooth.BluetoothDevice;

/**
 * Container class to keep information about found BLE devices
 */
//Container class describing the BLE device
class BLEDeviceInfo {
    //address of the bluetooth LE device
    public String address;

    //radio strength value
    public int rssi;

    //BluetoothDevice information from the BLE device
    public BluetoothDevice bluetoothDevice;

    //scanrecord from the BLE device
    byte[] scanRecord;

    BLEDeviceInfo(String address, int rssi, BluetoothDevice bluetoothDevice, byte[] scanRecord){
        this.address = address;
        this.rssi = rssi;
        this.bluetoothDevice = bluetoothDevice;
        this.scanRecord = scanRecord;
    }

    @Override
    public boolean equals(Object object){
        if ( this == object ) return true;
        if ( !(object instanceof BLEDeviceInfo) ) return false;
        return address.equals(((BLEDeviceInfo) object).address);
    }
    @Override
    public int hashCode(){
        return address.hashCode();
    }

}
