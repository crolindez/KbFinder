package es.carlosrolindez.kbfinder;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.ListView;
import android.widget.Toast;

public class KBfinderActivity extends Activity  /*implements KBdeviceListAdapter.ConnectOnClick */{
	private static String TAG = "KBfinder";

    private BluetoothAdapter mBluetoothAdapter = null;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.activity_main);
		
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

		// If the adapter is null, then Bluetooth is not supported
		if (mBluetoothAdapter == null) {
		    Toast.makeText(this, getString(R.string.bt_not_availabe), Toast.LENGTH_LONG).show();
		    finish();
		}	
    }
	
    @Override
    public void onStart() {
        super.onStart();

        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, Constants.REQUEST_ENABLE_BT);
        }
        else
    		new A2dpService(this, (ListView)findViewById(R.id.list));

    }
    
    @Override
    public void onResume() {
        super.onResume();
    }

    
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
    	Log.e(TAG,"result");
        switch (requestCode) {
            case Constants.REQUEST_ENABLE_BT:
            	Log.e(TAG,"REQUEST_ENABLE_BT");
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                		new A2dpService(this, (ListView)findViewById(R.id.list));
                } else {
                    Toast.makeText(this, R.string.bt_not_enabled,Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;           
        }
    }


		
	@Override
	protected void onDestroy() {
		A2dpService.closeService();
		super.onDestroy();
	}
		


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId())
		{
	        case R.id.bt_scan: 
	            // Launch the DeviceListActivity to see devices and do scan
	        	doDiscovery();
	            return true;

			case R.id.action_settings:
                return true;
		}

		return super.onOptionsItemSelected(item);
	}
	

	
	/**
     * Start device discover with the BluetoothAdapter
     */
    private void doDiscovery() {

        // Indicate scanning in the title
        setProgressBarIndeterminateVisibility(true);

        // Request discover from BluetoothAdapter
        mBluetoothAdapter.startDiscovery();
    }
    

}
