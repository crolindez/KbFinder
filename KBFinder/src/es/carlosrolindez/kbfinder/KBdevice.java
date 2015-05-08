package es.carlosrolindez.kbfinder;

import java.util.ArrayList;

import android.util.Log;



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

	
	public KBdevice () 	{
		deviceType = OTHER;
		deviceName = "";
		deviceMAC = "";
	}
	
	public KBdevice(String name, String MAC) {
		deviceName = name;
		deviceMAC = MAC;

		deviceType = getDeviceType(MAC);
		Log.e(TAG,MAC + ": " + deviceType);

	}
	
	public boolean deviceInArray(ArrayList<KBdevice> deviceList) {
		for (KBdevice device : deviceList)
		{
			if (deviceMAC.equals(device.deviceMAC)) return true;
		}
		return false;
	}
	
	private int getDeviceType(String deviceMAC) {
		String MAC = deviceMAC.substring(0,8);
		if (MAC.equals(iSelectFootprint)) return ISELECT;
		if (MAC.equals(inWalltFootprint)) return IN_WALL;
		if (MAC.equals(selectBtFootprint)) return SELECTBT;
		return OTHER;
		
	}

}
