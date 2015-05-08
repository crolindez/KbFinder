package es.carlosrolindez.kbfinder;

import java.util.ArrayList;
import java.util.Set;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.IBluetooth;
import android.bluetooth.IBluetoothA2dp;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

public class A2dpService {
	private static String TAG = "A2DP Service";

	private static IBluetoothA2dp iBtA2dp = null;
	private static IBluetooth iBt = null;
	
	private static Context mContextBt;
	private static Context mContextBtA2dp;
	
	private static boolean mBtIsBound = false;
	private static boolean mBtA2dpIsBound = false;
	
	public static ArrayList<KBdevice> deviceList = new ArrayList<KBdevice>();
	
	

	public static void searchBtPairedNames(Context context) {
		Intent intent = new Intent(IBluetooth.class.getName());
		mContextBt = context;
		if (!mBtIsBound) {
			if (mContextBt.bindService(intent, mBtServiceConnection, Context.BIND_AUTO_CREATE)) {

			} else {
				Log.e(TAG, "Could not bind to Bluetooth Service");
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
					KBdevice kbdevice = new KBdevice(currentName,device.getAddress());
					if (kbdevice.deviceType != KBdevice.OTHER)
						deviceList.add(kbdevice);
					
				}
			}
		}
		
		Intent intent = new Intent();
		intent.setAction(Constants.NameFilter);
		mContextBt.sendBroadcast(intent);
	}

	public static void startA2dp(Context context) {

		mContextBtA2dp = context;
		Intent i = new Intent(IBluetoothA2dp.class.getName());

		if (context.bindService(i, mBtA2dpServiceConnection, Context.BIND_AUTO_CREATE)) {

		} else {
			Log.e(TAG, "Could not bind to Bluetooth A2dp Service");
		}

	}

	private static void sendA2dpConnection() {
		Intent intent = new Intent();
		intent.setAction(Constants.a2dpFilter);
		mContextBtA2dp.sendBroadcast(intent);
	};

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

}

