package com.thoughts.apps.laprensa.fragments;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.thoughts.apps.laprensa.R;
import com.thoughts.apps.laprensa.adapter.ContactUsAdapter;
import com.thoughts.apps.laprensa.adapter.Item;
import com.thoughts.apps.laprensa.adapter.ListHeader;
import com.thoughts.apps.laprensa.adapter.ListItem;

public class ContactUsFragment extends Fragment {

	public ContactUsFragment () {}

	private ContactUsAdapter mAdapter;

	String [] jobTitles;
	String [] names;
	String [] emails;
	String [] phones;

	List<Item> items;

	ListView mListView;

	Resources mResources;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		mResources = activity.getResources();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		items = new ArrayList<Item>();

		jobTitles = mResources.getStringArray(R.array.publicidad_posiciones);
		names = mResources.getStringArray(R.array.publicidad_nombres);
		emails = mResources.getStringArray(R.array.publicidad_emails);
		phones = null;

		items.add(new ListHeader(getString(R.string.contactenos)));

		for (int i = 0; i < jobTitles.length; i++) {
			items.add(new ListItem(jobTitles[i], emails[i], names[i], null));
		}

		items.add(new ListHeader(getString(R.string.equipo_de_ventas)));

		jobTitles = getResources().getStringArray(R.array.ventas_posiciones);
		names = getResources().getStringArray(R.array.ventas_nombres);
		emails = getResources().getStringArray(R.array.ventas_emails);
		phones = getResources().getStringArray(R.array.ventas_telefonos);

		for (int i = 0; i < jobTitles.length; i++) {
			items.add(new ListItem(jobTitles[i], emails[i], names[i], phones[i]));
		}

		mAdapter = new ContactUsAdapter(getActivity(), items);

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View view = inflater.inflate(R.layout.contact_us_fragment, null);
		mListView = (ListView) view.findViewById(R.id.contact_list);
		mListView.setEmptyView(view.findViewById(R.id.empty));
		mListView.setAdapter(mAdapter);

		return view;
	}
}
