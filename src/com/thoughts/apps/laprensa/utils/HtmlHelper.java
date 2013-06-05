package com.thoughts.apps.laprensa.utils;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.ArrayList;

import org.htmlcleaner.CleanerProperties;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;
import org.htmlcleaner.XPatherException;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.text.Html;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;
import com.thoughts.apps.laprensa.Constants;
import com.thoughts.apps.laprensa.Home;
import com.thoughts.apps.laprensa.R;
import com.thoughts.apps.laprensa.database.DbManager;
import com.thoughts.apps.laprensa.service.NewsFetcher;


/**
 * Helper class to read data from an HTML Document
 */
@SuppressLint("DefaultLocale")
public class HtmlHelper {

	public static final String WEB_PREFIX = "http://www.laprensa.com.ni";	
	public static String FULL_HTML_PREFIX = "http://";

	/**
	 * Specifies when did we last query for information. 
	 */
	public static final String KEY_LAST_FEED_PARSE = "last_feed_parse";
	
	private class Noticias {

		private static final String XPATH_NOTICIAS = 
				"//div[@class='noticia']";

		private static final String XPATH_TITULO = 
				"//h1/a";

		private static final String XPATH_LINK_ARTICULO = 
				"//h1/a/@href";

		private static final String XPATH_BANNER = 
				"//img/@src";

		private static final String XPATH_CUERPO = 
				"///p";		
	}

	public static void actualizarLista (final Context context, String mUrl) {
		new DownloadArticleListTask(context).execute(new String [] {mUrl});
	}

	private static class DownloadArticleListTask extends AsyncTask<String, Void, Boolean> {

		String mUrlStr;
		final Context mContext;
		int mPortada;

		public DownloadArticleListTask (final Context context) {
			mContext = context;
		}

		@Override
		protected Boolean doInBackground(String... params) {
			mUrlStr = params[0];
			mPortada = isPortada(mUrlStr);
			Constants.logMessage("Loading url: " + mUrlStr);
			long msLoading = System.currentTimeMillis();
			
			CleanerProperties mCleanerProperties = new CleanerProperties();
			//Do not allow multi-word attribute so we can include everything as 'noticia'
			mCleanerProperties.setAllowMultiWordAttributes(false);
			mCleanerProperties.setOmitComments(true);
			HtmlCleaner mHtmlCleaner = new HtmlCleaner(mCleanerProperties);
			
			try {
				//Use HTML cleaner to get the HTML turned into XML for easier access. 
				TagNode mNode = mHtmlCleaner.clean(new URL(mUrlStr));
				
				Constants.logMessage("Loading finished. Time taken: " + ((System.currentTimeMillis() - msLoading)/1000) + " seconds.");

				//Use XPATH to obtain object arrays for later usage. 
				
				Object[] mMainNodes = mNode.evaluateXPath(Noticias.XPATH_NOTICIAS);				

				int lenght = mMainNodes == null? 0 : mMainNodes.length;

				//Due to changing size of the arrays, we will just use a List rather than a simple array
				ArrayList<String> mTitulos = new ArrayList<String>();
				ArrayList<String> mLinks = new ArrayList<String>();
				ArrayList<String> mDescripciones = new ArrayList<String>();
				ArrayList<String> mBanner = new ArrayList<String>();

				Object [] mInnerNodes;
				String link;
				
				for (int i = 0; i < lenght; i++) {

					mInnerNodes = ((TagNode)mMainNodes[i]).evaluateXPath(Noticias.XPATH_TITULO);
					mTitulos.add(((TagNode)mInnerNodes[0]).getText().toString());

					mInnerNodes = ((TagNode)mMainNodes[i]).evaluateXPath(Noticias.XPATH_LINK_ARTICULO);
					link = mInnerNodes[0].toString().startsWith(FULL_HTML_PREFIX) ? mInnerNodes[0].toString() : WEB_PREFIX + mInnerNodes[0].toString();
					mLinks.add(link);

					mInnerNodes = ((TagNode)mMainNodes[i]).evaluateXPath(Noticias.XPATH_BANNER);	
					if (mInnerNodes.length == 0) {
						mBanner.add("");
					}
					else {
						link = mInnerNodes[0].toString().startsWith(FULL_HTML_PREFIX) ? mInnerNodes[0].toString() : WEB_PREFIX + mInnerNodes[0].toString();
						mBanner.add(encodePath(link));
					}

					mInnerNodes = ((TagNode)mMainNodes[i]).evaluateXPath(Noticias.XPATH_CUERPO);
					if (mInnerNodes.length == 0) {
						mDescripciones.add("");
					}
					else {
						mDescripciones.add(((TagNode)mInnerNodes[0]).getText().toString());
					}
										
				}	

				DbManager.saveFeed(mContext, mTitulos, mLinks, mBanner, mDescripciones, mPortada);
				
				
				SharedPreferences mPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
				String syncInterval = mPreferences.getString(mContext.getString(R.string.pref_key_sync_interval), "7200000");
				
				if (!syncInterval.equals("never") && isPortada(mUrlStr) == 1) {
					NewsFetcher.scheduleNextRefresh(mContext, Long.valueOf(syncInterval));
				}						
				return true;

			}
			catch (IOException e) {
				Constants.logMessage("IOException: " + e.toString());
				return false;
			}
			catch (XPatherException e) {
				Constants.logMessage("XPatherException: " + e.toString());
				return false;
			}
			catch (Exception e) {
				Constants.logMessage("Error downloading articles: " + e.toString());
				return false;
			}
		}
		
		@Override
		protected void onPostExecute(Boolean isFeedSaved) {			
			super.onPostExecute(isFeedSaved);
			if (!isFeedSaved) {
				//If feed wasn't saved by calling DBManager.saveFeed
				//Notify activity so an empty cursor can be loaded, and refresh icon changes
				Intent intent = new Intent(Home.ACTION_FEED_LOADING_FINISHED);
				mContext.sendBroadcast(intent);
			}
		}
	}
	
	public static int isPortada(String mUrlStr) {
		if (mUrlStr.length() <= 38) {
			return 1;
		}
		return 0;
	}

	public static String getSection (String link) {
		link = link.substring(38, link.length());
		return link.substring(0, link.indexOf("/"));
	}

	public static String encodePath(String path) {        

		if (path.contains("&amp;")) {
			path = path.replace("&amp;", "&");
		}

		char[] chars = path.toCharArray();

		boolean needed = false;
		for (char c : chars) {
			if (c == '[' || c == ']' || c == '|' || c == ' ') {
				needed = true;
				break;
			}
		}

		if (needed == false) {
			return path;
		}

		StringBuilder sb = new StringBuilder("");
		for (char c : chars) {        	
			if (c == '[' || c == ']' || c == '|') {
				sb.append('%');
				sb.append(Integer.toHexString(c));
			}
			else if (c == ' ') {
				sb.append('%');
				sb.append("20");
			}
			else {
				sb.append(c);
			}
		}

		return sb.toString();
	}

	private class Articulo {

		private static final String XPATH_ARTICLE_TITLE = 
				"//div[@id='actualidad']/div[@class='izq lenft']/h1";

		private static final String XPATH_STORE_PARAGRAPHS = 
				"//div[@id='news-content']//p";

		private static final String XPATH_ARTICLE_IMAGE = 
				"//div[@id='news-content']//img/@src";	

	}

	public static void getArticleContent (final Context context, boolean setContent, final String mUrl, 
			final TextView title, final TextView subtitle, final TextView content, final ImageView banner) {
		new GetArticleContentTask(context, setContent, title, subtitle, content, banner).execute(new String [] {mUrl});
	}
	
	public static void getArticleContent (final Context context, boolean setContent, final String mUrl) {
		new GetArticleContentTask(context, setContent).execute(new String [] {mUrl});
	}



	private static class GetArticleContentTask extends AsyncTask<String, Void, Void> {

		String mUrlStr;

		String mTitle, mContent, mBannerLink, mSeccion;
		
		final Context mContext;
		boolean mSetContent;
		
		WeakReference<ImageView> mBannerRef;
		WeakReference<TextView> mTitleRef, mSubTitleRef, mContentRef;
		
		public GetArticleContentTask (final Context context, boolean setContent, final TextView title,
				final TextView subtitle, final TextView content, final ImageView banner) {
			this.mContext = context;
			this.mSetContent = setContent;
			mBannerRef = new WeakReference<ImageView>(banner);
			mTitleRef = new WeakReference<TextView>(title);
			mSubTitleRef = new WeakReference<TextView>(subtitle);
			mContentRef = new WeakReference<TextView>(content);
		}
		
		public GetArticleContentTask (final Context context, boolean setContent) {
			this.mContext = context;
			this.mSetContent = setContent;
		}

		@Override
		protected Void doInBackground(String... params) {
			mUrlStr = params[0];
			HtmlCleaner mHtmlCleaner = new HtmlCleaner();
			try {
				TagNode mNode = mHtmlCleaner.clean(new URL(mUrlStr));
				Object[] mNodes = mNode.evaluateXPath(Articulo.XPATH_ARTICLE_TITLE);
				
				if (mNodes == null || mNodes.length == 0) {
					mTitle = "";
				}
				else {
					mTitle = ((TagNode)mNodes[0]).getText().toString();
				}	
								
				mSeccion = getSection(mUrlStr);

				mNodes = mNode.evaluateXPath(Articulo.XPATH_STORE_PARAGRAPHS);
				mContent = "";								

				String text;
				for (int i = 0; i < mNodes.length; i++) {
					text = ((TagNode)mNodes[i]).getText().toString();
					if (text.equals("&nbsp;") || text.length() == 0) {
						
					}
					else {
						mContent = mContent + "<p>" + text + "</p>";
					}										
				}

				mNodes = mNode.evaluateXPath(Articulo.XPATH_ARTICLE_IMAGE);
				
				if (mNodes == null || mNodes.length == 0) {
					mBannerLink = "";
				}
				else {
					Constants.logMessage("Images available for article: " + mNodes.length);
					mBannerLink = StringUtils.hackImage(encodePath(mNodes[0].toString()));					
				}				

				DbManager.saveArticle(mContext, mSetContent, mUrlStr, mTitle, mContent, mBannerLink, mSeccion);				
			}
			catch (IOException e) {
				Constants.logMessage("IOException: " + e.toString());
			}
			catch (XPatherException e) {
				Constants.logMessage("XPatherException: " + e.toString());
			}		
			catch (Exception e) {
				Constants.logMessage(e.toString());
			}
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result) {		
			super.onPostExecute(result);
			if (isCancelled()) {
				return;			
			}
			if (!mSetContent) {
				//If we are not supposed to set the content
				DbManager.setBookmark(mContext, mUrlStr, 1);
				return;
			}
			if (mTitleRef != null) {
				if (mTitleRef.get() != null) {
					if (mTitle == null || mTitle == "") {
						mTitleRef.get().setText(mTitle);
					}
					else {
						mTitleRef.get().setText(Html.fromHtml(mTitle));
					}
					
					if (mSeccion == null || mSeccion == "") {
						mSubTitleRef.get().setText(mSeccion);
					}
					else {
						mSubTitleRef.get().setText(Html.fromHtml(mSeccion.toUpperCase()));
					}
					
					if (mContent == null || mContent == "") {
						mContentRef.get().setText(mContent);
					}
					else {
						mContentRef.get().setText(Html.fromHtml(mContent));
					}			
					
					if (mBannerLink == null || !mBannerLink.startsWith(HtmlHelper.FULL_HTML_PREFIX) || mBannerLink.contains("/play_large.png")) {
						mBannerRef.get().setVisibility(View.GONE);
					}					
					else {
						mBannerRef.get().setVisibility(View.VISIBLE);
						
						UrlImageViewHelper.setUrlDrawable(mBannerRef.get(), mBannerLink);
					}	
				}
			}
		}
	
	}

}