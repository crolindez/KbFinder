package es.carlosrolindez.kbfinder;

import java.util.ArrayList;

import android.os.Parcel;
import android.os.Parcelable;




public class SettingsClass implements Parcelable {

	private ArrayKBdeviceSettings listKBdeviceSettings;

	public SettingsClass ( ) {
		listKBdeviceSettings = new ArrayKBdeviceSettings();
	};
	
	public class KBdeviceSettings {
		private String MAC;
		private ArrayFmPackage fmPack;
		
		public KBdeviceSettings(String MAC) {
			this.MAC = MAC;
			fmPack = new ArrayFmPackage();
		}
	};
	
	
	public class FmSet {
		private String frequency;
		private boolean forcedMono;
		private String rds;
		
		public FmSet(String freq, boolean mono, String RDS ) {
			frequency = freq;
			forcedMono = mono;
			rds = RDS;
		}
	}
		

	
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
	
	

	public class ArrayFmPackage extends ArrayList<FmSet> {
		
		public boolean addSorted(FmSet newStation) {
			if (isEmpty()) {
				add(newStation);
				return true;
			}
			int position=0;
			for (FmSet station : this) {
				if (newStation.frequency.compareTo(station.frequency)<0) {
					add(position, newStation);
					return true;
				} else if (newStation.frequency.compareTo(station.frequency)==0) {
					station.frequency = newStation.frequency;
					station.forcedMono = newStation.forcedMono;
					station.rds = newStation.rds;
					return true;
				}
				position++;
			}
			add(newStation);
			return true;
		}

	}	
	
	@Override
    public int describeContents() {
        return 0;
    }
	
    @Override
    public void writeToParcel(Parcel parcel, int arg1) {
    	
    	parcel.writeInt(listKBdeviceSettings.size());
    	for (KBdeviceSettings deviceSettings:listKBdeviceSettings) {
       
            parcel.writeString(deviceSettings.MAC);
            
        	parcel.writeInt(deviceSettings.fmPack.size());
        	for (FmSet set:deviceSettings.fmPack) {
                parcel.writeString(set.frequency);
                parcel.writeString(set.rds);
            	boolean[] boolArraySet={set.forcedMono};
                parcel.writeBooleanArray(boolArraySet);	
        	}		
    	}
    }
     
    public static final Parcelable.Creator<SettingsClass> CREATOR = new Creator<SettingsClass>() {
    	boolean[] boolArray;
    	String freq;
    	String rds;
    	String mac;
    	int numberKBdevices;
    	int numberFmSets;
    	FmSet set;
    	KBdeviceSettings device;

    	
		
    	@Override
        public SettingsClass createFromParcel(Parcel parcel) {

    		SettingsClass settings = new SettingsClass();
        	for (numberKBdevices = parcel.readInt();numberKBdevices>0;numberKBdevices--) {
        		mac = parcel.readString();
        		device = settings.new KBdeviceSettings(mac);
	        	for (numberFmSets = parcel.readInt();numberFmSets>0;numberFmSets--) {
	        		freq = parcel.readString();
	        		rds = parcel.readString();
	        		parcel.readBooleanArray(boolArray);  
	        		set = settings.new FmSet(freq,boolArray[0],rds);
	        		device.fmPack.addSorted(set);
	        	}
	        	settings.listKBdeviceSettings.addSorted(device);
        	}
            return settings;
            
        }
 
        @Override
        public SettingsClass[] newArray(int size) {
            return new SettingsClass[size];
        }
    };

}
