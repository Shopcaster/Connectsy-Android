package com.connectsy.notifications;

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
		if (!listener.isRunning())
			listener.start(this);
		super.onStart(intent, startId);
	}
}
