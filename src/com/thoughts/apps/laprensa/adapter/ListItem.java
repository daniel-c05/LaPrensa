package com.thoughts.apps.laprensa.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.thoughts.apps.laprensa.R;
import com.thoughts.apps.laprensa.adapter.ContactUsAdapter.RowType;

public class ListItem implements Item {

	private final String mJobTitleStr, mEmailStr, mNameStr, mPhoneStr;

	public ListItem(String jobTitle, String email, String name, String phone) {
		this.mJobTitleStr = jobTitle;
		this.mEmailStr = email;
		this.mNameStr = name;
		this.mPhoneStr = phone;
	}

	@Override
	public int getViewType() {
		return RowType.LIST_ITEM.ordinal();
	}

	@Override
	public View getView(LayoutInflater inflater, View convertView) {
		ViewHolder mHolder;
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.list_item, null);
			mHolder = new ViewHolder();
			mHolder.mEmail = (TextView) convertView.findViewById(R.id.contact_email);
			mHolder.mJobTitle = (TextView) convertView.findViewById(R.id.contact_job_title);
			mHolder.mName = (TextView) convertView.findViewById(R.id.contact_name);
			mHolder.mPhone = (TextView) convertView.findViewById(R.id.contact_phone);
			convertView.setTag(mHolder);
		} else {
			mHolder = (ViewHolder) convertView.getTag();
		}
		
		mHolder.mJobTitle.setText(mJobTitleStr);
		mHolder.mEmail.setText(mEmailStr);		
		mHolder.mName.setText(mNameStr);
		
		if (mPhoneStr == null || mPhoneStr.equals("")) {
			mHolder.mPhone.setVisibility(View.GONE);
		}
		else{
			mHolder.mPhone.setVisibility(View.VISIBLE);
			mHolder.mPhone.setText(mPhoneStr);
		}

		return convertView;
	}
	
	private class ViewHolder {
		TextView mJobTitle, mName, mEmail, mPhone;
	}

}