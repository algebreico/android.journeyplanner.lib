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

import android.R.anim;
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
import com.github.espiandev.showcaseview.ShowcaseView;
import com.github.espiandev.showcaseview.ShowcaseView.OnShowcaseEventListener;

import eu.trentorise.smartcampus.android.feedback.utils.FeedbackFragmentInflater;
import eu.trentorise.smartcampus.jp.helper.JPHelper;
import eu.trentorise.smartcampus.jp.helper.JPHelper.Tutorial;
import eu.trentorise.smartcampus.jp.notifications.BroadcastNotificationsActivity;
import eu.trentorise.smartcampus.jp.notifications.NotificationsFragmentActivityJP;

<<<<<<< HEAD
public class HomeActivity extends BaseActivity implements OnShowcaseEventListener {
=======
public class HomeActivity extends BaseActivity implements
		OnShowcaseEventListener {
>>>>>>> branch '2013-11-6_crazy_week' of git@github.com:smartcampuslab/android.journeyplanner.lib.git

	private boolean mHiddenNotification;
	private ShowcaseView sv;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.home);
<<<<<<< HEAD

=======
		
		//DEBUG PURPOSE
		JPHelper.getTutorialPreferences(this).edit().clear().commit();
		
>>>>>>> branch '2013-11-6_crazy_week' of git@github.com:smartcampuslab/android.journeyplanner.lib.git
		if (getSupportActionBar().getNavigationMode() != ActionBar.NAVIGATION_MODE_STANDARD)
<<<<<<< HEAD
			getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
=======
			getSupportActionBar().setNavigationMode(
					ActionBar.NAVIGATION_MODE_STANDARD);
>>>>>>> branch '2013-11-6_crazy_week' of git@github.com:smartcampuslab/android.journeyplanner.lib.git

		// Feedback
		FeedbackFragmentInflater.inflateHandleButtonInRelativeLayout(this,
				(RelativeLayout) findViewById(R.id.home_relative_layout_jp));
<<<<<<< HEAD
=======

		setHiddenNotification();

		if (JPHelper.isFirstLaunch(this)){
			showTourDialog();
			JPHelper.disableFirstLaunch(this);
		}

	}
	
	

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		//This is needed because the ShowCaseLibrary doesn't provide anything
		//to manage screen rotation
		if(sv!=null && sv.isShown())
			sv.forceLayout();
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
>>>>>>> branch '2013-11-6_crazy_week' of git@github.com:smartcampuslab/android.journeyplanner.lib.git

<<<<<<< HEAD
		setHiddenNotification();

		if (JPHelper.isFirstLaunch(this)) {
			showTourDialog();
			JPHelper.disableFirstLaunch(this);
=======
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
>>>>>>> branch '2013-11-6_crazy_week' of git@github.com:smartcampuslab/android.journeyplanner.lib.git
		}
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
<<<<<<< HEAD
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.clear();
		MenuInflater inflater = getSupportMenuInflater();
		inflater.inflate(R.menu.emptymenu, menu);
		return true;
=======
		
	}

	@Override
	protected void onPostResume() {
		super.onPostResume();
		if (JPHelper.wantTour(this))
			showTutorials();
	}

	private void showTutorials() {
		JPHelper.Tutorial t = JPHelper.getLastTutorialNotShowed(this);
		String title = "", msg = "";
		int id = R.id.btn_myprofile;
		if (t != null)
			switch (t) {
			case PLAN:
				id = R.id.btn_planjourney;
				msg = getString(R.string.plan_tut);
				break;
			case WATCH:
				id = R.id.btn_monitorsavedjourney;
				msg = getString(R.string.watch_tut);
				break;
			case INFO:
				id = R.id.btn_smart;
				msg = getString(R.string.info_tut);
				break;
			case SEND:
				id = R.id.btn_broadcast;
				msg = getString(R.string.send_tut);
				break;
			case NOTIF:
				id = R.id.btn_notifications;
				msg = getString(R.string.notif_tut);
				break;
			case PREFST:
				id = R.id.btn_myprofile;
				msg = getString(R.string.prefs_tut);
				break;
			default:
				id = -1;
				break;
			}
		if (t != null){
			displayShowcaseView(id, title, msg);
			JPHelper.setTutorialAsShowed(this, t);
		}
		else
			JPHelper.setWantTour(this, false);
	}

	private void displayShowcaseView(int id, String title, String detail) {
		ShowcaseView.ConfigOptions options = new ShowcaseView.ConfigOptions();
		options.backColor = Color.argb(128, 34, 34, 34);
		options.hideOnClickOutside = false;
		options.buttonText = getString(R.string.next_tut);
		sv = ShowcaseView.insertShowcaseView(id, this, title, detail, options);
		sv.setOnShowcaseEventListener(this);
>>>>>>> branch '2013-11-6_crazy_week' of git@github.com:smartcampuslab/android.journeyplanner.lib.git
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
			if (sv != null && sv.isShown())
				sv.animateGesture(0, 0, 0, -300);
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
			ApplicationInfo ai = getPackageManager().getApplicationInfo(this.getPackageName(), PackageManager.GET_META_DATA);
			Bundle aBundle = ai.metaData;
			mHiddenNotification = aBundle.getBoolean("hidden-notification");
		} catch (NameNotFoundException e) {
			mHiddenNotification = false;
			Log.e(HomeActivity.class.getName(), "you should set the hidden-notification metadata in app manifest");
		}
		if (mHiddenNotification) {
			View notificationButton = findViewById(R.id.btn_notifications);
			if (notificationButton != null)
				notificationButton.setVisibility(View.GONE);
		}
	}

<<<<<<< HEAD
	private void showTourDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this).setMessage(getString(R.string.first_launch))
				.setPositiveButton(getString(R.string.begin_tut), new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						JPHelper.setWantTour(HomeActivity.this, true);
					}
				}).setNeutralButton(getString(android.R.string.cancel), new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						JPHelper.setWantTour(HomeActivity.this, false);
						dialog.dismiss();
					}
				});
		builder.create().show();
	}

=======
	@Override
	public void onShowcaseViewHide(ShowcaseView showcaseView) {
		if (JPHelper.wantTour(this))
			showTutorials();
	}

	@Override
	public void onShowcaseViewShow(ShowcaseView showcaseView) {
//		JPHelper.setTutorialAsShowed(this,
//				JPHelper.getLastTutorialNotShowed(this));
	}
>>>>>>> branch '2013-11-6_crazy_week' of git@github.com:smartcampuslab/android.journeyplanner.lib.git
}
