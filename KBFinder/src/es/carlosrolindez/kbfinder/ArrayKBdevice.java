package es.carlosrolindez.kbfinder;

import java.util.ArrayList;

@SuppressWarnings("serial")
public class ArrayKBdevice extends ArrayList<KBdevice> {
	
	public boolean addSorted(KBdevice newDevice) {
		if (isEmpty()) {
			add(newDevice);
			return true;
		}
		int position=0;
		for (KBdevice device : this) {
			if (newDevice.deviceType>device.deviceType) {
				add(position, newDevice);
				return true;
			} else if (newDevice.deviceType==device.deviceType) {
				if (newDevice.deviceName.compareToIgnoreCase(device.deviceName)<=0) {
					add(position, newDevice);
					return true;
				}
			}
			position++;
		}
		add(newDevice);
		return true;
	}
	

}
