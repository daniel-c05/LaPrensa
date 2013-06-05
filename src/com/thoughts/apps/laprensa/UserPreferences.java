package com.thoughts.apps.laprensa;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceScreen;

import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.actionbarsherlock.view.MenuItem;
import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;
import com.thoughts.apps.laprensa.database.DbManager;
import com.thoughts.apps.laprensa.service.CacheCleaner;
import com.thoughts.apps.laprensa.service.NewsFetcher;

public class UserPreferences extends SherlockPreferenceActivity {
	
	private static final String VERSION_UNAVAILABLE = "N/A";
	public static final String EXTRA_PARENT = "pref:parent";
	
	Preference mPrivacyPolicyPref, mVersionPref, mSyncIntervalPref, mFeedbackPref, mReportProblemPref,
		mClearCachePref, mCacheCleanerIntervalPref, mOpenSourcePref, mViewTutorialPref;
	String mParentClass;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mParentClass = getIntent().getExtras().getString(EXTRA_PARENT);
		setupActionBar();
		loadPreferences();
	}
	
	@SuppressWarnings("deprecation")
	private void loadPreferences() {
		addPreferencesFromResource(R.xml.preferences);   	
		mPrivacyPolicyPref = findPreference(getString(R.string.pref_key_privacy_policy));	
		mVersionPref = findPreference(getString(R.string.pref_key_app_version));
		mSyncIntervalPref = findPreference(getString(R.string.pref_key_sync_interval));
		mFeedbackPref = findPreference(getString(R.string.pref_key_send_feedback));
		mReportProblemPref = findPreference(getString(R.string.pref_key_report_problem));
		mClearCachePref = findPreference(getString(R.string.pref_key_clear_cache));
		mCacheCleanerIntervalPref = findPreference(getString(R.string.pref_key_ccleaner_interval));
		mOpenSourcePref = findPreference(getString(R.string.pref_key_open_source_licences));
		mViewTutorialPref = findPreference(getString(R.string.pref_key_view_tutorial));
		
		mSyncIntervalPref.setOnPreferenceChangeListener(mOnPreferenceChangeListener);
		mCacheCleanerIntervalPref.setOnPreferenceChangeListener(mOnPreferenceChangeListener);
		
		updateBuildVersionInfo();
	}
	
	@Override
	@Deprecated
	public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
			Preference preference) {
		if (preference == mPrivacyPolicyPref) {
			showWebViewActivity(WebHolder.PRIVACY);
		}
		else if (preference == mOpenSourcePref) {
			showWebViewActivity(WebHolder.LICENCE);
		}
		else if (preference == mViewTutorialPref) {
			startTutorial();
		}
		else if (preference == mFeedbackPref) {
			startEmail();
		}
		else if (preference == mReportProblemPref) {
			launchProblemForm();
		}
		else if (preference == mClearCachePref) {
			showDeleteHistoryDialog();
		}
		return super.onPreferenceTreeClick(preferenceScreen, preference);
	}
	
	private void startTutorial() {
		Intent tutorial = new Intent(this, Tutorial.class);
		startActivity(tutorial);
	}

	private void showDeleteHistoryDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);    		
		builder.setTitle(getString(R.string.pref_title_clear_cache))
		.setIcon(R.drawable.ic_action_warning)
		.setMessage(R.string.pref_warning_clear_cache)
		.setPositiveButton(android.R.string.ok, new OnClickListener() {				
			@Override
			public void onClick(DialogInterface dialog, int which) {
				DbManager.cleanup(UserPreferences.this);
				UrlImageViewHelper.cleanup(UserPreferences.this, UrlImageViewHelper.CACHE_DURATION_ONE_DAY);
				dialog.dismiss();
			}
		})
		.setCancelable(true).setNegativeButton(getString(android.R.string.cancel), new OnClickListener() {				
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		AlertDialog dialog = builder.create();
		dialog.show();
	}

	private void showWebViewActivity(int content) {
		Intent intent = new Intent(this, WebHolder.class);
		intent.putExtra(WebHolder.EXTRA_CONTENT, content);
		startActivity(intent);
	}
	
	private void launchProblemForm() {
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setData(Uri.parse(getString(R.string.pref_form_url)));
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
		startActivity(intent);
	}

	private void startEmail() {
		Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
	            "mailto", getString(R.string.pref_email_address), null));
		intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.pref_email_subject));
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
		startActivity(Intent.createChooser(intent, getString(R.string.email_prompt)));
	}

	OnPreferenceChangeListener mOnPreferenceChangeListener = new OnPreferenceChangeListener() {
		
		@Override
		public boolean onPreferenceChange(Preference preference, Object newValue) {
			if (preference == mSyncIntervalPref) {
				if (newValue.equals("never")) {
					NewsFetcher.scheduleNextRefresh(UserPreferences.this, -1);
				}
				else {
					long interval = Long.valueOf((String) newValue);
					NewsFetcher.scheduleNextRefresh(UserPreferences.this, interval);
				}				
			}
			else if (preference == mCacheCleanerIntervalPref) {
				int interval = Integer.valueOf((String) newValue);
				//Pass the new interval value as otherwise calling re-schedule here would use the previous value
				CacheCleaner.reScheduleCleanup(UserPreferences.this, interval);
			}
			return true;
		}
	};
	
	private void updateBuildVersionInfo() {
		PackageManager pm = getPackageManager();
		String packageName = getPackageName();
		String versionName;
		try {
			PackageInfo info = pm.getPackageInfo(packageName, 0);
			versionName = info.versionName;
		} catch (PackageManager.NameNotFoundException e) {
			versionName = VERSION_UNAVAILABLE;
		}
		
		mVersionPref.setSummary(packageName + " v" + versionName);
	}	

	private void setupActionBar() {

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);		

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:				 
			if (mParentClass.contains("ArticleView")) {
				Intent parentActivityIntent = new Intent(this, ArticleHub.class);
	            parentActivityIntent.addFlags(
	                    Intent.FLAG_ACTIVITY_CLEAR_TOP |
	                    Intent.FLAG_ACTIVITY_NEW_TASK);
	            startActivity(parentActivityIntent);
	            finish();
			}
			else {
				Intent parentActivityIntent = new Intent(this, Home.class);
	            parentActivityIntent.addFlags(
	                    Intent.FLAG_ACTIVITY_CLEAR_TOP |
	                    Intent.FLAG_ACTIVITY_NEW_TASK);
	            startActivity(parentActivityIntent);
	            finish();
			}			
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

}
