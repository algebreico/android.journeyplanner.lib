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
package eu.trentorise.smartcampus.jp.helper.processor;

import java.util.List;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.google.android.maps.MapView;

import eu.trentorise.smartcampus.jp.custom.AbstractAsyncTaskProcessor;
import eu.trentorise.smartcampus.jp.custom.map.ParkingsItemizedOverlay;
import eu.trentorise.smartcampus.jp.helper.JPHelper;
import eu.trentorise.smartcampus.jp.model.ParkingSerial;
import eu.trentorise.smartcampus.protocolcarrier.exceptions.SecurityException;

public class SmartCheckParkingMapProcessor extends AbstractAsyncTaskProcessor<Void, List<ParkingSerial>> {

	private String parkingAid;
	private MapView mapView;
	private ParkingsItemizedOverlay overlay;

	public SmartCheckParkingMapProcessor(SherlockFragmentActivity activity, MapView mapView, ParkingsItemizedOverlay overlay,
			String parkingAid) {
		super(activity);
		this.parkingAid = parkingAid;
		this.mapView = mapView;
		this.overlay = overlay;
	}

	@Override
	public List<ParkingSerial> performAction(Void... params) throws SecurityException, Exception {
		return JPHelper.getParkings(parkingAid);
	}

	@Override
	public void handleResult(List<ParkingSerial> result) {
		overlay.clearMarkers();
		overlay.addAllOverlays(result);
		overlay.populateAll();
		mapView.postInvalidate();
	}

}