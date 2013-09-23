package eu.trentorise.smartcampus.jp.timetable;

import it.sayservice.platform.smartplanner.data.message.cache.CacheUpdateResponse;
import it.sayservice.platform.smartplanner.data.message.otpbeans.CompressedTransitTimeTable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import android.os.AsyncTask;
import android.util.Log;
import eu.trentorise.smartcampus.jp.helper.JPHelper;
import eu.trentorise.smartcampus.jp.helper.RoutesDBHelper;
import eu.trentorise.smartcampus.jp.helper.RoutesDBHelper.AgencyDescriptor;
import eu.trentorise.smartcampus.protocolcarrier.exceptions.ConnectionException;
import eu.trentorise.smartcampus.protocolcarrier.exceptions.ProtocolException;
import eu.trentorise.smartcampus.protocolcarrier.exceptions.SecurityException;

public class CompressedTransitTimeTableCacheUpdaterAsyncTask extends
		AsyncTask<Map<String, String>, Integer, Map<String, AgencyDescriptor>> {

	private long time;

	@Override
	protected void onPreExecute() {
		time = System.currentTimeMillis();
		Log.e(getClass().getName(), "Agencies update started");
		super.onPreExecute();
	}

	@Override
	protected Map<String, AgencyDescriptor> doInBackground(Map<String, String>... params) {
		Map<String, String> map = params[0];
		Map<String, CacheUpdateResponse> cacheUpdateResponsesMap = null;
		Map<String, AgencyDescriptor> agencyDescriptorsMap = new HashMap<String, AgencyDescriptor>();

		try {
			cacheUpdateResponsesMap = JPHelper.getCacheStatus(map);

			for (Entry<String, CacheUpdateResponse> curEntry : cacheUpdateResponsesMap.entrySet()) {
				String agencyId = curEntry.getKey();
				List<String> addedList = curEntry.getValue().getAdded();
				List<CompressedTransitTimeTable> ctttList = new ArrayList<CompressedTransitTimeTable>();

				for (String addedFileName : addedList) {
					CompressedTransitTimeTable cttt = JPHelper.getCacheUpdate(agencyId, addedFileName);
					ctttList.add(cttt);
				}

				AgencyDescriptor agencyDescriptor = RoutesDBHelper.buildAgencyDescriptor(agencyId, curEntry.getValue(),
						ctttList);
				agencyDescriptorsMap.put(agencyId, agencyDescriptor);
			}
		} catch (ProtocolException e) {
			e.printStackTrace();
		} catch (ConnectionException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		}

		return agencyDescriptorsMap;
	}

	@Override
	protected void onPostExecute(Map<String, AgencyDescriptor> result) {
		time = (System.currentTimeMillis() - time) / 1000;
		Log.e(getClass().getName(), "Agencies updated: " + Integer.toString(result.size()) + " in " + Long.toString(time)
				+ " seconds.");
		super.onPostExecute(result);
	}

}
