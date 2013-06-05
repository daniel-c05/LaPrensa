package com.thoughts.apps.laprensa;

import android.os.Bundle;
import android.webkit.WebView;

import com.actionbarsherlock.app.SherlockActivity;

public class WebHolder extends SherlockActivity {
		
	public static final String EXTRA_CONTENT = "web:content";
	public static final int LICENCE = 0;
	public static final int PRIVACY = 1;
	private int mContent;
	private String mUri;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().setBackgroundDrawableResource(android.R.color.white);
		
		setContentView(R.layout.open_source_licences);
		
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			mContent = extras.getInt(EXTRA_CONTENT);
		}
		
		WebView mWebView = (WebView) findViewById(R.id.webview);
		
		switch (mContent) {
		case LICENCE:
			mUri = "file:///android_asset/open_source_licence.html";
			getSupportActionBar().setTitle(getString(R.string.pref_title_open_source_licences));
			break;
		case PRIVACY:
			mUri = "file:///android_asset/politica_privacidad.html";
			getSupportActionBar().setTitle(getString(R.string.pref_title_privacy_policy));
			break;
		}
		mWebView.loadUrl(mUri);
	}

}
