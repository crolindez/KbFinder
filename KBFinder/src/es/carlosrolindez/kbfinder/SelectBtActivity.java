package es.carlosrolindez.kbfinder;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Toast;





public class SelectBtActivity extends FragmentActivity {
	
	public static final String LAUNCH_MAC = "Launcher MAC intent";
	private static final int FM_CHANNEL = 0;
	private static final int BT_CHANNEL = 1;
	
	private static SelectBtService service;
	private final SelectBtHandler  handler = new SelectBtHandler();
	private static Context mContext;
	
	private static String deviceMAC;
	
	private static boolean bootPending;
	private static boolean closeWhenPossible;
	private static int answerPending = 0;

	private static final int NO_QUESTION = 0;
	private static final int QUESTION_ALL = 1;
	
	private static SelectBtState selectBtState;
	
	private static ImageButton mainButton;
	private static SeekBar volumeSeekBar;

	// swipe fragments
    private static final int NUM_PAGES = 2;
    private ViewPager mPager;
    private ScreenSlidePagerAdapter mAdapter;
 //   private PagerAdapter mPagerAdapter;
	
	// animation
	private	static AnimationDrawable frameAnimation;
	private static ImageView splashImageView;
	
	private	static RelativeLayout splashLayout;
	private	static RelativeLayout controlLayout;	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_selectbt);
		
		mContext = this;
		selectBtState = new SelectBtState();
				
		splashImageView = (ImageView) findViewById(R.id.SplashImageView);
		splashImageView.setBackgroundResource(R.drawable.on_animation);
		frameAnimation = (AnimationDrawable)splashImageView.getBackground(); 
		splashLayout = (RelativeLayout) findViewById(R.id.SplashLayout);
		controlLayout = (RelativeLayout) findViewById(R.id.ControlLoyaut);
		mainButton = (ImageButton) findViewById(R.id.MainPower);
		  
		volumeSeekBar = (SeekBar) findViewById(R.id.volumeControl);
	
		Intent myIntent = getIntent();
    	
	    deviceMAC = myIntent.getStringExtra(SelectBtActivity.LAUNCH_MAC);	

		service = new SelectBtService(this, handler, deviceMAC);
		service.start();			
		
        // Instantiate a ViewPager and a PagerAdapter.
        mPager = (ViewPager) findViewById(R.id.pager);
        mAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
        mPager.setAdapter(mAdapter);
        mPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageSelected(int position) 
            {     
            	if (position == 1) {
            		selectBtState.setChannel(BT_CHANNEL);
            		volumeSeekBar.setProgress(A2dpService.volumeBT);
            	} else {
            		selectBtState.setChannel(FM_CHANNEL);
            		volumeSeekBar.setProgress(selectBtState.volumeFM);
            	}
            }
            @Override
            public void onPageScrollStateChanged(int state)
            {
            }
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels)
            {
            }
        });
        
        volumeSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
        	int progress = 0;
        
        	@Override
        	public void onProgressChanged(SeekBar seekBar,int progresValue, boolean fromUser) {
        	}

        	@Override
        	public void onStartTrackingTouch(SeekBar seekBar) {
        	}

        	@Override
        	public void onStopTrackingTouch(SeekBar seekBar) {
        		if (selectBtState.channel==FM_CHANNEL) {
        			selectBtState.setVolumeFM(volumeSeekBar.getProgress());
        		} else {
                   	AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE); 
                   	A2dpService.volumeBT = volumeSeekBar.getProgress();
                   	am.setStreamVolume(AudioManager.STREAM_MUSIC, A2dpService.volumeBT,	0);  			
        		}
        	}
        });


        
        splashImageView.post(new Runnable(){
            @Override
            public void run() {
                frameAnimation.start();                
            }            
        }); 
	
		bootPending = true;
		closeWhenPossible = false;
          
		new Handler().postDelayed(new Runnable() {
		    @Override
		    public void run() {
		    	closeIfNotBooted();               
		    }
		}, 10000);

		mainButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				selectBtState.switchOnOff();				
			}
		});
    }

	

	public void closeIfNotBooted() {
		if (bootPending) {
		    Toast.makeText(this, getString(R.string.device_not_availabe), Toast.LENGTH_LONG).show();
			finish();
		}
	}
		
	public static void askAll() {
		service.write(("ALL ?\r").getBytes());
		answerPending = QUESTION_ALL;
    }
	
	public static void writeOnOffState(boolean onOff) {
		if (onOff) 	service.write(("STB OFF\r").getBytes());
		else 		service.write(("STB ON\r").getBytes());
    }
	
	public static void writeChannelState(int channel) {
		if (channel == BT_CHANNEL) 	service.write(("CHN BT\r").getBytes());
		else 		service.write(("CHN FM\r").getBytes());
    }
		
	public static void writeVolumeFMState(int volumeFM) {
		service.write(("VOL " + String.valueOf(volumeFM) +"\r").getBytes());
    }
		
	public static class MessageExtractor {
		private String message;
		
		public MessageExtractor(String m) {
			message = m;
		}
		
		public String getStringFromMessage() {
			if (message.isEmpty()) return "";
			String arr[] = message.trim().split(" ", 2);
			message = arr[1];
			return arr[0];		
		}
		
		public String getIdentifierFromMessage() {
			if (message.isEmpty()) return "";   // first "
			String arr[] = message.trim().split("\"", 2);
			message = arr[1]; 
			if (message.isEmpty()) return "";	// second "
			arr = message.split("\"", 2);
			message = arr[1]; 			        
			return arr[0];				
		}
		
		public String getRDSFromMessage() {
			if (message.length()<8) return "";
			String RDS = message.substring(0, 8);
			message = message.substring(8, message.length());
			return RDS;			
		}
		
	}

	public static void bootFinished() {
		splashImageView.setVisibility(View.INVISIBLE);
		splashLayout.setVisibility(View.INVISIBLE);
		controlLayout.setVisibility(View.VISIBLE);
		bootPending = false;

	}

	@Override
	protected void onDestroy() {
		service.stop();
		super.onDestroy();
	}
		
	public static void interpreter(String m) {

		MessageExtractor messageExtractor = new MessageExtractor(m);

		switch (answerPending) {
			case QUESTION_ALL:

				String password = messageExtractor.getStringFromMessage();						Log.e("Password",password);
				String identifier = messageExtractor.getIdentifierFromMessage();				Log.e("identifier",identifier);


				selectBtState.updateOnOff(messageExtractor.getStringFromMessage());
				
				String standByMasterSettings = messageExtractor.getStringFromMessage();			Log.e("standByMasterSettings",standByMasterSettings);
				String standBySlaveSettings = messageExtractor.getStringFromMessage();			Log.e("standBySlaveSettings",standBySlaveSettings);
				
				String autoPowerMaster = messageExtractor.getStringFromMessage();				Log.e("autoPowerMaster",autoPowerMaster);
				String autoPowerSlave = messageExtractor.getStringFromMessage();				Log.e("autoPowerSlave",autoPowerSlave);
				String autoPowerVolume = messageExtractor.getStringFromMessage();				Log.e("autoPowerVolume",autoPowerVolume);
				String autoPowerFM = messageExtractor.getStringFromMessage();					Log.e("autoPowerFM",autoPowerFM);
				String autoPowerEQ = messageExtractor.getStringFromMessage();					Log.e("autoPowerEQ",autoPowerEQ);
				
				selectBtState.updateChannel(messageExtractor.getStringFromMessage());
				
				String stationFM = messageExtractor.getStringFromMessage();						Log.e("stationFM",stationFM);
				String infoRDS = messageExtractor.getRDSFromMessage();							Log.e("infoRDS",infoRDS);
				String tunerSensitivity = messageExtractor.getStringFromMessage();				Log.e("tunerSensitivity",tunerSensitivity);
				String equalizationMode = messageExtractor.getStringFromMessage();				Log.e("equalizationMode",equalizationMode);
//				String volumeFM = messageExtractor.getStringFromMessage();						Log.e("volumeFM",volumeFM);
				
				selectBtState.updateVolumeFM(messageExtractor.getStringFromMessage());
				String keepFmOn = messageExtractor.message;										Log.e("keepFmOn",keepFmOn);
	
				break;
			
		} 
		answerPending = NO_QUESTION;		
	}


	

	public static class SelectBtHandler extends Handler {
		public final static int MESSAGE_STATE_CHANGE = 1;
		public final static int MESSAGE_CONNECTING_FAILURE = 2;
		public final static int MESSAGE_READ = 3;
		public final static int MESSAGE_READING_FAILURE = 4;
		public final static int MESSAGE_WRITE = 5;
		public final static int MESSAGE_WRITING_FAILURE = 6;

		
	    @Override
	    public void handleMessage(Message msg) {
	        switch (msg.what) {
	            case MESSAGE_STATE_CHANGE:
	                switch (msg.arg1) {
                    	case SelectBtService.STATE_CONNECTED:
        	                frameAnimation.stop();
        	        		splashImageView.setBackgroundResource(R.drawable.sec10);        	                
        	                
                    		new Handler().postDelayed(new Runnable() {
                    		    @Override
                    		    public void run() {
                    		    	askAll();               
                    		    }
                    		}, 300);
                    		break;

	                    case SelectBtService.STATE_DISCONNECTED:
	                    	closeWhenPossible = true;
	                    	break;
	                    case SelectBtService.STATE_CONNECTING:
	                    case SelectBtService.STATE_NONE:


	                }
	                break;
	            case MESSAGE_WRITE:
	                byte[] writeBuf = (byte[]) msg.obj;
	                String writeMessage = new String(writeBuf);
	                Log.e("message written",writeMessage+"-");
	                break;
	            case MESSAGE_READ:
	                byte[] readBuf = (byte[]) msg.obj;
	                String readMessage = new String(readBuf, 0, msg.arg1);
	                Log.e("message received",readMessage);
					if (bootPending) bootFinished();
	                interpreter(readMessage);
	                break;
	            case MESSAGE_CONNECTING_FAILURE:
	            case MESSAGE_READING_FAILURE:
	            case MESSAGE_WRITING_FAILURE:
	                break;
	        }
	    }
	}

    private class ScreenSlidePagerAdapter extends FragmentPagerAdapter {
        public ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
        	if (position == BT_CHANNEL)
        		return new BtFragment("BT");
        	else
        		return new BtFragment("FM");       		
        }

        @Override
        public int getCount() {
            return NUM_PAGES;
        }
        
/*        @Override
        public void finishUpdate(ViewGroup container) {
        	super.finishUpdate(container);
        }*/
    }
    
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) 
    {
       	final AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE); 
       	int volume;
       	if (!selectBtState.onOff) return true;
        if (event.getAction() == KeyEvent.ACTION_DOWN)
        {
            switch (event.getKeyCode()) 
            {
                case KeyEvent.KEYCODE_VOLUME_UP:
                	if (selectBtState.channel==BT_CHANNEL) {
                		volume = am.getStreamVolume(AudioManager.STREAM_MUSIC);
                       	if  (A2dpService.volumeBT < am.getStreamMaxVolume(AudioManager.STREAM_MUSIC)) {
                       		A2dpService.volumeBT = volume+1;
                       		am.setStreamVolume(AudioManager.STREAM_MUSIC, A2dpService.volumeBT,	0);
                        	volumeSeekBar.setProgress(A2dpService.volumeBT);
                       	}		
                	} else {
                      	if  (selectBtState.volumeFM < selectBtState.MAX_VOLUME_FM) {
                        	volumeSeekBar.setProgress(selectBtState.volumeFM+1);
                      		selectBtState.setVolumeFM(selectBtState.volumeFM+1);

                       	}		              		
                	}
                    return true;
                case KeyEvent.KEYCODE_VOLUME_DOWN:
                	if (selectBtState.channel==BT_CHANNEL) {
                		volume = am.getStreamVolume(AudioManager.STREAM_MUSIC);
                       	if  (volume > 0) {
                       		A2dpService.volumeBT = volume-1;
                       		am.setStreamVolume(AudioManager.STREAM_MUSIC, A2dpService.volumeBT,	0);
                        	volumeSeekBar.setProgress(A2dpService.volumeBT);
                        }		
                	} else {
                      	if  (selectBtState.volumeFM > 0) {
                        	volumeSeekBar.setProgress(selectBtState.volumeFM-1);
                      		selectBtState.setVolumeFM(selectBtState.volumeFM-1);
                       	}		              		
                	}
  
                    return true;
            }
        }

        return super.dispatchKeyEvent(event);
    }
	
    
    private class SelectBtState {
    	boolean onOff;
    	int channel;
    	int volumeFM;
    	int volumeBT;
    	
    	public static final int MAX_VOLUME_FM = 15;
    	
    	
    	public SelectBtState() {
    		onOff = false;
    		channel = BT_CHANNEL; 	
    		volumeBT = 0;
    		volumeFM = 0;
    	}
    	
    	public void updateOnOff(String onOffString) {
    		if (onOffString.equals("OFF")) {
    			onOff = true;
        		mainButton.setBackground(getResources().getDrawable(R.drawable.power_on_selector));	
        		volumeSeekBar.setVisibility(View.VISIBLE);
        	}
    		else {
    			onOff = false;
        		mainButton.setBackground(getResources().getDrawable(R.drawable.power_off_selector));	
        		volumeSeekBar.setVisibility(View.INVISIBLE);        	
        	}

    	}
    	   	
    	public void switchOnOff() {
    		setOnOff(!onOff);	
    	}

    	public void setOnOff(boolean on) {
    		onOff = on;
    		writeOnOffState(onOff);
    		if (onOff){
    			mainButton.setBackground(getResources().getDrawable(R.drawable.power_on_selector));
        		volumeSeekBar.setVisibility(View.VISIBLE);
    		} else  {
    			mainButton.setBackground(getResources().getDrawable(R.drawable.power_off_selector));
        		volumeSeekBar.setVisibility(View.INVISIBLE);
    		}
    	}
 
    	public void updateChannel(String channelString) {
    		if (channelString.equals("BT")) {
    			channel = BT_CHANNEL; 
        		mPager.setCurrentItem(1, false);
          		if (!((AudioManager) getSystemService(Context.AUDIO_SERVICE)).isBluetoothA2dpOn()) {
        			A2dpService.connectBluetoothA2dp(mContext, deviceMAC);
        		}
    		} else {
    			channel = FM_CHANNEL;
        		mPager.setCurrentItem(0, false);
           		if (((AudioManager) getSystemService(Context.AUDIO_SERVICE)).isBluetoothA2dpOn()) {
        			A2dpService.connectBluetoothA2dp(mContext, deviceMAC);
        		}
    		}
    	}
    	   	
    	public void setChannel(int numChannel) {
    		writeChannelState(numChannel);	
			channel = numChannel;
    		if (numChannel == BT_CHANNEL) {
        		if (!((AudioManager) getSystemService(Context.AUDIO_SERVICE)).isBluetoothA2dpOn()) {
        			A2dpService.connectBluetoothA2dp(mContext, deviceMAC);
        		}
    		} else {
        		if (((AudioManager) getSystemService(Context.AUDIO_SERVICE)).isBluetoothA2dpOn()) {
        			A2dpService.connectBluetoothA2dp(mContext, deviceMAC);
        		}
    		}
   			//View.selectBtState.OnOff = !selectBtState.OnOff;    			
    	}
    	
    	public void updateVolumeFM(String volumeString) {
    		volumeFM = Integer.parseInt(volumeString);
        	volumeSeekBar.setProgress(volumeFM);   
    	}   
    	
    	public void setVolumeFM(int volume) {
    		volumeFM = volume;
    		writeVolumeFMState(volumeFM);				
    	}
    	
    }
}
