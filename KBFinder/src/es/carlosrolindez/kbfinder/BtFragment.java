package es.carlosrolindez.kbfinder;

import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;




public class BtFragment extends Fragment {
	public String fragmentName;
	private final Context mContext;
	
	private static TextView songName;
	
	public BtFragment(Context context) {
		fragmentName =  "BT";
		mContext = context;
	}
	
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.bt_fragment, container, false);    
      	((TextView) rootView.findViewById(R.id.fragment_text)).setText(fragmentName);
      	songName = (TextView)rootView.findViewById(R.id.song_name);
		
		ImageView button_previous_BT = (ImageView)rootView.findViewById(R.id.previous_BT);
		ImageView button_play_pause_BT = (ImageView)rootView.findViewById(R.id.play_pause_BT);
		ImageView button_next_BT = (ImageView)rootView.findViewById(R.id.next_BT);
		songName.setSelected(true);
		
		
		button_previous_BT.setOnClickListener(new OnClickListener() 
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

		button_play_pause_BT.setOnClickListener(new OnClickListener() 
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

		button_next_BT.setOnClickListener(new OnClickListener() 
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
      	
      	
      	return rootView;	
        
        
    }
    
    public void setSongName(String name) {
        songName.setText(name);    	
    }    
    

}
