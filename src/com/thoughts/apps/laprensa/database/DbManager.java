package com.thoughts.apps.laprensa.database;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.thoughts.apps.laprensa.ArticleHub;
import com.thoughts.apps.laprensa.Constants;
import com.thoughts.apps.laprensa.Home;
import com.thoughts.apps.laprensa.fragments.ArticleViewFragment;
import com.thoughts.apps.laprensa.utils.HtmlHelper;
import com.thoughts.apps.laprensa.utils.StringUtils;

public class DbManager {
	
	private static DbOpenHelper mHelper;
	private static SQLiteDatabase mDatabase;
	
	private static final String KEY_LAST_SAVED_POS = "last_saved_position";

	private static void open (Context context) {
		if (mDatabase != null && mDatabase.isOpen()) {
			return;
		}
		try {
			mHelper = new DbOpenHelper(context);
			mDatabase = mHelper.getWritableDatabase();
		} catch (Exception e) {
			Constants.logMessage(e.toString());
		}
	}

	private static void close () {
		if (mDatabase != null) {
			mDatabase.close();
		}		
	}
	
	/**
	 * Query the news database for relevant results that match the supplied query. 
	 * @param context The context, required to open the database. 
	 * @param query The string to search for. 
	 * @return A Cursor with the relevant results. 
	 */
	public static Cursor searchArticles (Context context, String query) {
		if (mDatabase == null || !mDatabase.isOpen())
			open(context);		
		
		String where = DbOpenHelper.TITULO + " LIKE '%" + query + "%' OR " + 
				DbOpenHelper.SECCION + " LIKE '%" + query + "%' OR " +
				DbOpenHelper.DESCRIPCION + " LIKE '%" + query + "%'";
		
		return mDatabase.query(DbOpenHelper.TABLE_NAME_FEED, DbOpenHelper.PROJECTION_FEED, where, null, null, null, DbOpenHelper.SAVED_ON + " DESC");		
	}
	
	public static void cleanup (final Context context) {
		if (mDatabase == null || !mDatabase.isOpen())
			open(context);																	
		
		String where = "julianday('now') - julianday(" + DbOpenHelper.SAVED_ON + ") > 1" 
				+ " and " + DbOpenHelper.BOOKMARK + " = 0";
		Constants.logMessage("Cleaning up");
		Constants.logMessage("Where: " + where);
		
		int deleted = mDatabase.delete(DbOpenHelper.TABLE_NAME_FEED, where, null);
		deleted = deleted + mDatabase.delete(DbOpenHelper.TABLE_NAME_ARTICLES, where, null);
		Constants.logMessage("Items deleted: " + deleted);
	}
	
	public static void cleanup (final Context context, int daysBack) {
		if (mDatabase == null || !mDatabase.isOpen())
			open(context);	
		
		String where = "julianday('now') - julianday(" + DbOpenHelper.SAVED_ON + ") > " + daysBack 
				+ " and " + DbOpenHelper.BOOKMARK + " = 0";
		Constants.logMessage("Cleaning up");
		Constants.logMessage("Where: " + where);
		
		int deleted = mDatabase.delete(DbOpenHelper.TABLE_NAME_FEED, where, null);
		deleted = deleted + mDatabase.delete(DbOpenHelper.TABLE_NAME_ARTICLES, where, null);
		Constants.logMessage("Items deleted: " + deleted);
	}
	
	public static void clearDatabase (final Context context) {
		if (mDatabase == null || !mDatabase.isOpen())
			open(context);	
		
		Constants.logMessage("Clearing database");
		
		mDatabase.delete(DbOpenHelper.TABLE_NAME_FEED, null, null);
		mDatabase.delete(DbOpenHelper.TABLE_NAME_ARTICLES, null, null);
	}
	
	/**
	 * 
	 * @param context
	 * @param mode The Search Mode. See {@link ArticleHub} for details.
	 * @param date The Date to search for.
	 * @return A Cursor for the supplied params.
	 */
	public static Cursor getFeed (final Context context, int mode, String date) {
		if (mDatabase == null || !mDatabase.isOpen())
			open(context);	
						
		String where;
		Cursor mCursor; 
		//If no date supplied, we assume we are looking at today's info
		//Else, we use the date supplied
		String mDate = date == null? StringUtils.getTodaysDate() : date;
		String table = DbOpenHelper.TABLE_NAME_FEED;
		String sortBy = DbOpenHelper.LIST_POS + " ASC";
		
		switch (mode) {
		case ArticleHub.PORTADA:
			where = DbOpenHelper.PORTADA + " = '1' AND " + DbOpenHelper.FECHA + " LIKE '%" + mDate + "%'";
			break;
		case ArticleHub.ACTIVOS:
			where = DbOpenHelper.SECCION + " LIKE '%Activos%' AND " + DbOpenHelper.FECHA + " LIKE '%" + mDate + "%'";
			break;
		case ArticleHub.AMBITOS:
			where = DbOpenHelper.SECCION + " LIKE '%mbito%' AND " + DbOpenHelper.FECHA + " LIKE '%" + mDate + "%'";
			break;
		case ArticleHub.CULTURA:
			where = DbOpenHelper.SECCION + " LIKE '%Cultura%' AND " + DbOpenHelper.FECHA + " LIKE '%" + mDate + "%'";
			break;
		case ArticleHub.PLANETA:
			where = DbOpenHelper.SECCION + " LIKE '%Planeta%' AND " + DbOpenHelper.FECHA + " LIKE '%" + mDate + "%'";
			break;
		case ArticleHub.PLAY:
			where = DbOpenHelper.SECCION + " LIKE '%Play%' AND " + DbOpenHelper.FECHA + " LIKE '%" + mDate + "%'";
			break;
		case ArticleHub.PODERES:
			where = DbOpenHelper.SECCION + " LIKE '%Poderes%' AND " + DbOpenHelper.FECHA + " LIKE '%" + mDate + "%'";
			break;
		case ArticleHub.REPORTAJES:
			where = DbOpenHelper.SECCION + " LIKE '%Reportajes%' AND " + DbOpenHelper.FECHA + " LIKE '%" + mDate + "%'";
			break;
		case ArticleHub.TECNOLOGIA:
			where = DbOpenHelper.SECCION + " LIKE '%Tecnologia%' AND " + DbOpenHelper.FECHA + " LIKE '%" + mDate + "%'";
			break;
		case ArticleHub.VIDA:
			where = DbOpenHelper.SECCION + " LIKE '%Vida%' AND " + DbOpenHelper.FECHA + " LIKE '%" + mDate + "%'";
			break;
		case ArticleHub.VOCES:
			where = DbOpenHelper.SECCION + " LIKE '%Voces%' AND " + DbOpenHelper.FECHA + " LIKE '%" + mDate + "%'";
			break;
		case ArticleHub.DEPARTAMENTALES:
			where = DbOpenHelper.SECCION + " LIKE '%departamental%' AND " + DbOpenHelper.FECHA + " LIKE '%" + mDate + "%'";
			break;
		case ArticleHub.EMPRESARIALES:
			where = DbOpenHelper.SECCION + " LIKE '%empresariales%' AND " + DbOpenHelper.FECHA + " LIKE '%" + mDate + "%'";
			break;
		case ArticleHub.MARCADORES:
			//Bookmards don't care for the date
			where = DbOpenHelper.BOOKMARK + " = '1'";	
			table = DbOpenHelper.TABLE_NAME_FEED;
			sortBy = DbOpenHelper.TITULO;
			break;
		default:
			where = null;
			break;
		}
		
		mCursor = mDatabase.query(table, DbOpenHelper.PROJECTION_FEED, where, null, null, null, sortBy);
		
		return mCursor;
	}	
	
	public static boolean isFeedAvailable(final Context context, int mode, String mDate) {
		if (mDatabase == null || !mDatabase.isOpen())
			open(context);	
		
		Cursor mCursor;
		String where;
		
		switch (mode) {
		case ArticleHub.PORTADA:
			where = DbOpenHelper.PORTADA + " = '1' AND " + DbOpenHelper.FECHA + " LIKE '%" + mDate + "%'";
			break;
		case ArticleHub.ACTIVOS:
			where = DbOpenHelper.SECCION + " LIKE '%Activos%' AND " + DbOpenHelper.FECHA + " LIKE '%" + mDate + "%'";
			break;
		case ArticleHub.AMBITOS:
			where = DbOpenHelper.SECCION + " LIKE '%mbito%' AND " + DbOpenHelper.FECHA + " LIKE '%" + mDate + "%'";
			break;
		case ArticleHub.CULTURA:
			where = DbOpenHelper.SECCION + " LIKE '%Cultura%' AND " + DbOpenHelper.FECHA + " LIKE '%" + mDate + "%'";
			break;
		case ArticleHub.PLANETA:
			where = DbOpenHelper.SECCION + " LIKE '%Planeta%' AND " + DbOpenHelper.FECHA + " LIKE '%" + mDate + "%'";
			break;
		case ArticleHub.PLAY:
			where = DbOpenHelper.SECCION + " LIKE '%Play%' AND " + DbOpenHelper.FECHA + " LIKE '%" + mDate + "%'";
			break;
		case ArticleHub.PODERES:
			where = DbOpenHelper.SECCION + " LIKE '%Poderes%' AND " + DbOpenHelper.FECHA + " LIKE '%" + mDate + "%'";
			break;
		case ArticleHub.REPORTAJES:
			where = DbOpenHelper.SECCION + " LIKE '%Reportajes%' AND " + DbOpenHelper.FECHA + " LIKE '%" + mDate + "%'";
			break;
		case ArticleHub.TECNOLOGIA:
			where = DbOpenHelper.SECCION + " LIKE '%Tecnologia%' AND " + DbOpenHelper.FECHA + " LIKE '%" + mDate + "%'";
			break;
		case ArticleHub.VIDA:
			where = DbOpenHelper.SECCION + " LIKE '%Vida%' AND " + DbOpenHelper.FECHA + " LIKE '%" + mDate + "%'";
			break;
		case ArticleHub.VOCES:
			where = DbOpenHelper.SECCION + " LIKE '%Voces%' AND " + DbOpenHelper.FECHA + " LIKE '%" + mDate + "%'";
			break;
		case ArticleHub.DEPARTAMENTALES:
			where = DbOpenHelper.SECCION + " LIKE '%departamental%' AND " + DbOpenHelper.FECHA + " LIKE '%" + mDate + "%'";
			break;
		case ArticleHub.EMPRESARIALES:
			where = DbOpenHelper.SECCION + " LIKE '%empresariales%' AND " + DbOpenHelper.FECHA + " LIKE '%" + mDate + "%'";
			break;
		case ArticleHub.MARCADORES:
			//Bookmards don't care for the date
			where = DbOpenHelper.BOOKMARK + " = '1'";			
			break;
		default:
			where = null;
			break;
		}
		
		mCursor = mDatabase.query(DbOpenHelper.TABLE_NAME_FEED, new String [] {DbOpenHelper._ID}, where, null, null, null, DbOpenHelper.LIST_POS + " ASC");
		
		if (mCursor == null || mCursor.getCount() == 0) {
			return false;
		}
		
		else {
			mCursor.close();
			return true;
		}		
	}
	
	
	public static Cursor getFeed (final Context context) {
		if (mDatabase == null || !mDatabase.isOpen())
			open(context);	
		
		return mDatabase.query(DbOpenHelper.TABLE_NAME_FEED, DbOpenHelper.PROJECTION_FEED, null, null, null, null, DbOpenHelper.LIST_POS + " ASC");
	}	

	public static void saveFeed (final Context context, ArrayList<String> titles, ArrayList<String> links,
			ArrayList<String> banner, ArrayList<String> descriptions, int isPortada) {
		
		if (mDatabase == null || !mDatabase.isOpen())
			open(context);	
		
		String articleDate = StringUtils.getCustomDate(links.get(0));
		String gregorianDate = StringUtils.getGregorianDate(null);
		
		int lenght = titles.size();
		
		String where;		
		Cursor mCursor;
		ContentValues mValues;
		SharedPreferences mPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		int lastSavedPos = mPreferences.getInt(KEY_LAST_SAVED_POS, 0);
		boolean useNegative = lastSavedPos == 0? false : true;
		lastSavedPos = lastSavedPos >= 0? 0 : lastSavedPos; 
		
		
		for (int i = 0; i < lenght; i++) {
			where = DbOpenHelper.LINK + " = '" + links.get(i) + "'";
			mCursor = mDatabase.query(DbOpenHelper.TABLE_NAME_FEED, new String [] {DbOpenHelper.LINK}, where, null, null, null, null);
			mValues = new ContentValues();
			mValues.put(DbOpenHelper.TITULO, titles.get(i));
			mValues.put(DbOpenHelper.LINK, links.get(i));
			mValues.put(DbOpenHelper.BANNER, banner.get(i));
			mValues.put(DbOpenHelper.DESCRIPCION, descriptions.get(i));			
			mValues.put(DbOpenHelper.SECCION, HtmlHelper.getSection(links.get(i)));
			
			if (mCursor != null) {
				if (!mCursor.moveToFirst()) {					
					//Here, this item has never been stored, insert it.
					//We only add a position to new items, old items do not change.
					if (useNegative) {
						mValues.put(DbOpenHelper.LIST_POS, lastSavedPos - (lenght - i));
					}
					else {
						mValues.put(DbOpenHelper.LIST_POS, lastSavedPos + i);
					}
					mValues.put(DbOpenHelper.FECHA, articleDate);
					mValues.put(DbOpenHelper.SAVED_ON, gregorianDate);
					mValues.put(DbOpenHelper.PORTADA, isPortada);
					mValues.put(DbOpenHelper.BOOKMARK, 0);
					mDatabase.insert(DbOpenHelper.TABLE_NAME_FEED, null, mValues);
				}
				else {
					//Item was previously stored, update it.
					//Sometimes the banner image will be added later, as the article is updated, sometimes is removed, etc.
					mDatabase.update(DbOpenHelper.TABLE_NAME_FEED, mValues, where, null);
				}
			}
		}	
		
		SharedPreferences.Editor mEditor = mPreferences.edit();
		if (useNegative) {
			mEditor.putInt(KEY_LAST_SAVED_POS, lastSavedPos - lenght);
		}
		else {
			mEditor.putInt(KEY_LAST_SAVED_POS, lastSavedPos + lenght);
		}
		
		mEditor.commit();
		

		Intent intent = new Intent(Home.ACTION_FEED_LOADING_FINISHED);
		context.sendBroadcast(intent);
		
		return;
	}
	
	public static ArrayList<String> getFeedLinks (final Context context, int mode, String date) {
		if (mDatabase == null || !mDatabase.isOpen())
			open(context);	
						
		ArrayList<String> links = new ArrayList<String>();
		String where;
		String table = DbOpenHelper.TABLE_NAME_FEED;
		String sortBy = DbOpenHelper.LIST_POS + " ASC";
		Cursor mCursor = null; 
		String mDate = date == null? StringUtils.getTodaysDate() : date;
		
		switch (mode) {
		case ArticleHub.PORTADA:
			where = DbOpenHelper.PORTADA + " = '1' AND " + DbOpenHelper.FECHA + " LIKE '%" + mDate + "%'";
			break;
		case ArticleHub.ACTIVOS:
			where = DbOpenHelper.SECCION + " LIKE '%Activos%' AND " + DbOpenHelper.FECHA + " LIKE '%" + mDate + "%'";
			break;
		case ArticleHub.AMBITOS:
			where = DbOpenHelper.SECCION + " LIKE '%mbito%' AND " + DbOpenHelper.FECHA + " LIKE '%" + mDate + "%'";
			break;
		case ArticleHub.CULTURA:
			where = DbOpenHelper.SECCION + " LIKE '%Cultura%' AND " + DbOpenHelper.FECHA + " LIKE '%" + mDate + "%'";
			break;
		case ArticleHub.PLANETA:
			where = DbOpenHelper.SECCION + " LIKE '%Planeta%' AND " + DbOpenHelper.FECHA + " LIKE '%" + mDate + "%'";
			break;
		case ArticleHub.PLAY:
			where = DbOpenHelper.SECCION + " LIKE '%Play%' AND " + DbOpenHelper.FECHA + " LIKE '%" + mDate + "%'";
			break;
		case ArticleHub.PODERES:
			where = DbOpenHelper.SECCION + " LIKE '%Poderes%' AND " + DbOpenHelper.FECHA + " LIKE '%" + mDate + "%'";
			break;
		case ArticleHub.REPORTAJES:
			where = DbOpenHelper.SECCION + " LIKE '%Reportajes%' AND " + DbOpenHelper.FECHA + " LIKE '%" + mDate + "%'";
			break;
		case ArticleHub.TECNOLOGIA:
			where = DbOpenHelper.SECCION + " LIKE '%Tecnologia%' AND " + DbOpenHelper.FECHA + " LIKE '%" + mDate + "%'";
			break;
		case ArticleHub.VIDA:
			where = DbOpenHelper.SECCION + " LIKE '%Vida%' AND " + DbOpenHelper.FECHA + " LIKE '%" + mDate + "%'";
			break;
		case ArticleHub.VOCES:
			where = DbOpenHelper.SECCION + " LIKE '%Voces%' AND " + DbOpenHelper.FECHA + " LIKE '%" + mDate + "%'";
			break;
		case ArticleHub.DEPARTAMENTALES:
			where = DbOpenHelper.SECCION + " LIKE '%departamental%' AND " + DbOpenHelper.FECHA + " LIKE '%" + mDate + "%'";
			break;
		case ArticleHub.EMPRESARIALES:
			where = DbOpenHelper.SECCION + " LIKE '%empresariales%' AND " + DbOpenHelper.FECHA + " LIKE '%" + mDate + "%'";
			break;
		case ArticleHub.MARCADORES:
			where = DbOpenHelper.BOOKMARK + " = '1'";
			sortBy = DbOpenHelper.TITULO;
			break;
		default:
			where = null;
			break;
		}
		
		mCursor = mDatabase.query(table, new String [] {DbOpenHelper.LINK}, where, null, null, null, sortBy);
				
		if (mCursor != null) {			
			if (mCursor.moveToFirst()) {
				for (int i = 0; i < mCursor.getCount(); i++) {
					links.add(mCursor.getString(mCursor.getColumnIndex(DbOpenHelper.LINK)));
					mCursor.moveToNext();
				}
				return links;
			}
		}
		
		return links;
	}	
	
	public static Cursor getArticle (final Context context, String postLink) {
		
		if (mDatabase == null || !mDatabase.isOpen())
			open(context);	
		
		String where = DbOpenHelper.LINK + " = '" + postLink + "'";
		
		return mDatabase.query(DbOpenHelper.TABLE_NAME_ARTICLES, DbOpenHelper.PROJECTION_ARTICLES, where, null, null, null, null);
	
	}
	
	public static boolean isArticleAvailable (final Context context, String link) {
		if (mDatabase == null || !mDatabase.isOpen())
			open(context);	
		
		String where = DbOpenHelper.LINK + " = '" + link + "'";
		Cursor mCursor = mDatabase.query(DbOpenHelper.TABLE_NAME_ARTICLES, DbOpenHelper.PROJECTION_ARTICLES, where, null, null, null, null);
		if (mCursor == null) {
			return false;
		}
		if (!mCursor.moveToFirst()) {
			return false;
		}
		
		mCursor.close();
		
		return true;		
	}
	
	public static long saveArticle (final Context context, boolean notify, String url, String title, 
			String content, String banner, String seccion) {
		
		if (mDatabase == null || !mDatabase.isOpen())
			open(context);			
		
		String articleDate = StringUtils.getCustomDate(url);
		String gregorianDate = StringUtils.getGregorianDate(null);
		
		long insertId;
		
		String where = DbOpenHelper.LINK + " = '" + url + "'";
		
		Cursor cursor = mDatabase.query(DbOpenHelper.TABLE_NAME_ARTICLES, DbOpenHelper.PROJECTION_ARTICLES, where, null, null, null, null);
		
		ContentValues mValues = new ContentValues();
		mValues.put(DbOpenHelper.LINK, url);
		mValues.put(DbOpenHelper.TITULO, title);		
		mValues.put(DbOpenHelper.DESCRIPCION, content);
		mValues.put(DbOpenHelper.BANNER, banner);
		mValues.put(DbOpenHelper.SECCION, seccion);

		if (!cursor.moveToFirst()) {			
			//This article isn't stored yet			
			mValues.put(DbOpenHelper.FECHA, articleDate);
			mValues.put(DbOpenHelper.SAVED_ON, gregorianDate);
			mValues.put(DbOpenHelper.BOOKMARK, 0);
			insertId = mDatabase.insert(DbOpenHelper.TABLE_NAME_ARTICLES, null, mValues);
		}
		else {
			//Article stored, replace values
			insertId = mDatabase.update(DbOpenHelper.TABLE_NAME_ARTICLES, mValues, where, null);
		}		

		if (notify) {
			//Only notify when requested
			Intent intent = new Intent(ArticleViewFragment.ACTION_ARTICLE_LOADING_FINISHED);
			context.sendBroadcast(intent);
		}
		
		cursor.close();
		
		return insertId;
		
	}
	
	public static boolean isBookmark(final Context context, String url) {
		if (mDatabase == null || !mDatabase.isOpen())
			open(context);			
		
		String where = DbOpenHelper.LINK + " = '" + url + "'";
		Cursor cursor = mDatabase.query(DbOpenHelper.TABLE_NAME_FEED, new String [] {DbOpenHelper.BOOKMARK}, where, null, null, null, null);
		if (cursor != null) {
			if (cursor.moveToFirst()) {
				int bookmark = cursor.getInt(cursor.getColumnIndex(DbOpenHelper.BOOKMARK));
				cursor.close();
				if (bookmark == 1) {
					return true;
				}				
			}
		}		
		return false;
	}

	public static int setBookmark(final Context context, String url, int isBookmark) {
		if (mDatabase == null || !mDatabase.isOpen())
			open(context);			
		
		if (isBookmark == 0) {
			Toast.makeText(context, "Articulo removido de marcadores", Toast.LENGTH_SHORT).show();
		}
		else {
			Toast.makeText(context, "Articulo agregado a marcadores", Toast.LENGTH_SHORT).show();
		}
		
		String where = DbOpenHelper.LINK + " = '" + url + "'";
		ContentValues mValues = new ContentValues();
		mValues.put(DbOpenHelper.BOOKMARK, isBookmark);
		mDatabase.update(DbOpenHelper.TABLE_NAME_FEED, mValues, where, null);
		//updating article sometimes doesn't work right away as article may not be cached yet
		return mDatabase.update(DbOpenHelper.TABLE_NAME_ARTICLES, mValues, where, null);				
	}

}