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
	
//    public static final UUID MY_UUID_SECURE = UUID.fromString("00000000-0000-1000-8000-00805f9b34fb");
    
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
	
	public static BluetoothDevice deviceInArray(ArrayKBdevice deviceList, String MAC) {
		for (KBdevice device : deviceList)
		{
			if (MAC.equals(device.deviceMAC)) return device.mDevice;
		}
		return null;
	}
	
	public static int getDeviceType(String deviceMAC) {
		String MAC = deviceMAC.substring(0,8);
		if (MAC.equals(iSelectFootprint)) return ISELECT;
		if (MAC.equals(inWalltFootprint)) return IN_WALL;
		if (MAC.equals(selectBtFootprint)) return SELECTBT;
		return OTHER;
		
	}
	
	public static void connectDeviceInArray(String MAC,ArrayKBdevice deviceList) {
		for (KBdevice device : deviceList)
		{
			if (MAC.equals(device.deviceMAC)) {
				device.connected = true;
				return;
			}
		}	
	}
	
	public static void disconnectDevices(String MAC,ArrayKBdevice deviceList) {
		for (KBdevice device : deviceList)
		{
			if (MAC.equals(device.deviceMAC)) {
				device.connected = false;
				return;
			}
		}	
	}

	public static boolean isDeviceConnected(String MAC,ArrayKBdevice deviceList) {
		for (KBdevice device : deviceList)
		{
			if (MAC.equals(device.deviceMAC)) {
				return device.connected;
			}
		}	
		return false;
	}
	

	public static long password(String MAC) {

		String[] macAddressParts = MAC.split(":");
		long littleMac = 0;
		int rotation;
		long code = 0;
		long pin;
		
		for(int i=2; i<6; i++) {
		    Long hex = Long.parseLong(macAddressParts[i], 16);
		    littleMac *= 256;
		    littleMac += hex;
		}
		
		rotation = Integer.parseInt(macAddressParts[5], 16) & 0x0f;
		
		for(int i=0; i<4; i++) {
			Long hex =  Long.parseLong(macAddressParts[i], 16);
		    code *= 256;
		    code += hex;
		}
		code = code >> rotation;		
		code &= 0xffff;

		littleMac &= 0xffff;

		pin = littleMac ^ code;
		pin %= 10000;
		
		return pin;
				
	}
	
	public static String findConnectedDevice(ArrayKBdevice deviceList) {
		for (KBdevice device : deviceList)
		{
			if (device.connected) {
				return device.deviceMAC;
			}
		}
		return null;
	}
	
}
