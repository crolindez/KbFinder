package es.carlosrolindez.kbfinder;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
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
	private ArrayKBdevice mKBdeviceList;
	private Context mContext;
	private ListView listView;
	
//	private ConnectOnClick mConnectOnClick;
	
/*    public interface ConnectOnClick {
    	public void connectBluetoothA2dp(String deviceMAC);   	
    }*/
	
	public KBdeviceListAdapter(Context context,ArrayKBdevice deviceList,ListView list)
	{
		mContext = context;
		mKBdeviceList = deviceList;
		inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		listView = list;
	//	mConnectOnClick = connectOnClick;
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
	
					}
				});
			} else {
				button_app.setVisibility(View.VISIBLE);
				button_app.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) 
					{
	            		BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
	            		
	            		//disconnect current A2dp connection (if different to current device)
	        			String MAC = KBdevice.findConnectedDevice(mKBdeviceList);
	        			if ( (MAC!=null) && (!MAC.equals(device.deviceMAC)) )
	        				A2dpService.connectBluetoothA2dp(mContext, MAC);
	            		
	                   	Intent localIntent = new Intent (mContext, SelectBtActivity.class);
	                   	localIntent.putExtra(Constants.LAUNCH_MAC, device.deviceMAC);        	
	                   	mContext.startActivity(localIntent);
					}
				});
				if (device.deviceType == KBdevice.SELECTBT)
					localView.setOnTouchListener(new SwipeView(new KBfinderHolder(localView), position,listView));
				else 
					localView.setOnTouchListener(null);				

				localView.setOnTouchListener(null);
			}
		} else {  // not connected
			button_app.setVisibility(View.GONE);
			button_previous.setVisibility(View.GONE);
			button_play_pause.setVisibility(View.GONE);
			button_next.setVisibility(View.GONE);
			
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
	        		    BluetoothDevice device;

	        		    String deviceMAC = ((TextView) view.findViewById(R.id.device_mac)).getText().toString();
	            		BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
	        			
	            		if   ((device = KBdevice.deviceInArray(A2dpService.deviceList, deviceMAC)) != null) {
	        				if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
	        					device.createBond();
	        				} else {
	    	            		//disconnect current A2dp connection (if different to current device)
	    	        			String MAC = KBdevice.findConnectedDevice(mKBdeviceList);
	    	        			if ( (MAC!=null) && (!MAC.equals(deviceMAC)) )
	    	        				A2dpService.connectBluetoothA2dp(mContext, MAC);
	    	        			
			                   	Intent localIntent = new Intent (mContext, SelectBtActivity.class);
			                   	localIntent.putExtra(Constants.LAUNCH_MAC, deviceMAC);      
			                   	mContext.startActivity(localIntent);
        					}
	            		}
	        			
	        			
			        			
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


	public  void showResultSet(  ArrayKBdevice deviceList)
	{
	    mKBdeviceList = deviceList;
	    notifyDataSetChanged();		
	}


}
