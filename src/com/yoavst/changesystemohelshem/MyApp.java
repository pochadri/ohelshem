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

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EApplication;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.res.StringArrayRes;
import org.androidannotations.annotations.sharedpreferences.Pref;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.yoavst.changesystemohelshem.views.CardExpandInside;
import com.yoavst.changesystemohelshem.views.CardHeaderRtl;

import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;

/**
 * The general application class that store all the data, and include some
 * general methods.
 * 
 * @author Yoav Sternberg
 * 
 */
@EApplication
public class MyApp extends Application {
	public interface UpdateChanges {
		void onUpdateChangesCompleted(
				ArrayList<ArrayList<ChangeObject>> changes, int layer);
	}

	@Pref
	protected Prefs_ mPrefs;
	private static ArrayList<ArrayList<ChangeObject>> sChangesForNine;
	private static ArrayList<ArrayList<ChangeObject>> sChangesForTen;
	private static ArrayList<ArrayList<ChangeObject>> sChangesForEleven;
	private static ArrayList<ArrayList<ChangeObject>> sChangesForTwelve;
	private static String[][][][] sTimetable;
	private static long[] sLastUpdated = new long[] { 0, 0, 0, 0 };
	private static String[] sLastTime = new String[] { "", "", "", "" };
	private UpdateChanges mListener;
	private boolean[] mIsLayerEmpty = new boolean[] { false, false, false,
			false };

	// Help Section:
	/**
	 * For make HelpFragment load faster, we load the cards on background on app
	 * started
	 */
	private static Card[] sCards = new Card[11];
	private static String[] sAnswers = new String[11];
	@StringArrayRes(R.array.questions)
	protected static String[] sQuestions;
	private static int[] sAnswerIds = new int[] { R.raw.answer1, R.raw.answer2,
			R.raw.answer3, R.raw.answer4, R.raw.answer5, R.raw.answer6,
			R.raw.answer7, R.raw.answer8, R.raw.answer9, R.raw.answer10,
			R.raw.answer11 };

	public void onCreate() {
		super.onCreate();
		init();
	}

	@Background
	void init() {
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
			Calendar cal = Holidays.getDayOnly(Calendar.getInstance());
			long miliOfDay = cal.getTimeInMillis();
			cal.add(Calendar.DAY_OF_MONTH, 1);
			long tomorrowMili = cal.getTimeInMillis();
			// if the hour is later then 21, then the changes may be changed.
			// Therefore, it won't show you changes between 21:00 to 24:00.
			if (hours >= 21) {
				cleanCache();
			} else {
				// Check if the changes for all the layers is saved, and if they
				// are - restore them if there are up to date.
				if (mPrefs.getJsonOfLayerNine().exists()
						&& mPrefs.getJsonOfLayerNine().getOr("").length() > 1) {
					long LayerNineUpdatedFrom = mPrefs.LayerNineUpdatedFrom()
							.get();
					if (LayerNineUpdatedFrom == miliOfDay
							|| LayerNineUpdatedFrom == tomorrowMili) {
						sChangesForNine = gson.fromJson(mPrefs
								.getJsonOfLayerNine().get(), listType);
						sLastUpdated[0] = mPrefs.LayerNineUpdatedWhen().get();
						sLastTime[0] = getDateFormated(mPrefs
								.LayerNineUpdatedFrom().get());
					} else {
						mPrefs.getJsonOfLayerNine().put("");
						mPrefs.LayerNineUpdatedWhen().put(0);
						mPrefs.LayerNineUpdatedFrom().put(0);
					}
				}
				if (mPrefs.getJsonOfLayerTen().exists()
						&& mPrefs.getJsonOfLayerTen().getOr("").length() > 1) {
					long LayerTenUpdatedFrom = mPrefs.LayerNineUpdatedFrom()
							.get();
					if (LayerTenUpdatedFrom == miliOfDay
							|| LayerTenUpdatedFrom == tomorrowMili) {
						sChangesForTen = gson.fromJson(mPrefs
								.getJsonOfLayerTen().get(), listType);
						sLastUpdated[1] = mPrefs.LayerTenUpdatedWhen().get();
						sLastTime[1] = getDateFormated(mPrefs
								.LayerTenUpdatedFrom().get());
					} else {
						mPrefs.getJsonOfLayerTen().put("");
						mPrefs.LayerTenUpdatedWhen().put(0);
						mPrefs.LayerTenUpdatedFrom().put(0);
					}
				}
				if (mPrefs.getJsonOfLayerEleven().exists()
						&& mPrefs.getJsonOfLayerEleven().getOr("").length() > 1) {
					long LayerElevenUpdatedFrom = mPrefs.LayerNineUpdatedFrom()
							.get();
					if (LayerElevenUpdatedFrom == miliOfDay
							|| LayerElevenUpdatedFrom == tomorrowMili) {
						sChangesForEleven = gson.fromJson(mPrefs
								.getJsonOfLayerEleven().get(), listType);
						sLastUpdated[2] = mPrefs.LayerElevenUpdatedWhen().get();
						sLastTime[2] = getDateFormated(mPrefs
								.LayerElevenUpdatedFrom().get());
					} else {
						mPrefs.getJsonOfLayerEleven().put("");
						mPrefs.LayerElevenUpdatedWhen().put(0);
						mPrefs.LayerElevenUpdatedFrom().put(0);
					}
				}
				if (mPrefs.getJsonOfLayerTwelve().exists()
						&& mPrefs.getJsonOfLayerTwelve().getOr("").length() > 1) {
					long LayerTwelveUpdatedFrom = mPrefs.LayerNineUpdatedFrom()
							.get();
					if (LayerTwelveUpdatedFrom == miliOfDay
							|| LayerTwelveUpdatedFrom == tomorrowMili) {
						sChangesForNine = gson.fromJson(mPrefs
								.getJsonOfLayerTwelve().get(), listType);
						sLastUpdated[3] = mPrefs.LayerTwelveUpdatedWhen().get();
						sLastTime[3] = getDateFormated(mPrefs
								.LayerTwelveUpdatedFrom().get());
					} else {
						mPrefs.getJsonOfLayerTwelve().put("");
						mPrefs.LayerTwelveUpdatedWhen().put(0);
						mPrefs.LayerTwelveUpdatedFrom().put(0);
					}
				}
				if (getChangesForLayer(mPrefs.getLayer().get()) != null
						&& mListener != null)
					mListener.onUpdateChangesCompleted(
							getChangesForLayer(mPrefs.getLayer().get()), mPrefs
									.getLayer().get());
			}
		}
		for (int i = 0; i < sAnswerIds.length; i++) {
			sAnswers[i] = readRaw(this, sAnswerIds[i]);
		}
		initCards();
	}

	@Background
	void initCards() {
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
	 * @return the day of the week by miliseconds from 1/1/1970
	 */
	public static int getDayOfWeekByMili(long miliseconds) {
		GregorianCalendar cal = new GregorianCalendar();
		cal.setTime(new Date(miliseconds));
		return cal.get(Calendar.DAY_OF_WEEK);
	}

	public static int getDayOfWeek() {
		Calendar calendar = Calendar.getInstance();
		return calendar.get(Calendar.DAY_OF_WEEK);
	}

	/**
	 * @return the text of the given raw resource
	 */
	public static String readRaw(Context ctx, int res_id) {
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
	 * Clear all the cached data from preferences
	 */
	public void cleanCache() {
		mPrefs.getJsonOfLayerNine().put("");
		mPrefs.LayerNineUpdatedWhen().put(0);
		mPrefs.LayerNineUpdatedFrom().put(0);
		mPrefs.getJsonOfLayerTen().put("");
		mPrefs.LayerTenUpdatedWhen().put(0);
		mPrefs.LayerTenUpdatedFrom().put(0);
		mPrefs.getJsonOfLayerEleven().put("");
		mPrefs.LayerElevenUpdatedWhen().put(0);
		mPrefs.LayerElevenUpdatedFrom().put(0);
		mPrefs.getJsonOfLayerTwelve().put("");
		mPrefs.LayerTwelveUpdatedWhen().put(0);
		mPrefs.LayerTwelveUpdatedFrom().put(0);

	}

	/**
	 * @return the ArrayList of changes for the given layer
	 */
	public ArrayList<ArrayList<ChangeObject>> getChangesForLayer(int layer) {
		switch (layer) {
		case 9:
			return sChangesForNine;
		case 10:
			return sChangesForTen;
		case 11:
			return sChangesForEleven;
		case 12:
			return sChangesForTwelve;
		default:
			return null;
		}
	}

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
			return getDayOfWeekByMili(miliseconds);
		}
		return 0;
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
	public boolean isChangesEmpty(ArrayList<ChangeObject> changes) {
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

	@UiThread
	public void setChangesForLayer(ArrayList<ArrayList<ChangeObject>> changes,
			int layer, String date, long day) {
		long time = new GregorianCalendar().getTimeInMillis();
		sLastUpdated[layer - 9] = time;
		String json = new Gson().toJson(changes);
		switch (layer) {
		case 9:
			sChangesForNine = changes;
			mPrefs.getJsonOfLayerNine().put(json);
			mPrefs.LayerNineUpdatedWhen().put(time);
			mPrefs.LayerNineUpdatedFrom().put(day);
			break;
		case 10:
			sChangesForTen = changes;
			mPrefs.getJsonOfLayerTen().put(json);
			mPrefs.LayerTenUpdatedWhen().put(time);
			mPrefs.LayerTenUpdatedFrom().put(day);
			break;
		case 11:
			sChangesForEleven = changes;
			mPrefs.getJsonOfLayerEleven().put(json);
			mPrefs.LayerElevenUpdatedWhen().put(time);
			mPrefs.LayerElevenUpdatedFrom().put(day);
			break;
		case 12:
			sChangesForTwelve = changes;
			mPrefs.getJsonOfLayerTwelve().put(json);
			mPrefs.LayerTwelveUpdatedWhen().put(time);
			mPrefs.LayerTwelveUpdatedFrom().put(day);
			break;
		}
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

	@UiThread
	public void updateChanges(int layer) {
		// Create IntentService for downloading changes
		BackgroundService_.IntentBuilder_ intentBuilder = BackgroundService_
				.intent(this);
		// Put the layer and set it to download mode
		intentBuilder.get().putExtra(BackgroundService_.LAYER_EXTRA, layer)
				.putExtra(BackgroundService.DOWNLOAD_EXTRA, true);
		// Start service
		intentBuilder.start();
	}
}
