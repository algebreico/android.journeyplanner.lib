package eu.trentorise.smartcampus.jp.custom;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.os.AsyncTask;

import com.google.android.maps.MapView;

import eu.trentorise.smartcampus.jp.custom.map.StopsItemizedOverlay;
import eu.trentorise.smartcampus.jp.helper.JPHelper;
import eu.trentorise.smartcampus.jp.model.SmartCheckStop;

public class StopsAsyncTask extends AsyncTask<Object, SmartCheckStop, Boolean> {

	public interface OnStopLoadingFinished {
		public void onStopLoadingFinished(boolean result, double[] location, double diagonal);
	}

	private OnStopLoadingFinished mOnStopLoadingFinished;

	// private Collection<SmartCheckStop> list;
	private Map<String, SmartCheckStop> smartCheckStopMap;
	private StopsItemizedOverlay overlay;
	private double diagonal;
	private double[] location;
	private MapView mapView;
	private String[] selectedAgencyIds;
	private StopsItemizedOverlay old_overlay;

	public StopsAsyncTask(String[] selectedAgencyIds, Map<String, SmartCheckStop> smartCheckStopMap,
			StopsItemizedOverlay overlay, double[] location, double diagonal, MapView mapView, OnStopLoadingFinished listener) {
		super();
		this.overlay = overlay;
		this.mapView = mapView;
		this.diagonal = diagonal;
		this.location = location;
		this.mOnStopLoadingFinished = listener;
		// this.list = new ArrayList<SmartCheckStop>();
		this.smartCheckStopMap = smartCheckStopMap;

		this.selectedAgencyIds = selectedAgencyIds;
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		old_overlay = overlay;
	}

	@Override
	protected void onCancelled() {
		super.onCancelled();
	}

	@Override
	protected Boolean doInBackground(Object... params) {
		// params[0]=location
		// params[1]=radius
		try {
			List<SmartCheckStop> stops = new ArrayList<SmartCheckStop>();
			if (selectedAgencyIds != null) {
				for (int i = 0; i < selectedAgencyIds.length; i++) {
					try {
						stops.addAll(JPHelper.getStops(selectedAgencyIds[i], location, diagonal));
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			} else {
				try {
					stops.addAll(JPHelper.getStops(null, location, diagonal));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			for (SmartCheckStop stop : stops) {
				if (isCancelled()) {
					break;
				} else if (!smartCheckStopMap.containsKey(stop.getId())) {
					smartCheckStopMap.put(stop.getId(), stop);
					// list.add(stop);
					// publishProgress(stop);
					overlay.addOverlay(stop);
					overlay.populateAll();
					mapView.postInvalidate();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return !isCancelled();
	}

	@Override
	protected void onProgressUpdate(SmartCheckStop... values) {
		super.onProgressUpdate(values);
		// overlay.addOverlay(values[0]);
		// overlay.populateAll();
		// mapView.invalidate();
	}

	@Override
	protected void onPostExecute(Boolean result) {
		super.onPostExecute(result);
		if (!result)
			overlay = old_overlay;
		mOnStopLoadingFinished.onStopLoadingFinished(result, location, diagonal);
	}

}