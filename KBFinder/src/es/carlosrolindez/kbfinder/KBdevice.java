package es.carlosrolindez.kbfinder;

import java.util.ArrayList;

import android.bluetooth.BluetoothDevice;




public class KBdevice  {
	
    //  BT device type
	private static String TAG = "KBdevice"; 
	
    public static final int OTHER = 0;
    public static final int ISELECT = 1;
    public static final int IN_WALL = 2;
    public static final int SELECTBT = 3;
    
    private static final String iSelectFootprint = "00:08:F4";
    private static final String inWalltFootprint = "00:0D:18";
    private static final String selectBtFootprint = "8C:DE:52";
    
	public int deviceType;
	public String deviceName;
	public String deviceMAC;
	public boolean connected;
	BluetoothDevice mDevice;

	
	public KBdevice () 	{
		deviceType = OTHER;
		deviceName = "";
		deviceMAC = "";
		connected = false;
		mDevice = null;
	}
	
	public KBdevice(String name, BluetoothDevice device) {
		deviceName = name;
		deviceMAC = device.getAddress();
		connected = false;
		mDevice = device;

		deviceType = getDeviceType(deviceMAC);


	}
	
	public static BluetoothDevice deviceInArray(ArrayList<KBdevice> deviceList, String MAC) {
		for (KBdevice device : deviceList)
		{
			if (MAC.equals(device.deviceMAC)) return device.mDevice;
		}
		return null;
	}
	
	private int getDeviceType(String deviceMAC) {
		String MAC = deviceMAC.substring(0,8);
		if (MAC.equals(iSelectFootprint)) return ISELECT;
		if (MAC.equals(inWalltFootprint)) return IN_WALL;
		if (MAC.equals(selectBtFootprint)) return SELECTBT;
		return OTHER;
		
	}
	
	public static void connectDeviceInArray(String MAC,ArrayList<KBdevice> deviceList) {
		for (KBdevice device : deviceList)
		{
			if (MAC.equals(device.deviceMAC)) {
				device.connected = true;
				return;
			}
		}	
	}
	
	public static void disconnectDevices(String MAC,ArrayList<KBdevice> deviceList) {
		for (KBdevice device : deviceList)
		{
			if (MAC.equals(device.deviceMAC)) {
				device.connected = false;
				return;
			}
		}	
	}



}
