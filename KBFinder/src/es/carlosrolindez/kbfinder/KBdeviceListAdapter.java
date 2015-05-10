package es.carlosrolindez.kbfinder;


import java.util.ArrayList;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class KBdeviceListAdapter extends BaseAdapter {

	private LayoutInflater inflater;
	private ArrayList<KBdevice> mKBdeviceList;
	
	public KBdeviceListAdapter(Context context,ArrayList<KBdevice> deviceList)
	{
		mKBdeviceList = deviceList;
		inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}
	
	@Override
	public int getCount()
	{
		if (mKBdeviceList == null)
			return 0;
		else
			return mKBdeviceList.size();
	}
	
	@Override
	public Object getItem(int position)
	{
		if (mKBdeviceList == null)
			return 0;
		else			
			return mKBdeviceList.get(position);
	}
	
	@Override
	public long getItemId(int position)
	{
			return position;
	}
	
/*	public ArrayList<KBdevice> getProductList() 
	{
		return mKBdeviceList;
	}

    @TargetApi(16)
    private void drawResourceInView(int resource, View viewer)
    {
        viewer.setBackground(viewer.getResources().getDrawable(resource));

    }*/
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{	
		final KBdevice device =  mKBdeviceList.get(position);
		
	    if (mKBdeviceList == null)
	    	return null;
	    
		View localView = convertView;
	
		if (device.connected) {
			localView = inflater.inflate(R.layout.device_list_connected_row, parent, false);

		} else {
			localView = inflater.inflate(R.layout.device_list_row, parent, false);
		}
	
	/*	if (localView==null)
		{
			localView = inflater.inflate(R.layout.device_list_row, parent, false);
		}*/
		
		ImageView imageDeviceType = (ImageView)localView.findViewById(R.id.device_type);

		TextView deviceName = (TextView)localView.findViewById(R.id.device_name);
		TextView deviceMAC = (TextView)localView.findViewById(R.id.device_mac);


		
		switch (device.deviceType)
		{
		case KBdevice.IN_WALL:
			imageDeviceType.setVisibility(View.VISIBLE);
			imageDeviceType.setImageResource(R.drawable.inwall);
			break;
		case KBdevice.ISELECT:
			imageDeviceType.setVisibility(View.VISIBLE);
			imageDeviceType.setImageResource(R.drawable.iselect);
			break;
		case KBdevice.SELECTBT:
			imageDeviceType.setVisibility(View.VISIBLE);
			imageDeviceType.setImageResource(R.drawable.selectbt);
			break;		
		default:
			imageDeviceType.setVisibility(View.INVISIBLE);
		}
		
		deviceName.setText(device.deviceName);
		deviceMAC.setText(device.deviceMAC);

	/*	if (device.connected) {
			localView.setBackgroundResource(R.drawable.connected_selector);
			deviceName.setTextColor(Color.WHITE);
			deviceMAC.setTextColor(Color.WHITE);
		} else {
			localView.setBackgroundResource(R.drawable.notconnected_selector);
			deviceName.setTextColor(Color.BLACK);
			deviceMAC.setTextColor(Color.BLACK);
		}*/
		
        
		

		return localView;
	}
	

	public  void showResultSet(  ArrayList<KBdevice> deviceList)
	{
	    mKBdeviceList = deviceList;
	    notifyDataSetChanged();		
	}

	
}
