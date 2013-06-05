package com.thoughts.apps.laprensa.adapter;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

public class ContactUsAdapter extends ArrayAdapter<Item>{

	public static final int LIST_ITEM = 0;
	public static final int HEADER_ITEM = 1;

	public enum RowType {
		LIST_ITEM, HEADER_ITEM
	}

	private List<Item> mItems;
	private LayoutInflater mInflater;

	public ContactUsAdapter(Context context, List<Item> items) {
		super(context, 0, items);
		this.mItems = items;
		mInflater = LayoutInflater.from(context);
	}

	@Override
	public int getViewTypeCount() {
		return RowType.values().length;
	}

	@Override
	public int getItemViewType(int position) {
		return mItems.get(position).getViewType();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		return mItems.get(position).getView(mInflater, convertView);
	}

}
