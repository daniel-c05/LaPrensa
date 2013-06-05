package com.thoughts.apps.laprensa;

import uk.co.senab.photoview.PhotoViewAttacher;
import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.app.DownloadManager.Request;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.text.Html;
import android.widget.ImageView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;
import com.thoughts.apps.laprensa.database.DbManager;
import com.thoughts.apps.laprensa.database.DbOpenHelper;
import com.thoughts.apps.laprensa.utils.StringUtils;

@SuppressLint("NewApi")
public class PhotoViewer extends SherlockActivity {	
	
	public static final String EXTRA_ARTICLE_LINK = "photoviewer:link";
	public static final String EXTRA_TITLE = "photoviewer:title";
	
	String mArticleUrl, mImageLink, mTitle, mDate;
	
	Cursor mCursor;
	
	ImageView mPhoto;	
	TextView mTitleView;
	PhotoViewAttacher mAttacher;
	
	boolean isOldApi;
		
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_photo_viewer);
		
		isOldApi = Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB ? true : false;
		
		Bundle extras = getIntent().getExtras();
		
		if (extras != null) {
			mArticleUrl = extras.getString(ArticleHub.KEY_ARTICLE_URL);
			mDate = StringUtils.getReadableDate(mArticleUrl);
			
			mCursor = DbManager.getArticle(this, mArticleUrl);		
			if (mCursor.moveToFirst()) {
				mImageLink = mCursor.getString(mCursor.getColumnIndex(DbOpenHelper.BANNER));
				mTitle = mCursor.getString(mCursor.getColumnIndex(DbOpenHelper.TITULO));				
			}			
		}
		
		setupActionBar();
		setupViews();
	}
		
	private void setupViews() {
		mPhoto = (ImageView) findViewById(R.id.photo);		
		UrlImageViewHelper.setUrlDrawable(mPhoto, mImageLink);
		
		mTitleView = (TextView) findViewById(R.id.photo_title);
		mTitleView.setText(Html.fromHtml(mTitle));
		
		mAttacher = new PhotoViewAttacher(mPhoto);
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		mAttacher.cleanup();
	}

	private void setupActionBar() {
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setSubtitle(mDate);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.photo_viewer, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			NavUtils.navigateUpFromSameTask(this);
			return true;
		case R.id.action_download_photo:
			downloadPhoto();
			return true;
		}		
		return super.onOptionsItemSelected(item);
	}
	
	private void downloadPhoto () {
		DownloadManager downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
		Request mRequest = new Request(Uri.parse(mImageLink));	
		mRequest.setDescription(getString(R.string.download_description));
		if (!isOldApi) {
			mRequest.setNotificationVisibility(Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
		}	
		downloadManager.enqueue(mRequest);
	}
}
