package es.carlosrolindez.kbfinder;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;



public class FmFragment extends Fragment {
	public String fragmentName;
	private final Context mContext;
	
	private static TextView frequencyText;
	private static TextView RDSText;
	
	
	
	public FmFragment(Context context) {
		mContext = context;
		fragmentName =  "FM";
	}
	
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fm_fragment, container, false);
      	((TextView) rootView.findViewById(R.id.fragment_text)).setText(fragmentName);
        Typeface myTypeface = Typeface.createFromAsset(mContext.getAssets(), "seven_segments.ttf");
        frequencyText = (TextView)rootView.findViewById(R.id.frequency);
        frequencyText.setTypeface(myTypeface);
        RDSText = (TextView)rootView.findViewById(R.id.RDS);

		ImageView button_scan_down_FM = (ImageView)rootView.findViewById(R.id.scan_down_FM);
		ImageView button_dial_FM = (ImageView)rootView.findViewById(R.id.dial_FM);
		ImageView button_scan_up_FM = (ImageView)rootView.findViewById(R.id.scan_up_FM);
		
		frequencyText.setOnClickListener(new OnClickListener() 
		{
			@Override
			public void onClick(View v) 
			{
				showFmDialog();
			}
		});
		
		button_scan_down_FM.setOnClickListener(new OnClickListener() 
		{
			@Override
			public void onClick(View v) 
			{


			}
		});

		button_dial_FM.setOnClickListener(new OnClickListener() 
		{
			@Override
			public void onClick(View v) 
			{
				showFmDialog();
			}
		});

		button_scan_up_FM.setOnClickListener(new OnClickListener() 
		{
			@Override
			public void onClick(View v) 
			{

			}
		});
           
        
        
        return rootView;	        
    }
    
    public void setFrequency(String frequency) {
        frequencyText.setText(frequency);    	
    }
    
    public void setRDS(String RDS) {
    	RDSText.setText(RDS);    	
    }  
    
    public void showFmDialog() {
    	final Dialog fmDialog = new Dialog(mContext, R.style.CustomDialog);
    	fmDialog.setTitle(getResources().getString(R.string.fm_dial));
    	fmDialog.setContentView(R.layout.fm_dial);
        final FmPicker megaherzPicker = (FmPicker) fmDialog.findViewById(R.id.megaherzs);
        final FmPicker kiloherzPicker = (FmPicker) fmDialog.findViewById(R.id.kiloherzs);
        megaherzPicker.setMaxValue(108);
        megaherzPicker.setMinValue(87);
        megaherzPicker.setWrapSelectorWheel(true); 
        kiloherzPicker.setMaxValue(9);
        kiloherzPicker.setMinValue(0);
        kiloherzPicker.setWrapSelectorWheel(true);
        fmDialog.show();

    	
    }

}
