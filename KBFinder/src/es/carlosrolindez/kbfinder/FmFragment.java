package es.carlosrolindez.kbfinder;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;



public class FmFragment extends Fragment {
	public String fragmentName;
	
	public FmFragment() {
		fragmentName =  "FM";
	}
	
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fm_fragment, container, false);
      	((TextView) rootView.findViewById(R.id.fragment_text)).setText(fragmentName);
        return rootView;	
        
        
    }
    

}
