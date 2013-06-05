package com.thoughts.apps.laprensa.service;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.preference.PreferenceManager;

import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;
import com.thoughts.apps.laprensa.Constants;
import com.thoughts.apps.laprensa.R;
import com.thoughts.apps.laprensa.database.DbManager;

/**
 *
 * CacheCleaner is in charge of cleaning the old article data from the database. 
 *
 */
public class CacheCleaner extends IntentService {

	/**
	 * As long as it isn't zero and it isn't the same request code as another's PI we're fine
	 */
	private static final int REQUEST_CODE = 321;
	/**
	 * The time in milliseconds when the last alarm was placed
	 */
	public static final String KEY_LAST_ALARM = "key:last_alarm_time";
	public static final long MS_IN_A_DAY = 86400000;

	public CacheCleaner() {
		super("CacheManager");	
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Constants.logMessage("IntentService started, cleaning cache");

		SharedPreferences mPreferences = PreferenceManager.getDefaultSharedPreferences(this);

		int interval = Integer.valueOf(mPreferences.getString(getString(R.string.pref_key_ccleaner_interval), getString(R.string.pref_default_ccleaner_interval)));
		//Cleanup anything older than the interval back in time
		DbManager.cleanup(this, interval);

		//cleanup old images too
		switch (interval) {
		case 3:
			UrlImageViewHelper.cleanup(this, UrlImageViewHelper.CACHE_DURATION_THREE_DAYS);
			break;
		case 5:
			UrlImageViewHelper.cleanup(this, UrlImageViewHelper.CACHE_DURATION_FIVE_DAYS);
			break;
		case 7:
			UrlImageViewHelper.cleanup(this, UrlImageViewHelper.CACHE_DURATION_ONE_WEEK);
			break;
		}	

		//Simply schedule another cleanup to happen
		scheduleNextCleanup(this, (long)interval * MS_IN_A_DAY, true);			

	}

	/**
	 * 
	 * Re-schedule an alarm, either after boot, or after a preference was changed.
	 * This will try to find out when was the last time an alarm was set via {@link #KEY_LAST_ALARM} 
	 * and attempt to re-schedule based on that date. If no previous alarm was set, it will simply schedule a new alarm 
	 * using the newValue supplied.
	 * 
	 * @param context The context used to access the preference object. 
	 * @param newValue The new interval between cleanups, in days. Pass 0 to use the default value set in preferences.
	 */
	public static void reScheduleCleanup (final Context context, int newValue) {

		Constants.logMessage("Re-scheduling alarms after boot completed");

		SharedPreferences mPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		//get the current interval we are using for cleanups in days
		long alarmInterval = newValue != 0? newValue : Integer.valueOf(mPreferences.getString("pref_key_ccleaner_interval", "3"));
		//time the interval by amount of ms in a day
		alarmInterval = alarmInterval * MS_IN_A_DAY;
		//get the last time in miliseconds that we set an alarm
		//Whenever I schedule an alarm in the IntentService 
		//I store the time in ms to know when it was last scheduled
		long lastAlarm = mPreferences.getLong(CacheCleaner.KEY_LAST_ALARM, 0);
		if (lastAlarm == 0) {
			//If no previous alarm is set, schedule it normally
			CacheCleaner.scheduleNextCleanup(context, alarmInterval, true);
		}
		else {
			//If there was an alarm set previously
			//The difference between the alarmInterval and the amount of ms ellapsed since last alarm
			//is the new time we will schedule this for
			CacheCleaner.scheduleNextCleanup(context, (alarmInterval - (System.currentTimeMillis() - lastAlarm)), false);
		}
	}

	/**
	 * 
	 * Schedule an alarm to cleanup the old articles and images from our database. 
	 * 
	 * @param context The context used to access the preference object. 
	 * @param msFromNow How many milliseconds in the future should the task be executed at.  
	 * @param setLastAlarm Whether or not to overwrite the time the last alarm was set. 
	 * Pass false when re-scheduling after boot or preference change, otherwise pass true.
	 */
	public static void scheduleNextCleanup (final Context context, long msFromNow, boolean setLastAlarm) {

		Constants.logMessage("Scheduling cleanup alarm to happen within: " + msFromNow/(1000*60*60) + " hours");

		Intent intent = new Intent(context, AlarmReceiver.class);		
		intent.setAction(AlarmReceiver.CLEAN_CACHE);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(context, REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);

		AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

		if (msFromNow != -1) {
			Constants.logMessage("Alarm set");
			alarm.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + msFromNow, pendingIntent);
		}		

		if (setLastAlarm) {
			//Overwrite the value in milliseconds the last alarm was placed. 
			//This value will be used later to re-schedule alarms when the device is rebooted or when the cleanup interval changes
			SharedPreferences mPreferences = PreferenceManager.getDefaultSharedPreferences(context);
			SharedPreferences.Editor mEditor = mPreferences.edit();
			mEditor.putLong(KEY_LAST_ALARM, System.currentTimeMillis());
			mEditor.commit();
		}		
	}
	
	/**
	 * 
	 * Schedule an alarm to cleanup the old articles and images from our database.
	 * 
	 * @param context The context used to access the preference object.
	 * @param setLastAlarm Whether or not to overwrite the time the last alarm was set.
	 */
	public static void scheduleNextCleanup (final Context context, boolean setLastAlarm) {
		
		SharedPreferences mPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		
		long alarmInterval = Integer.valueOf(mPreferences.getString("pref_key_ccleaner_interval", "3"));
		//time the interval by amount of ms in a day
		alarmInterval = alarmInterval * MS_IN_A_DAY;

		Intent intent = new Intent(context, AlarmReceiver.class);		
		intent.setAction(AlarmReceiver.CLEAN_CACHE);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(context, REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);

		AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		
		if (alarmInterval != -1) {
			Constants.logMessage("Alarm set");
			alarm.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + alarmInterval, pendingIntent);
		}		

		if (setLastAlarm) {
			//Overwrite the value in milliseconds the last alarm was placed. 
			//This value will be used later to re-schedule alarms when the device is rebooted or when the cleanup interval changes
			SharedPreferences.Editor mEditor = mPreferences.edit();
			mEditor.putLong(KEY_LAST_ALARM, System.currentTimeMillis());
			mEditor.commit();
		}		
	}

}
