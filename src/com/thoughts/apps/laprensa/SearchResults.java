package com.thoughts.apps.laprensa;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.thoughts.apps.laprensa.adapter.CursorListAdapter;
import com.thoughts.apps.laprensa.database.DbManager;
import com.thoughts.apps.laprensa.database.DbOpenHelper;

public class SearchResults extends SherlockActivity implements OnEditorActionListener {
	
	private CursorListAdapter mAdapter;
	private EditText mInput;
	private ImageButton mClearButton;
	private MenuItem mSearchItem;
	private ListView mListView;
	boolean isKeyboardShown = true;
	private InputMethodManager mInputManager;
	
	/**
	 * Se Ocupa de actualizar las sugerencias cada vez que el usuario modifica el texto del input.
	 */
	private final TextWatcher watcher = new TextWatcher() {

		@Override
		public void afterTextChanged(Editable s) {	
			//No hacemos nada
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {	
			//No hacemos nada
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before,
				int count) {

			if (s.length() < 1) {
				mClearButton.setVisibility(View.GONE);
				mAdapter.swapCursor(null);				
			}
			else {
				mClearButton.setVisibility(View.VISIBLE);
				mAdapter.swapCursor(DbManager.searchArticles(SearchResults.this, s.toString()));				
			}
			mAdapter.notifyDataSetChanged();
			mListView.setAdapter(mAdapter);
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.article_list_view);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		mInputManager = (InputMethodManager)getSystemService(
			      Context.INPUT_METHOD_SERVICE);
		mListView = (ListView) findViewById(R.id.all_apps_list);
		mListView.setEmptyView(findViewById(R.id.empty));
		mListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int pos,
					long arg3) {
				showArticle(mAdapter.getString(pos, DbOpenHelper.LINK), pos, ArticleHub.STANDALONE);
			}			
		});
		//Hace visible el teclado por defecto al abrir la actividad.
				getWindow().setSoftInputMode(
						LayoutParams.SOFT_INPUT_STATE_VISIBLE);	
	}
	
	protected void showArticle(String url, int pos, int mode) {
		Intent articleHub = new Intent(this, ArticleHub.class);
		articleHub.putExtra(UserPreferences.EXTRA_PARENT, this.getClass().getCanonicalName());
		articleHub.putExtra(ArticleHub.KEY_ARTICLE_URL, url);
		articleHub.putExtra(ArticleHub.KEY_DISPLAY_MODE, mode);
		startActivity(articleHub);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getSupportMenuInflater().inflate(R.menu.search_results, menu);
		mSearchItem = menu.findItem(R.id.menu_search);		
		mSearchItem.setActionView(R.layout.collapsible_edit_text)
			.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		
		RelativeLayout mCollapsibleContainer = (RelativeLayout) mSearchItem.getActionView();
		mInput = (EditText) mCollapsibleContainer.findViewById(R.id.collapsible_input);
		mClearButton = (ImageButton) mCollapsibleContainer.findViewById(R.id.collapsible_action_button);
		mClearButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mInput.setText("");
			}
		});
		
		mAdapter = new CursorListAdapter(this, R.layout.article_list_item_1, null, Home.FROM, Home.TO, 0, 0, false); 	
		
		if (mAdapter != null || mAdapter.getCount() != 0) {
			mListView.setAdapter(mAdapter);
		}	

		mInput.addTextChangedListener(watcher);
		mInput.setOnEditorActionListener(this);
		
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			if (isKeyboardShown) {				
				mInputManager.hideSoftInputFromWindow(mInput.getWindowToken(), 0);
				isKeyboardShown = false;
			}
			else {
				Intent home = new Intent(this, Home.class);
				NavUtils.navigateUpTo(this, home);
			}			
			break;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		if (EditorInfo.IME_ACTION_DONE == actionId) {		
			isKeyboardShown = false;
		}
		return false;
	}

}
