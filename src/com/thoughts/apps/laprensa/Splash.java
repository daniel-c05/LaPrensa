package com.thoughts.apps.laprensa;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;

import com.thoughts.apps.laprensa.service.CacheCleaner;
import com.thoughts.apps.laprensa.utils.HtmlHelper;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public class Splash extends Activity {

	private static final int DELAY_MILLIS = 1500;

	public static final String KEY_FIRST_BOOT = "app:first_boot";
	
	private boolean isOldApi; 
	public static boolean isSupposedToBeRefreshing = false, isFirstBoot = false; 
	private boolean isRefreshOnStart = false;
	private Class<?> clazs;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		final SharedPreferences mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		
		isFirstBoot = mPreferences.getBoolean(KEY_FIRST_BOOT, true);	
		if (isFirstBoot) {
			//start refreshing
			loadData();
			//Schedule the cache cleaner to use default values
			CacheCleaner.scheduleNextCleanup(Splash.this, true);
			//prepare to show tutorial
			clazs = Tutorial.class;
		}
		else {
			isSupposedToBeRefreshing = false;
			clazs = Home.class;
		}
		
		isRefreshOnStart = mPreferences.getBoolean(getString(R.string.pref_key_sync_on_launch), false);
		if (isRefreshOnStart && !isFirstBoot) {
			//refresh the list only when it's not first boot, so we avoid double network request
			loadData();
		}
		else {
			isSupposedToBeRefreshing = false;
		}
		
		isOldApi = Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN ? true : false;
		
		Handler mHandler = new Handler();
		mHandler.postDelayed(new Runnable() {

			@Override
			public void run() {					
				Intent tutorial = new Intent(Splash.this, clazs);
				if (isOldApi) {
					startActivity(tutorial);
				}
				else {
					Bundle translateBundle = ActivityOptions.makeCustomAnimation(Splash.this, R.anim.slide_out, R.anim.slide_in).toBundle();
					startActivity(tutorial, translateBundle);
				}	
				
				Splash.this.finish();
			}
		}, DELAY_MILLIS);
	}
	
	private void loadData () {
		ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
		if (networkInfo != null && networkInfo.isConnected()) {						
			isSupposedToBeRefreshing = true;
			HtmlHelper.actualizarLista(Splash.this, HtmlHelper.WEB_PREFIX);
		} else {
			isSupposedToBeRefreshing = false;									
		}
	}	
}
