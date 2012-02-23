package ru.cyrusmith;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class TimerDemoActivity extends Activity {
	private static final int START_DIALOG = 1;
	
	private TextView mTicksView = null;	
	
	private class TimerServiceHandler extends Handler
	{
		@Override
		public void handleMessage(Message msg) {
			Log.d(TimerConstants.TAG,"TimerDemoActivity handle timer");
			mTicksView.setText(String.valueOf(msg.arg2));
		}		
	}
	
	private final Messenger mMessenger = new Messenger(new TimerServiceHandler());
	
	private Messenger mServiceMessenger = null;
	private Boolean isBound = false;
	
	private ServiceConnection mServiceConn = new ServiceConnection() {
		
		public void onServiceDisconnected(ComponentName name) {
			isBound = false;
		}
		
		public void onServiceConnected(ComponentName name, IBinder service) {
			Log.d(TimerConstants.TAG,"Bound to service");
			mServiceMessenger = new Messenger(service);
			Message msg = Message.obtain();
			msg.replyTo = mMessenger;
			try {
				mServiceMessenger.send(msg);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
			isBound = true;
		}
		
	};
	
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	
    	super.onCreate(savedInstanceState);
    	
        Log.d(TimerConstants.TAG,"Creating UI");
        
        setContentView(R.layout.main);
        
        mTicksView = (TextView)findViewById(R.id.ticks);
        
        ((Button)findViewById(R.id.btn_start)).setOnClickListener(new View.OnClickListener() {			
			public void onClick(View v) {
				showDialog(START_DIALOG);
			}
		});
        ((Button)findViewById(R.id.btn_stop)).setOnClickListener(new View.OnClickListener() {			
        	public void onClick(View v) {
        		Log.d(TimerConstants.TAG,"About to stop");
        		stopService(new Intent(TimerDemoActivity.this, TimerService.class));
        	}
        });
        
        doBindService();
        
    }

	@Override
	protected Dialog onCreateDialog(int id) {
		
		Dialog d = null;
		
		switch (id) {
		case START_DIALOG:
			AlertDialog.Builder alert = new AlertDialog.Builder(this);

			alert.setTitle("Title");
			alert.setMessage("Message");

			// Set an EditText view to get user input
			final EditText input = new EditText(this);
			alert.setView(input);

			alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					//startTimerService(Integer.valueOf(input.getText().toString()));
					doBindService();
				}
			});

			alert.setNegativeButton("Cancel",null);
			
			d = alert.create();
			
			break;

		default:
			break;
		}
		
		return d;
		
	}
	
	private void startTimerService(Integer duration){
		startService(new Intent(this,TimerService.class).putExtra("duration", duration));		
	}
	
	private void doBindService(){
		if(!isBound){
			Log.d(TimerConstants.TAG,"Binding to service");
			bindService(new Intent(this,TimerService.class), mServiceConn, Context.BIND_AUTO_CREATE);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		doBindService();
	}
}