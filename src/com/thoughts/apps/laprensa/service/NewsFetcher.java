package com.thoughts.apps.laprensa.service;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.SystemClock;
import android.preference.PreferenceManager;

import com.thoughts.apps.laprensa.Constants;
import com.thoughts.apps.laprensa.R;
import com.thoughts.apps.laprensa.utils.HtmlHelper;

/**
*
* NewsFetcher is in charge of downloading new articles periodically, 
* based on the refresh rate set by the user. 
*
*/
public class NewsFetcher extends IntentService {

	public NewsFetcher() {
		super("NewsFetcher");	
	}
	
	public static final int REQUEST_CODE = 123;

	@Override
	protected void onHandleIntent(Intent intent) {
		Constants.logMessage("IntentService started, fetching articles");
		SharedPreferences mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
		if (networkInfo != null && networkInfo.isConnected()) {
			//refresh only if there was an active connection
			HtmlHelper.actualizarLista(this, HtmlHelper.WEB_PREFIX);
		}
		else{
			Constants.logMessage("No connection, re-scheduling refresh");
			scheduleNextRefresh(this, Long.valueOf(mPreferences.getString(getString(R.string.pref_key_sync_interval), "7200000")));
		}		
	}

	/**
	 * 
	 * Schedule an alarm to have new articles fetched from www.laprensa.com.ni
	 * 
	 * @param context The context, used to obtain the AlarmManager instance.
	 * @param msFromNow Amount of milliseconds to wait before executing task. 
	 */
	public static void scheduleNextRefresh (final Context context, long msFromNow) {
		
		Constants.logMessage("Scheduling fetcher alarm to happen within: " + msFromNow/(1000*60) + " minutes");
		
		Intent intent = new Intent(context, AlarmReceiver.class);		
		intent.setAction(AlarmReceiver.FETCH_NEWS);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(context, REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		
		AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		
		if (msFromNow != -1) {
			Constants.logMessage("Alarm set");
			alarm.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + msFromNow, pendingIntent);
		}				
	}

}
