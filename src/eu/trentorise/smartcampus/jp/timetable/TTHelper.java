package eu.trentorise.smartcampus.jp.timetable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;
import eu.trentorise.smartcampus.android.common.Utils;
import eu.trentorise.smartcampus.jp.custom.data.TimeTable;

public class TTHelper {
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
	

		private static TTHelper instance = null;
		private static Context mContext;
		private static Map<Object, Object> calendar;
		private static final String calendarFilename= "calendar.js";

		protected TTHelper(Context mContext) {
			super();
			TTHelper.mContext = mContext;
			calendar=loadCalendar();
		}

		private Map<Object, Object> loadCalendar() {
			AssetManager assetManager = mContext.getResources().getAssets();
				InputStream in;
				try {
					in = assetManager.open(calendarFilename);
					String jsonParams = getStringFromInputStream(in);
					return Utils.convertJSONToObject(jsonParams, Map.class);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return null;
				}

				
		}
			private static String getStringFromInputStream(InputStream is) {
				String output = new String();

				BufferedReader br = null;
				StringBuilder sb = new StringBuilder();
				String line;

				try {
					br = new BufferedReader(new InputStreamReader(is));
					while ((line = br.readLine()) != null) {
						sb.append(line);
					}
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					if (br != null) {
						try {
							br.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}

				String json = sb.toString();

				try {
					JSONObject jsonObject = new JSONObject(json);
					output = jsonObject.toString();
				} catch (JSONException e) {
					Log.e("TTHelper", e.getMessage());
				}

				return output;
			}
		public static TimeTable getTTwithRouteIdAndTime(String routeId,long time){
			//convert time to date
			String date = convertMsToDateFormat(time);
			//get correct name of file
			String nameFile=routeId+ calendar.get(date)+".js";
			//get the new tt			
			return getTimeTable(nameFile);
			
		}
		private static TimeTable getTimeTable(String nameFile) {
			AssetManager assetManager = mContext.getResources().getAssets();
			InputStream in;
			try {
				in = assetManager.open(nameFile);
				String jsonParams = getStringFromInputStream(in);
				return Utils.convertJSONToObject(jsonParams, TimeTable.class);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			}
		}

		private static String convertMsToDateFormat(long time) {
			Date date=new Date(time);
	        SimpleDateFormat sdf = new SimpleDateFormat("yyyymmdd");
	        return sdf.format(date);
		}

		public static void init(Context mContext) {
			instance = new TTHelper(mContext);
		}

		public static boolean isInitialized() {
			return instance != null;
		}

		

	

}
