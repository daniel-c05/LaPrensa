package com.thoughts.apps.laprensa;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NavUtils;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;
import com.thoughts.apps.laprensa.fragments.ContactUsFragment;

public class ContactUs extends SherlockFragmentActivity {
	
	public static final String EXTRA_CONTENT = "fragment:content";
	public static final int CONTACT_US = 0;
	private int mContent;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_empty_holder);
		
		setupActionBar();
		
		FragmentTransaction mFragmentTransaction = getSupportFragmentManager().beginTransaction();
		switch (mContent) {
		case CONTACT_US:
			mFragmentTransaction.replace(R.id.frame, new ContactUsFragment()).commit();		
			break;
		}			
	}

	private void setupActionBar() {
		getSupportActionBar().setTitle(getString(R.string.contactenos));
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			NavUtils.navigateUpFromSameTask(this);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

}
