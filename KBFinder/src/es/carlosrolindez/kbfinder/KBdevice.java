package es.carlosrolindez.kbfinder;



public class KBdevice  {
	
    //  BT device type
	
    public static final int OTHER = 0;
    public static final int ISELECT = 1;
    public static final int IN_WALL = 2;
    public static final int SELECTBT = 3;
    
	public int deviceType;
	public String deviceName;
	public String deviceMAC;

	
	public KBdevice () 	{
		deviceType = OTHER;
		deviceName = "";
		deviceMAC = "";
	}
	
	public KBdevice(int type, String name, String MAC) {
		deviceType = type;
		deviceName = name;
		deviceMAC = MAC;
	}

}
