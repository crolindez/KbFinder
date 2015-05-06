package es.carlosrolindez.kbfinder;


import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
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
		final KBdevice device;
		
	    if (mKBdeviceList == null)
	    	return null;
	    
		View localView = convertView;
		
		if (localView==null)
		{
			localView = inflater.inflate(R.layout.device_list_row, parent, false);
		}
		
		ImageView imageDeviceType = (ImageView)localView.findViewById(R.id.device_type);

		TextView deviceName = (TextView)localView.findViewById(R.id.device_name);
		TextView deviceMAC = (TextView)localView.findViewById(R.id.device_mac);

		device = mKBdeviceList.get(position);
		
		switch (device.deviceType)
		{
		case KBdevice.IN_WALL:
			imageDeviceType.setImageResource(R.drawable.inwall);
			break;
		case KBdevice.ISELECT:
			imageDeviceType.setImageResource(R.drawable.iselect);
			break;
		case KBdevice.SELECTBT:
			imageDeviceType.setImageResource(R.drawable.selectbt);
			break;		
		}
		
		deviceName.setText(device.deviceName);
		deviceMAC.setText(device.deviceMAC);

	
/*		if ( (product.itemMode==NavisionTool.LOADER_PRODUCT_BOM) || (product.itemMode==NavisionTool.LOADER_PRODUCT_IN_USE) )
		{	
			purchaseValue =  Float.parseFloat(product.purchase);
			inProductionValue =  Float.parseFloat(product.inProduction);
			saleValue =  Float.parseFloat(product.sale);
			usedInProductionValue =  Float.parseFloat(product.usedInProduction);
			transferValue =  Float.parseFloat(product.transfer);
			orderPointValue = Float.parseFloat(product.orderPoint);
            if(android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                if ((stockValue + purchaseValue + inProductionValue) < (saleValue + transferValue + usedInProductionValue))
                    localView.setBackgroundDrawable(localView.getResources().getDrawable(R.drawable.consume_bg));
                else if ((stockValue + inProductionValue) < (saleValue + transferValue + usedInProductionValue))
                    localView.setBackgroundDrawable(localView.getResources().getDrawable(R.drawable.stock_bg));
                else if ((stockValue + purchaseValue + inProductionValue) < (saleValue + transferValue + usedInProductionValue + orderPointValue))
                    localView.setBackgroundDrawable(localView.getResources().getDrawable(R.drawable.danger_bg));
                else
                    localView.setBackgroundDrawable(localView.getResources().getDrawable(R.drawable.cost_bg));
            }
            else {
                if ((stockValue + purchaseValue + inProductionValue) < (saleValue + transferValue + usedInProductionValue))
                    drawResourceInView(R.drawable.consume_bg,localView);
                else if ((stockValue + inProductionValue) < (saleValue + transferValue + usedInProductionValue))
                    drawResourceInView(R.drawable.stock_bg,localView);
                else if ((stockValue + purchaseValue + inProductionValue) < (saleValue + transferValue + usedInProductionValue + orderPointValue))
                    drawResourceInView(R.drawable.danger_bg,localView);
                else
                    drawResourceInView(R.drawable.cost_bg,localView);
            }
		}				
		*/
		

		return localView;
	}
	

	public  void showResultSet(  ArrayList<KBdevice> deviceList)
	{
	    mKBdeviceList = deviceList;
	    notifyDataSetChanged();		
	}

	
}
