package com.thoughts.apps.laprensa.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.Html;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;
import com.thoughts.apps.laprensa.ArticleHub;
import com.thoughts.apps.laprensa.PhotoViewer;
import com.thoughts.apps.laprensa.R;
import com.thoughts.apps.laprensa.UserPreferences;
import com.thoughts.apps.laprensa.database.DbManager;
import com.thoughts.apps.laprensa.database.DbOpenHelper;
import com.thoughts.apps.laprensa.utils.HtmlHelper;

@SuppressLint("DefaultLocale")
public class ArticleViewFragment extends SherlockFragment {

	public static final String ACTION_ARTICLE_LOADING_FINISHED = "com.thoughts.apps.laprensa.article_loading_finished";

	public static final int FONT_SIZE_SMALL = 0;
	public static final int FONT_SIZE_MEDIUM = 1;
	public static final int FONT_SIZE_LARGE = 2;
	public static final int FONT_SIZE_EXTRA_LARGE = 3;

	public static final int READER_LIGHT = 4;
	public static final int READER_DARK = 6;
	public static final int READER_SEPIA = 5;


	private String mUrlActivo;
	private TextView mTitle, mSubTitle, mContent, mEmptyText;
	private ScrollView mScrollView;
	private ImageView mBanner;
	private Cursor mCursor;	
	private MenuItem mRefreshItem;
	boolean isArticleCached, isSupposedToBeRefreshing;
	boolean isBookmarkReattemptPending = false;	
	int isBookmark; 
	public static int mCurFontSize, mCurrReaderTheme;
	MenuItem mBookmarItem;

	private Context mContext;

	private OnClickListener mOnPhotoClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			if (mTitle.getText().toString().equals("")) {
				//article isn't finished downloading
				return;
			}
			Intent photoViewer = new Intent(mContext, PhotoViewer.class);
			photoViewer.putExtra(ArticleHub.KEY_ARTICLE_URL, mUrlActivo);
			photoViewer.putExtra(PhotoViewer.EXTRA_TITLE, mTitle.getText().toString());		
			startActivity(photoViewer);
		}
	};

	private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(ACTION_ARTICLE_LOADING_FINISHED)) {
				//The AsyncTask Finished loading data
				isSupposedToBeRefreshing = false;
				setRefreshing(false);
				mEmptyText.setVisibility(View.GONE);
				mScrollView.setVisibility(View.VISIBLE);
				if (isBookmarkReattemptPending) {
					DbManager.setBookmark(mContext, mUrlActivo, isBookmark);
					isBookmarkReattemptPending = false;
				}
			}
		}
	};	

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		this.mContext = activity;

		IntentFilter mIntentFilter = new IntentFilter();
		mIntentFilter.addAction(ACTION_ARTICLE_LOADING_FINISHED);
		activity.registerReceiver(mBroadcastReceiver, mIntentFilter);	

	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Bundle extras = getArguments();

		if (extras != null) {
			mUrlActivo = extras.getString(ArticleHub.KEY_ARTICLE_URL);	
			isBookmark = DbManager.isBookmark(mContext, mUrlActivo)? 1 : 0;
			mCurFontSize = extras.getInt(ArticleHub.KEY_FONT_SIZE);		
			mCurrReaderTheme = extras.getInt(ArticleHub.KEY_DISPLAY_THEME);
			mCursor = DbManager.getArticle(mContext, mUrlActivo);
			if (mCursor == null || !mCursor.moveToFirst()) {
				isArticleCached = false;
			}
			else {
				isArticleCached = true;
			}
		}

		setHasOptionsMenu(true);

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View view = inflater.inflate(R.layout.article_view, null);

		mTitle = (TextView) view.findViewById(R.id.article_title);
		mBanner = (ImageView) view.findViewById(R.id.article_banner);
		mSubTitle = (TextView) view.findViewById(R.id.article_category);
		mContent = (TextView) view.findViewById(R.id.article_content);	
		mEmptyText = (TextView) view.findViewById(R.id.empty);
		mScrollView = (ScrollView) view.findViewById(R.id.scroll_container);

		mBanner.setOnClickListener(mOnPhotoClickListener);

		if (isArticleCached) {
			setArticleContent();
		}
		else {
			refreshArticle();
		}		

		return view;
	}

	@Override
	public void onDetach() {
		//Detaching basically means we are no longer active, remove the receiver
		((Activity)mContext).unregisterReceiver(mBroadcastReceiver);
		super.onDetach();
	}

	@Override
	public void onResume() {
		super.onResume();
		adjustTextSize(mCurFontSize);
		setReaderTheme(mCurrReaderTheme);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		getSherlockActivity().getSupportMenuInflater().inflate(R.menu.article_hub, menu);
		mBookmarItem = menu.findItem(R.id.action_bookmark);		
		mRefreshItem = menu.findItem(R.id.action_reload_article);
		if (isSupposedToBeRefreshing) {
			setRefreshing(true);
		}
		if (isBookmark == 1) {
			mBookmarItem.setIcon(R.drawable.ic_action_bookmark_pressed);
		}
		else{
			mBookmarItem.setIcon(R.drawable.ic_action_bookmark);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_share:
			setDefaultShareIntent();
			return true;
		case R.id.action_reload_article:
			refreshArticle();
			return true;
		case R.id.action_bookmark:			
			if (DbManager.isBookmark(mContext, mUrlActivo)) {
				isBookmark = 0;
				mBookmarItem.setIcon(R.drawable.ic_action_bookmark);
			}
			else {
				mBookmarItem.setIcon(R.drawable.ic_action_bookmark_pressed);
				isBookmark = 1;
			}
			if (DbManager.setBookmark(mContext, mUrlActivo, isBookmark) == 0) {
				//If we were not able to update the bookmark status of an item, set reattempt pending
				//Once network connection is stablished and article is downloaded, we will reattempt
				isBookmarkReattemptPending = true;
			}
			return true;
		case R.id.action_settings:
			Intent preferences = new Intent(mContext, UserPreferences.class);
			preferences.putExtra(UserPreferences.EXTRA_PARENT, this.getClass().getCanonicalName());
			startActivity(preferences);
			return true;
		}		
		return super.onOptionsItemSelected(item);
	}

	private void refreshArticle() {
		ConnectivityManager connMgr = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
		if (networkInfo != null && networkInfo.isConnected()) {
			isSupposedToBeRefreshing = true;
			setRefreshing(true);
			HtmlHelper.getArticleContent(mContext, true, mUrlActivo, mTitle, mSubTitle, mContent, mBanner);
		}
		else {
			isSupposedToBeRefreshing = false;			
			if (isArticleCached) {
				//Article is already cached, don't worry, don't change anything. 
				//Just toast that there is no connection
				Toast.makeText(mContext, getString(R.string.no_connection), Toast.LENGTH_LONG).show();
				return;
			}
			else{
				mEmptyText.setVisibility(View.VISIBLE);
				mEmptyText.setText(getString(R.string.no_connection));				
				mScrollView.setVisibility(View.GONE);
			}			
		}
	}

	private void setReaderTheme(int theme) {
		Resources mResources = getResources();
		switch (theme) {
		case READER_DARK:			
			mSubTitle.setTextColor(mResources.getColor(R.color.font_color_on_dark_primary));
			mContent.setTextColor(mResources.getColor(R.color.font_color_on_dark_secondary));
			mEmptyText.setTextColor(mResources.getColor(R.color.font_color_on_dark_secondary));
			break;
		case READER_LIGHT:
			mSubTitle.setTextColor(mResources.getColor(R.color.font_color_on_light_primary));
			mContent.setTextColor(mResources.getColor(R.color.font_color_on_light_secondary));
			mEmptyText.setTextColor(mResources.getColor(R.color.font_color_on_light_primary));
			break;
		case READER_SEPIA:
			mSubTitle.setTextColor(mResources.getColor(R.color.font_color_on_sepia));
			mContent.setTextColor(mResources.getColor(R.color.font_color_on_sepia));
			mEmptyText.setTextColor(mResources.getColor(R.color.font_color_on_sepia));
			break;
		default:
			break;
		}
	}

	private void adjustTextSize(int size) {
		Resources mResources = getResources();
		switch (size) {
		case FONT_SIZE_SMALL:
			mSubTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, mResources.getDimension(R.dimen.section_size_small));
			mContent.setTextSize(TypedValue.COMPLEX_UNIT_PX, mResources.getDimension(R.dimen.content_size_small));			
			break;
		case FONT_SIZE_MEDIUM:
			mContent.setTextSize(TypedValue.COMPLEX_UNIT_PX, mResources.getDimension(R.dimen.content_size_medium));
			mSubTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, mResources.getDimension(R.dimen.section_size_medium));
			break;
		case FONT_SIZE_LARGE:
			mContent.setTextSize(TypedValue.COMPLEX_UNIT_PX, mResources.getDimension(R.dimen.content_size_large));
			mSubTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, mResources.getDimension(R.dimen.section_size_large));
			break;
		case FONT_SIZE_EXTRA_LARGE:
			mContent.setTextSize(TypedValue.COMPLEX_UNIT_PX, mResources.getDimension(R.dimen.content_size_extra_large));
			mSubTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, mResources.getDimension(R.dimen.section_size_extra_large));
			break;
		default:
			break;
		}
	}

	private void setArticleContent() {		
		if (!mCursor.moveToFirst()) {		
			return;
		}

		mTitle.setText(Html.fromHtml(mCursor.getString(mCursor.getColumnIndex(DbOpenHelper.TITULO))));
		mSubTitle.setText(Html.fromHtml(mCursor.getString(mCursor.getColumnIndex(DbOpenHelper.SECCION)).toUpperCase()));
		mContent.setText(Html.fromHtml(mCursor.getString(mCursor.getColumnIndex(DbOpenHelper.DESCRIPCION))));
		String linkImage = mCursor.getString(mCursor.getColumnIndex(DbOpenHelper.BANNER));
		if (linkImage == null || !linkImage.startsWith(HtmlHelper.FULL_HTML_PREFIX) || linkImage.contains("/play_large.png")) {
			mBanner.setVisibility(View.GONE);
		}
		else {
			mBanner.setVisibility(View.VISIBLE);
			UrlImageViewHelper.setUrlDrawable(mBanner, linkImage);
		}		
	}

	private void setRefreshing(boolean refreshing) {

		if(mRefreshItem == null) {
			return;
		}

		if(refreshing)
			mRefreshItem.setActionView(R.layout.actionbar_progress_view);
		else
			mRefreshItem.setActionView(null);
	}

	private void setDefaultShareIntent() {
		Intent share = new Intent(Intent.ACTION_SEND);
		share.setType("text/plain");
		share.putExtra(Intent.EXTRA_SUBJECT, mTitle.getText().toString());
		share.putExtra(Intent.EXTRA_TEXT, mUrlActivo);
		share.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
		startActivity(Intent.createChooser(share, getString(R.string.share_prompt)));		
	}

}
