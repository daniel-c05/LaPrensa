package com.thoughts.apps.laprensa.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.thoughts.apps.laprensa.R;

public class SectionsAdapter extends BaseAdapter {
	
	LayoutInflater mInflater;
	String [] mEntries;
	int [] mIcons;
	
	public SectionsAdapter (final Context context, String [] entries, int [] icons) {
		this.mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.mEntries = entries;
		this.mIcons = icons;
	}

	@Override
	public int getCount() {		
		return mEntries.length;
	}

	@Override
	public Object getItem(int position) {
		return mEntries[position];
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder mHolder;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.seccion_list_item, null);
			mHolder = new ViewHolder();
			mHolder.title = (TextView) convertView.findViewById(R.id.section_title);
			mHolder.icon = (ImageView) convertView.findViewById(R.id.seccion_icon);
			convertView.setTag(mHolder);
		}
		else{
			mHolder = (ViewHolder) convertView.getTag();
		}
		
		mHolder.title.setText(mEntries[position]);
		mHolder.icon.setImageResource(mIcons[position]);
		
		return convertView;
	}
	
	private class ViewHolder {
		TextView title;
		ImageView icon;
	}
	
}
