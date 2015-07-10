package es.carlosrolindez.kbfinder;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

import android.content.Context;
import android.util.Log;






public class SettingsClass {

	private ArrayKBdeviceSettings listKBdeviceSettings;
    private static final String TAG = "SettingsClass";
    private static Context mContext;

	public SettingsClass (Context context) {
		listKBdeviceSettings = new ArrayKBdeviceSettings();
		mContext = context;
	}

	
	public KBdeviceSettings getDeviceInArray(String MAC) {
		for (KBdeviceSettings device:listKBdeviceSettings) {
			if (MAC.equals(device.MAC)) return device;
		}
		KBdeviceSettings device = new KBdeviceSettings(MAC);
		listKBdeviceSettings.addSorted(device);
		return device;
	}
	

	public class KBdeviceSettings {
		private String MAC;
		private ArrayFmPackage fmPack;
		
		public KBdeviceSettings(String MAC) {
			this.MAC = MAC;
			fmPack = new ArrayFmPackage();
		}


		
		public FmSet getFreqInArray(String targetFreq) {
			for (FmSet set:fmPack) {
				if (targetFreq.equals(set.frequency)) return set;
			}
			return null;
		}
		
		public void addFreqFromArray(String targetFreq,String targetRDS) {
			FmSet set = new FmSet(targetFreq, targetRDS);
			fmPack.addSorted(set);
		}
		
		public void removeFreqFromArray(String targetFreq) {
			int counter = 0;
			for (FmSet set:fmPack) {
				if (targetFreq.equals(set.frequency)) {
					fmPack.remove(counter);
					return;
				} else {
					counter++;
				}
			}
		}


	}
	
	
	public class FmSet {
		private String frequency;
		private String rds;
		
		public FmSet(String freq, String RDS ) {
			frequency = freq;
			rds = RDS;
		}
		
		public String getRDS() {
			return rds;
		}
		
		public void setRDS(String rds) {
			this.rds = rds;
		}
	}
		

	
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
	
	
	@SuppressWarnings("serial")
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
					station.rds = newStation.rds;
					return true;
				}
				position++;
			}
			add(newStation);
			return true;
		}
	}	
	


    public void writeToFile(String filename) {
    	

    	try {	
    		Log.e(TAG,"1");
    		FileOutputStream outStream = mContext.openFileOutput(filename, Context.MODE_PRIVATE);
    		Log.e(TAG,"2");
    		OutputStreamWriter writer = new OutputStreamWriter(outStream, "UTF-8");
    		Log.e(TAG,"3");
//			OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(filename), "UTF-8");

	        writer.write(Integer.toString(listKBdeviceSettings.size())); writer.write("\n");
	      	for (KBdeviceSettings deviceSettings:listKBdeviceSettings) {
	      		writer.write(deviceSettings.MAC + "\n");
	      		writer.write(Integer.toString((deviceSettings.fmPack.size()))); writer.write("\n");
	      		Log.e(TAG,deviceSettings.MAC+" "+deviceSettings.fmPack.size());
	      		for (FmSet set:deviceSettings.fmPack) {
	      			writer.write(set.frequency + "\n");
	      			writer.write(set.rds + "\n");
		      		Log.e(TAG,set.frequency+" "+set.rds);
	      		}	      		
	      	}
	      	writer.flush();
	      	writer.close();   	
    	} catch (IOException e) {
      		Log.e(TAG,"error writing settings");
    	  e.printStackTrace();
    	}
    	
   }
     
    public static SettingsClass readFromFile(String filename,Context context) {
    	String freq;
    	String rds;
    	String mac;
    	int numberKBdevices;
    	int numberFmSets;
    	FmSet set;
    	KBdeviceSettings device;

		SettingsClass settings = new SettingsClass(context);
//		return settings;

        try {
//        	FileInputStream inputStream = new FileInputStream(filename);
        	FileInputStream inputStream = mContext.openFileInput(filename);           
            if ( inputStream != null ) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                
                if ( (receiveString = bufferedReader.readLine()) != null ) {
                	Log.e(TAG,"Device Number "+receiveString);
	               	for (numberKBdevices = Integer.parseInt(receiveString); numberKBdevices>0; numberKBdevices--) {
	                    mac = bufferedReader.readLine();
	            		device = settings.new KBdeviceSettings(mac);
	    	      		Log.e(TAG,device.MAC);
	                    if ( (receiveString = bufferedReader.readLine()) != null ) {
	                    	Log.e(TAG,"FM station Number "+receiveString);
		    	        	for (numberFmSets = Integer.parseInt(receiveString); numberFmSets>0; numberFmSets--) {
		    	        		freq = bufferedReader.readLine();
		    	        		rds = bufferedReader.readLine();
		    	        		set = settings.new FmSet(freq,rds);
		    	        		device.fmPack.addSorted(set);
			    	      		Log.e(TAG,set.frequency+" "+set.rds);
		    	        	}
		    	        	settings.listKBdeviceSettings.addSorted(device);
		            	}
	               	}
                }
                bufferedReader.close();
            }
            inputStream.close();
        }
        catch (FileNotFoundException e) {   
            Log.e(TAG, "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e(TAG, "Can not read file: " + e.toString());
        }

        return settings;

    };

}
