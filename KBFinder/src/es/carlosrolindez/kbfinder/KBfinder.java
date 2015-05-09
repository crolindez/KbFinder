package es.carlosrolindez.kbfinder;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

public class KBfinder extends Activity {
	private static String TAG = "Main Activity";
	
	private boolean namesReceiver = false;

    private BluetoothAdapter mBluetoothAdapter = null;
    
    private KBdeviceListAdapter deviceListAdapter = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.activity_main);

		
        Log.d(TAG, "onCreate");
		
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

		// If the adapter is null, then Bluetooth is not supported
		if (mBluetoothAdapter == null) {
		    Toast.makeText(this, getString(R.string.bt_not_availabe), Toast.LENGTH_LONG).show();
		    finish();
		}	
    }
	
    @Override
    public void onStart() {
        super.onStart();

        Log.d(TAG, "onStart");

        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, Constants.REQUEST_ENABLE_BT);
        } /*else if (mChatService == null) {
            setupChat();
        }*/
    }
    
    @Override
    public void onResume() {
        super.onResume();


        Log.d(TAG, "onResume");
        
		if (!namesReceiver) {
			IntentFilter filter1 = new IntentFilter(Constants.NameFilter);
			IntentFilter filter2 = new IntentFilter(BluetoothDevice.ACTION_FOUND);
	        IntentFilter filter3 = new IntentFilter(BluetoothDevice.ACTION_ACL_CONNECTED);
	        IntentFilter filter4 = new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED);
	        IntentFilter filter5 = new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED);

	        this.registerReceiver(mBtReceiver, filter1);
	        this.registerReceiver(mBtReceiver, filter2);
	        this.registerReceiver(mBtReceiver, filter3);	  
	        this.registerReceiver(mBtReceiver, filter4);	
	        this.registerReceiver(mBtReceiver, filter5);	

			
			namesReceiver = true;
			
	        A2dpService.searchBtPairedNames(this);
		}
		
    }

    
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult");

        switch (requestCode) {
            case Constants.REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {

                } else {
                    Log.d(TAG, "BT not enabled");
                    Toast.makeText(this, R.string.bt_not_enabled,Toast.LENGTH_SHORT).show();
                    finish();
                }
        }
    }


    
	OnItemClickListener onItemClickListener = new OnItemClickListener() 
	{
		@Override
    	public void onItemClick(AdapterView<?> parent, View view, int position, long id) 
    	{ 			
/*	    	Intent intent = new Intent (view.getContext(), InfoActivity.class);
	        Product product = (Product)parent.getItemAtPosition(position);
        	intent.putExtra(NavisionTool.LAUNCH_REFERENCE, product.reference);        	
        	intent.putExtra(NavisionTool.LAUNCH_DESCRIPTION, product.description);  
        	intent.putExtra(NavisionTool.LAUNCH_INFO_MODE, NavisionTool.INFO_MODE_SUMMARY);
        	startActivity(intent);*/
        	
           // Cancel discovery because it's costly and we're about to connect
			mBluetoothAdapter.cancelDiscovery();

    /*        // Get the device MAC address, which is the last 17 chars in the View
            String info = ((TextView) v).getText().toString();
            String address = info.substring(info.length() - 17);

            // Create the result Intent and include the MAC address
            Intent intent = new Intent();
            intent.putExtra(EXTRA_DEVICE_ADDRESS, address);

            // Set result and finish this Activity
            setResult(Activity.RESULT_OK, intent);
            finish();
            
        	*/
        	
//        	Toast.makeText(view.getContext(), "List Item", Toast.LENGTH_SHORT).show();ç
			
    	}
	};
  
		
	@Override
	protected void onDestroy() {
		try {
			if (namesReceiver) {
				unregisterReceiver(mBtReceiver);
				namesReceiver = false;
			}
			A2dpService.doUnbindServiceBt();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		super.onDestroy();
	}
		


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		switch (item.getItemId())
		{
	        case R.id.bt_scan: 
	            // Launch the DeviceListActivity to see devices and do scan
	        	doDiscovery();
	            return true;

			case R.id.action_settings:
                return true;
		}

		return super.onOptionsItemSelected(item);
	}
	
	BroadcastReceiver mBtReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                Log.e(TAG, "ACTION_FOUND");
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // If it's already paired, skip it, because it's been listed already
                /*if (device.getBondState() != BluetoothDevice.BOND_BONDED) */
                // TODO
                {
					KBdevice kbdevice = new KBdevice(device.getName(),device.getAddress());
                	if (kbdevice.deviceInArray(A2dpService.deviceList)) return;
					if (kbdevice.deviceType == KBdevice.OTHER) return;
					A2dpService.deviceList.add(kbdevice);
					deviceListAdapter.notifyDataSetChanged();
					
                }
            // When discovery is finished, change the Activity title
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                Log.e(TAG, "ACTION_DISCOVERY_FINISHED");
                setProgressBarIndeterminateVisibility(false);
            
            } else if (Constants.NameFilter.equals(action)) {
				deviceListAdapter = new KBdeviceListAdapter(context, A2dpService.deviceList);
				ListView list = (ListView)findViewById(R.id.list);  
				list.setAdapter(deviceListAdapter);
				list.setOnItemClickListener(onItemClickListener);  
			} else if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);    
                KBdevice.connectDeviceInArray(device.getAddress(),A2dpService.deviceList);
                Toast.makeText(getApplicationContext(), device.getName() + " Connected", Toast.LENGTH_SHORT).show();
				deviceListAdapter.notifyDataSetChanged();
            } else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);  
                KBdevice.disconnectDevices(A2dpService.deviceList);
                Toast.makeText(getApplicationContext(), device.getName() + " Disconnected", Toast.LENGTH_SHORT).show();
				deviceListAdapter.notifyDataSetChanged();
            }

		}

	};
	
    /**
     * Start device discover with the BluetoothAdapter
     */
    private void doDiscovery() {
        Log.e(TAG, "doDiscovery()");

        // Indicate scanning in the title
        setProgressBarIndeterminateVisibility(true);

        // Request discover from BluetoothAdapter
        mBluetoothAdapter.startDiscovery();
    }


}
