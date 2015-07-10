package es.carlosrolindez.kbfinder;

import java.util.Set;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.AnimationDrawable;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;
import es.carlosrolindez.kbfinder.FmFragment.SppBridge;
import es.carlosrolindez.kbfinder.SelectBtService.DisconnectActivity;
import es.carlosrolindez.kbfinder.SettingsClass.ArrayFmPackage;



// TODO improve dial FM
// TODO FM controls: forced mono; keypad; memories
// TODO sometimes track name from  spotify cracks




public class SelectBtActivity extends FragmentActivity implements DisconnectActivity, SppBridge {
	
    private static final String TAG = "SelectBtActivity";
    private static final String FILENAME = "settings.txt";
	
	private static final int FM_CHANNEL = 0;
	private static final int BT_CHANNEL = 1;
	
	public static final int NO_QUESTION = 0;
	public static final int QUESTION_ALL = 1;
//	public static final int RDS = 2;
//	public static final int BTID = 3;
//	public static final int FREQUENCY = 4;
	public static final int QUESTION_MON = 5;
	
	private static SelectBtService service;
	private final SelectBtHandler  handler = new SelectBtHandler();
	private static Context mContext;
	
	private static String deviceMAC;
	
	private static boolean bootPending;
	private static int questionPending;
	private static boolean allowDisconnect;

	
	private static SelectBtState selectBtState;
	
	private static ImageButton mainButton;
	private static SeekBar volumeSeekBar;
	private static TextView nameText;
	private	static RelativeLayout splashLayout;
	private	static RelativeLayout controlLayout;	
	private	static RelativeLayout windowLayout;
	
	
	private static CountDownTimer i2dpDisconnectionTimer;
	private static boolean	i2dpDisconnectionTimerStarted;
	private static BlockClass block;
	
	private static SettingsClass settingsFm;
	
	// swipe fragments
    private static final int NUM_PAGES = 2;
    private BlockingViewPager mPager;
    private ScreenSlidePagerAdapter mAdapter;

	
	// animation
	private	static AnimationDrawable frameAnimation;
	private static ImageView splashImageView;
	
	public void playBt() 
	{
		AudioManager am = (AudioManager)mContext.getSystemService(Context.AUDIO_SERVICE);

		long eventtime = SystemClock.uptimeMillis() - 1;
		KeyEvent downEvent = new KeyEvent(eventtime, eventtime, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PLAY, 0);
		am.dispatchMediaKeyEvent(downEvent);

		eventtime++;
		KeyEvent upEvent = new KeyEvent(eventtime,eventtime,KeyEvent.ACTION_UP,KeyEvent.KEYCODE_MEDIA_PLAY, 0);         
		am.dispatchMediaKeyEvent(upEvent);

	}

	
	public void changeStateI2dp(boolean state) {
		Log.e(TAG,"changeStateI2dp");
		if (i2dpDisconnectionTimerStarted) {
			i2dpDisconnectionTimerStarted = false;
			i2dpDisconnectionTimer.cancel();
			if (state) { // I2dp selected and already active or Not selected and not active
				Log.e(TAG,"Disconnecting canceled");
		  		if ( (selectBtState.channel == BT_CHANNEL) && (selectBtState.onOff) ) {
					Log.e(TAG,"Read Volume");
		  			AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		  			selectBtState.volumeBT = am.getStreamVolume(AudioManager.STREAM_MUSIC);
		  			volumeSeekBar.setProgress(selectBtState.volumeBT); 
		  			playBt();
		  		}
			} else {
				Log.e(TAG,"Disconnecting confirmed");
				i2dpDisconnectionTimer.start();
			}
		} else {
			if (((AudioManager) getSystemService(Context.AUDIO_SERVICE)).isBluetoothA2dpOn() == state) { // I2dp selected and already active or Not selected and not active
				Log.e(TAG,"No change");
		  		if ( (selectBtState.channel == BT_CHANNEL) && (selectBtState.onOff) ) {
					Log.e(TAG,"Read Volume");
		  			AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		  			selectBtState.volumeBT = am.getStreamVolume(AudioManager.STREAM_MUSIC);
		  			volumeSeekBar.setProgress(selectBtState.volumeBT); 
		  			playBt();
		  		}
			} else {
				if (state) {
					Log.e(TAG,"Change to BT");
			        block.lock(1000);
			 		A2dpService.connectBluetoothA2dp(mContext, deviceMAC);
			 		
	          		new Handler().postDelayed(new Runnable() {
	        		    @Override
	        		    public void run() {
	        				if ( (((AudioManager) getSystemService(Context.AUDIO_SERVICE)).isBluetoothA2dpOn() == true)
	    			  			&& (selectBtState.channel == BT_CHANNEL) && (selectBtState.onOff) ) {
	    						Log.e(TAG,"Read Volume");
	    			  			AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
	    			  			selectBtState.volumeBT = am.getStreamVolume(AudioManager.STREAM_MUSIC);
	    			  			volumeSeekBar.setProgress(selectBtState.volumeBT); 
	    			  			playBt();
	    			  		}
	        		    }
	        		}, 1000);
					
					
				} else {
					Log.e(TAG,"Change to Disconnected");	
					i2dpDisconnectionTimerStarted = true;
					i2dpDisconnectionTimer.start();
				}
			}
		}
	}
	
	public class BlockClass {
		public boolean locked;
		private CountDownTimer blockingCountDown;
		
		public BlockClass () {
			locked = false;
		}
		public void lock(long time) {
			Log.e(TAG,"blocked");
			blockingCountDown = new CountDownTimer(time,time) {
			     public void onTick(long millisUntilFinished) {
			     }
			     	
			     public void onFinish() {
						Log.e(TAG,"unblocked by timer");
						unlock();
			     }
			  }.start();
			setProgressBarIndeterminateVisibility(true);	
			service.block();
			mPager.setPagingEnabled(false);
			locked = true;
		}
		
		public void unlock() {
			Log.e(TAG,"unblocked");
			if (blockingCountDown!=null) blockingCountDown.cancel();
			setProgressBarIndeterminateVisibility(false);	
			service.unblock();
			mPager.setPagingEnabled(true);
			locked = false;
		}
		

		
	}


	
	
	public static void resetI2dpCounter() {
		if (i2dpDisconnectionTimerStarted) {
			Log.e(TAG,"resetI2dpCounter");
			i2dpDisconnectionTimer.cancel();
			i2dpDisconnectionTimer.start();
		}
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

		setContentView(R.layout.activity_selectbt);
		
		mContext = this;
		selectBtState = new SelectBtState();

				
		splashImageView = (ImageView) findViewById(R.id.SplashImageView);
		splashImageView.setBackgroundResource(R.drawable.on_animation);
		frameAnimation = (AnimationDrawable)splashImageView.getBackground(); 
		
		splashLayout = (RelativeLayout) findViewById(R.id.SplashLayout);		
		controlLayout = (RelativeLayout) findViewById(R.id.ControlLoyaut);
		windowLayout = (RelativeLayout) findViewById(R.id.WindowLayout);
		mainButton = (ImageButton) findViewById(R.id.MainPower);
		nameText = (TextView) findViewById(R.id.SelectBtName); 

		block = new BlockClass();
		  
		volumeSeekBar = (SeekBar) findViewById(R.id.volumeControl);
		
		i2dpDisconnectionTimerStarted = false;
		i2dpDisconnectionTimer = new CountDownTimer(1500,1500){
		     public void onTick(long millisUntilFinished) {
		     }
		     	
		     public void onFinish() {
		    	i2dpDisconnectionTimerStarted = false;
		        block.lock(5000);
      //          service.stop();
		 		A2dpService.connectBluetoothA2dp(mContext, deviceMAC);	
		     }
		  };

		
		Intent myIntent = getIntent();
		deviceMAC = myIntent.getStringExtra(Constants.LAUNCH_MAC);	

		
		service = new SelectBtService(this, handler, deviceMAC);
		allowDisconnect = false;
		service.start();	
		
		settingsFm = SettingsClass.readFromFile(FILENAME,this);

			
		
        // Instantiate a ViewPager and a PagerAdapter.
        mPager = (BlockingViewPager) findViewById(R.id.pager);
        mAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
        mPager.setAdapter(mAdapter);
        mPager.addOnPageChangeListener(new BlockingViewPager.OnPageChangeListener() {
            @Override
            public void onPageSelected(int position) 
            {   
            	if (position == 1) {
            		selectBtState.setChannel(BT_CHANNEL);
            		volumeSeekBar.setProgress(selectBtState.volumeBT);
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
        mPager.setPageTransformer(true, new ViewPager.PageTransformer() {
            private static final float MIN_SCALE = 0.85f;
            private static final float MIN_ALPHA = 0.5f;

            public void transformPage(View view, float position) {
                int pageWidth = view.getWidth();
                int pageHeight = view.getHeight();

                if (position < -1) { // [-Infinity,-1)
                    // This page is way off-screen to the left.
                    view.setAlpha(0);

                } else if (position <= 1) { // [-1,1]
                    // Modify the default slide transition to shrink the page as well
                    float scaleFactor = Math.max(MIN_SCALE, 1 - Math.abs(position));
                    float vertMargin = pageHeight * (1 - scaleFactor) / 2;
                    float horzMargin = pageWidth * (1 - scaleFactor) / 2;
                    if (position < 0) {
                        view.setTranslationX(horzMargin - vertMargin / 2);
                    } else {
                        view.setTranslationX(-horzMargin + vertMargin / 2);
                    }

                    // Scale the page down (between MIN_SCALE and 1)
                    view.setScaleX(scaleFactor);
                    view.setScaleY(scaleFactor);

                    // Fade the page relative to its size.
                    view.setAlpha(MIN_ALPHA +
                            (scaleFactor - MIN_SCALE) /
                            (1 - MIN_SCALE) * (1 - MIN_ALPHA));

                } else { // (1,+Infinity]
                    // This page is way off-screen to the right.
                    view.setAlpha(0);
                }
            }
        });
        
        volumeSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
        
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
                   	selectBtState.volumeBT = volumeSeekBar.getProgress();
                   	am.setStreamVolume(AudioManager.STREAM_MUSIC, selectBtState.volumeBT,	0);  			
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
          
		new Handler().postDelayed(new Runnable() {
		    @Override
		    public void run() {
		    	closeIfNotBooted();               
		    }
		}, 10000);

		mainButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (!block.locked)
					selectBtState.switchOnOff();				
			}
		});
    }

	@Override	
	protected void onResume() {
		super.onResume();
		Log.e(TAG,"resumed");
		
		// in case BT volume was modified when activity out of focus
  		if (((AudioManager) getSystemService(Context.AUDIO_SERVICE)).isBluetoothA2dpOn()) {
           	AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE); 
	    	selectBtState.volumeBT = am.getStreamVolume(AudioManager.STREAM_MUSIC);
	    	if (selectBtState.onOff) {
	    		if (selectBtState.channel == BT_CHANNEL) {
	    			volumeSeekBar.setProgress(selectBtState.volumeBT); 
	    			playBt();
	    		}
	    	}
  		}
		IntentFilter iF = new IntentFilter();
		iF.addAction("com.spotify.music.playbackstatechanged");
		iF.addAction("com.spotify.music.metadatachanged");
		iF.addAction("com.spotify.music.queuechanged");
		
		iF.addAction("com.android.music.metachanged");
		iF.addAction("com.android.music.playstatechanged");
		iF.addAction("com.android.music.playbackcomplete");
 		iF.addAction("com.android.music.queuechanged");

		iF.addAction("com.htc.music.metachanged");
		iF.addAction("com.htc.music.playstatechanged");
		iF.addAction("com.htc.music.playbackcomplete");
		
		registerReceiver(spotifyBroadcastReceiver, iF);

	}
	
	@Override	
	protected void onPause() {
		super.onPause();
		Log.e(TAG,"paused");
		unregisterReceiver(spotifyBroadcastReceiver);
}
	
	
	public void disconnect() {
  		finish();
	}
	
	public void closeIfNotBooted() {
		if (bootPending) {
		    Toast.makeText(this, getString(R.string.device_not_availabe), Toast.LENGTH_LONG).show();
			finish();
		}
	}
	
	public void sppMessage(String message) {
		sendSppMessage(message);
	}
	
	public static void sendSppMessage(String message) {
		service.write(message);
		resetI2dpCounter();
	}
		
	public static void askAll() {
		sendSppMessage("ALL ?\r"); 
		questionPending = QUESTION_ALL;
    }
	
	public static void askForcedMono() {
		sendSppMessage("MON ?\r"); 
		questionPending = QUESTION_MON;
		Log.e(TAG,"question "+questionPending);
    }
	
	public static void writeOnOffState(boolean onOff) {
		if (onOff) 	sendSppMessage("STB ON\r");
		else 		sendSppMessage("STB OFF\r");
    }
	
	public static void writeChannelState(int channel) {
		if (channel == BT_CHANNEL) 	sendSppMessage("CHN BT\r");
		else 						sendSppMessage("CHN FM\r");
    }
		
	public static void writeVolumeFMState(int volumeFM) {
		sendSppMessage("VOL " + String.valueOf(volumeFM) +"\r");
    }
		
	public static class MessageExtractor {
		private String message;
		
		public MessageExtractor(String m) {
			message = m;
		}
		
		public String getStringFromMessage() {
			if (message.isEmpty()) return "";
			String arr[] = message.trim().split(" ", 2);
			if(arr.length==1)
				message="";
			else
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
			int len = message.length();
			String RDS;
			if (len == 0) return "";
			if (len>8) {
				RDS = message.substring(0, 8);
				message = message.substring(8, message.length());			
			} else {
				RDS = message;
				message="";						
			}

			return RDS;			
		}
		
		public void removeCR() {
			if (message.length()>0)
				message = message.substring(0, message.length()-1);			
		}
		
	}

	public static void bootFinished() {
		splashImageView.setVisibility(View.INVISIBLE);
		splashLayout.setVisibility(View.INVISIBLE);
		controlLayout.setVisibility(View.VISIBLE);
		windowLayout.setVisibility(View.VISIBLE);
		bootPending = false;

	}

	@Override
	protected void onDestroy() {
		Log.e(TAG,"destroyed");
		allowDisconnect = true;
		block.unlock();
        service.stop();
        settingsFm.writeToFile(FILENAME);
		super.onDestroy();
	}
		
	public static void interpreter(String m) {
		Log.e(TAG,"Interpreter "+m);
		MessageExtractor messageExtractor = new MessageExtractor(m);

		String header = messageExtractor.getStringFromMessage();
		if (header.equals("RDS"))
			selectBtState.updateRds(messageExtractor.getRDSFromMessage());
		else if (header.equals("FMS"))
			selectBtState.updateFrequency(messageExtractor.getStringFromMessage());
		else {
			Log.e(TAG,"question "+questionPending);
			messageExtractor = new MessageExtractor(m);
			switch (questionPending) {
			case QUESTION_ALL:

				String password = messageExtractor.getStringFromMessage();						
				selectBtState.updateName(messageExtractor.getIdentifierFromMessage());

				selectBtState.updateOnOff(messageExtractor.getStringFromMessage());
				
				String standByMasterSettings = messageExtractor.getStringFromMessage();			
				String standBySlaveSettings = messageExtractor.getStringFromMessage();			
				
				String autoPowerMaster = messageExtractor.getStringFromMessage();				
				String autoPowerSlave = messageExtractor.getStringFromMessage();				
				String autoPowerVolume = messageExtractor.getStringFromMessage();				
				String autoPowerFM = messageExtractor.getStringFromMessage();					
				String autoPowerEQ = messageExtractor.getStringFromMessage();					
				
				selectBtState.updateChannel(messageExtractor.getStringFromMessage());
				
				selectBtState.updateFrequency(messageExtractor.getStringFromMessage());
				
				selectBtState.updateRds(messageExtractor.getRDSFromMessage());

				String tunerSensitivity = messageExtractor.getStringFromMessage();				
				String equalizationMode = messageExtractor.getStringFromMessage();				

				
				selectBtState.updateVolumeFM(messageExtractor.getStringFromMessage());
				String keepFmOn = messageExtractor.message;		
				questionPending = NO_QUESTION;
				askForcedMono();

				break;
				
			case QUESTION_MON:
				messageExtractor.removeCR();
				Log.e(TAG,"Interpreting MON");
				selectBtState.updateForceMono(messageExtractor.message);
				questionPending = NO_QUESTION;
				break;
				
			default:
				Log.e(TAG,"deleting question "+questionPending);
				questionPending = NO_QUESTION;
			} 

		}

	
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
        			        block.unlock();
        	                
                    		new Handler().postDelayed(new Runnable() {
                    		    @Override
                    		    public void run() {
                    		    	askAll();               
                    		    }
                    		}, 300);
                    		break;

	                    case SelectBtService.STATE_DISCONNECTED:
	                    case SelectBtService.STATE_CONNECTING:
	                    case SelectBtService.STATE_NONE:


	                }
	                break;
	            case MESSAGE_WRITE:
	                byte[] writeBuf = (byte[]) msg.obj;
	                String writeMessage = new String(writeBuf);
	                Log.e("message written",writeMessage);
	                break;
	            case MESSAGE_READ:
	                byte[] readBuf = (byte[]) msg.obj;
	                String readMessage = new String(readBuf, 0, msg.arg1);
	                Log.e("message received",readMessage);
					if (bootPending) bootFinished();
	                interpreter(readMessage);
	                break;
	            case MESSAGE_READING_FAILURE:
	            	Log.e(TAG,"Reading error");
	            	if (!allowDisconnect) {
		            	Log.e(TAG,"Re-connecting");
		                service.stop();
		        		new CountDownTimer(250,250){
				   		    public void onTick(long millisUntilFinished) {
				   		    }
				   		     	
				   		    public void onFinish() {
				            	service.start();
				   		    }
		        		}.start();
	            	}
	                break;     
	            case MESSAGE_CONNECTING_FAILURE:
	            	Log.e(TAG,"Connecting error");
                	break;     
	            case MESSAGE_WRITING_FAILURE:
	            	Log.e(TAG,"Writing error");
//	                service.start();
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
        		return new BtFragment(mContext);
        	else
        		return new FmFragment(mContext,false,settingsFm.getDeviceInArray(deviceMAC));       		
        }

        @Override
        public int getCount() {
            return NUM_PAGES;
        }
        
    }
    
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) 
    {
       	final AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE); 
       	int volume;
       	if (!selectBtState.onOff) 
       		if (event.getKeyCode()==KeyEvent.KEYCODE_BACK) 
       			return super.dispatchKeyEvent(event);
        if (event.getAction() == KeyEvent.ACTION_DOWN)
        {
            switch (event.getKeyCode()) 
            {
                case KeyEvent.KEYCODE_VOLUME_UP:
                	if (selectBtState.channel==BT_CHANNEL) {
                		if (((AudioManager) getSystemService(Context.AUDIO_SERVICE)).isBluetoothA2dpOn()) {
	                		volume = am.getStreamVolume(AudioManager.STREAM_MUSIC);
	                       	if  (selectBtState.volumeBT < am.getStreamMaxVolume(AudioManager.STREAM_MUSIC)) {
	                       		selectBtState.volumeBT = volume+1;
	                       		am.setStreamVolume(AudioManager.STREAM_MUSIC, selectBtState.volumeBT,	0);
	                        	volumeSeekBar.setProgress(selectBtState.volumeBT);
	                       	}	
                		}
	                } else {
                      	if  (selectBtState.volumeFM < SelectBtState.MAX_VOLUME_FM) {
                        	volumeSeekBar.setProgress(selectBtState.volumeFM+1);
                      		selectBtState.setVolumeFM(selectBtState.volumeFM+1);

                       	}		              		
                	}
                    return true;
                case KeyEvent.KEYCODE_VOLUME_DOWN:
                	if (selectBtState.channel==BT_CHANNEL) {
                		if (((AudioManager) getSystemService(Context.AUDIO_SERVICE)).isBluetoothA2dpOn()) {
	                		volume = am.getStreamVolume(AudioManager.STREAM_MUSIC);
	                       	if  (volume > 0) {
	                       		selectBtState.volumeBT = volume-1;
	                       		am.setStreamVolume(AudioManager.STREAM_MUSIC, selectBtState.volumeBT,	0);
	                        	volumeSeekBar.setProgress(selectBtState.volumeBT);
	                        }	
                		}
	                } else {
                      	if  (selectBtState.volumeFM > 0) {
                        	volumeSeekBar.setProgress(selectBtState.volumeFM-1);
                      		selectBtState.setVolumeFM(selectBtState.volumeFM-1);
                       	}		              		
                	}
  
                    return true;
                case KeyEvent.KEYCODE_BACK:
                	return super.dispatchKeyEvent(event);
            }
        } else {
        	if (event.getKeyCode()==KeyEvent.KEYCODE_BACK) return super.dispatchKeyEvent(event);
        }
        return true;
//        return super.dispatchKeyEvent(event);
    }
	
    
    private class SelectBtState {
    	public boolean onOff;
    	public int channel;
    	public int volumeFM;
    	public int volumeBT;
    	public String name;
    	public String frequency;
    	public String rds;
    	public String songName;
    	
    	public static final int MAX_VOLUME_FM = 15;
    	
    	
    	public SelectBtState() {
    		onOff = false;
    		channel = FM_CHANNEL; 	
    		volumeBT = 0;
    		volumeFM = 0;
    		frequency = "87.5";
    		songName = "";
    		rds = "";
    	}
  
    	public void updateName(String n) {
    		name = n;
    		nameText.setText(name);
    	}
  
    	
    	public void updateOnOff(String onOffString) {
    		if (onOffString.equals("OFF")) {
        		mainButton.setBackground(getResources().getDrawable(R.drawable.power_off_selector));	
        		volumeSeekBar.setVisibility(View.INVISIBLE);   
        		windowLayout.setVisibility(View.INVISIBLE);
    			onOff = false;
    			changeStateI2dp(onOff);
        	}
    		else {
    			onOff = true;
        		mainButton.setBackground(getResources().getDrawable(R.drawable.power_on_selector));	
        		volumeSeekBar.setVisibility(View.VISIBLE);
        		windowLayout.setVisibility(View.VISIBLE);
        		if (channel == BT_CHANNEL)
        			changeStateI2dp(onOff);
        		else
        			changeStateI2dp(false);
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
        		windowLayout.setVisibility(View.VISIBLE);
        		if (channel == BT_CHANNEL)
        			changeStateI2dp(true);
        		else
        			changeStateI2dp(false);
     		} else  {
    			mainButton.setBackground(getResources().getDrawable(R.drawable.power_off_selector));
        		volumeSeekBar.setVisibility(View.INVISIBLE);
        		windowLayout.setVisibility(View.INVISIBLE);
       			changeStateI2dp(onOff);
     		}
    	}
 
    	public void updateChannel(String channelString) {

    		if (channelString.equals("BT")) {
    			channel = BT_CHANNEL; 
        		mPager.setCurrentItem(1, false);
        		((BtFragment)mAdapter.getItem(1)).setSongName(songName);
        		changeStateI2dp(onOff);
    		} else {
    			channel = FM_CHANNEL;
        		mPager.setCurrentItem(0, false);
    			((FmFragment)mAdapter.getItem(0)).setFrequency(frequency);
    			changeStateI2dp(false);
    		}
    	}
    	   	
    	public void setChannel(int numChannel) {
           	writeChannelState(numChannel);	
			channel = numChannel;
    		if (numChannel == BT_CHANNEL) {
    			((BtFragment)mAdapter.getItem(1)).setSongName(songName);
    			changeStateI2dp(onOff);
    		} else {
    			changeStateI2dp(false);
    			((FmFragment)mAdapter.getItem(0)).setFrequency(frequency);
    		}		
    	}
    	
    	public void updateVolumeFM(String volumeString) {
//           	final AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE); 
    		volumeFM = Integer.parseInt(volumeString);
    		if (channel == FM_CHANNEL) {

            	volumeSeekBar.setProgress(volumeFM);     			
    		} 
    	}   
    	
    	public void setVolumeFM(int volume) {
    		volumeFM = volume;
    		writeVolumeFMState(volumeFM);				
    	}
    	
    	public void updateFrequency(String frequencyString) {
    		frequency = frequencyString;
    		if (mPager.getCurrentItem()==0) {
    			((FmFragment)mAdapter.getItem(0)).setFrequency(frequency);
    		}			
		}   
		
    	public void updateRds(String rdsString) {
    		rds = rdsString;
    		if (mPager.getCurrentItem()==0) {
    			((FmFragment)mAdapter.getItem(0)).setRDS(rds);
    		}			
		}   
		
     	public void updateTrackName(String name) {
    		songName = name;
    		if (mPager.getCurrentItem()==1) {
    			((BtFragment)mAdapter.getItem(1)).setSongName(songName);
    		}			
		}   
     	
     	public void updateForceMono(String state) {
     		if (state.equals("OFF")) {
     			Log.e(TAG,"Forced Mono off");
     			((FmFragment)mAdapter.getItem(0)).setStereo(true);
     		} else {
     			Log.e(TAG,"Forced Mono on");
 				((FmFragment)mAdapter.getItem(0)).setStereo(false);
     		}
     	}
		
     	
    }
    
    public BroadcastReceiver spotifyBroadcastReceiver = new BroadcastReceiver() {
    	final class BroadcastTypes {
            static final String SPOTIFY_PACKAGE = "com.spotify.music";
            static final String ANDROID_PACKAGE = "com.android.music";
            static final String HTC_PACKAGE = "com.htc.music";
            
            static final String ANDROID_METADATA_CHANGED = ANDROID_PACKAGE + ".metachanged";
            static final String ANDROID_PLAYBACK_STATE_CHANGED = ANDROID_PACKAGE + ".playstatechanged";
            static final String ANDROID_QUEUE_CHANGED = ANDROID_PACKAGE + ".queuechanged";
            static final String ANDROID_PLAYBACK_COMPLETE = ANDROID_PACKAGE + ".playbackcomplete";
            
            static final String SPOTIFY_METADATA_CHANGED = SPOTIFY_PACKAGE + ".metadatachanged";
            static final String SPOTIFY_PLAYBACK_STATE_CHANGED = SPOTIFY_PACKAGE + ".playbackstatechanged";
            static final String SPOTIFY_QUEUE_CHANGED = SPOTIFY_PACKAGE + ".queuechanged";            
            
            static final String HTC_METADATA_CHANGED = HTC_PACKAGE + ".metachanged";
            static final String HTC_PLAYBACK_STATE_CHANGED = HTC_PACKAGE + ".playstatechanged";
            static final String HTC_PLAYBACK_COMPLETE = HTC_PACKAGE + ".playbackcomplete";

            }


        @Override
        public void onReceive(Context context, Intent intent) {
            // This is sent with all broadcasts, regardless of type. The value is taken from
            // System.currentTimeMillis(), which you can compare to in order to determine how
            // old the event is.
//            long timeSentInMs = intent.getLongExtra("timeSent", 0L);
            String action = intent.getAction();
            Log.d("RECEIVER",action);
            Set<String> set = intent.getExtras().keySet();
            for (String text:set) {
            	Log.d("RECEIVER",text);
            }
            if ( (action.equals(BroadcastTypes.HTC_METADATA_CHANGED)) || (action.equals(BroadcastTypes.ANDROID_METADATA_CHANGED)) || (action.equals(BroadcastTypes.SPOTIFY_METADATA_CHANGED))  ){
 //               String trackId = intent.getStringExtra("id"); 			Log.d("ID",trackId);
 //               String artistName = intent.getStringExtra("artist");	Log.d("Artist",artistName);
 //               String albumName = intent.getStringExtra("album");		Log.d("Album",albumName);
 //               int trackLengthInSec = intent.getIntExtra("length", 0);	Log.d("length",""+trackLengthInSec);
                
                String trackName = intent.getStringExtra("track");		
                Log.d("Name",trackName);
                if (selectBtState!=null)
                	selectBtState.updateTrackName(trackName);
            } else if ( (action.equals(BroadcastTypes.ANDROID_PLAYBACK_STATE_CHANGED)) ||(action.equals(BroadcastTypes.SPOTIFY_PLAYBACK_STATE_CHANGED)) ) {
                boolean playing = intent.getBooleanExtra("playing", false);		Log.d("PLAY STATE ",""+playing);
                if (selectBtState!=null) {
	        		if (selectBtState.channel == BT_CHANNEL) {
	        			((BtFragment)mAdapter.getItem(1)).setBlinking(!playing);
	        		}
                }
            } else if  (action.equals(BroadcastTypes.HTC_PLAYBACK_STATE_CHANGED)) {
                boolean playing = intent.getBooleanExtra("isplaying", false);		Log.d("PLAY STATE ",""+playing);
                String trackName = intent.getStringExtra("track");					Log.d("Name",trackName);
                if (selectBtState!=null) {
                	selectBtState.updateTrackName(trackName);        
	        		if (selectBtState.channel == BT_CHANNEL) {
	        			((BtFragment)mAdapter.getItem(1)).setBlinking(!playing);
	        		}
                }
            } else if ( /*(action.equals(BroadcastTypes.SPOTIFY_QUEUE_CHANGED)) || */(action.equals(BroadcastTypes.ANDROID_QUEUE_CHANGED)) ) {
                Log.d("CHANGED","QUEUE");
                // Sent only as a notification, your app may want to respond accordingly.
            } else if ( (action.equals(BroadcastTypes.ANDROID_PLAYBACK_COMPLETE)) || (action.equals(BroadcastTypes.HTC_PLAYBACK_COMPLETE)) ){
                Log.d("PLAYBACK","COMPLETE");
                // Sent only as a notification, your app may want to respond accordingly.
            } 
        }
    };

}
