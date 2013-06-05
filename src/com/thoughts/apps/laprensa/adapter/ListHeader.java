package com.thoughts.apps.laprensa.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.thoughts.apps.laprensa.R;
import com.thoughts.apps.laprensa.adapter.ContactUsAdapter.RowType;

public class ListHeader implements Item {

	private final String name;

	public ListHeader(String name) {
		this.name = name;
	}

	@Override
	public int getViewType() {
		return RowType.HEADER_ITEM.ordinal();
	}

	@Override
	public View getView(LayoutInflater inflater, View convertView) {
		View view;
		if (convertView == null) {
			view = (View) inflater.inflate(R.layout.list_header, null);
			// Do some initialization
		} else {
			view = convertView;
		}

		TextView text = (TextView) view.findViewById(R.id.list_header_text);
		text.setText(name);
		view.setClickable(false);
		
		return view;
	}
}
