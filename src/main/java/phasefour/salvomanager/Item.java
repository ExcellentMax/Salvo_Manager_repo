package phasefour.salvomanager;

import android.bluetooth.BluetoothDevice;

public class Item {

    private BluetoothDevice dDevice;
    private byte[] dScanRecord;

    public Item(BluetoothDevice device, byte[] scanRecord) {
        this.dDevice = device;
        this.dScanRecord = scanRecord;
    }

    public BluetoothDevice getdDevice() {
        return dDevice;
    }

    public void setdDevice(BluetoothDevice dDevice) {
        this.dDevice = dDevice;
    }

    public byte[] getdScanRecord() {
        return dScanRecord;
    }

    public void setdScanRecord(byte[] dScanRecord) {
        this.dScanRecord = dScanRecord;
    }

    @Override
    public boolean equals(Object o){
        if(o instanceof Item){
            String toCompare = ((Item) o).getdDevice().getAddress();
            return getdDevice().getAddress().equals(toCompare);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return getdDevice().getAddress().hashCode();
    }



}
