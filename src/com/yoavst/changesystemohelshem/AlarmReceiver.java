package com.yoavst.changesystemohelshem;

import org.androidannotations.annotations.App;
import org.androidannotations.annotations.EReceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import java.util.Calendar;

/**
 * Being called on 21:05 by the AlarmManager.
 * 
 * @author Yoav Sternberg
 * 
 */
@EReceiver
public class AlarmReceiver extends BroadcastReceiver {
	@App
	MyApp mApp;
	public static final int REQUEST_CODE = 12345;

	@Override
	public void onReceive(Context context, Intent intent) {
		// Create Calendar
		Calendar cal = Calendar.getInstance();
		// Work only with day, month and year
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		boolean isTomorrowHoliday = new Holidays().isTomorrowHoliday();
		/*
		 * If the day of the last notification is less then the current day And
		 * the day isn't Friday
		 */
		if (mApp.getPreferences().getLastNotificationDay().get() < cal
				.getTimeInMillis()
				&& cal.get(Calendar.DAY_OF_WEEK) != Calendar.FRIDAY
				&& !isTomorrowHoliday) {
			// Show the notification
			BackgroundService_.IntentBuilder_ intentBuilder = BackgroundService_
					.intent(context);
			intentBuilder.get().putExtra(BackgroundService_.LAYER_EXTRA,
					mApp.getPreferences().getLayer().get());
			intentBuilder.get().putExtra(BackgroundService_.CLASS_EXTRA,
					mApp.getPreferences().getMotherClass().get());
			intentBuilder.get().putExtra(BackgroundService.DOWNLOAD_EXTRA,
					false);
			intentBuilder.start();
			// set the current day as last day
			mApp.getPreferences().getLastNotificationDay()
					.put(cal.getTimeInMillis());
		}

	}
}
