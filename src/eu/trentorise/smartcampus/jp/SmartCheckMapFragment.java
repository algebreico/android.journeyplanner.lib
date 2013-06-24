package eu.trentorise.smartcampus.jp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.RelativeLayout;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.Overlay;
import com.google.android.maps.Projection;

import eu.trentorise.smartcampus.android.feedback.fragment.FeedbackFragment;
import eu.trentorise.smartcampus.jp.custom.BetterMapView;
import eu.trentorise.smartcampus.jp.custom.BetterMapView.OnMapChanged;
import eu.trentorise.smartcampus.jp.custom.StopsAsyncTask;
import eu.trentorise.smartcampus.jp.custom.StopsAsyncTask.OnStopLoadingFinished;
import eu.trentorise.smartcampus.jp.custom.map.MapManager;
import eu.trentorise.smartcampus.jp.custom.map.StopObjectMapItemTapListener;
import eu.trentorise.smartcampus.jp.custom.map.StopsInfoDialog;
import eu.trentorise.smartcampus.jp.custom.map.StopsInfoDialog.OnDetailsClick;
import eu.trentorise.smartcampus.jp.custom.map.StopsItemizedOverlay;
import eu.trentorise.smartcampus.jp.helper.JPParamsHelper;
import eu.trentorise.smartcampus.jp.model.SmartCheckStop;
import eu.trentorise.smartcampus.jp.model.Square;

public class SmartCheckMapFragment extends FeedbackFragment implements StopObjectMapItemTapListener, OnMapChanged,
		OnStopLoadingFinished, OnDetailsClick {

	public final static String ARG_AGENCY_IDS = "agencyIds";
	public final static String ARG_STOP = "stop";
	public final static int REQUEST_CODE = 1983;

	private Context mContext;
	protected ViewGroup mapContainer;
	protected BetterMapView mapView;
	MyLocationOverlay mMyLocationOverlay = null;
	StopsItemizedOverlay stopsItemizedoverlay = null;

	private String[] selectedAgencyIds = null;
	private SmartCheckStop selectedStop = null;

	private StopsAsyncTask loader;
	private Runnable onFirstFixLoader;

	private Map<String, SmartCheckStop> smartCheckStopMap;
	private Square cache;

	private GeoPoint centerGeoPoint = null;

	private SmartCheckMapFragment mFragment = this;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.mContext = this.getSherlockActivity();

		setHasOptionsMenu(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mapContainer = new RelativeLayout(getActivity());

		mapView = MapManager.getBetterMapView();
		if (mapView == null) {
			mapView = new BetterMapView(getSherlockActivity(), getSherlockActivity().getResources().getString(
					R.string.maps_api_key), this);
			MapManager.setBetterMapView(mapView);
		}

		mapView.setClickable(true);
		mapView.setBuiltInZoomControls(true);

		// get arguments
		String[] argumentsSelectedAgencyIds = new String[] {};
		if (savedInstanceState != null && savedInstanceState.containsKey(ARG_AGENCY_IDS)) {
			argumentsSelectedAgencyIds = savedInstanceState.getStringArray(ARG_AGENCY_IDS);
		} else if (getArguments() != null && getArguments().containsKey(ARG_AGENCY_IDS)) {
			argumentsSelectedAgencyIds = getArguments().getStringArray(ARG_AGENCY_IDS);
		}

		final ViewGroup parent = (ViewGroup) mapView.getParent();
		if (parent != null) {
			parent.removeView(mapView);
		}
		mapContainer.addView(mapView);

		List<Overlay> listOfOverlays = mapView.getOverlays();
		mapView.getOverlays().clear();

		listOfOverlays.clear();
		stopsItemizedoverlay = new StopsItemizedOverlay(getSherlockActivity(), mapView);
		stopsItemizedoverlay.setMapItemTapListener(this);
		listOfOverlays.add(stopsItemizedoverlay);
		// setEventCategoriesToLoad("Family");

		mMyLocationOverlay = new MyLocationOverlay(getSherlockActivity(), mapView) {
			@Override
			protected void drawMyLocation(Canvas canvas, MapView mapView, Location lastFix, GeoPoint myLocation, long when) {
				Projection p = mapView.getProjection();
				float accuracy = p.metersToEquatorPixels(lastFix.getAccuracy());
				Point loc = p.toPixels(myLocation, null);
				Paint paint = new Paint();
				paint.setAntiAlias(true);
				// paint.setColor(Color.BLUE);
				paint.setColor(Color.parseColor(mContext.getResources().getString(R.color.jpappcolor)));

				if (accuracy > 10.0f) {
					paint.setAlpha(50);
					canvas.drawCircle(loc.x, loc.y, accuracy, paint);
					// border
					paint.setAlpha(200);
					paint.setStyle(Paint.Style.STROKE);
					canvas.drawCircle(loc.x, loc.y, accuracy, paint);
				}

				Bitmap bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.me).copy(
						Bitmap.Config.ARGB_8888, true);
				canvas.drawBitmap(bitmap, loc.x - (bitmap.getWidth() / 2), loc.y - bitmap.getHeight(), null);
			}
		};
		mMyLocationOverlay.enableMyLocation();
		listOfOverlays.add(mMyLocationOverlay);

		if (selectedAgencyIds == null
				|| !(Arrays.asList(argumentsSelectedAgencyIds).containsAll(Arrays.asList(selectedAgencyIds)))) {
			this.selectedAgencyIds = argumentsSelectedAgencyIds;
			this.smartCheckStopMap = new HashMap<String, SmartCheckStop>();
			mapView.getController().setZoom(JPParamsHelper.getZoomLevelMap() + 2);
		}

		onFirstFixLoader = new Runnable() {
			public void run() {
				SherlockFragmentActivity sfa = getSherlockActivity();

				if (sfa != null) {
					// mapView.getDiagonalLenght();
					// load with radius? Not for now.
					centerGeoPoint = mMyLocationOverlay.getMyLocation();
					mapView.getController().animateTo(centerGeoPoint);

					if (loader != null) {
						loader.cancel(true);
					}

					loader = new StopsAsyncTask(selectedAgencyIds, smartCheckStopMap, stopsItemizedoverlay, new double[] {
							centerGeoPoint.getLatitudeE6() / 1e6, centerGeoPoint.getLongitudeE6() / 1e6 },
							mapView.getDiagonalLenght(), mapView, mFragment);
					loader.execute();
				}
			}
		};

		// move to me
		mMyLocationOverlay.runOnFirstFix(onFirstFixLoader);

		// LOAD
		if (smartCheckStopMap != null && smartCheckStopMap.size() > 0) {
			stopsItemizedoverlay.addAllOverlays(smartCheckStopMap.values());
			stopsItemizedoverlay.populateAll();
			mapView.postInvalidate();
		}

		return mapContainer;
	}

	@Override
	public void onStart() {
		super.onStart();

		cache = null;
		// ActionBar actionBar = getSherlockActivity().getSupportActionBar();
		// actionBar.setDisplayHomeAsUpEnabled(true); // back arrow
		// actionBar.setDisplayUseLogoEnabled(false); // system logo
		// actionBar.setDisplayShowTitleEnabled(true); // system title
		// actionBar.setDisplayShowHomeEnabled(false); // home icon bar
		// actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
		// tabs

		// hide keyboard if it is still open
		InputMethodManager imm = (InputMethodManager) getSherlockActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(mapView.getWindowToken(), 0);
	}

	@Override
	public void onPause() {
		super.onPause();

		if (loader != null) {
			loader.cancel(true);
		}

		getSherlockActivity().setSupportProgressBarIndeterminateVisibility(false);

//		final ViewGroup parent = (ViewGroup) mapView.getParent();
//		if (parent != null) {
//			parent.removeView(mapView);
//		}
	}

	@Override
	public void onStopObjectTap(SmartCheckStop stopObject) {
		StopsInfoDialog stopInfoDialog = new StopsInfoDialog(this);
		Bundle args = new Bundle();
		args.putSerializable(StopsInfoDialog.ARG_STOP, stopObject);
		stopInfoDialog.setArguments(args);
		stopInfoDialog.show(getSherlockActivity().getSupportFragmentManager(), "stopselected");
	}

	@Override
	public void onStopObjectsTap(List<SmartCheckStop> stopObjectsList) {
		StopsInfoDialog stopInfoDialog = new StopsInfoDialog(this);
		Bundle args = new Bundle();
		args.putSerializable(StopsInfoDialog.ARG_STOPS, (ArrayList<SmartCheckStop>) stopObjectsList);
		stopInfoDialog.setArguments(args);
		stopInfoDialog.show(getSherlockActivity().getSupportFragmentManager(), "stopselected");
	}

	@Override
	public void OnDialogDetailsClick(SmartCheckStop stop) {
		FragmentTransaction fragmentTransaction = getSherlockActivity().getSupportFragmentManager().beginTransaction();
		Fragment fragment = new SmartCheckStopFragment();
		Bundle args = new Bundle();
		args.putSerializable(SmartCheckStopFragment.ARG_STOP, stop);
		fragment.setArguments(args);
		fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
		fragmentTransaction.replace(Config.mainlayout, fragment, "map");
		fragmentTransaction.addToBackStack(fragment.getTag());
		// fragmentTransaction.commitAllowingStateLoss();
		fragmentTransaction.commit();
		selectedStop = null;
	}

	@Override
	public void onStopLoadingFinished(boolean result, double[] location, double diagonal) {
		if (result) {
			if (cache != null) {
				cache.add(new Square(location, diagonal));
			} else {
				cache = new Square(location, diagonal);
			}
		}

		getSherlockActivity().setSupportProgressBarIndeterminateVisibility(false);
	}

	@Override
	public void onCenterChanged(GeoPoint center) {
		SherlockFragmentActivity sfa = getSherlockActivity();
		centerGeoPoint = center;
		Log.i("where", "Center Long: " + center.getLongitudeE6() / 1e6 + " Lat: " + center.getLatitudeE6() / 1e6);
		final double[] location = { center.getLatitudeE6() / 1e6, center.getLongitudeE6() / 1e6 };
		final double diagonal = mapView.getDiagonalLenght();

		// if (cache == null || cache.getLat() != location[0]
		// || cache.getLong() != location[1]) {
		Square s = new Square(location, diagonal);
		if (cache == null || cache.compareTo(s)) {
			if (loader != null) {
				loader.cancel(true);
			}

			if (sfa != null) {
				getSherlockActivity().setSupportProgressBarIndeterminateVisibility(true);
				loader = new StopsAsyncTask(selectedAgencyIds, smartCheckStopMap, stopsItemizedoverlay, location, diagonal,
						mapView, this);
				loader.execute();
			}
		}
		// }
	}

	@Override
	public void onZoomChanged(GeoPoint center, double diagonal) {
		SherlockFragmentActivity sfa = getSherlockActivity();
		Log.i("where",
				"DiagonalLenght: " + diagonal + "\nCenter Long: " + center.getLongitudeE6() / 1e6 + " Lat: "
						+ center.getLatitudeE6() / 1e6);
		final double[] location = { center.getLatitudeE6() / 1e6, center.getLongitudeE6() / 1e6 };
		// if (cache == null || diagonalLenght > cache.getDiagonal()) {
		Square s = new Square(location, diagonal);
		if (cache == null || cache.compareTo(s)) {
			if (loader != null) {
				loader.cancel(true);
			}

			if (sfa != null) {
				getSherlockActivity().setSupportProgressBarIndeterminateVisibility(true);
				loader = new StopsAsyncTask(selectedAgencyIds, smartCheckStopMap, stopsItemizedoverlay, location, diagonal,
						mapView, this);
				loader.execute();
			}
		}
		// }
	}

	public SmartCheckStop getSelectedStop() {
		return selectedStop;
	}

	public void setSelectedStop(SmartCheckStop selectedStop) {
		this.selectedStop = selectedStop;
	}
	
	@Override
	public void onConfigurationChanged(Configuration arg0) {
		super.onConfigurationChanged(arg0);
	}

	@Override
	public void onResume() {
		super.onResume();
	}
}
