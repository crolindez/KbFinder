package es.carlosrolindez.kbfinder;


import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.SystemClock;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class KBdeviceListAdapter extends BaseAdapter {

	private LayoutInflater inflater;
	private ArrayList<KBdevice> mKBdeviceList;
	private Context mContext;
	
	public KBdeviceListAdapter(Context context,ArrayList<KBdevice> deviceList)
	{
		mContext = context;
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
	
		if (localView==null)
		{
			localView = inflater.inflate(R.layout.device_list_row, parent, false);
		}
		
		ImageView imageDeviceType = (ImageView)localView.findViewById(R.id.device_type);

		TextView deviceName = (TextView)localView.findViewById(R.id.device_name);
		TextView deviceMAC = (TextView)localView.findViewById(R.id.device_mac);

		ImageView button_previous = (ImageView)localView.findViewById(R.id.previous);
		ImageView button_play_pause = (ImageView)localView.findViewById(R.id.play_pause);
		ImageView button_next = (ImageView)localView.findViewById(R.id.next);
		
		
		

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

		if (device.connected) {
			button_previous.setVisibility(View.VISIBLE);
			button_play_pause.setVisibility(View.VISIBLE);
			button_next.setVisibility(View.VISIBLE);
	
			button_previous.setOnClickListener(new OnClickListener() 
			{
				@Override
				public void onClick(View v) 
				{
					AudioManager am = (AudioManager)mContext.getSystemService(Context.AUDIO_SERVICE);

					long eventtime = SystemClock.uptimeMillis() - 1;
					KeyEvent downEvent = new KeyEvent(eventtime, eventtime, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PREVIOUS, 0);
					am.dispatchMediaKeyEvent(downEvent);

					eventtime++;
					KeyEvent upEvent = new KeyEvent(eventtime,eventtime,KeyEvent.ACTION_UP,KeyEvent.KEYCODE_MEDIA_PREVIOUS, 0);         
					am.dispatchMediaKeyEvent(upEvent);
					
					/*Intent intent = new Intent("com.android.music.musicservicecommand");
					intent.putExtra("command", "previous");
					mContext.sendBroadcast(intent);*/
				}
			});

			button_play_pause.setOnClickListener(new OnClickListener() 
			{
				@Override
				public void onClick(View v) 
				{
					AudioManager am = (AudioManager)mContext.getSystemService(Context.AUDIO_SERVICE);

					long eventtime = SystemClock.uptimeMillis() - 1;
					KeyEvent downEvent = new KeyEvent(eventtime, eventtime, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE, 0);
					am.dispatchMediaKeyEvent(downEvent);

					eventtime++;
					KeyEvent upEvent = new KeyEvent(eventtime,eventtime,KeyEvent.ACTION_UP,KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE, 0);         
					am.dispatchMediaKeyEvent(upEvent);
					
					/*Intent intent = new Intent("com.android.music.musicservicecommand");
					intent.putExtra("command", "togglepause");
					mContext.sendBroadcast(intent);*/
				}
			});

			button_next.setOnClickListener(new OnClickListener() 
			{
				@Override
				public void onClick(View v) 
				{
					AudioManager am = (AudioManager)mContext.getSystemService(Context.AUDIO_SERVICE);

					long eventtime = SystemClock.uptimeMillis() - 1;
					KeyEvent downEvent = new KeyEvent(eventtime, eventtime, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_NEXT, 0);
					am.dispatchMediaKeyEvent(downEvent);

					eventtime++;
					KeyEvent upEvent = new KeyEvent(eventtime,eventtime,KeyEvent.ACTION_UP,KeyEvent.KEYCODE_MEDIA_NEXT, 0);         
					am.dispatchMediaKeyEvent(upEvent);

					/*Intent intent = new Intent("com.android.music.musicservicecommand");
					intent.putExtra("command", "next");
					mContext.sendBroadcast(intent);*/
				}
			});

		} else {
			button_previous.setVisibility(View.GONE);
			button_play_pause.setVisibility(View.GONE);
			button_next.setVisibility(View.GONE);

		}
		
		

		return localView;
	}
	

	public  void showResultSet(  ArrayList<KBdevice> deviceList)
	{
	    mKBdeviceList = deviceList;
	    notifyDataSetChanged();		
	}

	
}
