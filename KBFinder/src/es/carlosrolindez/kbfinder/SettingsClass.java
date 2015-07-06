package es.carlosrolindez.kbfinder;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

import android.util.Log;






public class SettingsClass {

	private ArrayKBdeviceSettings listKBdeviceSettings;
    private static final String TAG = "SettingsClass";

	public SettingsClass () {
		listKBdeviceSettings = new ArrayKBdeviceSettings();
	}
	
	public class KBdeviceSettings {
		private String MAC;
		private ArrayFmPackage fmPack;
		
		public KBdeviceSettings(String MAC) {
			this.MAC = MAC;
			fmPack = new ArrayFmPackage();
		}
	}
	
	
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
	


    public void writeToFile(String filename) {
    	

    	try {	
			OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(filename), "UTF-8");
			
	        writer.write(listKBdeviceSettings.size()); writer.write("\n");
	      	for (KBdeviceSettings deviceSettings:listKBdeviceSettings) {
	      		writer.write(deviceSettings.MAC + "\n");
	      		writer.write(deviceSettings.fmPack.size()); writer.write("\n");
	      		for (FmSet set:deviceSettings.fmPack) {
	      			writer.write(set.frequency + "\n");
	      			writer.write(set.rds + "\n");
	      			if (set.forcedMono) writer.write("MONO\n");
	      			else writer.write("STEREO\n");
	      		}	      		
	      	}
	      	writer.flush();
	      	writer.close();   	
    	} catch (IOException e) {
    	  e.printStackTrace();
    	}
    	
   }
     
    public static SettingsClass readFromFile(String filename) {
    	boolean mono;
    	String freq;
    	String rds;
    	String mac;
    	int numberKBdevices;
    	int numberFmSets;
    	FmSet set;
    	KBdeviceSettings device;

		SettingsClass settings = new SettingsClass();
    	
        try {
        	FileInputStream inputStream = new FileInputStream(filename);
             
            if ( inputStream != null ) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                
                if ( (receiveString = bufferedReader.readLine()) != null ) {
                
	               	for (numberKBdevices = Integer.parseInt(receiveString); numberKBdevices>0; numberKBdevices--) {
	                    mac = bufferedReader.readLine();
	            		device = settings.new KBdeviceSettings(mac);
	                    if ( (receiveString = bufferedReader.readLine()) == null ) {
	                    
		    	        	for (numberFmSets = Integer.parseInt(receiveString); numberFmSets>0; numberFmSets--) {
		    	        		freq = bufferedReader.readLine();
		    	        		rds = bufferedReader.readLine();
		    	        		receiveString = bufferedReader.readLine();
		    	        		if (receiveString.equals("MONO"))
		    	        			mono = true;
		    	        		else
		    	        			mono = false;
		    	        		set = settings.new FmSet(freq,mono,rds);
		    	        		device.fmPack.addSorted(set);
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
