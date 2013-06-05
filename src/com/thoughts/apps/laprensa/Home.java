package com.thoughts.apps.laprensa;

import java.util.Calendar;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;
import com.slidingmenu.lib.SlidingMenu;
import com.thoughts.apps.laprensa.adapter.CursorListAdapter;
import com.thoughts.apps.laprensa.adapter.SectionsAdapter;
import com.thoughts.apps.laprensa.database.DbManager;
import com.thoughts.apps.laprensa.database.DbOpenHelper;
import com.thoughts.apps.laprensa.utils.HtmlHelper;
import com.thoughts.apps.laprensa.utils.StringUtils;

public class Home extends SherlockFragmentActivity {

	public static final String ACTION_FEED_LOADING_FINISHED = "com.thoughts.apps.laprensa.feed_loading_finished";
	public static final String KEY_READABLE_DATE = "home:readabledate";
	public static final String KEY_LAST_USED_FONT_SIZE = "app:last_used_font_size";

	ListView mArticlesListView, mSeccionsListView;
	TextView mMessageBar;

	CursorListAdapter mArticleAdapter;
	SectionsAdapter mSectionsAdapter;

	MenuItem mRefreshItem, mCalendarItem, mBookmarItem;

	SharedPreferences mPreferences;
	SharedPreferences.Editor mEditor;

	boolean isAdapterChangeRequired = false;
	boolean isTodayShown = true;

	int mCurFontSize;

	String mCurrDate, mToday, mReadableDate;
	/**
	 * The URL to be used when {@link #refresh()} is called.
	 * String value is updated each time a different date is selected, or if the current section changes.
	 */
	String mQueryUrl;	

	ActionBar mActionBar;

	SlidingMenu mSlidingMenu;

	public static final String [] FROM = {
		DbOpenHelper.TITULO,
		DbOpenHelper.DESCRIPCION,
		DbOpenHelper.BANNER
	};

	public static final int [] TO = {
		R.id.article_title,
		R.id.article_descrp,
		R.id.article_image
	};

	public static final int [] ICONS = {
		R.drawable.ic_seccion_portada,
		R.drawable.ic_seccion_ambitos,
		R.drawable.ic_seccion_deportes,
		R.drawable.ic_seccion_poderes,
		R.drawable.ic_seccion_reportaje,
		R.drawable.ic_seccion_vida,
		R.drawable.ic_seccion_voces,
		R.drawable.ic_seccion_activos,
		R.drawable.ic_seccion_planeta,
		R.drawable.ic_seccion_cultura,
		R.drawable.ic_seccion_tecnologia,
		R.drawable.ic_seccion_departamentales,
		R.drawable.ic_seccion_empresas,		
		R.drawable.ic_seccion_contactenos,
	};	

	private boolean isSupposedToBeRefreshing;
	private int mCurDisplayMode = ArticleHub.PORTADA;

	public static final String KEY_LIST_POSITION = "mainlist:position";

	private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(ACTION_FEED_LOADING_FINISHED)) {
				//The AsyncTask Finished loading data
				//No need to refresh the Ui, the AsyncTask did that for us, 
				//simply remove the progress bar
				
				Constants.logMessage("Feed loading finished");				
				int lastVisiblePos = mArticlesListView.getFirstVisiblePosition();
				int oldLenght = mArticleAdapter.getCount();
				
				if (isAdapterChangeRequired) {
					mArticleAdapter = new CursorListAdapter(Home.this, R.layout.article_list_item_1, null, FROM, TO, 0, mCurFontSize, true);
					mArticlesListView.setAdapter(mArticleAdapter);
					isAdapterChangeRequired = false;							
				}
				
				mArticleAdapter.swapCursor(DbManager.getFeed(Home.this, mCurDisplayMode, mCurrDate));
				mArticleAdapter.notifyDataSetChanged();
				int newItems = mArticleAdapter.getCount() - oldLenght;
				mArticlesListView.setSelection(lastVisiblePos + newItems);
				showMessageBar(oldLenght, mArticleAdapter.getCount());
				isSupposedToBeRefreshing = false;
				setRefreshing(false);
			}
		}
	};

	private OnItemClickListener mOnSeccionClickListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {


			//Hide the menu
			mSlidingMenu.toggle();

			if (arg2 == ArticleHub.CONTACTENOS) {
				showPublicidad();
				return;
			}
			
			if (mCurDisplayMode == ArticleHub.MARCADORES ) {
				//change the icon for bookmarks
				mBookmarItem.setIcon(R.drawable.ic_action_bookmark);
			}

			//Set the display mode
			mCurDisplayMode = arg2;
			//Set the title of the ActionBar
			setActionBarTitle();
			//update the query url
			updateQueryUrl();

			if (DbManager.isFeedAvailable(Home.this, mCurDisplayMode, mCurrDate)) {
				if (isAdapterChangeRequired) {
					mBookmarItem.setIcon(R.drawable.ic_action_bookmark);
					mArticleAdapter = new CursorListAdapter(Home.this, R.layout.article_list_item_1, null, FROM, TO, 0, mCurFontSize, true);
					mArticlesListView.setAdapter(mArticleAdapter);
				}
				mArticleAdapter.swapCursor(DbManager.getFeed(Home.this, mCurDisplayMode, mCurrDate));
				isAdapterChangeRequired = false;
			}
			else {	
				refresh(true);
			}	
		}		
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//Get the user preferences
		updatePrefDependentValues(false);

		setContentView(R.layout.article_list_view);		

		setupActionBar();

		//register intent filters
		IntentFilter mIntentFilter = new IntentFilter();
		mIntentFilter.addAction(ACTION_FEED_LOADING_FINISHED);
		registerReceiver(mBroadcastReceiver, mIntentFilter);

		setupViews();

		setupSlidingMenu();		

		mToday = StringUtils.getTodaysDate();
	}

	@Override
	protected void onResume() {
		super.onResume();
		updatePrefDependentValues(true);
		if (mCurDisplayMode == ArticleHub.MARCADORES) {
			//Reload the cursor if we're showing bookmarks
			mArticleAdapter.swapCursor(DbManager.getFeed(Home.this, mCurDisplayMode, mCurrDate));
		}
	}

	public void onCreateContextMenu(android.view.ContextMenu menu, View v, android.view.ContextMenu.ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		android.view.MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.article_context_menu, menu);

		AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
		final String url = mArticleAdapter.getString(info.position, DbOpenHelper.LINK);
		if (DbManager.isBookmark(Home.this, url)) {
			menu.findItem(R.id.action_read_later).setTitle(getString(R.string.remove_from_bookmarks));
		}		
	};

	@Override
	public boolean onContextItemSelected(android.view.MenuItem item) {
		//TODO be smarter about handling the database actions
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		final String url = mArticleAdapter.getString(info.position, DbOpenHelper.LINK);

		switch (item.getItemId()) {
		case R.id.action_read_later:
			if (DbManager.isBookmark(Home.this, url)) {
				DbManager.setBookmark(Home.this, url, 0);
			}
			else{
				if (!DbManager.isArticleAvailable(Home.this, url)) {
					HtmlHelper.getArticleContent(Home.this, false, url);
				}
				else{
					DbManager.setBookmark(Home.this, url, 1);
				}
			}	
			if (mCurDisplayMode == ArticleHub.MARCADORES) {
				//Reload the cursor if we're showing bookmarks
				mArticleAdapter.swapCursor(DbManager.getFeed(Home.this, mCurDisplayMode, mCurrDate));
			}
			return true;
		case R.id.action_share_article:
			shareArticle(url);
			return true;
		default:
			return super.onContextItemSelected(item);
		}
	}

	private void shareArticle(String url) {
		Intent share = new Intent(Intent.ACTION_SEND);
		share.setType("text/plain");
		share.putExtra(Intent.EXTRA_TEXT, url);
		share.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
		startActivity(Intent.createChooser(share, getString(R.string.share_prompt)));		
	}

	@Override
	public void onBackPressed() {		

		if (mCurDisplayMode != ArticleHub.PORTADA) {
			//Set the display mode
			mCurDisplayMode = ArticleHub.PORTADA;
			//Set the title of the ActionBar
			setActionBarTitle();
			//update the query url with the current date and portada mode
			updateQueryUrl();
			if (isAdapterChangeRequired) {
				//Change the Bookmark Icon
				mBookmarItem.setIcon(R.drawable.ic_action_bookmark);
				mArticleAdapter = new CursorListAdapter(Home.this, R.layout.article_list_item_1, null, FROM, TO, 0, mCurFontSize, true);
				mArticlesListView.setAdapter(mArticleAdapter);
			}
			mArticleAdapter.swapCursor(DbManager.getFeed(Home.this, mCurDisplayMode, mCurrDate));
		}
		else if (mCurDisplayMode == ArticleHub.PORTADA && !mCurrDate.equals(mToday)) {
			handleOnDateSet();
		}
		else {
			super.onBackPressed();
		}
	}

	protected void showPublicidad() {
		Intent contact = new Intent(this, ContactUs.class);
		contact.putExtra(ContactUs.EXTRA_CONTENT, ContactUs.CONTACT_US);
		startActivity(contact);
	}

	private void updatePrefDependentValues(boolean fast) {
		if (mPreferences == null) {
			mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		}

		if (fast) {
			//Use fast during onResume so not many operations are done
			final int oldSize = mCurFontSize; 
			mCurFontSize = Integer.valueOf(mPreferences.getString(getString(R.string.pref_key_font_size), "0"));
			if (oldSize != mCurFontSize) {
				//Only re-do the adapter if needed
				Constants.logMessage("re-creating adapter");
				if (mCurDisplayMode == ArticleHub.MARCADORES) {
					mArticleAdapter = new CursorListAdapter(Home.this, R.layout.bookmark_list_item, 
							DbManager.getFeed(Home.this, mCurDisplayMode, mCurrDate), FROM, TO, 0, mCurFontSize, true);
					mArticlesListView.setAdapter(mArticleAdapter);
				}
				else{
					mArticleAdapter = new CursorListAdapter(Home.this, R.layout.article_list_item_1, 
							DbManager.getFeed(Home.this, mCurDisplayMode, mCurrDate), FROM, TO, 0, mCurFontSize, true);
					mArticlesListView.setAdapter(mArticleAdapter);
				}
			}						
			//mArticlesListView.setSelection(mPreferences.getInt(KEY_LIST_POSITION, 0));
			mArticlesListView.setSelection(0);
			return;
		}

		//get last date accessed
		//as per friend's suggestions we want to show today, rather than going back to last date accessed
		//mCurrDate = mPreferences.getString(ArticleHub.KEY_DISPLAY_DATE, DbManager.getTodaysDate());
		mCurrDate = StringUtils.getTodaysDate();
		
		//mReadableDate = mPreferences.getString(KEY_READABLE_DATE, DbManager.getTodaysReadableDate());
		mReadableDate = StringUtils.getTodaysReadableDate();
		
		mCurFontSize = mPreferences.getInt(KEY_LAST_USED_FONT_SIZE, 0);
		//get the last display mode used
		//again, we want to show most recent articles, so we'll go to portada. 
		//mCurDisplayMode = mPreferences.getInt(ArticleHub.KEY_DISPLAY_MODE, ArticleHub.PORTADA);
		//if (mCurDisplayMode == ArticleHub.MARCADORES ) {
		//	isAdapterChangeRequired = true;
		//}
		//are we supposed to refresh on start?
		mCurDisplayMode = ArticleHub.PORTADA;		

		updateQueryUrl();

	}

	private void updateQueryUrl () {
		
		switch (mCurDisplayMode) {
		//Buid a Query String in case refresh needs to be called later
		case ArticleHub.PORTADA:
			mQueryUrl = HtmlHelper.WEB_PREFIX + mCurrDate;
			break;
		case ArticleHub.ACTIVOS:
			mQueryUrl = HtmlHelper.WEB_PREFIX + mCurrDate + "activos/";
			break;
		case ArticleHub.AMBITOS:
			mQueryUrl = HtmlHelper.WEB_PREFIX + mCurrDate + "ambito/";
			break;
		case ArticleHub.CULTURA:
			mQueryUrl = HtmlHelper.WEB_PREFIX + mCurrDate + "cultura/";
			break;
		case ArticleHub.PLANETA:
			mQueryUrl = HtmlHelper.WEB_PREFIX + mCurrDate + "planeta/";
			break;
		case ArticleHub.PLAY:
			mQueryUrl = HtmlHelper.WEB_PREFIX + mCurrDate + "play/";
			break;
		case ArticleHub.PODERES:
			mQueryUrl = HtmlHelper.WEB_PREFIX + mCurrDate + "poderes/";
			break;
		case ArticleHub.REPORTAJES:
			mQueryUrl = HtmlHelper.WEB_PREFIX + mCurrDate + "reportajes-especiales/";
			break;
		case ArticleHub.TECNOLOGIA:
			mQueryUrl = HtmlHelper.WEB_PREFIX + mCurrDate + "tecnologia/";
			break;
		case ArticleHub.VIDA:
			mQueryUrl = HtmlHelper.WEB_PREFIX + mCurrDate + "vida/";
			break;
		case ArticleHub.VOCES:
			mQueryUrl = HtmlHelper.WEB_PREFIX + mCurrDate + "voces/";
			break;
		case ArticleHub.DEPARTAMENTALES:
			mQueryUrl = HtmlHelper.WEB_PREFIX + mCurrDate + "departamentales/";
			break;
		case ArticleHub.EMPRESARIALES:
			mQueryUrl = HtmlHelper.WEB_PREFIX + mCurrDate + "empresariales/";
			break;
		default:
			Constants.logMessage("Defaulting on search url");
			break;
		}
	}

	protected void showMessageBar(int oldCount, int newCount) {
		mMessageBar.setVisibility(View.VISIBLE);
		int newItems = oldCount > newCount ? newCount : newCount - oldCount;
		if (newItems == 0) {
			//plurals don't really work with zeros
			mMessageBar.setText(getString(R.string.no_new_articles));
		}
		else {
			mMessageBar.setText(getResources().getQuantityString(R.plurals.new_articles, newItems, newItems));
		}
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				mMessageBar.post(new Runnable() {
					@Override
					public void run() {
						mMessageBar.setVisibility(View.GONE);
					}
				});
			}
		};
		new Thread(runnable).start();
	}

	private void setupSlidingMenu() {

		mSlidingMenu = new SlidingMenu(this);
		mSlidingMenu.setMode(SlidingMenu.LEFT);
		mSlidingMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
		mSlidingMenu.setBehindOffsetRes(R.dimen.slidingmenu_offset);
		//No scaling on the behind menu
		mSlidingMenu.setBehindScrollScale(0);
		mSlidingMenu.setShadowDrawable(R.drawable.sliding_menu_shadow);
		mSlidingMenu.setShadowWidthRes(R.dimen.slidingmenu_shadow);		
		mSlidingMenu.attachToActivity(this, SlidingMenu.SLIDING_WINDOW);
		mSlidingMenu.setMenu(R.layout.sliding_sections);

		mSectionsAdapter = new SectionsAdapter(this, getResources().getStringArray(R.array.colleccion_secciones), ICONS);				

		mSeccionsListView = (ListView) findViewById(R.id.seccions_list);
		mSeccionsListView.setAdapter(mSectionsAdapter);
		mSeccionsListView.setOnItemClickListener(mOnSeccionClickListener);

	}

	private void setActionBarTitle() {
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
		}
		mActionBar.setTitle(getString(title));
		if (mCurDisplayMode == ArticleHub.MARCADORES) {
			//hide the subtitle
			getSupportActionBar().setSubtitle(null);
		}
		else {
			getSupportActionBar().setSubtitle(mReadableDate);
		}
	}

	private void setupActionBar () {
		mActionBar = getSupportActionBar();
		mActionBar.setDisplayHomeAsUpEnabled(true);	
		setActionBarTitle();
	}

	private void setupViews () {

		mArticlesListView = (ListView) findViewById(R.id.all_apps_list);
		mMessageBar = (TextView) findViewById(R.id.custom_message_bar);
		//set the empty view, this needs to be done before setting the adapter
		mArticlesListView.setEmptyView(findViewById(R.id.empty));		
		registerForContextMenu(mArticlesListView);

		mArticleAdapter = mCurDisplayMode == ArticleHub.MARCADORES ? new CursorListAdapter(this, R.layout.bookmark_list_item, 
				DbManager.getFeed(Home.this, mCurDisplayMode, mCurrDate), FROM, TO, 0, mCurFontSize, true)
		: new CursorListAdapter(this, R.layout.article_list_item_1, 
				DbManager.getFeed(this, mCurDisplayMode, mCurrDate), FROM, TO, 0, mCurFontSize, true);

		mArticlesListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int pos,
					long arg3) {
				showArticle(mArticleAdapter.getString(pos, DbOpenHelper.LINK), pos, mCurDisplayMode);
			}			
		});

		mArticlesListView.setAdapter(mArticleAdapter);		

		if (mArticleAdapter == null || mArticleAdapter.isEmpty()) {		
			if (!Splash.isSupposedToBeRefreshing) {
				//If refresh on start IS enabled, we would be doing a duplicate request. 
				//Only refresh when it's not enabled
				refresh(true);
			}			
		}
		else {		
			//mArticlesListView.setSelection(mPreferences.getInt(KEY_LIST_POSITION, 0));
			mArticlesListView.setSelection(0);
		}
	}
	
	public void showCustomDatePicker () {

		int year = Integer.valueOf(mCurrDate.substring(1, 5));
		int month = Integer.valueOf(mCurrDate.substring(6, 8)) -1;
		int day = Integer.valueOf(mCurrDate.substring(9, 11));

		final DatePicker mDatePicker = (DatePicker) getLayoutInflater().inflate(R.layout.date_picker_view, null);
		mDatePicker.updateDate(year, month, day);

		AlertDialog.Builder mBuilder = new Builder(this);
		//set the title
		mBuilder.setTitle(getString(R.string.date_picker_title))
		.setPositiveButton(android.R.string.ok, new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				handleOnDateSet(mDatePicker.getYear(), mDatePicker.getMonth(), mDatePicker.getDayOfMonth());
			}
		})
		.setNeutralButton(getString(R.string.back_to_today), new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {		
				handleOnDateSet();
			}
		})
		.setNegativeButton(android.R.string.cancel, new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		})
		.setView(mDatePicker)
		.create().show();
	}

	protected void handleOnDateSet() {
		final Calendar c = Calendar.getInstance();
		int year = c.get(Calendar.YEAR);
		int month = c.get(Calendar.MONTH);
		int day = c.get(Calendar.DAY_OF_MONTH);
		handleOnDateSet(year, month, day);
	}

	protected void handleOnDateSet(int year, int month, int day) {
		mCurrDate = StringUtils.getCustomDate(year, month, day);
		mReadableDate = StringUtils.getReadableDate(year, month, day);

		Constants.logMessage("Date set to: ");
		Constants.logMessage(mCurrDate);	

		if (mCurrDate.equals(StringUtils.getTodaysDate())) {
			mCalendarItem.setIcon(R.drawable.ic_action_calendar_day);
		}
		else {
			mCalendarItem.setIcon(R.drawable.ic_action_calendar_month);
		}				

		updateQueryUrl();
		setActionBarTitle();

		//If the cursor is null, get more data 
		if (!DbManager.isFeedAvailable(this, mCurDisplayMode, mCurrDate)) {
			refresh(true);
		}

		else {
			mArticleAdapter.swapCursor(DbManager.getFeed(this, mCurDisplayMode, mCurrDate));
		}

		Toast.makeText(Home.this, "Cargando artículos con fecha de: " + mReadableDate, Toast.LENGTH_LONG).show();
	}

	protected void showArticle(String url, int pos, int mode) {
		Intent articleHub = new Intent(this, ArticleHub.class);
		articleHub.putExtra(UserPreferences.EXTRA_PARENT, this.getClass().getCanonicalName());
		articleHub.putExtra(ArticleHub.KEY_ARTICLE_URL, url);
		articleHub.putExtra(ArticleHub.KEY_DISPLAY_MODE, mode);
		articleHub.putExtra(ArticleHub.KEY_DISPLAY_DATE, mCurrDate);
		startActivity(articleHub);
	}

	private void saveState () {
		mEditor = mPreferences.edit();
		mEditor.putInt(KEY_LIST_POSITION, mArticlesListView.getFirstVisiblePosition());
		mEditor.putInt(ArticleHub.KEY_DISPLAY_MODE, mCurDisplayMode);
		mEditor.putString(ArticleHub.KEY_DISPLAY_DATE, mCurrDate);
		mEditor.putInt(KEY_LAST_USED_FONT_SIZE, mCurFontSize);
		mEditor.putString(KEY_READABLE_DATE, mReadableDate);
		mEditor.commit();
	}

	@Override
	protected void onPause() {
		saveState();
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		//Detaching basically means we are no longer active, remove the receiver
		unregisterReceiver(mBroadcastReceiver);
		super.onDestroy();
	}

	/**
	 * Refreshes the current feed, passing the current {@link #mQueryUrl}
	 * @param forceCursorSwap Whether or not we should swap to an empty cursor, despite of no network connection
	 */
	private void refresh (boolean forceCursorSwap) {

		ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
		if (networkInfo != null && networkInfo.isConnected()) {						
			isSupposedToBeRefreshing = true;
			setRefreshing(true);
			HtmlHelper.actualizarLista(this, mQueryUrl);
		} else {
			isSupposedToBeRefreshing = false;
			setRefreshing(false);
			Toast.makeText(this, getString(R.string.no_connection), Toast.LENGTH_LONG).show();			
			if (forceCursorSwap) {
				mArticleAdapter.swapCursor(DbManager.getFeed(Home.this, mCurDisplayMode, mCurrDate));
			}
		}
	}

	private void setRefreshing(boolean refreshing) {

		if(mRefreshItem == null) {
			Constants.logMessage("Refresh item was null");
			return;
		}

		if(refreshing)
			mRefreshItem.setActionView(R.layout.actionbar_progress_view);
		else
			mRefreshItem.setActionView(null);
	}

	@Override
	public boolean onCreateOptionsMenu(com.actionbarsherlock.view.Menu menu) {
		getSupportMenuInflater().inflate(R.menu.home, menu);
		if (Splash.isFirstBoot && mArticleAdapter.getCount() == 0 && Splash.isSupposedToBeRefreshing) {
			isSupposedToBeRefreshing = true;
		}
		else if (!Splash.isFirstBoot && Splash.isSupposedToBeRefreshing) {
			isSupposedToBeRefreshing = true;
		}
		mRefreshItem = menu.findItem(R.id.action_refresh);
		if (isSupposedToBeRefreshing) {
			setRefreshing(true);
		}	
		mCalendarItem = menu.findItem(R.id.action_calendar);
		if (mCurrDate.equals(mToday))
			mCalendarItem.setIcon(R.drawable.ic_action_calendar_day);
		else
			mCalendarItem.setIcon(R.drawable.ic_action_calendar_month);
		mBookmarItem = menu.findItem(R.id.action_show_bookmark);
		if (mCurDisplayMode == ArticleHub.MARCADORES) {
			mBookmarItem.setIcon(R.drawable.ic_action_bookmark_pressed);
		}
		else {
			mBookmarItem.setIcon(R.drawable.ic_action_bookmark);
		}
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			mSlidingMenu.toggle();
			return true;		
		case R.id.action_calendar:
			showCustomDatePicker();
			return true;
		case R.id.action_settings:
			Intent settings = new Intent(this, UserPreferences.class);
			settings.putExtra(UserPreferences.EXTRA_PARENT, this.getClass().getCanonicalName());
			startActivity(settings);
			return true;
		case R.id.action_refresh:
			refresh(false);
			return true;
		case R.id.action_back_to_top:
			mArticlesListView.setSelection(0);
			return true;	
		case R.id.action_show_bookmark:
			isAdapterChangeRequired = true;
			mCurDisplayMode = ArticleHub.MARCADORES;
			setActionBarTitle();
			mBookmarItem.setIcon(R.drawable.ic_action_bookmark_pressed);			
			mArticleAdapter = new CursorListAdapter(this, R.layout.bookmark_list_item, 
					DbManager.getFeed(Home.this, mCurDisplayMode, mCurrDate), FROM, TO, 0, mCurFontSize, true);
			mArticlesListView.setAdapter(mArticleAdapter);
			return true;
		case R.id.action_search_articles:
			Intent search = new Intent(this, SearchResults.class);
			startActivity(search);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}		
	}
}