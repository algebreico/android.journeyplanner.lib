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
//	private String[][] times = null;
	private List<String> timesArr = null;
//	private final int ROW_HEIGHT = 50;
//	private final int COL_WIDTH = 100;
//	private TableLayout tlMainContent = null;
//	private int firstColumn = 0;
//	private int endColumn = 0;
	private ProgressBar mProgressBar;
//	private EndlessLinkedScrollView mElsvMainContent;
//	private TextView tvday;
	private int displayedDay;
//	private boolean firstHasNoCourses;
	private Date basic_date;
	private boolean todayView;
//	private RenderTimeTableAsyncTask renderTimeTableAsyncTask;
	private LinearLayout layout;
	private Boolean typeOfTransport = false;
	private Boolean created = true;
	// private String agencyId;
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
		// RoutesHelper.getAgencyIdByRouteId(this.params.getRouteID().get(0));
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

//		getView().setOnKeyListener(new OnKeyListener() {
//			@Override
//			public boolean onKey(View v, int keyCode, KeyEvent event) {
//				if (keyCode == KeyEvent.KEYCODE_BACK) {
//					renderTimeTableAsyncTask.cancel(true);
//					return true;
//				}
//				return false;
//			}
//		});
//
		mProgressBar = (ProgressBar) getView().findViewById(R.id.smartcheckbustt_content_pb);
		if (created)
			{
			toggleProgressDialog();
			created=false;
			}
		LinearLayout linelayout = (LinearLayout) getSherlockActivity().findViewById(R.id.line_day);
		linelayout.setBackgroundColor(params.getColor());

		TextView lineNumber = (TextView) getSherlockActivity().findViewById(R.id.lineNumber);
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

//	@Override
//	public boolean onOptionsItemSelected(MenuItem item) {
//		if (item.getItemId() == android.R.id.home) {
//			renderTimeTableAsyncTask.cancel(true);
//		}
//		return super.onOptionsItemSelected(item);
//	}

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
			super.handleFailure(e);
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
//							firstHasNoCourses = true;
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
//			try {
//				reloadTimeTable(actualTimeTable);
//			} catch (Exception e) {
//				e.printStackTrace();
//			}	
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
//				if (delays == null)
//					reloadTimeTable(actualTimeTable);
//				else {
//					initData(actualTimeTable);
//					refreshTimes(0);
//
//				}

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

		// method that change the global variables when I have a new TT
//		private void initData(final TimeTable actualBusTimeTable) {
//			final int COL_PLACE_WIDTH = 170;
//			actualTimeTable = actualBusTimeTable;
//			long actualDate = from_date_millisecond;
//			SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yy", Locale.getDefault());
//			List<Integer> courseForDay = new ArrayList<Integer>();
//			// sum of every column
//			int tempNumbCol = 0;
//			courseForDay.add(0);
//
//			for (List<Map<String, String>> tt : actualBusTimeTable.getDelays()) {
//				tempNumbCol += tt.size();
//				courseForDay.add(tempNumbCol);
//			}
//
//			final int NUM_COLS = tempNumbCol;
//			final int NUM_ROWS = actualBusTimeTable.getStops().size();
//
//			delays = new HashMap[NUM_COLS];
//			stops = new String[NUM_ROWS];
//			times = new String[NUM_ROWS][NUM_COLS];
//			tripids = new String[NUM_COLS];
//			// Initializing data
//			for (int i = 0; i < NUM_ROWS; i++) {
//
//				int indexOfDay = 0;
//				int indexOfCourseInThatDay = 0;
//				stops[i] = actualBusTimeTable.getStops().get(i);
//
//				for (int j = 0; j < NUM_COLS; j++) {
//					while (actualBusTimeTable.getDelays().get(indexOfDay).isEmpty()) {
//						if (indexOfDay == 0) {
//							firstHasNoCourses = true;
//							displayedDay = 1;
//						}
//						indexOfDay++;
//					}
//
//					if (i == 0) {
//						Map<String, String> actualDelays = actualBusTimeTable.getDelays().get(indexOfDay)
//								.get(indexOfCourseInThatDay);
//						delays[j] = actualDelays;
//						if (typeOfTransport)
//						{
//						String actualTripId = actualBusTimeTable.getTripIds().get(indexOfDay).get(indexOfCourseInThatDay);
//						tripids[j] = actualTripId;
//						} else typeOfTransport = false;
//
//					}
//
//					times[i][j] = actualBusTimeTable.getTimes().get(indexOfDay).get(indexOfCourseInThatDay).get(i);
//
//					if (indexOfCourseInThatDay == actualBusTimeTable.getDelays().get(indexOfDay).size() - 1) {
//						if (indexOfDay < DAYS_WINDOWS)
//							indexOfDay++;
//						indexOfCourseInThatDay = 0;
//					} else {
//						indexOfCourseInThatDay++;
//					}
//				}
//			}
//		}

	}

	private void reloadDelays() {
		GridView gwDelays = (GridView) getActivity().findViewById(R.id.delays);
		gwDelays.setAdapter(new TTDelaysAdapter(getSherlockActivity(),delays));
	}
	
	/*
	 * big method that build in runtime the timetable using the result get from
	 * processing. It's used only the first time
	 */

	private void reloadTimeTable(final TimeTable actualBusTimeTable) throws Exception {

		loadView(NUM_COLS, NUM_ROWS, minFutureCol);
//		layout = (LinearLayout) getSherlockActivity().findViewById(R.id.layout_bustt);
//
//		// setup left column with row labels
//		LinearLayout leftlayout = new LinearLayout(getSherlockActivity());
//		leftlayout.setOrientation(LinearLayout.VERTICAL);
//		TextView dayLabel = new TextView(getSherlockActivity());
//		dayLabel.setText(getString(R.string.dayLabel));
//		dayLabel.setTextAppearance(getSherlockActivity(), R.style.place_tt_jp);
//		dayLabel.setBackgroundResource(R.drawable.cell_place);
//		dayLabel.setGravity(Gravity.CENTER);
//		dayLabel.setMinHeight(ROW_HEIGHT);
		
//		if (typeOfTransport){
//			TextView TypeLabel = new TextView(getSherlockActivity());
//			TypeLabel.setText(R.string.typeLabel);
//			TypeLabel.setTextAppearance(getSherlockActivity(), R.style.late_tt_jp);
//			TypeLabel.setBackgroundResource(R.drawable.cell_place);
//			TypeLabel.setGravity(Gravity.CENTER);
//			TypeLabel.setMinHeight(ROW_HEIGHT);
//			leftlayout.addView(TypeLabel);
//		}
//		
//		TextView delaysLabel = new TextView(getSherlockActivity());
//		delaysLabel.setText(R.string.delaysLabel);
//		delaysLabel.setTextAppearance(getSherlockActivity(), R.style.late_tt_jp);
//		delaysLabel.setBackgroundResource(R.drawable.cell_place);
//		delaysLabel.setGravity(Gravity.CENTER);
//		delaysLabel.setMinHeight(ROW_HEIGHT);
//
//		// leftlayout.addView(dayLabel);
//		leftlayout.addView(delaysLabel);
//
//		LinkedScrollView lsvLeftCol = new LinkedScrollView(getSherlockActivity());
//		lsvLeftCol.setVerticalScrollBarEnabled(false);
//
//		TableLayout tlLeftCol = new TableLayout(getSherlockActivity());
//		TableLayout.LayoutParams tlLeftColParams = new TableLayout.LayoutParams();
//		tlLeftColParams.width = COL_PLACE_WIDTH;
//		tlLeftCol.setLayoutParams(tlLeftColParams);
//		for (int i = 0; i < stops.length; i++) {
//			TableRow tr = new TableRow(getSherlockActivity());
//			TextView tv = new TextView(getSherlockActivity());
//			if (i >= 0) {
//				final String stop = stops[i];
//				tv.setText(stop);
//				tv.setMinimumHeight(ROW_HEIGHT);
//				tv.setWidth(COL_PLACE_WIDTH);
//				tv.setEllipsize(TruncateAt.MARQUEE);
//				tv.setFocusable(true);
//				tv.setFocusableInTouchMode(true);
//				tv.setMarqueeRepeatLimit(1);
//				tv.setHorizontallyScrolling(true);
//				tv.setSingleLine(true);
//				tv.setGravity(Gravity.CENTER_VERTICAL);
//				tv.setTextAppearance(getSherlockActivity(), R.style.place_tt_jp);
//				tv.setBackgroundResource(R.drawable.cell_place);
//				tv.setPadding(10, 0, 0, 0);
//				tv.setOnClickListener(new OnClickListener() {
//
//					@Override
//					public void onClick(View v) {
//						// this is needed to view all content of the cell
//						Toast.makeText(getSherlockActivity(), stop, Toast.LENGTH_LONG).show();
//					}
//				});
//
//			} else
//				tr.addView(new TextView(getSherlockActivity()));
//			tr.addView(tv);
//			tr.setMinimumHeight(ROW_HEIGHT);
//
//			tlLeftCol.addView(tr);
//		}
//		lsvLeftCol.addView(tlLeftCol);
//
//		// add the main horizontal scroll
//		HorizontalScrollView hsvMainContent = new HorizontalScrollView(getSherlockActivity());
//
//		// you could probably leave this one enabled if you want
//		hsvMainContent.setHorizontalScrollBarEnabled(false);
//
//		// Scroll view needs a single child
//		LinearLayout llMainContent = new LinearLayout(getSherlockActivity());
//		llMainContent.setOrientation(LinearLayout.VERTICAL);
//
//		// add the headings
//		TableLayout tlColHeadings = new TableLayout(getSherlockActivity());
//
//		// Day row
//		tvday = new TextView(getSherlockActivity());
//		tvday.setBackgroundColor(getSherlockActivity().getResources().getColor(android.R.color.white));
//		tvday.setTextAppearance(getSherlockActivity(), R.style.day_tt_jp);
//		tvday.setMinimumHeight(ROW_HEIGHT);
//
//		refreshDayTextView(displayedDay);
//		if (typeOfTransport){
//		// Type row
//		TableRow trType = new TableRow(getSherlockActivity());
//		trType.setId(R.id.type_row);
//		trType.setGravity(Gravity.BOTTOM);
//		trType.setMinimumHeight(ROW_HEIGHT);
//		tlColHeadings.addView(trType);
//		}
//		// Delays row
//		TableRow trDelays = new TableRow(getSherlockActivity());
//		trDelays.setId(R.id.delays_row);
//		trDelays.setGravity(Gravity.BOTTOM);
//		trDelays.setMinimumHeight(ROW_HEIGHT);
//		tlColHeadings.addView(trDelays);
//
//		// llMainContent.addView(tvday);
//		llMainContent.addView(tlColHeadings);
//
//		// now lets add the main content
//		mElsvMainContent = new EndlessLinkedScrollView(getSherlockActivity(), SmartCheckTTFragment.this);
//		mElsvMainContent.tollerance += 20;
//
//		tlMainContent = new TableLayout(getSherlockActivity());
//		tlMainContent.setId(R.id.ttTimeTable);
//		tlMainContent.setVerticalScrollBarEnabled(true);
//
//		renderTimeTableAsyncTask = new RenderTimeTableAsyncTask(this);
//		renderTimeTableAsyncTask.execute(0, NUM_ROWS);
//
//		mElsvMainContent.addView(tlMainContent);
//
//		llMainContent.addView(mElsvMainContent);
//
//		hsvMainContent.addView(llMainContent);
//
//		leftlayout.addView(lsvLeftCol);
//
//		layout.addView(leftlayout);
//		layout.addView(hsvMainContent);
//
//		// the magic
//		mElsvMainContent.others.add(lsvLeftCol);
//		lsvLeftCol.others.add(mElsvMainContent);
//
//		// this is here because it needs the delays rows already visible.
//		refreshDelays(0);
//		if (typeOfTransport)
//			refreshType(0);
//
	}

	/**
	 * @param actualBusTimeTable
	 * @throws ParseException
	 */
	protected void initData(final TimeTable actualBusTimeTable)
			throws ParseException {
		//		final int COL_PLACE_WIDTH = 170;
				actualTimeTable = actualBusTimeTable;
		//		long actualDate = from_date_millisecond;
		//		SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yy", Locale.getDefault());
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
		//		times = new String[NUM_ROWS][NUM_COLS];
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
		//						firstHasNoCourses = true;
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
		//				times[i][j] = time;
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
//		TACGridView gwMain = (TACGridView) getSherlockActivity().findViewById(R.id.gridview);
//		gwMain.setAdapter(new TTTimesAdapter(getSherlockActivity(),timesArr));
//		gwMain.setVerticalScrollBarEnabled(false);
//		gwMain.setNumColumns(NUM_COLS);
//		gwMain.setExpanded(true);
		CustomView gw = (CustomView)getSherlockActivity().findViewById(R.id.gridview);
		gw.setMinimumHeight(TTAdapter.getPixels(getSherlockActivity(),TTAdapter.ROW_HEIGHT*NUM_ROWS)+NUM_ROWS+1);
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

//	private void refreshType(int displayedDay) {
//		TableRow trType = (TableRow) getView().findViewById(R.id.type_row);
//		trType.removeAllViews();
//
//		// Delays are available only for the current day.
//		if (displayedDay == 0) {
//			for (int i = 0; i < actualTimeTable.getTripIds().get(displayedDay).size(); i++) {
//				LinearLayout dll = new LinearLayout(getSherlockActivity());
//				dll.setMinimumWidth(COL_WIDTH);
//				dll.setMinimumHeight(ROW_HEIGHT);
//				dll.setOrientation(LinearLayout.HORIZONTAL);
//				dll.setBackgroundColor(getSherlockActivity().getResources().getColor(R.color.sc_light_gray));
//				dll.setBackgroundResource(R.drawable.cell_late);
//				dll.setGravity(Gravity.CENTER);
//
//				String tripidlist = tripids[i];
//
//						//set the element
//						TextView tv = new TextView(getSherlockActivity());
//						if (tripidlist.toLowerCase().startsWith("r"))
//							tv.setText("R");
//						if (tripidlist.toLowerCase().startsWith("e"))
//							tv.setText("E");
//						if (tripidlist.toLowerCase().startsWith("i"))
//							tv.setText("IC");
////						CreatorType ct = CreatorType.getAlertType(delay.getKey());
////
////						delaysCreatorTypesMap.put(ct, delay.getValue());
////
////						TextView tv = new TextView(getSherlockActivity());
////						tv.setLayoutParams(new TableLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
////								LayoutParams.WRAP_CONTENT, 1f));
////						tv.setBackgroundColor(getSherlockActivity().getResources().getColor(R.color.sc_light_gray));
////						tv.setBackgroundResource(R.drawable.cell_late);
////						tv.setGravity(Gravity.CENTER);
////						tv.setTextAppearance(getSherlockActivity(), android.R.style.TextAppearance_Small);
////
////						if (ct.equals(CreatorType.USER)) {
////							tv.setTextColor(getSherlockActivity().getResources().getColor(R.color.blue));
////							tv.setText(getSherlockActivity().getString(R.string.smart_check_tt_delay_user,
////									delay.getValue()));
////						} else {
////							tv.setTextColor(getSherlockActivity().getResources().getColor(R.color.red));
////							tv.setText(getSherlockActivity().getString(R.string.smart_check_tt_delay, delay.getValue()));
////						}
//
//						dll.addView(tv);
//					
//				
//
////				if (!delaysCreatorTypesMap.isEmpty()) {
////					dll.setOnClickListener(new OnClickListener() {
////						@Override
////						public void onClick(View v) {
////							DelaysDialogFragment delaysDialog = new DelaysDialogFragment();
////							Bundle args = new Bundle();
////							args.putSerializable(DelaysDialogFragment.ARG_DELAYS, (Serializable) delaysCreatorTypesMap);
////							delaysDialog.setArguments(args);
////							delaysDialog.show(getSherlockActivity().getSupportFragmentManager(), "delaysdialog");
////						}
////					});
////				}
//
//				trType.addView(dll);
//			}
//		}
//	}
//
//	@Override
//	public void addToTimetable(TableRow tr) {
//		tlMainContent.addView(tr);
//	}
//
//	@Override
//	public void onDayFinished(boolean result) {
//		toggleProgressDialog();
//		mElsvMainContent.setEnabled(true);
//		tlMainContent.setEnabled(true);
//	}
//
//	@Override
//	public void onRightOverScrolled() {
//		if (displayedDay < DAYS_WINDOWS) {
//			displayedDay++;
//			refreshTimes(displayedDay);
//		}
//	}
//
//	@Override
//	public void onLeftOverScrolled() {
//		if (displayedDay > 0 && !firstHasNoCourses) {
//			displayedDay--;
//			firstColumn = 0;
//			refreshTimes(displayedDay);
//		}
//	}

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
//		tvday.setText(dateFormat.format(actualDate));

		TextView lineDay = (TextView) getSherlockActivity().findViewById(R.id.lineDay);
		if (lineDay != null) {
			lineDay.setText(dateFormat.format(actualDate));
			lineDay.setTextColor(getSherlockActivity().getResources().getColor(R.color.transparent_white));
			lineDay.setBackgroundColor(params.getColor());
		}

	}

//	private void refreshDelays(int displayedDay) {
//		TableRow trDelays = (TableRow) getView().findViewById(R.id.delays_row);
//		trDelays.removeAllViews();
//
//		// Delays are available only for the current day.
//		if (displayedDay == 0) {
//			for (int i = 0; i < actualTimeTable.getDelays().get(displayedDay).size(); i++) {
//				LinearLayout dll = new LinearLayout(getSherlockActivity());
//				dll.setMinimumWidth(COL_WIDTH);
//				dll.setMinimumHeight(ROW_HEIGHT);
//				dll.setOrientation(LinearLayout.HORIZONTAL);
//				dll.setBackgroundColor(getSherlockActivity().getResources().getColor(R.color.sc_light_gray));
//				dll.setBackgroundResource(R.drawable.cell_late);
//				dll.setGravity(Gravity.CENTER);
//
//				Map<String, String> delaysStringsMap = delays[i];
//				final Map<CreatorType, String> delaysCreatorTypesMap = new HashMap<CreatorType, String>();
//
//				for (Entry<String, String> delay : delaysStringsMap.entrySet()) {
//					if (!delay.getValue().equalsIgnoreCase("0")) {
//						CreatorType ct = CreatorType.getAlertType(delay.getKey());
//
//						delaysCreatorTypesMap.put(ct, delay.getValue());
//
//						TextView tv = new TextView(getSherlockActivity());
//						tv.setLayoutParams(new TableLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
//								LayoutParams.WRAP_CONTENT, 1f));
//						tv.setBackgroundColor(getSherlockActivity().getResources().getColor(R.color.sc_light_gray));
//						tv.setBackgroundResource(R.drawable.cell_late);
//						tv.setGravity(Gravity.CENTER);
//						tv.setTextAppearance(getSherlockActivity(), android.R.style.TextAppearance_Small);
//
//						if (ct.equals(CreatorType.USER)) {
//							tv.setTextColor(getSherlockActivity().getResources().getColor(R.color.blue));
//							tv.setText(getSherlockActivity().getString(R.string.smart_check_tt_delay_user,
//									delay.getValue()));
//						} else {
//							tv.setTextColor(getSherlockActivity().getResources().getColor(R.color.red));
//							tv.setText(getSherlockActivity().getString(R.string.smart_check_tt_delay, delay.getValue()));
//						}
//
//						dll.addView(tv);
//					}
//				}
//
//				if (!delaysCreatorTypesMap.isEmpty()) {
//					dll.setOnClickListener(new OnClickListener() {
//						@Override
//						public void onClick(View v) {
//							DelaysDialogFragment delaysDialog = new DelaysDialogFragment();
//							Bundle args = new Bundle();
//							args.putSerializable(DelaysDialogFragment.ARG_DELAYS, (Serializable) delaysCreatorTypesMap);
//							delaysDialog.setArguments(args);
//							delaysDialog.show(getSherlockActivity().getSupportFragmentManager(), "delaysdialog");
//						}
//					});
//				}
//
//				trDelays.addView(dll);
//			}
//		}
//	}
//
//	@Override
//	public void onPause() {
//		if (renderTimeTableAsyncTask != null)
//			renderTimeTableAsyncTask.cancel(true);
//		super.onPause();
//	}
//
//	private void refreshTimes(int displayDay) {
//		toggleProgressDialog();
//		mElsvMainContent.setEnabled(false);
//		tlMainContent.setEnabled(false);
//		refreshDayTextView(displayedDay);
//		refreshDelays(displayedDay);
//		tlMainContent.removeAllViews();
//		if (renderTimeTableAsyncTask != null)
//			renderTimeTableAsyncTask.cancel(true);
//		renderTimeTableAsyncTask = new RenderTimeTableAsyncTask(this);
//		renderTimeTableAsyncTask.execute(displayedDay, actualTimeTable.getStops().size());
//	}
//
//	private class RenderTimeTableAsyncTask extends AsyncTask<Integer, TableRow, Boolean> {
//
//		private RenderListener mRenderListener;
//		private int mDayIndex;
//
//		public RenderTimeTableAsyncTask(RenderListener mRenderListener) {
//			super();
//			this.mRenderListener = mRenderListener;
//		}
//
//		@Override
//		protected Boolean doInBackground(Integer... params) {
//			try {
//				Integer dayIndex = params[0];
//				mDayIndex = dayIndex;
//				endColumn = firstColumn + actualTimeTable.getDelays().get(dayIndex).size() - 1;
//				for (int i = 0; i < params[1]; i++) {
//					TableRow tr = new TableRow(getSherlockActivity());
//					tr.setMinimumHeight(ROW_HEIGHT);
//					for (int j = firstColumn; j <= endColumn; j++) {
//						if (times[i][j] != null) {
//							TextView tv = new TextView(getSherlockActivity());
//							if (tv != null) {
//								tv.setMinimumHeight(ROW_HEIGHT);
//								if (times[i][j].length() > 0)
//									tv.setText(times[i][j].substring(0, 5));
//								else
//									tv.setText(times[i][j]);
//								tv.setMinWidth(COL_WIDTH);
//								tv.setTextAppearance(getSherlockActivity(), R.style.hour_tt_jp);
//								tv.setGravity(Gravity.CENTER);
//								tv.setBackgroundResource(R.drawable.cell_hour);
//								tr.addView(tv);
//							}
//						}
//					}
//					publishProgress(tr);
//				}
//				return true;
//			} catch (Exception e) {
//
//				return false;
//			}
//		}
//
//		@Override
//		protected void onProgressUpdate(TableRow... values) {
//			super.onProgressUpdate(values);
//			mRenderListener.addToTimetable(values[0]);
//		}
//
//		@Override
//		protected void onPostExecute(Boolean result) {
//			super.onPostExecute(result);
//
//			if (!result) {
//				Toast.makeText(getSherlockActivity(), getString(R.string.problem_loading), Toast.LENGTH_LONG).show();
//			}
//
//		}
//
//	}
}
