package es.carlosrolindez.kbfinder;

import java.util.ArrayList;

@SuppressWarnings("serial")
public class ArrayKBdeviceSettings extends ArrayList<KBdeviceSettings> {
	
	public boolean addSorted(KBdeviceSettings newKbSettings) {
		if (isEmpty()) {
			add(newKbSettings);
			return true;
		}
		int position=0;
		for (KBdeviceSettings kbSettings : this) {
			if (newKbSettings.MAC.compareTo(kbSettings.MAC)<0) {
				add(position, newKbSettings);
				return true;
			} else if (newKbSettings.MAC.compareTo(kbSettings.MAC)==0) {
				kbSettings.MAC = newKbSettings.MAC;
				kbSettings.fmPack = newKbSettings.fmPack;
				return true;
			}
			position++;
		}
		add(newKbSettings);
		return true;
	}

}	