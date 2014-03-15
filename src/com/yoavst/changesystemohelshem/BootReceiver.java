package com.yoavst.changesystemohelshem;

import java.util.Calendar;
import java.util.GregorianCalendar;

import org.androidannotations.annotations.EReceiver;
import org.androidannotations.annotations.SystemService;
import org.androidannotations.annotations.sharedpreferences.Pref;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * The class set alarm to 21:05 for download changes if the user enabled that.
 * 
 * @author yoav Sternberg
 */
@EReceiver
public class BootReceiver extends BroadcastReceiver {
	/**
	 * User preferences for checking if the user enabled notification
	 */
	@Pref
	Prefs_ mPrefs;
	@SystemService
	AlarmManager mAlarmManager;

	@Override
	public void onReceive(Context context, Intent intent) {
		// If notifications enabled by the user
		if (mPrefs.getNotification().getOr(false)) {
			// Create intent that the AlarmReceiver will catch
			Intent mIntent = new Intent("UPDATE_CHANGES");
			PendingIntent mPendingIntent = PendingIntent.getBroadcast(context,
					0, mIntent, 0);
			// Set alarm for 21:05
			Calendar cal = new GregorianCalendar();
			cal.set(Calendar.HOUR_OF_DAY, 21);
			cal.set(Calendar.MINUTE, 05);
			mAlarmManager.setRepeating(AlarmManager.RTC_WAKEUP,
					cal.getTimeInMillis(), 86400000l, mPendingIntent);
		}
	}
}
