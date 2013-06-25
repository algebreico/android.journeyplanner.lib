package eu.trentorise.smartcampus.jp;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridView;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragmentActivity;

import eu.trentorise.smartcampus.android.feedback.fragment.FeedbackFragment;
import eu.trentorise.smartcampus.jp.custom.AbstractAsyncTaskProcessorNoDialog;
import eu.trentorise.smartcampus.jp.custom.AsyncTaskNoDialog;
import eu.trentorise.smartcampus.jp.custom.CustomView;
import eu.trentorise.smartcampus.jp.custom.LinkedScrollView;
import eu.trentorise.smartcampus.jp.custom.TACGridView;
import eu.trentorise.smartcampus.jp.custom.TTAdapter;
import eu.trentorise.smartcampus.jp.custom.TTDelaysAdapter;
import eu.trentorise.smartcampus.jp.custom.TTStopsAdapter;
import eu.trentorise.smartcampus.jp.custom.TTTypesAdapter;
import eu.trentorise.smartcampus.jp.custom.data.SmartLine;
import eu.trentorise.smartcampus.jp.custom.data.TimeTable;
import eu.trentorise.smartcampus.jp.helper.JPHelper;
import eu.trentorise.smartcampus.jp.helper.RoutesHelper;
import eu.trentorise.smartcampus.protocolcarrier.exceptions.SecurityException;

public class SmartCheckTTFragment extends FeedbackFragment {

	protected static final String PARAM_SMARTLINE = "smartline";
	private static final int DAYS_WINDOWS = 0;

	private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm");
	
	private SmartLine params;
	private TimeTable actualTimeTable;
	private long from_date_millisecond;
	private long to_date_millisecond;
	private String[] stops = null;
	private String[] tripids = null;

	private Map<String, String>[] delays = null;
	private List<String> timesArr = null;
	private ProgressBar mProgressBar;
	private int displayedDay;
	private Date basic_date;
	private boolean todayView;
	private LinearLayout layout;
	private Boolean typeOfTransport = false;
	private Boolean created = true;
	private int NUM_COLS;
	private int NUM_ROWS;
	private int minFutureCol;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (savedInstanceState != null && savedInstanceState.containsKey(PARAM_SMARTLINE)) {
			this.params = (SmartLine) savedInstanceState.getParcelable(PARAM_SMARTLINE);
		} else if (getArguments() != null && getArguments().containsKey(PARAM_SMARTLINE)) {
			this.params = (SmartLine) getArguments().getParcelable(PARAM_SMARTLINE);
		}
		 if (this.params!=null)
			 if (RoutesHelper.AGENCYIDS_TRAINS.contains(RoutesHelper.getAgencyIdByRouteId(params.getRouteID().get(0))))
				 this.typeOfTransport=true;
		create_interval();
		// get the BusTimeTable
		AsyncTaskNoDialog<Object, Void, TimeTable> task = new AsyncTaskNoDialog<Object, Void, TimeTable>(
				getSherlockActivity(), new GetBusTimeTableProcessor(getSherlockActivity()));
		task.execute(from_date_millisecond, to_date_millisecond, params.getRouteID().get(0));
	}

	private void create_interval() {
		// create interval for 1 day and set from and to
		if (basic_date == null)
			basic_date = new Date();
		Calendar cal = Calendar.getInstance();
		cal.setTime(basic_date);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		Date from_date = cal.getTime();
		cal.set(Calendar.HOUR_OF_DAY, 23);
		cal.set(Calendar.MINUTE, 59);
		cal.set(Calendar.SECOND, 59);
		cal.set(Calendar.MILLISECOND, 999);
		Date to_date = cal.getTime();
		from_date_millisecond = from_date.getTime();
		to_date_millisecond = to_date.getTime();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.smartcheckbustt, container, false);
	}

	@Override
	public void onStart() {
		super.onStart();

		mProgressBar = (ProgressBar) getView().findViewById(R.id.smartcheckbustt_content_pb);
		if (created)
			{
			toggleProgressDialog();
			created=false;
			}
		LinearLayout linelayout = (LinearLayout) getSherlockActivity().findViewById(R.id.line_day);
		linelayout.setBackgroundColor(params.getColor());

		TextView lineNumber = (TextView) getSherlockActivity().findViewById(R.id.lineNumber);
		if (this.typeOfTransport)
			lineNumber.setTextSize(17);
		lineNumber.setText(params.getLine());
		lineNumber.setTextColor(getSherlockActivity().getResources().getColor(R.color.transparent_white));
		lineNumber.setBackgroundColor(params.getColor());
		TextView lineDay = (TextView) getSherlockActivity().findViewById(R.id.lineDay);
		lineDay.setTextColor(getSherlockActivity().getResources().getColor(R.color.transparent_white));
		lineDay.setBackgroundColor(params.getColor());
		// set the buttons for navigation

		// today
		Button todayButton = (Button) getView().findViewById(R.id.button_today);
		todayButtonCheck();

		todayButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				if (!mProgressBar.isShown())
					toggleProgressDialog();
				// -1 day
				AsyncTaskNoDialog<Object, Void, TimeTable> task = new AsyncTaskNoDialog<Object, Void, TimeTable>(
						getSherlockActivity(), new GetBusTimeTableProcessor(getSherlockActivity()));
				basic_date = null;
				create_interval();
				todayButtonCheck();
				task.execute(from_date_millisecond, to_date_millisecond, params.getRouteID().get(0));
			}
		});

		Button previousButton = (Button) getView().findViewById(R.id.button_previous);
		previousButton.setTextColor(getSherlockActivity().getResources().getColor(R.color.transparent_white));
		((GradientDrawable) previousButton.getBackground()).setColor(params.getColor());
		previousButton.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				Button previousButton = (Button) getView().findViewById(R.id.button_previous);

				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					// PRESSED
					((GradientDrawable) previousButton.getBackground()).setColor(getResources().getColor(
							android.R.color.holo_blue_light));

					return true; // if you want to handle the touch event
				case MotionEvent.ACTION_UP:
					// RELEASED
					((GradientDrawable) previousButton.getBackground()).setColor(params.getColor());
					if (!mProgressBar.isShown())
						toggleProgressDialog();
					// -1 day
					AsyncTaskNoDialog<Object, Void, TimeTable> task = new AsyncTaskNoDialog<Object, Void, TimeTable>(
							getSherlockActivity(), new GetBusTimeTableProcessor(getSherlockActivity()));
					Calendar cal = Calendar.getInstance();
					cal.setTime(basic_date);
					cal.add(Calendar.DAY_OF_YEAR, -1);
					cal.set(Calendar.MINUTE, 0);
					cal.set(Calendar.SECOND, 0);
					basic_date = cal.getTime();
					create_interval();
					todayButtonCheck();

					task.execute(from_date_millisecond, to_date_millisecond, params.getRouteID().get(0));
					return true; // if you want to handle the touch event
				}
				return false;
			}
		});

		Button nextButton = (Button) getView().findViewById(R.id.button_next);
		nextButton.setTextColor(getSherlockActivity().getResources().getColor(R.color.transparent_white));
		((GradientDrawable) nextButton.getBackground()).setColor(params.getColor());
		nextButton.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				Button nextButton = (Button) getView().findViewById(R.id.button_next);

				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					// PRESSED
					((GradientDrawable) nextButton.getBackground()).setColor(getResources().getColor(
							android.R.color.holo_blue_light));

					return true; // if you want to handle the touch event
				case MotionEvent.ACTION_UP:
					// RELEASED
					((GradientDrawable) nextButton.getBackground()).setColor(params.getColor());
					if (!mProgressBar.isShown())
						toggleProgressDialog();
					// +1 day
					AsyncTaskNoDialog<Object, Void, TimeTable> task = new AsyncTaskNoDialog<Object, Void, TimeTable>(
							getSherlockActivity(), new GetBusTimeTableProcessor(getSherlockActivity()));
					Calendar cal = Calendar.getInstance();
					cal.setTime(basic_date);
					cal.add(Calendar.DAY_OF_YEAR, 1);
					cal.set(Calendar.MINUTE, 0);
					cal.set(Calendar.SECOND, 0);
					basic_date = cal.getTime();

					create_interval();
					todayButtonCheck();

					task.execute(from_date_millisecond, to_date_millisecond, params.getRouteID().get(0));
					return true; // if you want to handle the touch event
				}
				return false;
			}
		});

	}

	// check if enable or disable the today button
	protected void todayButtonCheck() {
		Button todayButton = (Button) getView().findViewById(R.id.button_today);
		Date today = new Date();
		Calendar cal = Calendar.getInstance();
		cal.setTime(today);
		cal.set(Calendar.HOUR, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		Date morning = cal.getTime();
		cal.set(Calendar.HOUR, 23);
		cal.set(Calendar.MINUTE, 59);
		cal.set(Calendar.SECOND, 999);
		Date evening = cal.getTime();
		if (basic_date.after(morning) && basic_date.before(evening)) {
			todayView = true;
			todayButton.setTextColor(getSherlockActivity().getResources().getColor(R.color.transparent_white));
			((GradientDrawable) todayButton.getBackground()).setColor(getResources().getColor(
					android.R.color.holo_blue_light));
		} else {
			todayView = false;
			todayButton.setTextColor(getSherlockActivity().getResources().getColor(R.color.transparent_white));
			((GradientDrawable) todayButton.getBackground()).setColor(params.getColor());
		}

	}


	private class GetDelayProcessor extends AbstractAsyncTaskProcessorNoDialog<Object, List<List<Map<String, String>>>> {

		public GetDelayProcessor(SherlockFragmentActivity activity) {
			super(activity);
		}

		@Override
		public List<List<Map<String, String>>> performAction(Object... params) throws SecurityException, Exception {

			return JPHelper.getDelay((String) params[2], (Long) params[0], (Long) params[1]);
		}

		@Override
		public void handleFailure(Exception e) {
//			super.handleFailure(e);
		}

		@Override
		public void handleConnectionError() {
//			super.handleConnectionError();
		}

		@Override
		public void handleResult(List<List<Map<String, String>>> result) {
			// refresh delay with new data
			int tempNumbCol=0;
			for (List<Map<String, String>> tt : result) {
				tempNumbCol += tt.size();
			}

			final int NUM_COLS = tempNumbCol;
				int indexOfDay = 0;
				int indexOfCourseInThatDay = 0;

				for (int j = 0; j < NUM_COLS; j++) {
					while (result.get(indexOfDay).isEmpty()) {
						if (indexOfDay == 0) {
							displayedDay = 1;
						}
						indexOfDay++;
					}

					Map<String, String> actualDelays = result.get(indexOfDay)
								.get(indexOfCourseInThatDay);
						delays[j] = actualDelays;
					


					if (indexOfCourseInThatDay == result.get(indexOfDay).size() - 1) {
						if (indexOfDay < DAYS_WINDOWS)
							indexOfDay++;
						indexOfCourseInThatDay = 0;
					} else {
						indexOfCourseInThatDay++;
					}
				}
			
			// reload Delay part
			reloadDelays();
		}

	}

	private class GetBusTimeTableProcessor extends AbstractAsyncTaskProcessorNoDialog<Object, TimeTable> {

		public GetBusTimeTableProcessor(SherlockFragmentActivity activity) {
			super(activity);
		}

		@Override
		public TimeTable performAction(Object... params) throws SecurityException, Exception {
			long from_day = (Long) params[0];
			long to_day = (Long) params[1];
			String routeId = (String) params[2];
			TimeTable returnTimeTable = JPHelper.getLocalTransitTimeTableById(from_day, to_day, routeId);
			if (returnTimeTable == null)
				returnTimeTable = JPHelper.getTransitTimeTableById(from_day, to_day, routeId);

			actualTimeTable = returnTimeTable;
			initData(actualTimeTable);
			return returnTimeTable;
		}

		@Override
		public void handleFailure(Exception e) {
			super.handleFailure(e);
			getFragmentManager().popBackStack();
		}

		@Override
		public void handleResult(TimeTable result) {
			try {
				reloadTimeTable(actualTimeTable);

			} catch (Exception e) {
				e.printStackTrace();
				DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						switch (which) {
						case DialogInterface.BUTTON_POSITIVE:
							if (SmartCheckTTFragment.this.getSherlockActivity()!=null)
								SmartCheckTTFragment.this.getSherlockActivity().getSupportFragmentManager().popBackStack();
							break;

						}
					}
				};
				if (SmartCheckTTFragment.this.getSherlockActivity() != null) {
					AlertDialog.Builder builder = new AlertDialog.Builder(
							SmartCheckTTFragment.this.getSherlockActivity());

					builder.setMessage("Problem loading").setPositiveButton("Back", dialogClickListener).show();
				}
			}
			if (mProgressBar.isShown())
				toggleProgressDialog();

			AsyncTaskNoDialog<Object, Void, List<List<Map<String, String>>>> task = new AsyncTaskNoDialog<Object, Void, List<List<Map<String, String>>>>(
					getSherlockActivity(), new GetDelayProcessor(getSherlockActivity()));
			task.execute(from_date_millisecond, to_date_millisecond, params.getRouteID().get(0));

		}

	

	}

	private void reloadDelays() {
		GridView gwDelays = null;
		if (getSherlockActivity()!=null)
			gwDelays = (GridView) getSherlockActivity().findViewById(R.id.delays);
		if (gwDelays!=null)
			gwDelays.setAdapter(new TTDelaysAdapter(getSherlockActivity(),delays));
	}
	
	/*
	 * big method that build in runtime the timetable using the result get from
	 * processing. It's used only the first time
	 */

	private void reloadTimeTable(final TimeTable actualBusTimeTable) throws Exception {

		loadView(NUM_COLS, NUM_ROWS, minFutureCol);

	}

	/**
	 * @param actualBusTimeTable
	 * @throws ParseException
	 */
	protected void initData(final TimeTable actualBusTimeTable)
			throws ParseException {
				actualTimeTable = actualBusTimeTable;
				List<Integer> courseForDay = new ArrayList<Integer>();
				// sum of every column
				int tempNumbCol = 0;
				courseForDay.add(0);
		
				for (List<Map<String, String>> tt : actualBusTimeTable.getDelays()) {
					tempNumbCol += tt.size();
					courseForDay.add(tempNumbCol);
				}
		
				NUM_COLS = tempNumbCol;
				NUM_ROWS = actualBusTimeTable.getStops().size();
		
				delays = new HashMap[NUM_COLS];
				stops = new String[NUM_ROWS];
				tripids = new String[NUM_COLS];
				timesArr = new ArrayList<String>(NUM_COLS*NUM_ROWS);
				
				minFutureCol = Integer.MAX_VALUE;
				String refTime = TIME_FORMAT.format(new Date());
				
				// Initializing data
				for (int i = 0; i < NUM_ROWS; i++) {
		
					int indexOfDay = 0;
					int indexOfCourseInThatDay = 0;
					stops[i] = actualBusTimeTable.getStops().get(i);
		
					for (int j = 0; j < NUM_COLS; j++) {
						while (actualBusTimeTable.getDelays().get(indexOfDay).isEmpty()) {
							if (indexOfDay == 0) {
								displayedDay = 1;
							}
							indexOfDay++;
						}
		
						if (i == 0) {
							Map<String, String> actualDelays = actualBusTimeTable.getDelays().get(indexOfDay)
									.get(indexOfCourseInThatDay);
							delays[j] = actualDelays;
							if (typeOfTransport)
							{
								if (actualBusTimeTable.getTripIds()!=null)
									{
									String actualTripId = actualBusTimeTable.getTripIds().get(indexOfDay).get(indexOfCourseInThatDay);
									tripids[j] = actualTripId;
									}
								else typeOfTransport = false;
		
							}
						}
		
						String time = actualBusTimeTable.getTimes().get(indexOfDay).get(indexOfCourseInThatDay).get(i);
						time = time == null || time.length()==0 ? "":time.substring(0,5);
						timesArr.add(time);
						if (time.length() > 0) {
							if (time.compareTo(refTime) > 0 && minFutureCol > j) {
								minFutureCol = j;
							}
						}
						
						if (indexOfCourseInThatDay == actualBusTimeTable.getDelays().get(indexOfDay).size() - 1) {
							if (indexOfDay < DAYS_WINDOWS)
								indexOfDay++;
							indexOfCourseInThatDay = 0;
						} else {
							indexOfCourseInThatDay++;
						}
					}
				}
	}

	protected void loadView(int NUM_COLS, int NUM_ROWS, int minFutureCol) {
		getSherlockActivity().findViewById(R.id.layout_bustt).setVisibility(View.VISIBLE);
		// delays label
		getSherlockActivity().findViewById(R.id.twDelays).setMinimumHeight(TTAdapter.rowHeight(getSherlockActivity()));

		// stop list
		TACGridView lwStops = (TACGridView) getActivity().findViewById(R.id.stops);
		lwStops.setAdapter(new TTStopsAdapter(getSherlockActivity(),Arrays.asList(stops)));
		lwStops.setVerticalScrollBarEnabled(false);
		lwStops.setExpanded(true);

		GridView gwDelays = (GridView) getActivity().findViewById(R.id.delays);
		gwDelays.setAdapter(new TTDelaysAdapter(getSherlockActivity(),delays));
		gwDelays.setNumColumns(NUM_COLS);

		// times

		CustomView gw = (CustomView)getSherlockActivity().findViewById(R.id.gridview);
		gw.setMinimumHeight(TTAdapter.rowHeight(getActivity())*NUM_ROWS);
		gw.setMinimumWidth(TTAdapter.getPixels(getSherlockActivity(),TTAdapter.COL_WIDTH*NUM_COLS)+NUM_COLS+1);
		gw.setRowHeight(TTAdapter.rowHeight(getSherlockActivity()));
		gw.setColWidth(TTAdapter.colWidth(getSherlockActivity()));
		gw.setNumCols(NUM_COLS);
		gw.setNumRows(NUM_ROWS);
		gw.setTexts(timesArr);
		
		LinkedScrollView lswmain = (LinkedScrollView)getActivity().findViewById(R.id.mainscrollview);
		lswmain.setMinimumWidth(TTAdapter.colWidth(getActivity())*NUM_COLS);
		lswmain.setMinimumHeight(TTAdapter.rowHeight(getActivity())*NUM_ROWS);
		LinkedScrollView lswleft = (LinkedScrollView)getActivity().findViewById(R.id.leftscrollview);
		lswleft.setMinimumHeight(TTAdapter.rowHeight(getActivity())*NUM_ROWS);
		lswmain.others.add(lswleft);
		lswleft.others.add(lswmain);
		
		if (typeOfTransport && tripids != null){
			getSherlockActivity().findViewById(R.id.twTypes).setVisibility(View.VISIBLE);
			getSherlockActivity().findViewById(R.id.twTypes).setMinimumHeight(TTAdapter.rowHeight(getSherlockActivity()));
						
			// Type row
			GridView gwTypes = (GridView) getActivity().findViewById(R.id.types);
			gwTypes.setVisibility(View.VISIBLE);
			gwTypes.setAdapter(new TTTypesAdapter(getSherlockActivity(),Arrays.asList(tripids)));
			gwTypes.setNumColumns(NUM_COLS);
	
		} else {
			getSherlockActivity().findViewById(R.id.twTypes).setVisibility(View.GONE);
			getSherlockActivity().findViewById(R.id.types).setVisibility(View.GONE);
		}
		
		if (todayView) {
			final HorizontalScrollView hsw = (HorizontalScrollView)getActivity().findViewById(R.id.ttHsv); 
			final int shift = (minFutureCol < NUM_COLS ? minFutureCol : (NUM_COLS - 1)) * TTAdapter.colWidth(getActivity());
			hsw.post(new Runnable() {
				public void run() {
					hsw.smoothScrollTo(shift, 0);
				}
			});
		}
		refreshDayTextView(0); 
	}



	private void toggleProgressDialog() {
		if (mProgressBar != null) {
			if (mProgressBar.isShown()) {
				mProgressBar.setVisibility(View.INVISIBLE);
				if (layout != null) {
					layout.setVisibility(View.VISIBLE);
				}
			} else {
				mProgressBar.setVisibility(View.VISIBLE);
				if (layout != null) {
					layout.setVisibility(View.INVISIBLE);
				}
			}
		}
	}

	private void refreshDayTextView(int displayDay) {
		Date tempDate = new Date(from_date_millisecond);
		Calendar cal = Calendar.getInstance();
		cal.setTime(tempDate);
		cal.add(Calendar.DAY_OF_YEAR, displayedDay);
		tempDate = cal.getTime();
		long actualDate = tempDate.getTime();
		DateFormat dateFormat = new SimpleDateFormat("dd/MM/yy", Locale.getDefault());

		TextView lineDay = (TextView) getSherlockActivity().findViewById(R.id.lineDay);
		if (lineDay != null) {
			lineDay.setText(dateFormat.format(actualDate));
			lineDay.setTextColor(getSherlockActivity().getResources().getColor(R.color.transparent_white));
			lineDay.setBackgroundColor(params.getColor());
		}

	}


}
