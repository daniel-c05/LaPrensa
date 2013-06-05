package com.thoughts.apps.laprensa.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.support.v4.widget.SimpleCursorAdapter;
import android.text.Html;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.TextView;

import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;
import com.thoughts.apps.laprensa.R;
import com.thoughts.apps.laprensa.database.DbOpenHelper;
import com.thoughts.apps.laprensa.fragments.ArticleViewFragment;

/**
 * Adapter used to populate the News Feed List. 
 *
 */
@SuppressLint("DefaultLocale")
public class CursorListAdapter extends SimpleCursorAdapter {

	/**
	 * The layout inflater used to inflate the resource file. 
	 */
	private LayoutInflater mInflater;
	private int mRootLayout;
	Resources mResources;
	int mTextSize = ArticleViewFragment.FONT_SIZE_SMALL;
	int mTitleTextSizeRes = R.dimen.title_size_small;
	int mCategoryTextSizeRes = R.dimen.section_size_small;
	int mDescripTextSizeRes = R.dimen.content_size_small;
	boolean mShowImages;

	public CursorListAdapter(Context context, int layout, Cursor c,
			String[] from, int[] to, int flags, int textSize, boolean showImages) {
		super(context, layout, c, from, to, flags);
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mRootLayout = layout;
		mResources = context.getResources();
		setAdjustTextSize(textSize);
		mShowImages = showImages;
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		View container = mInflater.inflate(mRootLayout, null);

		ViewHolder mHolder = new ViewHolder();
		
		mHolder.category = (TextView) container.findViewById(R.id.article_category);
		mHolder.title = (TextView) container.findViewById(R.id.article_title);
		mHolder.description = (TextView) container.findViewById(R.id.article_descrp);
		mHolder.image = (ImageView) container.findViewById(R.id.article_image);

		mHolder.category.setTextSize(TypedValue.COMPLEX_UNIT_PX, mResources.getDimension(mCategoryTextSizeRes));
		mHolder.title.setTextSize(TypedValue.COMPLEX_UNIT_PX, mResources.getDimension(mTitleTextSizeRes));
		mHolder.description.setTextSize(TypedValue.COMPLEX_UNIT_PX, mResources.getDimension(mDescripTextSizeRes));
		
		if (mRootLayout == R.layout.bookmark_list_item) {		
			LayoutParams mLayoutParams = null;
			switch (mTextSize) {
			case ArticleViewFragment.FONT_SIZE_LARGE:
				mLayoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, (int) mResources.getDimension(R.dimen.bookmark_image_height_large_text));
				mHolder.image.setLayoutParams(mLayoutParams);
				break;
			case ArticleViewFragment.FONT_SIZE_EXTRA_LARGE:
				mLayoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, (int) mResources.getDimension(R.dimen.bookmark_image_height_extra_large_text));
				mHolder.image.setLayoutParams(mLayoutParams);
				break;
			default:
				break;
			}
		}
		else if (!mShowImages) {
			mHolder.image.setVisibility(View.GONE);
		}

		container.setTag(mHolder);

		return container;		
	}

	@Override
	public void bindView(View view, Context arg1, Cursor arg2) {
		ViewHolder mHolder = (ViewHolder) view.getTag();

		if (mHolder == null) {
			mHolder = new ViewHolder();
			mHolder.category = (TextView) view.findViewById(R.id.article_category);
			mHolder.title = (TextView) view.findViewById(R.id.article_title);
			mHolder.description = (TextView) view.findViewById(R.id.article_descrp);
			mHolder.image = (ImageView) view.findViewById(R.id.article_image);
		}
		
		if (mShowImages) {
			String bannerLink = mCursor.getString(mCursor.getColumnIndex(DbOpenHelper.BANNER));
			if (!bannerLink.contains("http") || bannerLink.contains("/star_") || !bannerLink.contains(".jpg")) {		
				if (mRootLayout == R.layout.bookmark_list_item) {
					mHolder.image.setVisibility(View.VISIBLE);
					UrlImageViewHelper.setUrlDrawable(mHolder.image, "noimage", R.drawable.bg_bookmarks_no_image);
				}
				else {
					mHolder.image.setVisibility(View.GONE);
				}			
			}
			else{
				mHolder.image.setVisibility(View.VISIBLE);
				UrlImageViewHelper.setUrlDrawable(mHolder.image, bannerLink);
			}
		}

		mHolder.category.setText(Html.fromHtml(mCursor.getString(mCursor.getColumnIndex(DbOpenHelper.SECCION)).toUpperCase()));
		mHolder.title.setText(Html.fromHtml(mCursor.getString(mCursor.getColumnIndex(DbOpenHelper.TITULO))));
		mHolder.description.setText(Html.fromHtml(mCursor.getString(mCursor.getColumnIndex(DbOpenHelper.DESCRIPCION))));
	}

	public String getString (int pos, String columnName) {
		if (mCursor.getCount() < pos) {
			return "";
		}

		mCursor.moveToPosition(pos);
		return mCursor.getString(mCursor.getColumnIndex(columnName));
	}

	/**
	 * Class used to store the references to Views in the container. 
	 * The usage of this ViewHolder patterns helps reduce the time in which the list is drawn. 
	 */
	private class ViewHolder {
		ImageView image;
		TextView category, title, description;
	}

	public void setAdjustTextSize(int size) {		
		mTextSize = size;
		switch (size) {
		case ArticleViewFragment.FONT_SIZE_SMALL:
			mTitleTextSizeRes = R.dimen.title_size_small;
			mCategoryTextSizeRes = R.dimen.description_size_small;
			mDescripTextSizeRes = R.dimen.description_size_small;
			break;
		case ArticleViewFragment.FONT_SIZE_MEDIUM:
			mTitleTextSizeRes = R.dimen.title_size_medium;
			mCategoryTextSizeRes = R.dimen.description_size_medium;
			mDescripTextSizeRes = R.dimen.description_size_medium;
			break;
		case ArticleViewFragment.FONT_SIZE_LARGE:			
			mTitleTextSizeRes = R.dimen.title_size_large;
			mCategoryTextSizeRes = R.dimen.description_size_large;
			mDescripTextSizeRes = R.dimen.description_size_large;
			break;
		case ArticleViewFragment.FONT_SIZE_EXTRA_LARGE:
			mTitleTextSizeRes = R.dimen.title_size_extra_large;
			mCategoryTextSizeRes = R.dimen.description_size_extra_large;
			mDescripTextSizeRes = R.dimen.description_size_extra_large;
			break;
		default:
			break;
		}
	}

}
