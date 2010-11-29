package com.connectsy2.notifications;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class NotificationService extends Service {
	NotificationListener listener;

	@Override
	public void onCreate() {
		super.onCreate();
		
		listener = NotificationListener.getInstance();
		listener.start(this);
	}
	
	@Override
	public void onDestroy() {
		listener.stop();
		
		super.onDestroy();
	}
	
	public NotificationListener getListener() {
		return listener;
	}

	@Override
	public IBinder onBind(Intent intent) {
		return new NotificationServiceBinder(listener);
	}

	@Override
	public void onStart(Intent intent, int startId) {
		if (intent == null || intent.getAction() == null || 
				!intent.getAction().equals("com.connectsy2.STOP_NOTIFICATIONS"))
			if (!listener.isRunning())
				listener.start(this);
		super.onStart(intent, startId);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (intent != null){
			String action = intent.getAction();
			if (action != null && action.equals("com.connectsy2.STOP_NOTIFICATIONS"))
				stopSelf();
		}
		return Service.START_STICKY;
	}
}
