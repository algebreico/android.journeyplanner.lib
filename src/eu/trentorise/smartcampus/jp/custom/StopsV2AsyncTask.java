package eu.trentorise.smartcampus.jp.custom;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import android.os.AsyncTask;
import android.util.Log;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import eu.trentorise.smartcampus.jp.custom.map.MapManager;
import eu.trentorise.smartcampus.jp.helper.JPHelper;
import eu.trentorise.smartcampus.jp.helper.JPParamsHelper;
import eu.trentorise.smartcampus.jp.model.SmartCheckStop;

//import com.google.android.gms.maps.GoogleMap;

public class StopsV2AsyncTask extends AsyncTask<Object, SmartCheckStop, Boolean> {

	public interface OnStopLoadingFinished {
		public void onStopLoadingFinished(boolean result, double[] location, double diagonal);
	}

	private final String TAG = "StopsV2AsyncTask";

	private SherlockFragmentActivity mActivity;

	private OnStopLoadingFinished mOnStopLoadingFinished;

	private String[] selectedAgencyIds;
	private double[] location;
	private boolean zoomLevelChanged;
	private double diagonal;
	private GoogleMap map;

	List<SmartCheckStop> stops = new ArrayList<SmartCheckStop>();

	public StopsV2AsyncTask(SherlockFragmentActivity mActivity, String[] selectedAgencyIds, LatLng latLng, double diagonal,
			GoogleMap map, boolean zoomLevelChanged, OnStopLoadingFinished listener) {
		super();
		this.mActivity = mActivity;
		this.selectedAgencyIds = selectedAgencyIds;
		if (latLng != null) {
			this.location = new double[] { latLng.latitude, latLng.longitude };
		} else {
			List<Double> center = JPParamsHelper.getCenterMap();
			if (center != null) {
				this.location = new double[] {center.get(0),center.get(1)};
			}

		}

		this.diagonal = diagonal;
		this.map = map;
		this.zoomLevelChanged = zoomLevelChanged;
		this.mOnStopLoadingFinished = listener;
	}

	@Override
	protected void onPreExecute() {
		mActivity.setSupportProgressBarIndeterminateVisibility(true);
		super.onPreExecute();
	}

	@Override
	protected Boolean doInBackground(Object... params) {
		try {
			if (selectedAgencyIds != null) {
				for (int i = 0; i < selectedAgencyIds.length; i++) {
					stops.addAll(JPHelper.getStops(selectedAgencyIds[i], location, diagonal));
				}
			} else {
				stops.addAll(JPHelper.getStops(null, location, diagonal));
			}
		} catch (Exception e) {
			Log.e(getClass().getSimpleName(), e.getMessage());
			return false;
		}
		return !isCancelled();
	}

	@Override
	protected void onPostExecute(Boolean result) {
		super.onPostExecute(result);

		int newStops = 0;

		for (SmartCheckStop stop : stops) {
			if (isCancelled()) {
				Log.e(TAG, "loader cancelled");
				break;
			} else {
				boolean added = MapManager.getCache().addStop(stop);
				if (added) {
					newStops++;
				}
			}
		}

		Collection<SmartCheckStop> stops = MapManager.getCache().getStopsByAgencyIds(selectedAgencyIds);
		if (!stops.isEmpty() || newStops > 0 || zoomLevelChanged) {
			map.clear();
			List<MarkerOptions> cluster = MapManager.ClusteringHelper.cluster(mActivity.getApplicationContext(), map, stops);
			MapManager.ClusteringHelper.render(map, cluster);
		}

		if (mOnStopLoadingFinished != null) {
			mOnStopLoadingFinished.onStopLoadingFinished(result, location, diagonal);
		}

		mActivity.setSupportProgressBarIndeterminateVisibility(false);
	}

	@Override
	protected void onCancelled() {
		mActivity.setSupportProgressBarIndeterminateVisibility(false);
		super.onCancelled();
	}
}