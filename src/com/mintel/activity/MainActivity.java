package com.mintel.activity;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
//import android.widget.TextView;
import android.widget.*;
import com.mintel.sip.R;
import com.mintel.sip.PeerManager;
import com.mintel.sip.PeerListner;

public class MainActivity extends Activity implements PeerListner{

	private static final int RESULT_SETTINGS = 1;
	EditText mNumber;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
       
		statusTxt = (TextView) findViewById(R.id.txtStatus);
		mNumber   = (EditText)findViewById(R.id.TxtNumPad);
		//showUserSettings();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.settings, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {

		case R.id.menu_settings:
			Intent i = new Intent(this, UserSettingActivity.class);
			startActivityForResult(i, RESULT_SETTINGS);
			break;

		}

		return true;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		switch (requestCode) {
		case RESULT_SETTINGS:
			showUserSettings();
			break;

		}

	}
	
	public void onClick(View v) {

		switch (v.getId()) {
		case  R.id.btnRegister: {
	   
	    	peer = new PeerManager(this,this);
	    	peer.register();
	   
		break;
		}

		case R.id.btnEnd: {
		// do something for button 2 click
			    if(peer == null) return;
			    String num = mNumber.getText().toString();
			    if(num.length()==0) return;
				peer.invite(num);
			//	statusTxt.setText("Calling... " + "");
			
		break;
		}

		default:
		break;	

		}
	}

	private void showUserSettings() {
		SharedPreferences sharedPrefs = PreferenceManager
				.getDefaultSharedPreferences(this);

		StringBuilder builder = new StringBuilder();

		builder.append("\n Username: "
				+ sharedPrefs.getString("prefUsername", "NULL"));

		builder.append("\n Password:"
				+ sharedPrefs.getString("prefPassword","NULL"));

		builder.append("\n Sip Server: "
				+ sharedPrefs.getString("prefServerIp", "NULL"));

		//TextView settingsTextView = (TextView) findViewById(R.id.textUserSettings);

		//settingsTextView.setText(builder.toString());
	}

	
	

	@Override
	public void setRegisterStatus(final String msg) {
		// TODO Auto-generated method stub
		System.out.println("peer: " + msg);
		// 
		statusTxt.post(new Runnable() {
		    public void run() {
		    	statusTxt.setText(msg);
		    } 
		});
		
		//statusTxt.setText(msg);
	}
	
	private PeerManager peer;
	TextView statusTxt;
	@Override
	public void setCallAlert(final String calle) {
		final Context mContext = this;
		 runOnUiThread(new Runnable() {
             public void run() {

            		// TODO Auto-generated method stub
         		new AlertDialog.Builder(mContext)
         	    .setTitle("Incoming call")
         	    .setMessage("Incoming from " + calle + " . Accept?")
         	    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
         	        public void onClick(DialogInterface dialog, int which) { 
         	            // continue with delete
         	        	peer.acceptCall();
         	        }
         	     })
         	    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
         	        public void onClick(DialogInterface dialog, int which) { 
         	            // do nothing
         	        	peer.rejectCall();
         	        }
         	     })
         	    .setIcon(android.R.drawable.ic_dialog_alert)
         	     .show();	


            }
        });
		 
		}

}
