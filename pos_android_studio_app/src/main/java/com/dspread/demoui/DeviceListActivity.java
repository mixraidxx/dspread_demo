/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dspread.demoui;

import java.util.Set;

import com.dspread.demoui.R;


import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
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
public class DeviceListActivity extends Activity {
	private static final String TAG = "DeviceListActivity";
	private static final boolean D = true;

	public static String EXTRA_DEVICE_ADDRESS = "device_address";

	private BluetoothAdapter mBtAdapter;
	private ArrayAdapter<String> mPairedDevicesArrayAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.device_list);

		setResult(Activity.RESULT_CANCELED);

		mPairedDevicesArrayAdapter = new ArrayAdapter<String>(this, R.layout.device_name);

		ListView pairedListView = (ListView) findViewById(R.id.paired_devices);
		pairedListView.setAdapter(mPairedDevicesArrayAdapter);
		pairedListView.setOnItemClickListener(mDeviceClickListener);

		mBtAdapter = BluetoothAdapter.getDefaultAdapter();

		Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();

		if (pairedDevices.size() > 0) {
			mPairedDevicesArrayAdapter.add(getResources().getString(R.string.audio));
			mPairedDevicesArrayAdapter.add(getResources().getString(R.string.serialport));
			findViewById(R.id.title_paired_devices).setVisibility(View.VISIBLE);
			for (BluetoothDevice device : pairedDevices) {
				mPairedDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
			}
			
		} else {
			//添加音频和串口
			mPairedDevicesArrayAdapter.add(getResources().getString(R.string.audio));
			mPairedDevicesArrayAdapter.add(getResources().getString(R.string.serialport));
		}
		mPairedDevicesArrayAdapter.add(getResources().getString(R.string.scan_bt_device));
		
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		if (mBtAdapter != null) {
			mBtAdapter.cancelDiscovery();
		}

	}

	private void doDiscovery() {
		if (D)
			Log.d(TAG, "doDiscovery()");

		setProgressBarIndeterminateVisibility(true);
		setTitle(R.string.scanning);

		findViewById(R.id.title_new_devices).setVisibility(View.VISIBLE);

		if (mBtAdapter.isDiscovering()) {
			mBtAdapter.cancelDiscovery();
		}

		mBtAdapter.startDiscovery();
	}

	private OnItemClickListener mDeviceClickListener = new OnItemClickListener() {
		public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
			mBtAdapter.cancelDiscovery();

			int count = mPairedDevicesArrayAdapter.getCount();
			String info = ((TextView) v).getText().toString();
			String address = "";
			if(arg2>=2 && arg2 != (count-1)){
				address = info.substring(info.length() - 17);
			}
			Intent intent = new Intent();
			intent.putExtra(EXTRA_DEVICE_ADDRESS, address);
			intent.putExtra("index", arg2);
			setResult(Activity.RESULT_OK, intent);
			finish();
		}
	};

}
