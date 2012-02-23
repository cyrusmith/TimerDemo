package ru.cyrusmith;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

public class TimerService extends Service {
	
	private ScheduledExecutorService mScheduer = null;
	private static Boolean isRunning = false;
	private final CopyOnWriteArrayList<Messenger> mListeners = new CopyOnWriteArrayList<Messenger>(); 
	
	private class BindHandler extends Handler
	{
		@Override
		public void handleMessage(Message msg) {
			if(msg.replyTo!=null){
				Log.d(TimerConstants.TAG,"Added activity to server");
				mListeners.add(msg.replyTo);
			}
			super.handleMessage(msg);
		}		
	}	
	
	private final Messenger mMessenger = new Messenger(new BindHandler());
	
	private AtomicInteger mTicks = new AtomicInteger(0);
	
	private Runnable mUpdTask = new Runnable() {		
		public void run() {
			int ticks = mTicks.incrementAndGet();
			Log.d(TimerConstants.TAG,"Tick = " + ticks);
			if(mListeners.size() > 0){
				Log.d(TimerConstants.TAG,"Send to activity");
				Message msg = Message.obtain();
				msg.arg2 = ticks;
				try {
					mListeners.get(0).send(msg);
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}
			TimerWidetProvider.updateWidgets(getApplicationContext(), ticks);
		}
	};
	
	@Override
	public void onCreate() {
		Log.d(TimerConstants.TAG,"Service created");
		isRunning = true;
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		Log.d(TimerConstants.TAG,"onDestroy");
		stopTimer();
		super.onDestroy();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Bundle data = intent.getExtras();
		if(mScheduer==null || mScheduer.isShutdown()){
			Log.d(TimerConstants.TAG,"onStartCommand duration : " + data.getInt("duration"));
			startTimer();		
		}
		else
		{
			Log.d(TimerConstants.TAG,"Already started. Stop first");
		}
		return START_NOT_STICKY;		
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mMessenger.getBinder();
	}
	
	private void restartTimer(){

		stopTimer();
		
		mScheduer = Executors.newSingleThreadScheduledExecutor(); 
		mScheduer.scheduleAtFixedRate(mUpdTask, 0, 1, TimeUnit.SECONDS);
	}
	
	private void stopTimer()
	{
		if(mScheduer != null && !mScheduer.isTerminated()){
			mScheduer.shutdownNow();
			mTicks.set(0);
		}		
	}
	
	private void startTimer(){
		restartTimer();
	}
	
	public static boolean isRunning()
	{
		return isRunning;
	}
	
}
