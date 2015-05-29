package es.carlosrolindez.kbfinder;

import java.util.List;

import android.app.Activity;
import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

public class KBfinderActivity extends Activity  implements KBdeviceListAdapter.ConnectOnClick {
	private static String TAG = "KBfinder";
	
	private boolean namesReceiverRegistered = false;
	private boolean a2dpReceiverRegistered = false;

    private BluetoothAdapter mBluetoothAdapter = null;
    
    private KBdeviceListAdapter deviceListAdapter = null;
    
    private String connectingMAC;
 
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.activity_main);
		
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

        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, Constants.REQUEST_ENABLE_BT);
        };

    }
    
    @Override
    public void onResume() {
        super.onResume();

		if (!namesReceiverRegistered) {
			IntentFilter filter1 = new IntentFilter(Constants.NameFilter);
			IntentFilter filter2 = new IntentFilter(BluetoothDevice.ACTION_FOUND);			
			IntentFilter filter3 = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);			
	        IntentFilter filter4 = new IntentFilter(BluetoothDevice.ACTION_ACL_CONNECTED);
	        IntentFilter filter5 = new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED);	
	        IntentFilter filter6 = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);	    

	        this.registerReceiver(mBtReceiver, filter1);
	        this.registerReceiver(mBtReceiver, filter2);
	        this.registerReceiver(mBtReceiver, filter3);	  
	        this.registerReceiver(mBtReceiver, filter4);	
	        this.registerReceiver(mBtReceiver, filter5);	
	        this.registerReceiver(mBtReceiver, filter6);	

			
	        namesReceiverRegistered = true;
			
	        A2dpService.searchBtPairedNames(this);
		}
		
    }

    
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode) {
            case Constants.REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {

                } else {
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
			mBluetoothAdapter.cancelDiscovery();
			KBdevice device = (KBdevice)parent.getItemAtPosition(position);			

			if (device.mDevice.getBondState() != BluetoothDevice.BOND_BONDED)
				device.mDevice.createBond();
			else
				connectBluetoothA2dp(device.deviceMAC);
				
    	}
	};
  
		
	@Override
	protected void onDestroy() {
		a2dpDone();
		
		if (namesReceiverRegistered) {
			unregisterReceiver(mBtReceiver);
			namesReceiverRegistered = false;
		}
		A2dpService.doUnbindServiceBt();
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
	
	public void connectBluetoothA2dp(String deviceMAC) {
		connectingMAC = deviceMAC;
		
		if (!a2dpReceiverRegistered) {
			IntentFilter filter1 = new IntentFilter(Constants.a2dpFilter);
			registerReceiver(mA2dpReceiver, filter1);
			a2dpReceiverRegistered = true;
		}
		A2dpService.startA2dp(this);

	}
	
	BluetoothProfile.ServiceListener mProfileListener = new BluetoothProfile.ServiceListener() {
        public void onServiceConnected(int profile, BluetoothProfile proxy) {
            if (profile == BluetoothProfile.A2DP) {
                BluetoothA2dp btA2dp = (BluetoothA2dp) proxy;
                List<BluetoothDevice> a2dpConnectedDevices = btA2dp.getConnectedDevices();
                if (a2dpConnectedDevices.size() != 0) {
                    for (BluetoothDevice a2dpDevice : a2dpConnectedDevices) {
                        KBdevice.connectDeviceInArray(a2dpDevice.getAddress(),A2dpService.deviceList);
         				deviceListAdapter.notifyDataSetChanged();
                    }
                }
                mBluetoothAdapter.closeProfileProxy(BluetoothProfile.A2DP, btA2dp);
            }
        }

        public void onServiceDisconnected(int profile) {
        }
    };


	

	private final BroadcastReceiver mBtReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                
				KBdevice kbdevice = new KBdevice(device.getName(), device);
            	if (KBdevice.deviceInArray(A2dpService.deviceList, device.getAddress())!=null) return;
				if (kbdevice.deviceType == KBdevice.OTHER) return;
				A2dpService.deviceList.add(kbdevice);
				deviceListAdapter.notifyDataSetChanged();
					
            // When discovery is finished, change the Activity title
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                setProgressBarIndeterminateVisibility(false);
            
            } else if (Constants.NameFilter.equals(action)) {
				ListView list = (ListView)findViewById(R.id.list);  
				deviceListAdapter = new KBdeviceListAdapter(context, A2dpService.deviceList , list, (KBfinderActivity)context);
				list.setAdapter(deviceListAdapter);
				list.setOnItemClickListener(onItemClickListener);  
				mBluetoothAdapter.getProfileProxy(context, mProfileListener, BluetoothProfile.A2DP);
				
			} else if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);    
                KBdevice.connectDeviceInArray(device.getAddress(),A2dpService.deviceList);
                deviceListAdapter.notifyDataSetChanged();
                Toast.makeText(getApplicationContext(), device.getName() + " Connected", Toast.LENGTH_SHORT).show();

                if (KBdevice.getDeviceType(device.getAddress()) == KBdevice.IN_WALL) {
	                new CountDownTimer(2000, 1000) {
	                	AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
	    				@Override
	    				public void onFinish() {
	    					am.setStreamVolume(AudioManager.STREAM_MUSIC, (int)(am.getStreamMaxVolume(AudioManager.STREAM_MUSIC) *0.6),	0);
	    				}
	    				@Override
	    				public void onTick(long millisUntilFinished) {
	    				}
	    			}.start();
                } else if (KBdevice.getDeviceType(device.getAddress()) == KBdevice.ISELECT) {
	                new CountDownTimer(2000, 1000) {
	                	AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
	    				@Override
	    				public void onFinish() {
	    					am.setStreamVolume(AudioManager.STREAM_MUSIC, (int)(am.getStreamMaxVolume(AudioManager.STREAM_MUSIC) * 0.9), 0);
	    				}
	    				@Override
	    				public void onTick(long millisUntilFinished) {
	    				}
	    			}.start();
                }
	
            } else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);  
                KBdevice.disconnectDevices(device.getAddress(),A2dpService.deviceList);
                Toast.makeText(getApplicationContext(), device.getName() + " A2dp Disconnected", Toast.LENGTH_SHORT).show();
				deviceListAdapter.notifyDataSetChanged();
            } else if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device.getBondState()==BluetoothDevice.BOND_BONDED) {
                	if ((KBdevice.getDeviceType(device.getAddress()) == KBdevice.IN_WALL) || (KBdevice.getDeviceType(device.getAddress()) == KBdevice.ISELECT)) {
                    	connectBluetoothA2dp(device.getAddress());  
	            	} else if (KBdevice.getDeviceType(device.getAddress()) == KBdevice.SELECTBT) {
	            		//disconnect current A2dp connection (if different to current device)
	        			String MAC = KBdevice.findConnectedDevice(A2dpService.deviceList);
	        			if ( (MAC!=null) && (!MAC.equals(device.getAddress())) )
	        				connectBluetoothA2dp(MAC);
	        			
	                   	Intent localIntent = new Intent (context, SelectBtActivity.class);
	                   	localIntent.putExtra(SelectBtActivity.LAUNCH_MAC, device.getAddress());        	
	                   	context.startActivity(localIntent);
	                }
                }
            }
		}

	};
	
	private final BroadcastReceiver mA2dpReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context arg0, Intent arg1) {
			new connectA2dpTask().execute(connectingMAC);
		}

	};

	private class connectA2dpTask extends AsyncTask<String, Void, Boolean> {


		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			a2dpDone();
		}

		protected void onPreExecute() {
		}

		@Override
		protected Boolean doInBackground(String... arg0) {
			
			BluetoothDevice device;

			BluetoothAdapter mBTA = BluetoothAdapter.getDefaultAdapter();
			if (mBTA == null || !mBTA.isEnabled())
				return false;
			if   ((device = KBdevice.deviceInArray(A2dpService.deviceList, arg0[0])) == null) {
				return false;
			}

			try {
				if ( (A2dpService.iBtA2dp != null) && (A2dpService.iBtA2dp.getConnectionState(device) == 0) ) {
					A2dpService.iBtA2dp.connect(device);
				} else {
					A2dpService.iBtA2dp.disconnect(device);
				}

			} catch (Exception e) {
			}
			return true;
		}

	}	

	
	/**
     * Start device discover with the BluetoothAdapter
     */
    private void doDiscovery() {

        // Indicate scanning in the title
        setProgressBarIndeterminateVisibility(true);

        // Request discover from BluetoothAdapter
        mBluetoothAdapter.startDiscovery();
    }
    
	private void a2dpDone() {
		if (a2dpReceiverRegistered) {
			unregisterReceiver(mA2dpReceiver);
			a2dpReceiverRegistered = false;
		}
		A2dpService.doUnbindServiceBtA2dp();

	}



}
