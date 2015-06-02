package es.carlosrolindez.kbfinder;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.IBluetooth;
import android.bluetooth.IBluetoothA2dp;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.os.RemoteException;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;


public class A2dpService {
	private static String TAG = "A2DP Service";

	public static IBluetoothA2dp iBtA2dp = null;
	private static IBluetooth iBt = null;
	
	private static Context mContextBt;
	private static Context mContextBtA2dp;
	
	private static boolean mBtIsBound = false;
	private static boolean mBtA2dpIsBound = false;
	
	private static boolean a2dpReceiverRegistered = false;
	
    private static String connectingMAC;
	
	public static ArrayList<KBdevice> deviceList = new ArrayList<KBdevice>();
	private static KBdeviceListAdapter deviceListAdapter = null;
	private static ListView mListView = null;
	
	
	public A2dpService(Context context, ListView listView) {
		
		mContextBt = context;
		mListView = listView;
		
		IntentFilter filter1 = new IntentFilter(Constants.NameFilter);
		IntentFilter filter2 = new IntentFilter(BluetoothDevice.ACTION_FOUND);			
		IntentFilter filter3 = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);			
        IntentFilter filter4 = new IntentFilter(BluetoothDevice.ACTION_ACL_CONNECTED);
        IntentFilter filter5 = new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED);	
        IntentFilter filter6 = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);	    

        context.registerReceiver(mBtReceiver, filter1);
        context.registerReceiver(mBtReceiver, filter2);
        context.registerReceiver(mBtReceiver, filter3);	  
        context.registerReceiver(mBtReceiver, filter4);	
        context.registerReceiver(mBtReceiver, filter5);	
        context.registerReceiver(mBtReceiver, filter6);	
	
		searchBtPairedNames(/*context*/);	

		}
		
	public static void searchBtPairedNames(/*Context context*/) {
		Intent intent = new Intent(IBluetooth.class.getName());
//		mContextBt = context;
		if (!mBtIsBound) {
			if (mContextBt.bindService(intent, mBtServiceConnection, Context.BIND_AUTO_CREATE)) {

			} else {
			}
		} else {
			sendNames();
		}
	}

	public static ServiceConnection mBtServiceConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			mBtIsBound = true;
			iBt = IBluetooth.Stub.asInterface(service);
			sendNames();
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			mBtIsBound = false;

		}

	};
	

	private static void sendNames() {
		BluetoothAdapter mBTA = BluetoothAdapter.getDefaultAdapter();

		if (mBTA != null) {
			Set<BluetoothDevice> pairedDevices = mBTA.getBondedDevices();
			deviceList.clear();

			if (pairedDevices.size() > 0) {
				
				for (BluetoothDevice device : pairedDevices) {
					String currentName = device.getName();
					try {
						currentName = iBt.getRemoteAlias(device);
					} catch (RemoteException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					if (currentName == null)
						currentName = device.getName();
					KBdevice kbdevice = new KBdevice(currentName,device);

					if (kbdevice.deviceType != KBdevice.OTHER)
						deviceList.add(kbdevice);
								
				}
			}
		}
		
		Intent intent = new Intent();
		intent.setAction(Constants.NameFilter);
		mContextBt.sendBroadcast(intent);
	}

	
	
	public static void connectBluetoothA2dp(Context context, String deviceMAC) {
		connectingMAC = deviceMAC;
		mContextBtA2dp = context;
		
		if (!a2dpReceiverRegistered) {
			IntentFilter filter1 = new IntentFilter(Constants.a2dpFilter);
			context.registerReceiver(mA2dpReceiver, filter1);
			a2dpReceiverRegistered = true;
		}
		startA2dp(/*context*/);

	}	
	
	public static void startA2dp(/*Context context*/) {

//		mContextBtA2dp = context;
		Intent i = new Intent(IBluetoothA2dp.class.getName());

		if (mContextBtA2dp.bindService(i, mBtA2dpServiceConnection, Context.BIND_AUTO_CREATE)) {

		} else {
		}

	}

	public static ServiceConnection mBtA2dpServiceConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {

			mBtA2dpIsBound = true;
			iBtA2dp = IBluetoothA2dp.Stub.asInterface(service);
			sendA2dpConnection();
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			mBtA2dpIsBound = false;

		}

	};

	private static void sendA2dpConnection() {
		Intent intent = new Intent();
		intent.setAction(Constants.a2dpFilter);
		mContextBtA2dp.sendBroadcast(intent);
	};

	
	
	public static void doUnbindServiceBt() {
		if (mBtIsBound) {
			try {
				mContextBt.unbindService(mBtServiceConnection);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}


	}
	
	public static void doUnbindServiceBtA2dp() {
		if (mBtA2dpIsBound) {
			try {
				mContextBtA2dp.unbindService(mBtA2dpServiceConnection);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
	}
	
	
	private static void a2dpDone() {
		if (a2dpReceiverRegistered) {
			mContextBtA2dp.unregisterReceiver(mA2dpReceiver);
			a2dpReceiverRegistered = false;
			doUnbindServiceBtA2dp();
		}
//		doUnbindServiceBtA2dp();

	}
	
	public static void closeService( ){
		a2dpDone();
	
		mContextBt.unregisterReceiver(mBtReceiver);
		doUnbindServiceBt();


	}
	
	private static final BroadcastReceiver mBtReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                
				KBdevice kbdevice = new KBdevice(device.getName(), device);
            	if (KBdevice.deviceInArray(deviceList, device.getAddress())!=null) return;
				if (kbdevice.deviceType == KBdevice.OTHER) return;
				deviceList.add(kbdevice);
				deviceListAdapter.notifyDataSetChanged();
					
            // When discovery is finished, change the Activity title
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
            	((KBfinderActivity)context).setProgressBarIndeterminateVisibility(false);
            
            } else if (Constants.NameFilter.equals(action)) {
				deviceListAdapter = new KBdeviceListAdapter(context, deviceList , mListView);
				mListView.setAdapter(deviceListAdapter);
				mListView.setOnItemClickListener(new OnItemClickListener() 
				{
					@Override
			    	public void onItemClick(AdapterView<?> parent, View view, int position, long id) 
			    	{ 	
						BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
						KBdevice device = (KBdevice)parent.getItemAtPosition(position);			

						if (device.mDevice.getBondState() != BluetoothDevice.BOND_BONDED)
							device.mDevice.createBond();
						else
							A2dpService.connectBluetoothA2dp(view.getContext(), device.deviceMAC);
							
			    	}
				});

				
				BluetoothAdapter.getDefaultAdapter().getProfileProxy(context, mProfileListener, BluetoothProfile.A2DP);

				
			} else if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);    
                KBdevice.connectDeviceInArray(device.getAddress(),A2dpService.deviceList);
                deviceListAdapter.notifyDataSetChanged();
                Toast.makeText(context, device.getName() + " Connected", Toast.LENGTH_SHORT).show();

                if (KBdevice.getDeviceType(device.getAddress()) == KBdevice.IN_WALL) {
	                new CountDownTimer(2000, 1000) {
	                	AudioManager am = (AudioManager) mContextBt.getSystemService(Context.AUDIO_SERVICE);
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
	                	AudioManager am = (AudioManager) mContextBt.getSystemService(Context.AUDIO_SERVICE);
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
                Toast.makeText(context, device.getName() + " A2dp Disconnected", Toast.LENGTH_SHORT).show();
				deviceListAdapter.notifyDataSetChanged();
            } else if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device.getBondState()==BluetoothDevice.BOND_BONDED) {
                	if ((KBdevice.getDeviceType(device.getAddress()) == KBdevice.IN_WALL) || (KBdevice.getDeviceType(device.getAddress()) == KBdevice.ISELECT)) {
                    	connectBluetoothA2dp(mContextBt, device.getAddress());  
	            	} else if (KBdevice.getDeviceType(device.getAddress()) == KBdevice.SELECTBT) {
	            		//disconnect current A2dp connection (if different to current device)
	        			String MAC = KBdevice.findConnectedDevice(A2dpService.deviceList);
	        			if ( (MAC!=null) && (!MAC.equals(device.getAddress())) )
	        				connectBluetoothA2dp(mContextBt, MAC);
	        			
	                   	Intent localIntent = new Intent (context, SelectBtActivity.class);
	                   	localIntent.putExtra(SelectBtActivity.LAUNCH_MAC, device.getAddress());        	
	                   	context.startActivity(localIntent);
	                }
                }
            }
		}

	};

	private static final BroadcastReceiver mA2dpReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context arg0, Intent arg1) {
			new connectA2dpTask().execute(connectingMAC);
		}

	};
	
	private static BluetoothProfile.ServiceListener mProfileListener = new BluetoothProfile.ServiceListener() {
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
                BluetoothAdapter.getDefaultAdapter().closeProfileProxy(BluetoothProfile.A2DP, btA2dp);
            }
        }

        public void onServiceDisconnected(int profile) {
        }
    };


	private static class connectA2dpTask extends AsyncTask<String, Void, Boolean> {


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

	

}

