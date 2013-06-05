package com.thoughts.apps.laprensa.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

import com.thoughts.apps.laprensa.Constants;

public class DbOpenHelper extends SQLiteOpenHelper {
	
	public static final String DB_NAME = "laprensa.db";
	
	public static final String TABLE_NAME_ARTICLES = "articles";
	public static final String TABLE_NAME_FEED = "feed";
	
	private static final int DB_VERSION = 1;
	
	/**
	 * The autoincrement key
	 */
	public static final String _ID = "_id";
	/**
	 * The Article Headline
	 */
	public static final String TITULO = "titulo";
	/**
	 * The full article body when viewing a Article or the preview of the body if viewing the feed. 
	 */
	public static final String DESCRIPCION = "descripcion";
	/**
	 * The Large Image that accompanies the article. 
	 */
	public static final String BANNER = "banner";
	/**
	 * The Url pointing to the full article
	 */
	public static final String LINK = "post_link";
	/**
	 * For Sorting
	 */
	public static final String LIST_POS = "position";
	/**
	 * 1 for Bookmarked, 0 for not bookmarked. 
	 */
	public static final String BOOKMARK = "bookmark";
	/**
	 * For Searching, could be Portada, Ambito, Planeta, etc
	 */
	public static final String SECCION = "seccion";
	/**
	 * The Date the article was posted
	 */
	public static final String FECHA = "fecha";
	/**
	 * Whether the article is part of the cover articles or not
	 */
	public static final String SAVED_ON = "f_descarga";
	/**
	 * Whether the article is part of the cover articles or not
	 */
	public static final String PORTADA = "portada";
		
	public static final String [] PROJECTION_FEED = {
		_ID,
		TITULO,
		DESCRIPCION,
		BANNER,
		LINK,
		BOOKMARK,
		SECCION,
		FECHA,
		PORTADA,
		SAVED_ON,
		LIST_POS		
	};
	
	public static final String [] PROJECTION_ARTICLES = {
		_ID,
		TITULO,
		DESCRIPCION,
		BANNER,
		LINK,
		BOOKMARK,
		SECCION,
		SAVED_ON,
		FECHA
	};	
	
	private static final String DATABASE_CREATE_ARTICLES = "create table " + TABLE_NAME_ARTICLES 
			+ "(" + _ID + " integer primary key autoincrement, " 
			+ TITULO + " text not null, " 
			+ DESCRIPCION + " text not null, "
			+ BANNER + " text not null, "
			+ LINK + " text not null, "
			+ BOOKMARK + " integer not null, "
			+ SECCION + " text not null, "
			+ SAVED_ON + " text not null, "
			+ FECHA + " text not null);"
			;
	
	private static final String DATABASE_CREATE_FEED = "create table " + TABLE_NAME_FEED 
			+ "(" + _ID + " integer primary key autoincrement, " 
			+ TITULO + " text not null, " 
			+ DESCRIPCION + " text not null, "
			+ BANNER + " text not null, "
			+ LINK + " text not null, "
			+ BOOKMARK + " integer not null, "
			+ SECCION + " text not null, "
			+ FECHA + " text not null, "
			+ PORTADA + " integer not null, "
			+ SAVED_ON + " text not null, "
			+ LIST_POS + " integer not null);"
			;
	
	public DbOpenHelper(Context context, CursorFactory factory,
			int version) {
		super(context, DB_NAME, factory, version);
	}
	
	public DbOpenHelper(Context context) {
		super(context, DB_NAME, null, DB_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(DATABASE_CREATE_ARTICLES);
		db.execSQL(DATABASE_CREATE_FEED);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Constants.logMessage("Upgrading database from version " + oldVersion + " to "
	            + newVersion + ", which will destroy all old data");
	    db.execSQL("DROP TABLE IF EXISTS " + DATABASE_CREATE_ARTICLES);
	    db.execSQL("DROP TABLE IF EXISTS " + DATABASE_CREATE_FEED);
	    onCreate(db);
	}
	
}
