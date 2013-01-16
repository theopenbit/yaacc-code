package de.yaacc;

import java.util.LinkedList;

import org.teleal.cling.model.meta.Device;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import de.yaacc.config.SettingsActivity;
import de.yaacc.upnp.UpnpClient;

public class MainActivity extends Activity implements OnClickListener{

	UpnpClient uClient = null;
	
    
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        uClient = new UpnpClient();
    	uClient.initialize(getApplicationContext());
        
    	final Button showDeviceNumber = (Button) findViewById(R.id.nbDev);
    	showDeviceNumber.setOnClickListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
    	switch(item.getItemId()){
    	case R.id.menu_settings:
    		Intent i = new Intent(this,SettingsActivity.class);
    		startActivity(i);
    		return true;
    	default:
    		return super.onOptionsItemSelected(item);
    	}
    }

	@Override
	public void onClick(View v) {
		final TextView helloWorld = (TextView) findViewById(R.id.helloWorld);
		LinkedList<Device> deviceList= new LinkedList<Device>();
		deviceList.addAll(uClient.getDevices());
    	helloWorld.setText(uClient.getDevices().size()+" devices found");
		
	}
}
