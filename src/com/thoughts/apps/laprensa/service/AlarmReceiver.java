package com.thoughts.apps.laprensa.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.thoughts.apps.laprensa.Constants;

/**
 * 
 * AlarmReceiver is in charge of receiving PendingIntents from the AlarmManager
 * to later start either a CacheCleaning service or a News Fetching service. 
 *
 */
public class AlarmReceiver extends BroadcastReceiver {

	/**
	 * Start a {@link NewsFetcher} Intent Service to download latest articles from www.laprensa.com.ni
	 */
	public static final String FETCH_NEWS ="com.thoughts.apps.laprensa.fetch_news";
	/**
	 * Start a {@link CacheCleaner} Intent Service to delete old data from the database
	 */
	public static final String CLEAN_CACHE ="com.thoughts.apps.laprensa.clean_cache";

	@Override
	public void onReceive(Context context, Intent intent) {

		Constants.logMessage("Broadcast received from AlarmManager");
		
		final String action = intent.getAction();
		
		if (action == null) {
			return;
		}
		
		if (action.equals(FETCH_NEWS)) {
			Intent fetcher = new Intent(context, NewsFetcher.class);
			context.startService(fetcher);
		}
		else if (action.equals(CLEAN_CACHE)) {
			Intent cleaner = new Intent(context, CacheCleaner.class);
			context.startService(cleaner);
		}
		else if (action.equals(Intent.ACTION_BOOT_COMPLETED)) {
			//re-schedule cache cleaner
			CacheCleaner.reScheduleCleanup(context, 0);
			//set an alarm for the news fetcher to start
			SharedPreferences mPreferences = PreferenceManager.getDefaultSharedPreferences(context);
			NewsFetcher.scheduleNextRefresh(context, Long.valueOf(mPreferences.getString("pref_key_sync_interval", "7200000")));
		}
	}
}
