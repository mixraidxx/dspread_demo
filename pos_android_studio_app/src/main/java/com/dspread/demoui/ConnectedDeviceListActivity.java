package com.dspread.demoui;

import java.util.ArrayList;
import java.util.Set;

import com.dspread.demoui.R;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;


public class ConnectedDeviceListActivity extends Activity{
    // Debugging
    private static final String TAG = "ConnectedDeviceListActivity";
    private static final boolean D = true;
    // Return Intent extra
    public static String EXTRA_CONNECTED_DEVICE= "connected_device";
    public static String EXTRA_CONNECTED_ADDRESS = "connected_socket_address";
    
    // Member fields
  //  private BluetoothAdapter mBtAdapter;
    private ArrayAdapter<String> mConnectedDeviceArrayAdapter;
    private boolean noConnected = false;
	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Setup the window
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        try{
        setContentView(R.layout.connected_device_list);
        }catch(IllegalStateException e){
        	e.printStackTrace();
        }
     // Set result CANCELED in case the user backs out
        setResult(Activity.RESULT_CANCELED);
     // Initialize array adapters. One for already paired devices and
        // one for newly discovered devices
        mConnectedDeviceArrayAdapter = new ArrayAdapter<String>(this, R.layout.device_name);
     // Find and set up the ListView for paired devices
        ListView connectedListView = (ListView) findViewById(R.id.connected_devices);
        connectedListView.setAdapter(mConnectedDeviceArrayAdapter);
        connectedListView.setOnItemClickListener(mDeviceClickListener);
    //    IntentFilter filter = new IntentFilter();
    //    this.registerReceiver(mReceiver, filter);
        Bundle bundle = getIntent().getBundleExtra("bundle");
        ArrayList<CharSequence> list = bundle.getCharSequenceArrayList("list");
        if (list.size()>0){
        	findViewById(R.id.title_connected_devices).setVisibility(View.VISIBLE);
        	for(CharSequence socket:list){
        		mConnectedDeviceArrayAdapter.add(socket.toString());
        	}
        }else{
        	noConnected = true;
        	String noConnectedDevice = getResources().getText(R.string.not_connected).toString();
        	mConnectedDeviceArrayAdapter.add(noConnectedDevice);
        }
	}
//	@Override
//	protected void onDestroy() {
//		// TODO Auto-generated method stub
//		super.onDestroy();
//	}
    
	private OnItemClickListener mDeviceClickListener = new OnItemClickListener() {
		
		public void onItemClick(AdapterView<?> arg0, View v, int position, long id) {
			if (D)
				Log.i(TAG, "[mDeviceClickListener] choosed position: " + position);
			if (noConnected) {
				try {
					Intent intent = new Intent();
					intent.putExtra(EXTRA_CONNECTED_ADDRESS, "no_devices");
					setResult(Activity.RESULT_OK, intent);
				} catch (IllegalStateException e) {
					if (D)
						Log.e(TAG, "[mDeviceClickListener] IllegalStateException");
					e.printStackTrace();
				}

				finish();
			} else {
				try {
					String info = ((TextView) v).getText().toString();
					String address = info.substring(info.length() - 17);
					Intent intent = new Intent();
					intent.putExtra(EXTRA_CONNECTED_ADDRESS, address);
					setResult(Activity.RESULT_OK, intent);
				} catch (IllegalStateException e) {
					if (D)
						Log.e(TAG, "[mDeviceClickListener] IllegalStateException");
					e.printStackTrace();
				}

				finish();
			}
		}
		
	};
    
}
