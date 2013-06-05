package com.thoughts.apps.laprensa;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.thoughts.apps.laprensa.fragments.TutorialPageFragment;
import com.viewpagerindicator.UnderlinePageIndicator;

public class Tutorial extends SherlockFragmentActivity implements ViewPager.OnPageChangeListener {

	public static final int PAGER_COUNT = 5;

	UnderlinePageIndicator mIndicator;
	ViewPager mViewPager;
	TutorialPagerAdapter mAdapter;
	
	private int mCurrentPagerItem;
	
	private MenuItem mNextItem, mPrevItem;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tutorial);

		getSupportActionBar().show();
		
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mIndicator = (UnderlinePageIndicator) findViewById(R.id.pager_indicator);

		mAdapter = new TutorialPagerAdapter(getSupportFragmentManager());

		mViewPager.setPageMargin(8);
		mViewPager.setAdapter(mAdapter);
		mIndicator.setViewPager(mViewPager);
		mIndicator.setOnPageChangeListener(this);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getSupportMenuInflater().inflate(R.menu.tutorial, menu);
		mNextItem = menu.findItem(R.id.action_next);
		mPrevItem = menu.findItem(R.id.action_previous);
		updateActionBar();
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_skip:
			showHome();
			return true;
		case R.id.action_next:
			mCurrentPagerItem++;
			if (mCurrentPagerItem == PAGER_COUNT) {
				showHome();
				return true;
			}
			mViewPager.setCurrentItem(mCurrentPagerItem);
			return true;
		case R.id.action_previous:
			mCurrentPagerItem--;
			mViewPager.setCurrentItem(mCurrentPagerItem);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void showHome() {
		SharedPreferences mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		SharedPreferences.Editor mEditor = mPreferences.edit();
		mEditor.putBoolean(Splash.KEY_FIRST_BOOT, false);
		mEditor.commit();
		
		Intent home = new Intent(this, Home.class);
		startActivity(home);
		this.finish();
	}

	/**
	 * Simple Pager implementation set for to the ViewPager.
	 */
	class TutorialPagerAdapter extends FragmentStatePagerAdapter {

		Fragment mPrimaryFragment;
		Bundle args;

		public TutorialPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			mPrimaryFragment = new TutorialPageFragment();
			Bundle args = new Bundle();
			args.putInt(TutorialPageFragment.EXTRA_PAGE_NUM, position);
			mPrimaryFragment.setArguments(args);
			return mPrimaryFragment;			
		}

		@Override
		public int getCount() {
			return PAGER_COUNT;
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
		mCurrentPagerItem = arg0;
		updateActionBar();
	}

	private void updateActionBar() {
		if (mNextItem == null || mPrevItem == null) {
			return;
		}
		switch (mCurrentPagerItem) {
		case PAGER_COUNT - 1:
			mPrevItem.setEnabled(false);
			mPrevItem.setVisible(false);
			mNextItem.setTitle(R.string.take_me_to_the_app);			
			break;
		case 0:
			mNextItem.setTitle(R.string.next);
			mPrevItem.setEnabled(false);
			mPrevItem.setVisible(false);
			break;
		default:
			mNextItem.setTitle(R.string.next);
			mPrevItem.setEnabled(true);
			mPrevItem.setVisible(true);
			break;
		}
	}

}
