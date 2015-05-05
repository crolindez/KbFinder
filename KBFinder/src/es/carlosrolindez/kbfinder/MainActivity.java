package es.carlosrolindez.kbfinder;

import java.util.ArrayList;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class MainActivity extends Activity {

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		ArrayList<KBdevice> deviceList = new ArrayList<KBdevice>();
		
		KBdevice device1 = new KBdevice(KBdevice.IN_WALL,"Salón","23-23-23-23-23");
		KBdevice device2 = new KBdevice(KBdevice.ISELECT,"Cocina","45-45-45-45-45");
		KBdevice device3 = new KBdevice(KBdevice.ISELECT,"Baño","67-67-67-67-67-67");
		KBdevice device4 = new KBdevice(KBdevice.SELECTBT,"Despacho","89-89-89-89-89");
		KBdevice device5 = new KBdevice(KBdevice.IN_WALL,"China","01-01-01-01-01");
		
		deviceList.add(device1);
		deviceList.add(device2);
		deviceList.add(device3);
		deviceList.add(device4);
		deviceList.add(device5);		

		KBdeviceListAdapter deviceListAdapter = new KBdeviceListAdapter(this,deviceList);
		ListView list = (ListView)findViewById(R.id.list);  
        list.setAdapter(deviceListAdapter);
        list.setOnItemClickListener(onItemClickListener);  
        
 //       showResultSet(productList);
    }
    
	OnItemClickListener onItemClickListener = new OnItemClickListener() 
	{
		@Override
    	public void onItemClick(AdapterView<?> parent, View view, int position, long id) 
    	{ 			
/*	    	Intent intent = new Intent (view.getContext(), InfoActivity.class);
	        Product product = (Product)parent.getItemAtPosition(position);
        	intent.putExtra(NavisionTool.LAUNCH_REFERENCE, product.reference);        	
        	intent.putExtra(NavisionTool.LAUNCH_DESCRIPTION, product.description);  
        	intent.putExtra(NavisionTool.LAUNCH_INFO_MODE, NavisionTool.INFO_MODE_SUMMARY);
        	startActivity(intent);*/
        	
//        	Toast.makeText(view.getContext(), "List Item", Toast.LENGTH_SHORT).show();
    	}
	};
  
		
		


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
