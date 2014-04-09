package com.yoavst.changesystemohelshem;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardHeader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import javax.annotation.Nullable;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EApplication;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.res.StringArrayRes;
import org.androidannotations.annotations.sharedpreferences.Pref;

import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.yoavst.changesystemohelshem.views.CardExpandInside;
import com.yoavst.changesystemohelshem.views.CardHeaderRtl;

/**
 * The general application class that store all the data, and include some
 * general methods.
 * 
 * @author Yoav Sternberg
 * 
 */
@EApplication
public class MyApp extends Application {
	@Pref
	protected Prefs_ mPrefs;
	/**
	 * The hours of the start and the end of the lessons. * To get the Start
	 * time of lesson X: [X*2-2] * To get the End time of lesson X: [X*2-1]
	 */
	@StringArrayRes(R.array.lessons)
	protected static String[] mLessonsHours;

	public enum LessonTime {
		Start, End
	}

	/**
	 * The changes of the whole school. <br>
	 * Level 1: Layer -> 0-3 <br>
	 * Level 2: Class -> 0-10 or 0-11 <br>
	 * Level 3: Hour -> 0-9 <br>
	 * Each change is being represented as {@link ChangeObject}
	 */
	private static ArrayList<ArrayList<ArrayList<ChangeObject>>> sChanges = new ArrayList<ArrayList<ArrayList<ChangeObject>>>(
			4);
	/**
	 * The timetable of the whole school, loaded from json. <br>
	 * Level 1: Layer -> 0-3 <br>
	 * Level 2: Class -> 0-10 or 0-11 <br>
	 * Level 3: Day -> 0-6 <br>
	 * Level 4: Hour -> 0-9
	 * 
	 * @see DownloadTimetable
	 */
	private static String[][][][] sTimetable;
	/**
	 * The time in miliseconds of when the changes was taken.
	 */
	private static long[] sLastUpdated = new long[] { 0, 0, 0, 0 };
	/**
	 * The time of the changes, formatted as "31/12/2014"
	 */
	private static String[] sLastTime = new String[] { "", "", "", "" };
	/**
	 * Listener for events belongs to update changes.
	 * 
	 * @see UpdateChanges
	 */
	private static UpdateChanges mListener;
	/**
	 * Info about every layer - are the changes empty For making the
	 * application. <br>
	 * more effective, since it would not require the application to check each
	 * class every time.
	 */
	private boolean[] mIsLayerEmpty = new boolean[] { false, false, false,
			false };

	// Help Section:
	/**
	 * For make HelpFragment load faster, we load the cards on background on app
	 * started
	 */
	private static Card[] sCards = new Card[11];
	/**
	 * The answers of the cards. Will be {@link #sAnswerIds} but html formatted.
	 */
	private static String[] sAnswers = new String[11];
	/**
	 * The questions of the cards.
	 */
	@StringArrayRes(R.array.questions)
	protected static String[] sQuestions;
	/**
	 * The answer ids of the cards.
	 */
	private static int[] sAnswerIds = new int[] { R.raw.answer1, R.raw.answer2,
			R.raw.answer3, R.raw.answer4, R.raw.answer5, R.raw.answer6,
			R.raw.answer7, R.raw.answer8, R.raw.answer9, R.raw.answer10,
			R.raw.answer11 };

	// Android methods:

	@Override
	public void onCreate() {
		super.onCreate();
		// Put 4 items so it will not throw ArrayIndexOutOfBoundsException
		for (int i = 1; i <= 4; i++)
			sChanges.add(null);
		init();
	}

	// Private methods:
	/**
	 * Init the application.
	 * <ul>
	 * <li>Convert the json to {@link #sTimetable timetable} array.
	 * <li>Update changes.
	 * <li>Check the cached data.
	 * <li>Load the question's {@link #sCards cards}.
	 * </ul>
	 * 
	 * @see #cleanCache()
	 * @see #updateChanges(layer)
	 */
	@SuppressWarnings("unchecked")
	@Background
	protected void init() {
		// Get the timetable from file
		String json = MyApp.readRaw(this, R.raw.timetable);
		// Convert it from JSON to String array
		sTimetable = new Gson().fromJson(json, String[][][][].class);
		if (isNetworkAvailable())
			// First get for user's layer
			updateChanges(mPrefs.getLayer().get());
		else {
			// if there is no internet connection, look for the cached changes
			Calendar calendar = Calendar.getInstance();
			int hours = calendar.get(Calendar.HOUR_OF_DAY);
			Gson gson = new Gson();
			Type listType = new TypeToken<ArrayList<ArrayList<ChangeObject>>>() {
			}.getType();
			// if the hour is later then 21, then the changes may be changed.
			// Therefore, it won't show you the cached changes between 21:00 to
			// 24:00.
			if (hours >= 21) {
				cleanCache();
			} else {
				// Check if the changes for all the layers is saved, and if they
				// are - restore them if there are up to date.
				for (int layer = 9; layer <= 12; layer++) {
					if (isCacheExsistsForLayer(layer)) {
						if (isChangesUpdatedForLayer(layer)) {
							setChangesForLayer(layer,
									(ArrayList<ArrayList<ChangeObject>>) gson
											.fromJson(
													mPrefs.getJsonOfLayerNine()
															.get(), listType));
							sLastUpdated[layer - 9] = getLayerLastUpdatedWhen(layer);
							sLastTime[layer - 9] = getDateFormated(getLayerLastUpdatedFrom(layer));
						} else {
							cleanCacheForLayer(layer);
						}
					}
				}
				// If the changes are not null and there is a listener attached
				if (getChangesForLayer(mPrefs.getLayer().get()) != null
						&& mListener != null)
					// Nofity the listneer about the changes
					mListener.onUpdateChangesCompleted(
							getChangesForLayer(mPrefs.getLayer().get()), mPrefs
									.getLayer().get());
			}
		}
		// Load the answers
		for (int i = 0; i < sAnswerIds.length; i++) {
			sAnswers[i] = readRaw(this, sAnswerIds[i]);
		}
		// Init the cards
		initCards();
	}

	/**
	 * Create card from each Q&A that can be expanded inside.
	 */
	@Background
	protected void initCards() {
		for (int i = 0; i < sAnswerIds.length; i++) {
			// Create Card
			sCards[i] = new Card(this);
			// Create header
			CardHeader header = new CardHeaderRtl(this);
			// Set title for the header
			header.setTitle(sQuestions[i]);
			// Add the header to the card
			sCards[i].addCardHeader(header);
			// Create inside CardExpand
			CardExpandInside expand = new CardExpandInside(this, i, sAnswers);
			// Add it to the card
			sCards[i].addCardExpand(expand);
		}
	}

	/**
	 * Set the changes for the specific layer.
	 * 
	 * @param layer
	 *            the layer
	 * @param changes
	 *            the changes
	 */
	private void setChangesForLayer(int layer,
			@Nullable ArrayList<ArrayList<ChangeObject>> changes) {
		sChanges.set(layer - 9, changes);
	}

	/**
	 * @param layer
	 *            the layer
	 * @return true if the changes of layer are stored in preferences
	 */
	private boolean isCacheExsistsForLayer(int layer) {
		switch (layer) {
		case 9:
			return (mPrefs.getJsonOfLayerNine().exists() && mPrefs
					.getJsonOfLayerNine().getOr("").length() > 1);
		case 10:
			return (mPrefs.getJsonOfLayerTen().exists() && mPrefs
					.getJsonOfLayerTen().getOr("").length() > 1);
		case 11:
			return (mPrefs.getJsonOfLayerEleven().exists() && mPrefs
					.getJsonOfLayerEleven().getOr("").length() > 1);
		case 12:
			return (mPrefs.getJsonOfLayerTwelve().exists() && mPrefs
					.getJsonOfLayerTwelve().getOr("").length() > 1);
		default:
			return false;
		}
	}

	private Long getLayerLastUpdatedWhen(int layer) {
		switch (layer) {
		case 9:
			return mPrefs.LayerNineUpdatedWhen().getOr(-2);
		case 10:
			return mPrefs.LayerTenUpdatedWhen().getOr(-2);
		case 11:
			return mPrefs.LayerElevenUpdatedWhen().getOr(-2);
		case 12:
			return mPrefs.LayerTwelveUpdatedWhen().getOr(-2);
		default:
			return -1l;
		}
	}

	private Long getLayerLastUpdatedFrom(int layer) {
		switch (layer) {
		case 9:
			return mPrefs.LayerNineUpdatedFrom().getOr(-2);
		case 10:
			return mPrefs.LayerTenUpdatedFrom().getOr(-2);
		case 11:
			return mPrefs.LayerElevenUpdatedFrom().getOr(-2);
		case 12:
			return mPrefs.LayerTwelveUpdatedFrom().getOr(-2);
		default:
			return -1l;
		}
	}

	/**
	 * Clear all the cached data from preferences
	 */
	private void cleanCache() {
		for (int i = 9; i <= 12; i++)
			cleanCacheForLayer(9);
	}

	/**
	 * Clean cache for specific layer
	 * 
	 * @param layer
	 *            the layer
	 */
	private void cleanCacheForLayer(int layer) {
		putLayerLastUpdatedData(layer, "", 0, 0);
	}

	/**
	 * Update layer's cached data. If json is null then it will not change it.
	 * 
	 * @param layer
	 *            the layer
	 * @param json
	 *            the changes in json format
	 * @param when
	 *            the specific time it taken in miliseconds
	 * @param from
	 *            the date in miliseconds
	 */
	private void putLayerLastUpdatedData(int layer, @Nullable String json,
			long when, long from) {
		switch (layer) {
		case 9:
			if (json != null)
				mPrefs.getJsonOfLayerNine().put(json);
			mPrefs.LayerNineUpdatedWhen().put(when);
			mPrefs.LayerNineUpdatedFrom().put(from);
			break;
		case 10:
			if (json != null)
				mPrefs.getJsonOfLayerTen().put(json);
			mPrefs.LayerTenUpdatedWhen().put(when);
			mPrefs.LayerTenUpdatedFrom().put(from);
			break;
		case 11:
			if (json != null)
				mPrefs.getJsonOfLayerEleven().put(json);
			mPrefs.LayerElevenUpdatedWhen().put(when);
			mPrefs.LayerElevenUpdatedFrom().put(from);
			break;
		case 12:
			if (json != null)
				mPrefs.getJsonOfLayerTwelve().put(json);
			mPrefs.LayerTwelveUpdatedWhen().put(when);
			mPrefs.LayerTwelveUpdatedFrom().put(from);
			break;
		}
	}

	// Public methods:
	/**
	 * @return the start or end time of the lesson
	 */
	public String getLessonTime(int lessonNum, LessonTime time) {
		return mLessonsHours[(time == LessonTime.Start ? lessonNum * 2 - 2
				: lessonNum * 2 - 1)];
	}

	/**
	 * @return the ArrayList of changes for the given layer
	 */
	public ArrayList<ArrayList<ChangeObject>> getChangesForLayer(int layer) {
		return sChanges.get(layer - 9);
	}

	/**
	 * @param layer
	 * @return true if the changes are the changes of today or tomorrow
	 */
	public boolean isChangesUpdatedForLayer(int layer) {
		Calendar cal = Holidays.getDayOnly(Calendar.getInstance());
		long miliOfDay = cal.getTimeInMillis();
		cal.add(Calendar.DAY_OF_MONTH, 1);
		long tomorrowMili = cal.getTimeInMillis();
		switch (layer) {
		case 9:
			long LayerNineUpdatedFrom = mPrefs.LayerNineUpdatedFrom().get();
			if (LayerNineUpdatedFrom == miliOfDay
					|| LayerNineUpdatedFrom == tomorrowMili) {
				return true;
			}
			break;
		case 10:
			long LayerTenUpdatedFrom = mPrefs.LayerNineUpdatedFrom().get();
			if (LayerTenUpdatedFrom == miliOfDay
					|| LayerTenUpdatedFrom == tomorrowMili) {
				return true;
			}
			break;
		case 11:
			long LayerElevenUpdatedFrom = mPrefs.LayerNineUpdatedFrom().get();
			if (LayerElevenUpdatedFrom == miliOfDay
					|| LayerElevenUpdatedFrom == tomorrowMili) {
				return true;
			}
			break;
		case 12:
			long LayerTwelveUpdatedFrom = mPrefs.LayerNineUpdatedFrom().get();
			if (LayerTwelveUpdatedFrom == miliOfDay
					|| LayerTwelveUpdatedFrom == tomorrowMili) {
				return true;
			}
			break;
		default:
			return false;
		}
		return false;
	}

	/**
	 * @return the day of the week by miliseconds from 1/1/1970.
	 */
	public static int getDayOfWeekByMili(long miliseconds) {
		GregorianCalendar cal = new GregorianCalendar();
		cal.setTime(new Date(miliseconds));
		int day = cal.get(Calendar.DAY_OF_WEEK);
		return day;
	}

	/**
	 * @return the day of the week
	 */
	public static int getDayOfWeek() {
		Calendar calendar = Calendar.getInstance();
		return calendar.get(Calendar.DAY_OF_WEEK);
	}

	/**
	 * @return the text of the given raw resource
	 */
	public static String readRaw(Context ctx, int res_id) {
		if (ctx == null)
			return "error";
		String alltext = "";
		InputStream is = ctx.getResources().openRawResource(res_id);
		InputStreamReader isr = new InputStreamReader(is);
		BufferedReader br = new BufferedReader(isr, 8192);
		try {
			String test;
			while (true) {
				test = br.readLine();
				// readLine() returns null if no more lines in the file
				if (test == null)
					break;
				else
					alltext += test;
			}
			isr.close();
			is.close();
			br.close();
			return alltext;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "error";
	}

	/**
	 * Nofity the listener that there is an error.
	 */
	public void showError() {
		if (mListener != null)
			mListener.showConnectionError();
	}

	/**
	 * @param miliseconds
	 *            miliseconds from 1/1/1970
	 * @return The data formatted as dd/MM/yyyy
	 */
	public String getDateFormated(long miliseconds) {
		Date date = new Date(miliseconds);
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy",
				Locale.ROOT);
		String formattedDate = simpleDateFormat.format(date);
		return formattedDate;
	}

	public int getDayOfWeekFromLayerLastUpdate(int layer) {
		return getDayOfWeekByMili(sLastUpdated[layer - 9]);
	}

	public int getDayOfWeekFromLayerLastUpdateChangeTime(int layer) {
		int toReturn = 0;
		if (layer >= 9 && layer <= 13) {
			String date = sLastTime[layer - 9];
			if (date == null || date.equals(""))
				return 0;
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat(
					"dd/MM/yyyy", Locale.ROOT);
			long miliseconds = 0;
			try {
				miliseconds = simpleDateFormat.parse(date).getTime();
			} catch (ParseException e) {
				return 0;
			}
			toReturn = getDayOfWeekByMili(miliseconds);
		}
		return toReturn;
	}

	public String getLastUpdateDay(int layer) {
		if (sLastUpdated[layer - 9] == 0)
			return getResources().getString(R.string.never_updated);
		SimpleDateFormat formatter = new SimpleDateFormat("dd/MM HH:mm",
				Locale.ROOT);
		String dateString = formatter.format(new Date(sLastUpdated[layer - 9]));
		return dateString;
	}

	/**
	 * @return the preferences
	 */
	public Prefs_ getPreferences() {
		return mPrefs;
	}

	public String getsLastTime(int layer) {
		return sLastTime[layer - 9];
	}

	/**
	 * @return the timetable
	 */
	public String[][][][] getTimetable() {
		return sTimetable;
	}

	/**
	 * @return true if all the items are "-" (all empty)
	 */
	public boolean isChangesEmpty(@Nullable ArrayList<ChangeObject> changes) {
		if (changes == null)
			return true;
		for (ChangeObject change : changes) {
			if (!change.getChangeText().equals("-"))
				return false;
		}
		return true;
	}

	/**
	 * return true if the given layer is empty
	 */
	public boolean isLayerEmpty(int layer) {
		return mIsLayerEmpty[layer - 9];
	}

	/**
	 * @return true if network avaliable using ConnectivityManager
	 */
	public boolean isNetworkAvailable() {
		ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		return connectivityManager.getActiveNetworkInfo() != null
				&& connectivityManager.getActiveNetworkInfo().isConnected();
	}

	public static Card[] getHelpCards() {
		return sCards;
	}

	/**
	 * @param timetable
	 *            the timetable
	 * @return true if the timetable is empty
	 */
	public static boolean isTimetableEmpty(String[] timetable) {
		for (String lesson : timetable) {
			if (lesson != null && !lesson.equals(" ") && !lesson.equals("")
					&& !lesson.equals("null"))
				return false;
		}
		return true;
	}

	@UiThread
	public void setChangesForLayer(
			@Nullable ArrayList<ArrayList<ChangeObject>> changes, int layer,
			String date, long day) {
		long time = new GregorianCalendar().getTimeInMillis();
		sLastUpdated[layer - 9] = time;
		String json = new Gson().toJson(changes);
		setChangesForLayer(layer, changes);
		putLayerLastUpdatedData(layer, json, time, day);
		sLastTime[layer - 9] = date;
		if (changes == null)
			mIsLayerEmpty[layer - 9] = true;
		if (layer >= 9 && layer <= 13 && mListener != null)
			mListener.onUpdateChangesCompleted(changes, layer);
	}

	/**
	 * Set listener for the application
	 */
	public void setListener(UpdateChanges listener) {
		mListener = listener;
	}

	/**
	 * Update the changes of specific layer
	 * 
	 * @param layer
	 *            the layer
	 */
	@UiThread
	public void updateChanges(int layer) {
		// Create IntentService for downloading changes
		BackgroundService_.IntentBuilder_ intentBuilder = BackgroundService_
				.intent(this);
		// Put the layer and set it to download mode
		intentBuilder.get().putExtra(BackgroundService.LAYER_EXTRA, layer)
				.putExtra(BackgroundService.DOWNLOAD_EXTRA, true);
		// Start service
		intentBuilder.start();
	}

	/**
	 * The listener of update changes.
	 */
	public interface UpdateChanges {
		/**
		 * Will be called when downloading changes will be finished.
		 * 
		 * @param changes
		 *            the changes or null if there are no changes.
		 * @param layer
		 *            the layer of the changes
		 */
		void onUpdateChangesCompleted(
				@Nullable ArrayList<ArrayList<ChangeObject>> changes, int layer);

		/**
		 * Will be called when there is error.
		 */
		void showConnectionError();
	}
}
