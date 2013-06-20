/*******************************************************************************
 * Copyright 2012-2013 Trento RISE
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *        http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either   express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package eu.trentorise.smartcampus.jp;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import eu.trentorise.smartcampus.android.feedback.fragment.FeedbackFragment;
import eu.trentorise.smartcampus.jp.custom.FavoritesAdapter;
import eu.trentorise.smartcampus.jp.custom.UserPrefsHolder;
import eu.trentorise.smartcampus.jp.helper.PrefsHelper;

public class FavoritesFragment extends FeedbackFragment {
	// to be replaced with real favorites
	private UserPrefsHolder userPrefsHolder = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// TODO: REMEMBER THIS!!!
		// setHasOptionsMenu(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.favorites, container, false);
	}

	@Override
	public void onStart() {
		super.onStart();
		ListView list = (ListView) getSherlockActivity().findViewById(R.id.favorites_list);
		if (userPrefsHolder == null) {
			userPrefsHolder = PrefsHelper.sharedPreferences2Holder(getSherlockActivity().getSharedPreferences(
					Config.USER_PREFS, Context.MODE_PRIVATE));
		}
		FavoritesAdapter adapter = new FavoritesAdapter(getActivity(), R.layout.favorites_row, userPrefsHolder);
		list.setAdapter(adapter);
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		menu.clear();
		getSherlockActivity().getSupportMenuInflater().inflate(R.menu.addmenu, menu);

		super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.menu_add) {
			Toast.makeText(getSherlockActivity(), "Coming soon", Toast.LENGTH_SHORT).show();
			return true;
		} else {
			return super.onOptionsItemSelected(item);
		}
	}
}
