package com.thoughts.apps.laprensa.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.thoughts.apps.laprensa.R;

public class TutorialPageFragment extends Fragment {
	
	public static final String EXTRA_PAGE_NUM = "tutorial:page_num";
	public static final int WELCOME = 0;
	public static final int SIDEBAR = 1;
	public static final int ARCHIVE = 2;
	public static final int MARCADORES = 3;
	public static final int THANKS = 4;
	private int mDisplayPage, mLayoutRes;

	public TutorialPageFragment () {}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mDisplayPage = getArguments().getInt(EXTRA_PAGE_NUM);
		findAppropriateResourceForPage();
	}

	private void findAppropriateResourceForPage() {
		switch (mDisplayPage) {
		case WELCOME:
			mLayoutRes = R.layout.welcome_pager_one;
			break;
		case SIDEBAR:
			mLayoutRes = R.layout.welcome_pager_two;
			break;
		case ARCHIVE:
			mLayoutRes = R.layout.welcome_pager_three;
			break;
		case MARCADORES:
			mLayoutRes = R.layout.welcome_pager_four;
			break;	
		case THANKS:
			mLayoutRes = R.layout.welcome_pager_five;
			break;
		default:
			mLayoutRes = R.layout.welcome_pager_one;
			break;
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View view = inflater.inflate(mLayoutRes, null);
		return view;
	}
}
