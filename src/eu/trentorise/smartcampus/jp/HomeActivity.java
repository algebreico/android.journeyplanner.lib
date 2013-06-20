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

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.github.espiandev.showcaseview.BaseTutorialActivity;

import eu.trentorise.smartcampus.android.feedback.utils.FeedbackFragmentInflater;
import eu.trentorise.smartcampus.jp.custom.TutorialActivity;
import eu.trentorise.smartcampus.jp.helper.JPHelper;
import eu.trentorise.smartcampus.jp.notifications.BroadcastNotificationsActivity;
import eu.trentorise.smartcampus.jp.notifications.NotificationsFragmentActivityJP;

public class HomeActivity extends BaseActivity {

	private boolean mHiddenNotification;

	private final static int TUTORIAL_REQUEST_CODE = 1;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.home);
		if (getSupportActionBar().getNavigationMode() != ActionBar.NAVIGATION_MODE_STANDARD)
			getSupportActionBar().setNavigationMode(
					ActionBar.NAVIGATION_MODE_STANDARD);

		// DEBUG PURPOSE
		JPHelper.getTutorialPreferences(this).edit().clear().commit();

		// Feedback
		FeedbackFragmentInflater.inflateHandleButtonInRelativeLayout(this,
				(RelativeLayout) findViewById(R.id.home_relative_layout_jp));

		setHiddenNotification();

		if (JPHelper.isFirstLaunch(this)) {
			showTourDialog();
			JPHelper.disableFirstLaunch(this);
		}
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		// This is needed because the ShowCaseLibrary doesn't provide anything
		// to manage screen rotation
	}

	@Override
	protected void onStart() {
		super.onStart();

		getSupportActionBar().setHomeButtonEnabled(false);
		getSupportActionBar().setDisplayHomeAsUpEnabled(false);
		getSupportActionBar().setDisplayShowTitleEnabled(true);

		if (getSupportActionBar().getNavigationMode() != ActionBar.NAVIGATION_MODE_STANDARD) {
			getSupportActionBar().setNavigationMode(
					ActionBar.NAVIGATION_MODE_STANDARD);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.clear();
		MenuInflater inflater = getSupportMenuInflater();
		inflater.inflate(R.menu.emptymenu, menu);
		return true;
	}

	@Override
	protected void onPostResume() {
		super.onPostResume();
	}

	private void showTutorials() {
		JPHelper.Tutorial t = JPHelper.getLastTutorialNotShowed(this);
		String title = "", msg = "";
		int id = R.id.btn_myprofile;
		if (t != null)
			switch (t) {
			case PLAN:
				id = R.id.btn_planjourney;
				title = getString(R.string.btn_planjourney);
				msg = getString(R.string.plan_tut);
				break;
			case WATCH:
				id = R.id.btn_monitorsavedjourney;
				title = getString(R.string.btn_monitorsaved);
				msg = getString(R.string.watch_tut);
				break;
			case INFO:
				id = R.id.btn_smart;
				title = getString(R.string.btn_smartcheck);
				msg = getString(R.string.info_tut);
				break;
			case SEND:
				id = R.id.btn_broadcast;
				title = getString(R.string.btn_broadcast);
				msg = getString(R.string.send_tut);
				break;
			case NOTIF:
				id = R.id.btn_notifications;
				title = getString(R.string.btn_notifications);
				msg = getString(R.string.notif_tut);
				break;
			case PREFST:
				id = R.id.btn_myprofile;
				title = getString(R.string.btn_myprofile);
				msg = getString(R.string.prefs_tut);
				break;
			default:
				id = -1;
				break;
			}
		if (t != null) {
			displayShowcaseView(id, title, msg);
			JPHelper.setTutorialAsShowed(this, t);
		} else
			JPHelper.setWantTour(this, false);
	}

	private void displayShowcaseView(int id, String title, String detail) {
		int[] position = new int[2];
		View v = findViewById(id);

		if (v != null) {
			v.getLocationInWindow(position);
			BaseTutorialActivity.newIstance(this, position, v.getWidth(),Color.WHITE,null,
					title, detail, TUTORIAL_REQUEST_CODE,
					TutorialActivity.class);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			onBackPressed();
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onResume() {
		super.onResume();
		getSupportActionBar().setHomeButtonEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		JPHelper.getLocationHelper().start();
	}

	@Override
	protected void onPause() {
		super.onPause();
		JPHelper.getLocationHelper().stop();
	}

	public int getMainlayout() {
		return Config.mainlayout;
	}

	public void goToFunctionality(View view) {
		Intent intent;
		int viewId = view.getId();

		if (viewId == R.id.btn_planjourney) {
			intent = new Intent(this, PlanJourneyActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
			startActivity(intent);
			return;
		} else if (viewId == R.id.btn_monitorrecurrentjourney) {
			intent = new Intent(this, MonitorJourneyActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
			startActivity(intent);
			return;
		} else if (viewId == R.id.btn_broadcast) {
			intent = new Intent(this, BroadcastNotificationsActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
			startActivity(intent);
			return;
		} else if (viewId == R.id.btn_myprofile) {
			intent = new Intent(this, ProfileActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
			startActivity(intent);
			return;
		} else if (viewId == R.id.btn_monitorsavedjourney) {
			intent = new Intent(this, SavedJourneyActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
			startActivity(intent);
			return;
		} else if (viewId == R.id.btn_smart) {
			intent = new Intent(this, SmartCheckActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
			startActivity(intent);
			return;
		} else if (viewId == R.id.btn_notifications) {
			intent = new Intent(this, NotificationsFragmentActivityJP.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
			startActivity(intent);
			return;
		} else {
			Toast toast = Toast.makeText(getApplicationContext(), R.string.tmp,
					Toast.LENGTH_SHORT);
			toast.show();
			return;
		}
	}

	private void setHiddenNotification() {
		try {
			ApplicationInfo ai = getPackageManager().getApplicationInfo(
					this.getPackageName(), PackageManager.GET_META_DATA);
			Bundle aBundle = ai.metaData;
			mHiddenNotification = aBundle.getBoolean("hidden-notification");
		} catch (NameNotFoundException e) {
			mHiddenNotification = false;
			Log.e(HomeActivity.class.getName(),
					"you should set the hidden-notification metadata in app manifest");
		}
		if (mHiddenNotification) {
			View notificationButton = findViewById(R.id.btn_notifications);
			if (notificationButton != null)
				notificationButton.setVisibility(View.GONE);
		}
	}

	private void showTourDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this)
				.setMessage(getString(R.string.first_launch))
				.setPositiveButton(getString(R.string.begin_tut),
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								JPHelper.setWantTour(HomeActivity.this, true);
								showTutorials();
							}
						})
				.setNeutralButton(getString(android.R.string.cancel),
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								JPHelper.setWantTour(HomeActivity.this, false);
								dialog.dismiss();
							}
						});
		builder.create().show();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == TUTORIAL_REQUEST_CODE) {
			if (resultCode == RESULT_CANCELED) {
				if (JPHelper.wantTour(this))
					showTutorials();
			}
		}
	}
}
