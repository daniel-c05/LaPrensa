package com.thoughts.apps.laprensa;

import java.util.ArrayList;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
import android.view.View;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;
import com.google.ads.AdRequest;
import com.google.ads.AdView;
import com.thoughts.apps.laprensa.database.DbManager;
import com.thoughts.apps.laprensa.fragments.ArticleViewFragment;
import com.thoughts.apps.laprensa.utils.StringUtils;
import com.viewpagerindicator.UnderlinePageIndicator;

public class ArticleHub extends SherlockFragmentActivity implements ViewPager.OnPageChangeListener {
	
	public static final String KEY_ARTICLE_URL = "url:articulo";
	public static final String KEY_DISPLAY_THEME = "app:readertheme";
	public static final String KEY_DISPLAY_MODE = "app:displaymode";
	public static final String KEY_DISPLAY_DATE = "app:displaydate";
	public static final String KEY_FONT_SIZE = "app:fontsize";
	
	public static final int PORTADA = 0;
	public static final int AMBITOS = 1;
	public static final int PLAY = 2;
	public static final int PODERES = 3;
	public static final int REPORTAJES = 4;
	public static final int VIDA = 5;
	public static final int VOCES = 6;
	public static final int ACTIVOS = 7;
	public static final int PLANETA = 8;
	public static final int CULTURA = 9;
	public static final int TECNOLOGIA = 10;
	public static final int DEPARTAMENTALES = 11;
	public static final int EMPRESARIALES = 12;	
	public static final int CONTACTENOS = 13;
	public static final int MARCADORES = 14;
	public static final int STANDALONE = 15;
	
	AdView mAdView;
	
	ArrayList<String> mArticleLinks;	
	ViewPager mViewPager;
	ScreenSlidePagerAdapter mPagerAdapter;
	UnderlinePageIndicator mIndicator;
	int mCurrentPos, mCurDisplayMode, mCurFontSize, mCurrDisplayTheme;
	String mCurArticleLink, mCurrDate, mReadableDate, mParentClass;
	SharedPreferences mPreferences;
	SharedPreferences.Editor mEditor;
	/**
	 * True when reading an article url that was grabbed from a View Intent
	 */
	boolean isStandAlone = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_article_hub);	
		
		updatePrefDependentValues(false);
		
		mCurArticleLink = getIntent().getDataString();
		
		if (mCurArticleLink != null && mCurArticleLink != "") {
			if (mCurArticleLink.charAt(mCurArticleLink.length() -1) != '/' && mCurArticleLink.length() >= 38) {					
				isStandAlone = true;
				mCurDisplayMode = STANDALONE;
				mCurrDate = StringUtils.getTodaysDate();
				mArticleLinks = new ArrayList<String>();
				mArticleLinks.add(mCurArticleLink);
				mCurrentPos = 0;
			}
			else {
				setContentView(R.layout.empty);
				return;
			}			
		}
		else {
			Bundle extras = getIntent().getExtras();
			if (extras != null) {		
				//get the class name that started the intent
				mParentClass = getIntent().getExtras().getString(UserPreferences.EXTRA_PARENT);
				mCurDisplayMode = extras.getInt(KEY_DISPLAY_MODE);
				//If mCurrDate is null, getFeedLink will only returns today's values
				mCurArticleLink = extras.getString(KEY_ARTICLE_URL);
				
				if (mCurDisplayMode == STANDALONE) {
					isStandAlone = true;
					mArticleLinks = new ArrayList<String>();
					mCurrDate = StringUtils.getTodaysDate();
					mArticleLinks.add(mCurArticleLink);
				}
				else {
					//Get the supplied date, if no date is supplied it doesn't matter
					mCurrDate = extras.getString(KEY_DISPLAY_DATE);
					mArticleLinks = DbManager.getFeedLinks(this, mCurDisplayMode, mCurrDate);
				}				
				
				if (mArticleLinks == null || mArticleLinks.size() == 0) {
					//Nothing to display on the Pager
					setContentView(R.layout.empty);
					return;
				}
				mCurrentPos = mArticleLinks.indexOf(mCurArticleLink);
			}
		}			
		
		setupActionBar();
		
		setupAds();
		
		mPagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
		
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mIndicator = (UnderlinePageIndicator) findViewById(R.id.pager_indicator);
		
		mViewPager.setAdapter(mPagerAdapter);
		mIndicator.setViewPager(mViewPager);
		mIndicator.setOnPageChangeListener(this);
				
		mViewPager.setCurrentItem(mCurrentPos);
		if (isStandAlone || mArticleLinks.size() == 1) {
			mIndicator.setVisibility(View.GONE);
		}					
	}

	@Override
	protected void onResume() {
		super.onResume();
		updatePrefDependentValues(true);
	}
	
	@Override
	protected void onPause() {
		saveState();
		super.onPause();
	}
	
	private void setupAds() {
		 // Look up the AdView as a resource and load a request.
	    mAdView = (AdView)this.findViewById(R.id.adView);
	    mAdView.loadAd(new AdRequest());
	}
	
	private void saveState () {
		if (isStandAlone) {
			//Don't save position if only reading the one article
			return;
		}
		mEditor = mPreferences.edit();
		mEditor.putInt(Home.KEY_LIST_POSITION, mCurrentPos);
		mEditor.commit();
	}

	private void updatePrefDependentValues(boolean onResume) {
		if (mPreferences == null) {
			mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		}	
		if (isStandAlone) {
			mReadableDate = StringUtils.getCustomDate(mCurArticleLink);
		}
		else {
			mReadableDate = mPreferences.getString(Home.KEY_READABLE_DATE, StringUtils.getTodaysReadableDate());
		}
		if (onResume) {
			mCurFontSize = Integer.valueOf(mPreferences.getString(getString(R.string.pref_key_font_size), "0"));
			mCurrDisplayTheme = Integer.valueOf(mPreferences.getString(getString(R.string.pref_key_reader_theme), "4"));
			setWindowBackground();
			ArticleViewFragment.mCurFontSize = mCurFontSize;
			ArticleViewFragment.mCurrReaderTheme = mCurrDisplayTheme;
		}
	}

	private void setWindowBackground() {
		switch (mCurrDisplayTheme) {
		case ArticleViewFragment.READER_DARK:			
			getWindow().setBackgroundDrawableResource(R.color.bg_black);
			break;
		case ArticleViewFragment.READER_LIGHT:
			getWindow().setBackgroundDrawableResource(R.color.bg_light_gray);
			break;
		case ArticleViewFragment.READER_SEPIA:
			getWindow().setBackgroundDrawableResource(R.color.bg_sepia);
			break;
		default:
			Constants.logMessage("Failed to change background");
			break;
		}
	}

	private void setupActionBar() {

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		int title = R.string.portada;
		switch (mCurDisplayMode) {
		//set the action bar title
		case ArticleHub.MARCADORES:
			title = R.string.marcadores;
			break;
		case ArticleHub.ACTIVOS:
			title = R.string.activos;
			break;
		case ArticleHub.AMBITOS:
			title = R.string.ambitos;
			break;
		case ArticleHub.CULTURA:
			title = R.string.cultura;
			break;
		case ArticleHub.PLANETA:
			title = R.string.planeta;
			break;
		case ArticleHub.PLAY:
			title = R.string.play;
			break;
		case ArticleHub.PODERES:
			title = R.string.poderes;
			break;
		case ArticleHub.REPORTAJES:
			title = R.string.reportajes;
			break;
		case ArticleHub.TECNOLOGIA:
			title = R.string.tecnologia;
			break;
		case ArticleHub.VIDA:
			title = R.string.vida;
			break;
		case ArticleHub.VOCES:
			title = R.string.voces;
			break;
		case ArticleHub.DEPARTAMENTALES:
			title = R.string.departamentales;
			break;
		case ArticleHub.EMPRESARIALES:
			title = R.string.empresariales;
			break;
		case ArticleHub.STANDALONE:
			title = R.string.app_name;
			break;
		}
		getSupportActionBar().setTitle(getString(title));
		if (mCurDisplayMode == MARCADORES) {
			//hide the subtitle
			getSupportActionBar().setSubtitle(StringUtils.getReadableDate(mCurArticleLink));
		}
		else {
			getSupportActionBar().setSubtitle(mReadableDate);
		}
		

	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {		
		switch (item.getItemId()) {
		case android.R.id.home:
			Intent parentActivityIntent;
			if (mParentClass.contains("SearchResults")) {
				parentActivityIntent = new Intent(this, SearchResults.class);
	            parentActivityIntent.addFlags(
	                    Intent.FLAG_ACTIVITY_CLEAR_TOP |
	                    Intent.FLAG_ACTIVITY_NEW_TASK);
	            NavUtils.navigateUpTo(this, parentActivityIntent);
			}
			else {
				NavUtils.navigateUpFromSameTask(this);
			}					
			return true;		
		}
		return super.onOptionsItemSelected(item);
	}
	
	/**
	 * Simple Pager implementation set for to the ViewPager.
	 */
	class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
		
		Fragment mPrimaryFragment;
		Bundle args;

		public ScreenSlidePagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			mPrimaryFragment = new ArticleViewFragment();
			args = new Bundle();
			args.putString(KEY_ARTICLE_URL, mArticleLinks.get(position));
			args.putInt(KEY_FONT_SIZE, mCurFontSize);
			args.putInt(KEY_DISPLAY_THEME, mCurrDisplayTheme);
			mPrimaryFragment.setArguments(args);
			return mPrimaryFragment;			
		}

		@Override
		public int getCount() {
			return mArticleLinks.size();
		}
		
	}

	@Override
	public void onPageScrollStateChanged(int arg0) {
	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {
	}

	@Override
	public void onPageSelected(int arg0) {	
		mCurrentPos = arg0;
		mCurArticleLink = mArticleLinks.get(mCurrentPos);	
		if (isStandAlone || mCurDisplayMode == MARCADORES) {
			getSupportActionBar().setSubtitle(StringUtils.getReadableDate(mCurArticleLink));
		}		
	}
}
