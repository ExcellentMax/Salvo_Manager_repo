package phasefour.salvomanager;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.ArrayList;

public class ScanActivity extends Activity implements BluetoothAdapter.LeScanCallback {

    public static String TAG = "DEBUGGING";
    private BluetoothAdapter mBluetoothAdapter;                                                     // Bluetooth adapter
    private Button startScanButton;                                                                 // Start scan button
    private Button stopScanButton;                                                                  // Stop scan button
    private ArrayList<Item> items;
    private ArrayList<Item> duplicateItems;
    private customAdapter adapter;
    private ListView listView;
    private Handler handler = new Handler();
    private Handler tempHandler = new Handler();
    private ProgressBar menuPBar;
    private int REFRESH_FLAG = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        isNetworkAvailable();
        initAdapter();
        initComponents();
        startLeScan();

        tempHandler.post(tempRefreshList);
        handler.post(refreshList);
    }


    //
    // Refreshing the list every 2 seconds
    //
    public Runnable refreshList = new Runnable() {
        @Override
        public void run() {
            handler.postDelayed(refreshList, 2000);
            stopLeScan();
            sortListByName();
            startLeScan();
        }
    };


    //
    // Initializing UI components
    //
    private void initComponents() {

        startScanButton = (Button) findViewById(R.id.scanButton);
        stopScanButton = (Button) findViewById(R.id.stopButton);
        menuPBar = (ProgressBar) findViewById(R.id.menuProgressBar);
        menuPBar.setVisibility(View.VISIBLE);
        startScanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adapter.clear();
                REFRESH_FLAG = 0;
                tempHandler.post(tempRefreshList);
                startLeScan();
                startScanButton.setVisibility(View.GONE);
                stopScanButton.setVisibility(View.VISIBLE);
                handler.post(refreshList);
                menuPBar.setVisibility(View.VISIBLE);
            }
        });
        stopScanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopLeScan();
                tempHandler.removeCallbacks(tempRefreshList);
                stopScanButton.setVisibility(View.GONE);
                startScanButton.setVisibility(View.VISIBLE);
                handler.removeCallbacks(refreshList);
                menuPBar.setVisibility(View.GONE);
            }
        });
        items = new ArrayList<Item>();
        duplicateItems = new ArrayList<Item>();
        listView = (ListView) findViewById(R.id.listView);
        adapter = new customAdapter(this, items);
        listView.setAdapter(adapter);

    }


    //
    // Removing lost beacons
    //
    private void sortListByName() {

        for (int i = 0; i < items.size(); i++) {
            if (!(duplicateItems.contains(items.get(i)))) {
                items.remove(i);
            }
        }
        adapter.notifyDataSetChanged();
        duplicateItems.clear();

    }


    //
    // Refresh the list for the first 2 seconds
    //
    public Runnable tempRefreshList = new Runnable() {
        @Override
        public void run() {
            class tempListRefresh extends AsyncTask<Void, Void, Integer> {
                @Override
                protected Integer doInBackground(Void... params) {
                    try {
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                if (REFRESH_FLAG == 5) {
                                    tempHandler.removeCallbacks(refreshList);
                                } else {
                                    adapter.notifyDataSetChanged();
                                    REFRESH_FLAG++;
                                }
                            }
                        });
                    } catch (Exception e) {
                    }
                    return 0;
                }
            }
            new tempListRefresh().execute();
            tempHandler.postDelayed(tempRefreshList, 200);
        }
    };



    /*
    *
    *
    Methods for managing Bluetooth and internet access
    *
    *
    */


    //
    // Checking for internet connection
    //
    private void isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        if (activeNetworkInfo == null || !(activeNetworkInfo.isConnected())) {
            Toast.makeText(this, "No internet access", Toast.LENGTH_SHORT).show();
        }
    }


    //
    // Initializing the adapter
    //
    private void initAdapter() {
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        checkForBluetooth();
    }


    //
    // Checking if Bluetooth is on
    //
    private void checkForBluetooth() {
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, 1);
        }
    }


    public class HandleScan extends ScanActivity{

        private BluetoothAdapter mBluetoothAdapter;
        private Context mContext;

        public HandleScan(BluetoothAdapter adapter, Context context) {
            this.mBluetoothAdapter = adapter;
            this.mContext = context;
        }

        public boolean startLeScan() {
            mBluetoothAdapter.startLeScan(this);
            return true;
        }

        public void stopLeScan() {
            mBluetoothAdapter.stopLeScan(this);
        }

    }


    //
    // Starting a scan
    //
    public boolean startLeScan() {
        mBluetoothAdapter.startLeScan(this);
        return true;
    }


    //
    // Stopping a scan
    //
    public void stopLeScan() {
        mBluetoothAdapter.stopLeScan(this);
    }


    //
    // Reacting to item found
    //
    @Override
    public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {

        Item newItem = new Item(device, scanRecord);
        if (!(items.contains(newItem))) {
            items.add(newItem);
        }
        duplicateItems.add(newItem);

    }


    @Override
    protected void onPause() {
        super.onPause();
        stopLeScan();
        handler.removeCallbacks(refreshList);
        menuPBar.setVisibility(View.GONE);
        stopScanButton.setVisibility(View.GONE);
        startScanButton.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopLeScan();
        handler.removeCallbacks(refreshList);
        menuPBar.setVisibility(View.GONE);
        stopScanButton.setVisibility(View.GONE);
        startScanButton.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopLeScan();
        handler.removeCallbacks(refreshList);
        menuPBar.setVisibility(View.GONE);
        stopScanButton.setVisibility(View.GONE);
        startScanButton.setVisibility(View.VISIBLE);
    }
}
