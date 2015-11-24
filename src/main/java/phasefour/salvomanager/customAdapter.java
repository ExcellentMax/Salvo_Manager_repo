package phasefour.salvomanager;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class customAdapter extends ArrayAdapter<Item> {

    private final static String TAG = "DEBUGGING";
    private final ArrayList<Item> itemsArrayList;
    private BluetoothGatt mBluetoothGatt;
    private final Context context;
    private static int transmissionPower;
    private static int broadcastingInterval;
    private static final int SLEEP_TIME = 2500;
    private Object[] arrayOfCurrentChars = new Object[5];
    private Object[] arrayOfNewChars = new Object[5];
    private int UPDATED_NAME_FLAG = 0;
    private int UPDATED_MAJOR_FLAG = 0;
    private int UPDATED_MINOR_FLAG = 0;
    private int UPDATED_INTERVAL_FLAG = 0;
    private int UPDATED_POWER_FLAG = 0;
    private int UPDATED_CONNECTION_FLAG = 0;
    private int UPDATED_REBOOT_FLAG = 0;

    public customAdapter(Context context, ArrayList<Item> itemsArrayList) {

        super(context, R.layout.list_full_info, itemsArrayList);

        this.context = context;
        this.itemsArrayList = itemsArrayList;

    }

    //
    // Getting the view and populating it
    //
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View rowView = inflater.inflate(R.layout.list_full_info, parent, false);

        byte[] scanRecord = itemsArrayList.get(position).getdScanRecord();

        // Buttons
        final Button addInfoButton = (Button) rowView.findViewById(R.id.expandButton);
        final Button updateInfoButton = (Button) rowView.findViewById(R.id.softRebootButton);
        updateInfoButton.setEnabled(false);

        // Header device name
        final TextView displayedDeviceName = (TextView) rowView.findViewById(R.id.parentName);
        displayedDeviceName.setText(itemsArrayList.get(position).getdDevice().getName());

        // Header device MAC
        TextView displayedDeviceMAC = (TextView) rowView.findViewById(R.id.parentMAC);
        displayedDeviceMAC.setText(itemsArrayList.get(position).getdDevice().getAddress());

        // Change beacon name
        final EditText nameEditText = (EditText) rowView.findViewById(R.id.beaconNameEditText);
        String tempName = itemsArrayList.get(position).getdDevice().getName();
        if (tempName != null) {
            nameEditText.setText(tempName.substring(0,tempName.length()-5));
        }

        // Change beacon major
        final EditText majorEditText = (EditText) rowView.findViewById(R.id.beaconMajorEditText);
        majorEditText.setText(String.valueOf(getMajorMinorPower(scanRecord)[0]));

        // Change beacon minor
        final EditText minorEditText = (EditText) rowView.findViewById(R.id.beaconMinorEditText);
        minorEditText.setText(String.valueOf(getMajorMinorPower(scanRecord)[1]));

        // Change beacon broadcasting interval
        final EditText intervalEditText = (EditText) rowView.findViewById(R.id.beaconIntervalEditText);
        intervalEditText.setText("-");

        // Change beacon TxPower
        final EditText tPowerEditText = (EditText) rowView.findViewById(R.id.tPowerEditText);
        tPowerEditText.setText("-");

        // Change beacon connection mode
        final EditText connectableEditText = (EditText) rowView.findViewById(R.id.connectableEditText);
        connectableEditText.setText("-");
        connectableEditText.setVisibility(View.GONE);
        TextView conTextView = (TextView) rowView.findViewById(R.id.connectableLabel);
        conTextView.setVisibility(View.GONE);

        // Child layout actions
        final RelativeLayout addInfo = (RelativeLayout) rowView.findViewById(R.id.childLayout);
        addInfo.setVisibility(View.GONE);

        final ProgressBar pBar = (ProgressBar) rowView.findViewById(R.id.progressBar);
        pBar.setVisibility(View.INVISIBLE);


        //
        // Reading characteristics button
        //
        addInfoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (addInfo.getVisibility() == View.GONE) {

                    pBar.setVisibility(View.VISIBLE);
                    addInfo.setVisibility(View.VISIBLE);
                    addInfo.setFocusable(true);
                    addInfoButton.setText("less");
                    arrayOfCurrentChars[0] = new String(String.valueOf((nameEditText.getText())));
                    arrayOfCurrentChars[1] = new Integer(String.valueOf(majorEditText.getText()));
                    arrayOfCurrentChars[2] = new Integer(String.valueOf(minorEditText.getText()));
                    arrayOfCurrentChars[3] = null;
                    arrayOfCurrentChars[4] = null;
                    broadcastingInterval = 999;
                    transmissionPower = 999;

                    class asyncGattGetCharacteristics extends AsyncTask<Void, Void, Integer> {

                        @Override
                        protected Integer doInBackground(Void... params) {
                            mBluetoothGatt = itemsArrayList.get(position).getdDevice().connectGatt(context, false, readGattCallback);
                            try {
                                new Handler(Looper.getMainLooper()).post(new Runnable() {
                                    @Override
                                    public void run() {
                                        pBar.setVisibility(View.VISIBLE);
                                    }
                                });
                                Thread.sleep(SLEEP_TIME);
                                new Handler(Looper.getMainLooper()).post(new Runnable() {
                                    @Override
                                    public void run() {
                                        if ((tPowerEditText.getText().toString().equals("-") && transmissionPower != 999) || (transmissionPower != 999)) {
                                            tPowerEditText.setText(String.valueOf(transmissionPower));
                                        }

                                        if ((intervalEditText.getText().toString().equals("-") && broadcastingInterval != 999) || (broadcastingInterval != 999)) {
                                            intervalEditText.setText(String.valueOf(broadcastingInterval));
                                        }

                                        pBar.setVisibility(View.GONE);

                                        if(tPowerEditText.getText().toString().equals("-") || intervalEditText.getText().toString().equals("---")) {

                                        } else {
                                            arrayOfCurrentChars[3] = new Integer(String.valueOf(intervalEditText.getText()));
                                            arrayOfCurrentChars[4] = new Integer(String.valueOf(tPowerEditText.getText()));
                                            //arrayOfCurrentChars[5] = new Integer(String.valueOf(connectableEditText.getText()));
                                            updateInfoButton.setBackgroundColor(Color.parseColor("#ffac3f"));
                                            updateInfoButton.setEnabled(true);
                                        }
                                    }
                                });
                            } catch (Exception e) {
                                Log.e(TAG, String.valueOf(e));
                            }
                            return 0;
                        }

                        @Override
                        protected void onPostExecute(Integer integer) {
                        }

                    }

                    new asyncGattGetCharacteristics().execute();

                } else {

                    addInfo.setVisibility(View.GONE);
                    pBar.setVisibility(View.GONE);
                    addInfoButton.setText("more");
                    mBluetoothGatt.disconnect();

                }
            }
        });

        //
        // Writing to characteristics button
        //
        updateInfoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                UPDATED_REBOOT_FLAG = 0;
                UPDATED_MINOR_FLAG = 0;
                UPDATED_NAME_FLAG = 0;
                UPDATED_INTERVAL_FLAG = 0;
                UPDATED_MAJOR_FLAG = 0;
                UPDATED_POWER_FLAG = 0;

                for (int i = 0; i < 5; i++) {
                    Log.v(TAG, String.valueOf(arrayOfCurrentChars[i]));
                }

                arrayOfNewChars[0] = new String(nameEditText.getText().toString());
                arrayOfNewChars[1] = new Integer(Integer.valueOf(majorEditText.getText().toString()));
                arrayOfNewChars[2] = new Integer(Integer.valueOf(minorEditText.getText().toString()));
                arrayOfNewChars[3] = new Integer(Integer.valueOf(intervalEditText.getText().toString()));
                arrayOfNewChars[4] = new Integer(Integer.valueOf(tPowerEditText.getText().toString()));
                //arrayOfNewChars[5] = new Integer(Integer.valueOf(connectableEditText.getText().toString()));

                Log.v(TAG, "------------------------");

                for (int i = 0; i < 5; i++) {
                    Log.v(TAG, String.valueOf(arrayOfNewChars[i]));
                }


                class asyncGattWriteCharacteristics extends AsyncTask<Void, Void, Integer> {
                    @Override
                    protected Integer doInBackground(Void... params) {
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                pBar.setVisibility(View.VISIBLE);
                            }
                        });
                        mBluetoothGatt = itemsArrayList.get(position).getdDevice().connectGatt(context, false, writeGattCallback);
                        try {
                            Thread.sleep(SLEEP_TIME);
                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {

                                    pBar.setVisibility(View.GONE);
                                    if (UPDATED_POWER_FLAG == 1 &&
                                        UPDATED_INTERVAL_FLAG == 1 &&
                                        UPDATED_MAJOR_FLAG == 1 &&
                                        UPDATED_MINOR_FLAG == 1 &&
                                        UPDATED_NAME_FLAG == 1 &&
                                        UPDATED_REBOOT_FLAG == 1) {

                                        Toast.makeText(context, "Beacon updated", Toast.LENGTH_SHORT).show();

                                        UPDATED_REBOOT_FLAG = 0;
                                        UPDATED_MINOR_FLAG = 0;
                                        UPDATED_NAME_FLAG = 0;
                                        UPDATED_INTERVAL_FLAG = 0;
                                        UPDATED_MAJOR_FLAG = 0;
                                        UPDATED_POWER_FLAG = 0;

                                    } else {
                                        Toast.makeText(context, "Failed to update beacon", Toast.LENGTH_LONG).show();
                                    }
                                }
                            });

                        } catch (Exception e) {

                        }
                        return 0;
                    }
                }

                new asyncGattWriteCharacteristics().execute();

            }
        });

        return rowView;

    }


    //
    // Change Name Gatt Callback
    //
    public final BluetoothGattCallback writeGattCallback = new BluetoothGattCallback() {

        List<BluetoothGattCharacteristic> chars = new ArrayList<>();
        BluetoothGattService rightService = null;

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.v(TAG, "Connected!");
                gatt.discoverServices();
            }
            if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.v(TAG, "Disconnected...");
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {

            List<BluetoothGattService> services = gatt.getServices();

            for (int i = 0; i < services.size(); i++) {
                if (services.get(i).getCharacteristics().size() > 8) {
                    rightService = services.get(i);
                }
            }

            chars.add(rightService.getCharacteristics().get(11));
            chars.add(rightService.getCharacteristics().get(8));
            chars.add(rightService.getCharacteristics().get(1));
            chars.add(rightService.getCharacteristics().get(2));
            chars.add(rightService.getCharacteristics().get(6));
            chars.add(rightService.getCharacteristics().get(4));

            writeCharacteristics(gatt);

        }

        public void writeCharacteristics(BluetoothGatt gatt) {

            // Writing transmission power
            if (chars.size() == 6) {
                int newValue = (Integer) arrayOfNewChars[4];
                rightService.getCharacteristics().get(4).setValue(newValue, BluetoothGattCharacteristic.FORMAT_UINT8, 0);
                gatt.writeCharacteristic(rightService.getCharacteristics().get(4));
            }

            // Writing broadcasting interval
            if (chars.size() == 5) {
                int newValue = (Integer) arrayOfNewChars[3];
                rightService.getCharacteristics().get(6).setValue(newValue, BluetoothGattCharacteristic.FORMAT_UINT8, 0);
                gatt.writeCharacteristic(rightService.getCharacteristics().get(6));
            }

            // Writing minor number
            if (chars.size() == 4) {

                int newValue = ((Integer) arrayOfNewChars[2]);
                StringBuilder builder = new StringBuilder();
                String newStringHexValue = Integer.toHexString(newValue);

                if (newStringHexValue.length() == 1) {
                    builder.append("0x0").append(newStringHexValue).append("00");
                    newValue = Integer.decode(builder.toString());
                } else if (newStringHexValue.length() == 2) {
                    builder.append("0x").append(newStringHexValue).append("00");
                    newValue = Integer.decode(builder.toString());
                } else if (newStringHexValue.length() == 3) {
                    StringBuilder tempstr = builder.append("0").append(newStringHexValue);
                    String lastBits = tempstr.substring(0,2);
                    String firstBits = tempstr.substring(2);
                    tempstr.delete(0, 4);
                    newValue = Integer.decode(String.valueOf(tempstr.append("0x").append(firstBits).append(lastBits)));
                } else if (newStringHexValue.length() == 4) {
                    StringBuilder tempstr = builder.append(newStringHexValue);
                    String lastBits = tempstr.substring(0, 2);
                    String firstBits = tempstr.substring(2);
                    tempstr.delete(0,4);
                    newValue = Integer.decode(String.valueOf(tempstr.append("0x").append(firstBits).append(lastBits)));
                }

                UUID uuid = rightService.getCharacteristics().get(2).getUuid();
                rightService.getCharacteristics().get(2).setValue(newValue, BluetoothGattCharacteristic.FORMAT_UINT16, 0);
                gatt.writeCharacteristic(rightService.getCharacteristic(uuid));
            }

            // Writing major number
            if (chars.size() == 3) {

                int newValue = ((Integer) arrayOfNewChars[1]);
                StringBuilder builder = new StringBuilder();
                String newStringHexValue = Integer.toHexString(newValue);

                if (newStringHexValue.length() == 1) {
                    builder.append("0x0").append(newStringHexValue).append("00");
                    newValue = Integer.decode(builder.toString());
                } else if (newStringHexValue.length() == 2) {
                    builder.append("0x").append(newStringHexValue).append("00");
                    newValue = Integer.decode(builder.toString());
                } else if (newStringHexValue.length() == 3) {
                    StringBuilder tempstr = builder.append("0").append(newStringHexValue);
                    String lastBits = tempstr.substring(0,2);
                    String firstBits = tempstr.substring(2);
                    tempstr.delete(0, 4);
                    newValue = Integer.decode(String.valueOf(tempstr.append("0x").append(firstBits).append(lastBits)));
                } else if (newStringHexValue.length() == 4) {
                    StringBuilder tempstr = builder.append(newStringHexValue);
                    String lastBits = tempstr.substring(0, 2);
                    String firstBits = tempstr.substring(2);
                    tempstr.delete(0,4);
                    newValue = Integer.decode(String.valueOf(tempstr.append("0x").append(firstBits).append(lastBits)));
                }

                UUID uuid = rightService.getCharacteristics().get(1).getUuid();
                rightService.getCharacteristics().get(1).setValue(newValue, BluetoothGattCharacteristic.FORMAT_UINT16, 0);
                gatt.writeCharacteristic(rightService.getCharacteristic(uuid));
            }

            // Writing beacon name
            if (chars.size() == 2) {
                String newValue = (String) arrayOfNewChars[0];

                UUID uuid = rightService.getCharacteristics().get(8).getUuid();
                rightService.getCharacteristics().get(8).setValue(newValue);
                gatt.writeCharacteristic(rightService.getCharacteristic(uuid));
            }

            // Writing beacon soft reboot
            if (chars.size() == 1) {
                String password = "Bo0b(!*)";
                byte[] bytePassword = password.getBytes();
                UUID uuid = rightService.getCharacteristics().get(11).getUuid();
                rightService.getCharacteristics().get(11).setValue(bytePassword);
                gatt.writeCharacteristic(rightService.getCharacteristic(uuid));
            }


        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if (status == 0) {
                if (chars.size() == 6) {
                    UPDATED_POWER_FLAG = 1;
                    writeCharacteristics(gatt);
                    Log.v("CHARACTERISTIC 1: ", "Success");
                }
                else if (chars.size() == 5) {
                    UPDATED_INTERVAL_FLAG = 1;
                    writeCharacteristics(gatt);
                    Log.v("CHARACTERISTIC 2: ", "Success");
                }
                else if (chars.size() == 4) {
                    UPDATED_MINOR_FLAG = 1;
                    writeCharacteristics(gatt);
                    Log.v("CHARACTERISTIC 3: ", "Success");
                }
                else if (chars.size() == 3) {
                    UPDATED_MAJOR_FLAG = 1;
                    writeCharacteristics(gatt);
                    Log.v("CHARACTERISTIC 4: ", "Success");
                }
                else if (chars.size() == 2) {
                    UPDATED_NAME_FLAG = 1;
                    writeCharacteristics(gatt);
                    Log.v("CHARACTERISTIC 5: ", "Success");
                }
                else if (chars.size() == 1) {
                    UPDATED_REBOOT_FLAG = 1;
                    writeCharacteristics(gatt);
                    Log.v("CHARACTERISTIC 6: ", "Success");
                }
                else if (chars.size() == 0) {
                    gatt.disconnect();
                }

                if (chars.size() != 0) {
                    chars.remove(chars.get(chars.size()-1));
                }

            }

        }
    };


    //
    // Broadcasting Interval Gatt Callback
    //
    public static final BluetoothGattCallback readGattCallback = new BluetoothGattCallback() {

        List<BluetoothGattCharacteristic> chars = new ArrayList<>();

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {

            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.v(TAG, "Connected!");
                broadcastingInterval = 999;
                transmissionPower = 999;
                gatt.discoverServices();
            }
            if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.v(TAG, "Disconnected...");

            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {

            List<BluetoothGattService> services = gatt.getServices();
            BluetoothGattService rightService = null;

            for (int i = 0; i < services.size(); i++) {
                if (services.get(i).getCharacteristics().size() > 8) {
                    rightService = services.get(i);
                }
            }

            chars.add(rightService.getCharacteristics().get(4));
            chars.add(rightService.getCharacteristics().get(6));

            requestCharacteristics(gatt);

        }

        public void requestCharacteristics(BluetoothGatt gatt) {
            gatt.readCharacteristic(chars.get(chars.size()-1));
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if (status == 0) {

                if (characteristic.getUuid().toString().substring(7, 8).equals("5")) {
                    transmissionPower = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0);
                    Log.v(TAG, "tPOWER READ");

                } else if (characteristic.getUuid().toString().substring(7,8).equals("7")) {
                    broadcastingInterval = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0);
                    Log.v(TAG, "INTERVAL READ");
                }

                chars.remove(chars.get(chars.size() - 1));

                if (chars.size() > 0) {
                    requestCharacteristics(gatt);
                } else {
                    gatt.disconnect();
                }
            }
        }

    };


    //
    // Getting Major and Minor numbers
    //
    public int[] getMajorMinorPower(byte[] scanRecord) {
        int[] toReturn = new int[2];
        int startByte = 2;
        boolean patternFound = false;
        while (startByte <= 5) {
            if (    ((int) scanRecord[startByte + 2] & 0xff) == 0x02 && //Identifies an iBeacon
                    ((int) scanRecord[startByte + 3] & 0xff) == 0x15) { //Identifies correct data length
                patternFound = true;
                break;
            }
            startByte++;
        }
        if (patternFound) {

            int major = (scanRecord[startByte+20] & 0xff) * 0x100 + (scanRecord[startByte+21] & 0xff);
            int minor = (scanRecord[startByte+22] & 0xff) * 0x100 + (scanRecord[startByte+23] & 0xff);
            toReturn[0] = major;
            toReturn[1] = minor;

            return toReturn;

        } else {

            toReturn[0] = 0;
            toReturn[1] = 0;

            return toReturn;

        }
    }


}
