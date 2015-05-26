package es.carlosrolindez.kbfinder;


import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.SystemClock;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;


public class KBdeviceListAdapter extends BaseAdapter {
	private static String TAG = "KBdeviceListAdapter";

	private LayoutInflater inflater;
	private ArrayList<KBdevice> mKBdeviceList;
	private Context mContext;
	private ListView listView;
//	private SwipeListViewTouchListener.OnClickCallBack mCallBack;
	
	public KBdeviceListAdapter(Context context,ArrayList<KBdevice> deviceList,ListView list)
	{
		mContext = context;
		mKBdeviceList = deviceList;
		inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		listView = list;
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
	
	
    public static class KBfinderHolder {
        public RelativeLayout mainView;
        public RelativeLayout shareView;
        
        public KBfinderHolder(View view)
        {
                mainView = (RelativeLayout)view.findViewById(R.id.main_layout);
                shareView = (RelativeLayout)view.findViewById(R.id.back_layout);
        }
    }
	

	
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
		TextView password = (TextView)localView.findViewById(R.id.back_text);
		password.setText(""+KBdevice.password(device.deviceMAC));


		ImageView button_previous = (ImageView)localView.findViewById(R.id.previous);
		ImageView button_play_pause = (ImageView)localView.findViewById(R.id.play_pause);
		ImageView button_next = (ImageView)localView.findViewById(R.id.next);
		ImageView button_app = (ImageView)localView.findViewById(R.id.app_button);

		
		

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
//			localView.findViewById(R.id.device_type).setBackground(mContext.getResources().getDrawable(R.drawable.grill));
			if ( (device.deviceType == KBdevice.IN_WALL) || (device.deviceType == KBdevice.ISELECT)) {
				
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
				button_app.setVisibility(View.VISIBLE);
/*				button_app.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) 
					{
				    	Intent intent = new Intent (v.getContext(), SelectBtActivity.class);
			        	intent.putExtra(SelectBtActivity.LAUNCH_MAC, device.deviceMAC);        	
			        	v.getContext().startActivity(intent);
					}
				});*/

				localView.setOnTouchListener(null);
			}
		} else {
			button_app.setVisibility(View.GONE);
			button_previous.setVisibility(View.GONE);
			button_play_pause.setVisibility(View.GONE);
			button_next.setVisibility(View.GONE);
//			localView.findViewById(R.id.device_type).setBackground(mContext.getResources().getDrawable(R.drawable.notconnected_selector));
			
			if (device.deviceType == KBdevice.SELECTBT)
				localView.setOnTouchListener(new SwipeView(new KBfinderHolder(localView), position,listView));
			else 
				localView.setOnTouchListener(null);
		}
		
		return localView;
	}
	
	public class SwipeView implements View.OnTouchListener {
		
		private int mSlop;

        private boolean motionInterceptDisallowed = false;
        private KBfinderHolder holder;

	    private float mDownX;
	    private ListView listView;
	 
	    public SwipeView(KBfinderHolder h, int position, ListView list) {
	        ViewConfiguration vc = ViewConfiguration.get(mContext);
	        mSlop = vc.getScaledTouchSlop();

	        listView = list;
	        holder = h;
	    }

	   
		@Override
	    public boolean onTouch(View view, MotionEvent motionEvent) {

	        switch (motionEvent.getActionMasked()) {
	        case MotionEvent.ACTION_DOWN:
	            {
	                mDownX = motionEvent.getRawX();
	                motionInterceptDisallowed = false;
	                view.setPressed(true);
	            }

	            return true;

	        case MotionEvent.ACTION_MOVE:
	            {
	                float deltaX = motionEvent.getRawX() - mDownX;
	                if ( (Math.abs(deltaX) > mSlop) && !motionInterceptDisallowed ) {
	                	listView.requestDisallowInterceptTouchEvent(true);
	                	motionInterceptDisallowed = true;
	                    view.setPressed(false);
	                }

	                
	                swipeView((int)deltaX);

	                return true;
	            }
	 
	        case MotionEvent.ACTION_UP:
	        	{
	        		view.setPressed(false);
        			swipeView(0);
	        		if (motionInterceptDisallowed) {
	        			listView.requestDisallowInterceptTouchEvent(false);
	        			motionInterceptDisallowed = false;
	        		} else {
	        			TextView deviceMAC = (TextView)view.findViewById(R.id.device_mac);
				    	Intent intent = new Intent (mContext, SelectBtActivity.class);
			        	intent.putExtra(SelectBtActivity.LAUNCH_MAC, deviceMAC.getText().toString());        	
			        	mContext.startActivity(intent);	        			
	        		}

		            return true;          

	        	}

	        case MotionEvent.ACTION_CANCEL:
	        	{

		    	   swipeView(0);
		    	   listView.requestDisallowInterceptTouchEvent(false);
		    	   motionInterceptDisallowed = false;

	               return false;

	        	}
	        }
	        return true;
	    }
		
		
	    private void swipeView(int distance) {
	        View animationView = holder.mainView;
	        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) animationView.getLayoutParams();
	        params.rightMargin = -distance;
	        params.leftMargin = distance;
	        animationView.setLayoutParams(params);
	    }
	    

	}




	public  void showResultSet(  ArrayList<KBdevice> deviceList)
	{
	    mKBdeviceList = deviceList;
	    notifyDataSetChanged();		
	}


}
