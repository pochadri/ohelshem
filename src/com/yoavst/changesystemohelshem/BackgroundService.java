package com.yoavst.changesystemohelshem;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

import org.androidannotations.annotations.App;
import org.androidannotations.annotations.EService;
import org.androidannotations.annotations.SystemService;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.yoavst.changesystemohelshem.activities.MainActivity_;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Color;
import android.support.v4.app.NotificationCompat;

/**
 * <p>
 * The class download changes from school site.<br>
 * If the class called by AlarmReceiver then it will also show a notification.
 * </p>
 * 
 * @author Yoav Sternberg
 */
@EService
public class BackgroundService extends IntentService {
	@App
	MyApp mApp;
	@SystemService
	NotificationManager mNotificationManager;
	/**
	 * The id of the notification.
	 */
	int mNotificationId = 904;
	/**
	 * The layer for download
	 */
	int mLayer;
	/**
	 * The class of the user if it is notification
	 */
	int mClass;
	/**
	 * False if it specific for one class (Notificiation), True if it for
	 * downloading layer
	 */
	boolean mDownloadService;
	/**
	 * The service name
	 */
	static final String SERVICE_NAME = "changesystem_service";
	/**
	 * The name of the extra that the service will be check for the layer
	 * 
	 * @see #mLayer
	 */
	static final String LAYER_EXTRA = "layer";
	/**
	 * The name of the extra that the service will be check for the class
	 * 
	 * @see #mClass
	 */
	static final String CLASS_EXTRA = "class";
	/**
	 * The name of the extra that the service will be check for the
	 * downloadService
	 * 
	 * @see #mDownloadService
	 */
	static final String DOWNLOAD_EXTRA = "download";

	/**
	 * Default constructor
	 */
	public BackgroundService() {
		super(SERVICE_NAME);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void onHandleIntent(Intent intent) {
		// Get extras
		mLayer = intent.getIntExtra("layer", 9);
		mClass = intent.getIntExtra("class", 4);
		mDownloadService = intent.getBooleanExtra("download", true);
		// If the layer is invalid
		if (mLayer < 9 || mLayer > 12) {
			// Throw error
			throw new IllegalArgumentException("no layer selected");
			// If no connection
		} else if (!mApp.isNetworkAvailable()) {
			// If should show notification
			if (!mDownloadService)
				// Show notification of no connection
				showNotification(mApp.getResources().getString(
						R.string.no_connection));
			return;

		} else {
			try {
				ArrayList<ArrayList<ChangeObject>> changes = new ArrayList<ArrayList<ChangeObject>>();
				// Download the html of the changes
				Document doc = Jsoup.connect(
						"http://ohel-shem.com/php/changes/changes_sys/index.php?layer="
								+ mLayer).get();
				// Select the changes table
				Element table = doc.select("table[bgcolor=white]").first();
				// Select the date table
				Element dateData = doc.select(
						"#changesTable tbody tr td b font").first();
				// Remove the fixed text
				String baseDate = dateData.text().replace(
						"לוח השינויים שמוצג הינו רלוונטי ליום", "");
				// Remove days of the week
				baseDate = baseDate.replace("ראשון", "").replace("שני", "")
						.replace("שלישי", "").replace("רביעי", "")
						.replace("חמישי", "").replace("שישי", "")
						.replace("שבת", "");
				// Remove the text come before the data and replace - with /
				baseDate = baseDate.replace("ה-", "").replace('-', '/');
				// Convert the date to miliseconds
				SimpleDateFormat simpleDateFormat = new SimpleDateFormat(
						"dd/MM/yyyy", Locale.ROOT);
				long miliseconds = 0;
				try {
					miliseconds = simpleDateFormat.parse(baseDate).getTime();
				} catch (ParseException e) {
					e.printStackTrace();
				}
				// if there are no changes (no table)
				if (table == null) {
					// show results of no results
					publishResults(null, baseDate, miliseconds);
					return;
				}
				// Skip first row and column and insert the data into the
				// ArrayList
				boolean skippedRowOne = false;
				int hour = 1;
				boolean firstTime = true;
				for (Element row : table.select("tr")) {
					if (!skippedRowOne) {
						skippedRowOne = true;
						continue;
					}
					boolean skippedColumnOne = false;
					int classNum = 1;
					for (Element cell : row.select("td")) {
						if (!skippedColumnOne) {
							skippedColumnOne = true;
							continue;
						}
						if (firstTime)
							changes.add(new ArrayList<ChangeObject>());
						if (classNum <= mApp.getResources().getIntArray(
								R.array.layers_classes)[mLayer - 9]) {
							int day = MyApp.getDayOfWeekByMili(miliseconds) - 1;
							String lessonName = mApp.getTimetable()[mLayer - 9][classNum - 1][day][hour - 1];
							changes.get(classNum - 1).add(
									new ChangeObject(hour, cell.text(),
											lessonName, Color.parseColor("#"
													+ cell.attr("bgcolor"))));
						}
						classNum++;
					}
					firstTime = false;
					hour++;
				}
				// Publish the results
				publishResults(changes, baseDate, miliseconds);
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
	}

	/**
	 * Publish the results from {@link #onHandleIntent(Intent)}
	 * 
	 * @param changes
	 *            the changes of the {@link #mLayer layer}.
	 * @param dateToShow
	 *            the date to show in the Activity.
	 * @param day
	 *            the day of the changes in miliseconds.
	 */
	private void publishResults(ArrayList<ArrayList<ChangeObject>> changes,
			String dateToShow, long day) {
		if (mDownloadService) {
			// if should show notification and changes are null
		} else if (changes == null)
			showNotification(mApp.getResources().getString(R.string.no_changes));
		// if should show notification and changes are empty
		else if (changes.get(mClass) == null
				|| mApp.isChangesEmpty(changes.get(mClass)))
			showNotification(mApp.getResources().getString(R.string.no_changes));
		// if should show notification and there are changes
		else {
			showNotification(mApp.getResources().getString(
					R.string.notification_text));
		}
		// save the changes
		mApp.setChangesForLayer(changes, mLayer, dateToShow, day);

	}

	/**
	 * Show notification
	 * 
	 * @param text
	 *            The text that will be shown on the notification
	 */
	private void showNotification(String text) {
		// Cretae intent to MainActivity to be called on notification clicked
		Intent intent = new Intent(this, MainActivity_.class);
		PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, 0);
		// Create notification
		Notification mNotification = new NotificationCompat.Builder(this)
				.setSmallIcon(R.drawable.nav_one)
				.setContentTitle(
						mApp.getResources().getString(
								R.string.notification_title))
				.setContentText(text).setAutoCancel(true)
				.setContentIntent(pIntent).build();
		// Show notification
		mNotificationManager.notify(mNotificationId, mNotification);
	}
}
