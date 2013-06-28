package eu.trentorise.smartcampus.jp;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import eu.trentorise.smartcampus.jp.custom.StopsV2AsyncTask;
import eu.trentorise.smartcampus.jp.custom.map.MapManager;
import eu.trentorise.smartcampus.jp.custom.map.StopsInfoDialog;
import eu.trentorise.smartcampus.jp.custom.map.StopsInfoDialog.OnDetailsClick;
import eu.trentorise.smartcampus.jp.helper.JPHelper;
import eu.trentorise.smartcampus.jp.helper.JPParamsHelper;
import eu.trentorise.smartcampus.jp.model.LocatedObject;
import eu.trentorise.smartcampus.jp.model.SmartCheckStop;

public class SmartCheckMapV2Fragment extends SupportMapFragment implements OnCameraChangeListener, OnMarkerClickListener,
		OnDetailsClick {

	public final static String ARG_AGENCY_IDS = "agencyIds";
	public final static String ARG_STOP = "stop";
	public final static int REQUEST_CODE = 1983;

	private SherlockFragmentActivity mActivity;

	private String[] selectedAgencyIds = new String[] {};
	private LatLng centerLatLng;
	private float zoomLevel = JPParamsHelper.getZoomLevelMap() + 2;
	private StopsV2AsyncTask loader;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mActivity = (SherlockFragmentActivity) getActivity();

		// get arguments
		if (savedInstanceState != null && savedInstanceState.containsKey(ARG_AGENCY_IDS)) {
			selectedAgencyIds = savedInstanceState.getStringArray(ARG_AGENCY_IDS);
		} else if (getArguments() != null && getArguments().containsKey(ARG_AGENCY_IDS)) {
			selectedAgencyIds = getArguments().getStringArray(ARG_AGENCY_IDS);
		}

		setHasOptionsMenu(true);
	}

	@Override
	public void onResume() {
		super.onResume();

		getMap().setOnCameraChangeListener(this);
		getMap().setOnMarkerClickListener(this);

		// show my location
		getMap().setMyLocationEnabled(true);
		// move to my location
		if (JPHelper.getLocationHelper().getLocation() != null) {
			centerLatLng = new LatLng(JPHelper.getLocationHelper().getLocation().getLatitudeE6() / 1e6, JPHelper
					.getLocationHelper().getLocation().getLongitudeE6() / 1e6);

			getMap().animateCamera(CameraUpdateFactory.newLatLngZoom(centerLatLng, zoomLevel), 1, null);
		} else {
			getMap().animateCamera(CameraUpdateFactory.zoomTo(zoomLevel), 1, null);
		}

	}

	@Override
	public void onPause() {
		getMap().setMyLocationEnabled(false);
		getMap().setOnCameraChangeListener(null);
		super.onPause();
	}

	@Override
	public void onCameraChange(CameraPosition position) {
		boolean zoomLevelChanged = false;
		if (zoomLevel != position.zoom) {
			zoomLevelChanged = true;
			zoomLevel = position.zoom;
		}

		if (loader != null) {
			loader.cancel(true);
		}

		loader = new StopsV2AsyncTask(mActivity, selectedAgencyIds, centerLatLng, getDiagonalLenght(), getMap(),
				zoomLevelChanged, null);
		loader.execute();
	}

	@Override
	public boolean onMarkerClick(Marker marker) {
		String id = marker.getTitle();

		List<LocatedObject> list = MapManager.ClusteringHelper.getFromGridId(id);

		if (list == null || list.isEmpty()) {
			return true;
		}

		if (list.size() > 1 && getMap().getCameraPosition().zoom == getMap().getMaxZoomLevel()) {
			StopsInfoDialog stopInfoDialog = new StopsInfoDialog(this);
			Bundle args = new Bundle();
			args.putSerializable(StopsInfoDialog.ARG_STOPS, (ArrayList) list);
			stopInfoDialog.setArguments(args);
			stopInfoDialog.show(mActivity.getSupportFragmentManager(), "stopselected");
		} else if (list.size() > 1) {
			// getMap().animateCamera(CameraUpdateFactory.newLatLngZoom(marker.getPosition(),
			// zoomLevel + 1));
			MapManager.fitMapWithOverlays(list, getMap());
		} else {
			SmartCheckStop stop = (SmartCheckStop) list.get(0);
			if (stop != null) {
				StopsInfoDialog stopInfoDialog = new StopsInfoDialog(this);
				Bundle args = new Bundle();
				args.putSerializable(StopsInfoDialog.ARG_STOP, stop);
				stopInfoDialog.setArguments(args);
				stopInfoDialog.show(mActivity.getSupportFragmentManager(), "stopselected");
			}
		}
		// // default behavior
		// return false;
		return true;
	}

	@Override
	public void OnDialogDetailsClick(SmartCheckStop stop) {
		FragmentTransaction fragmentTransaction = mActivity.getSupportFragmentManager().beginTransaction();
		Fragment fragment = new SmartCheckStopFragment();
		Bundle args = new Bundle();
		args.putSerializable(SmartCheckStopFragment.ARG_STOP, stop);
		fragment.setArguments(args);
		fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
		fragmentTransaction.replace(Config.mainlayout, fragment, "map");
		fragmentTransaction.addToBackStack(fragment.getTag());
		// fragmentTransaction.commitAllowingStateLoss();
		fragmentTransaction.commit();
	}

	private double getDiagonalLenght() {
		LatLng lu = getMap().getProjection().getVisibleRegion().farLeft;
		LatLng rd = getMap().getProjection().getVisibleRegion().nearRight;
		double h = rd.longitude - lu.longitude;
		double w = lu.latitude - rd.latitude;
		double diagonal = Math.sqrt(Math.pow(w, 2) + Math.pow(h, 2));
		return diagonal;
	}

}
