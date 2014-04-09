package com.yoavst.changesystemohelshem.activities;

import java.util.Calendar;
import java.util.GregorianCalendar;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.App;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.StringArrayRes;
import org.androidannotations.annotations.res.StringRes;
import org.androidannotations.annotations.sharedpreferences.Pref;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.yoavst.changesystemohelshem.MyApp;
import com.yoavst.changesystemohelshem.Prefs_;
import com.yoavst.changesystemohelshem.R;
import com.yoavst.changesystemohelshem.views.BetterSegmentedGroup;

/**
 * The wizard Activity. Here the user setup his layer and class
 * 
 * @author Yoav Sternberg
 */
@EActivity(R.layout.activity_wizard_v2)
public class WizardActivity extends SherlockFragmentActivity {
	@ViewById(R.id.segmentedlayer)
	BetterSegmentedGroup mChooseLayer;
	@ViewById(R.id.segmentedclass)
	BetterSegmentedGroup mChooseClass;
	@ViewById(R.id.segmentedalert)
	BetterSegmentedGroup mChooseAlert;
	@ViewById(R.id.hoscrollview)
	HorizontalScrollView mHorizontalScrollView;
	@ViewById(R.id.finish_button)
	Button mFinishButton;
	@ViewById(R.id.cancel_button)
	Button mCancelButton;
	@StringRes(R.string.layer_nine)
	String mLayerNine;
	@StringRes(R.string.layer_ten)
	String mLayerTen;
	@StringRes(R.string.layer_eleven)
	String mLayerEleven;
	@StringRes(R.string.layer_twelve)
	String mLayerTwelve;
	@StringArrayRes(R.array.layers)
	String[] mClasses;
	@Pref
	Prefs_ mPrefs;
	@App
	MyApp mApp;
	@Extra("settings")
	boolean mIsSettings;
	int[] mSelectClassIds = new int[] { R.id.radioclass1, R.id.radioclass2,
			R.id.radioclass3, R.id.radioclass4, R.id.radioclass5,
			R.id.radioclass6, R.id.radioclass7, R.id.radioclass8,
			R.id.radioclass9, R.id.radioclass10, R.id.radioclass11,
			R.id.radioclass12 };

	@AfterViews
	void init() {
		if (!mIsSettings)
			mCancelButton.setVisibility(View.GONE);
		mChooseLayer.check(R.id.radiolayer9);
		mChooseClass.check(R.id.radioclass4);
		mChooseAlert.check(R.id.radioalertyes);
		for (int id : mSelectClassIds) {
			RadioButton button = (RadioButton) mChooseClass.findViewById(id);
			int classNum = Integer.parseInt((String) button.getTag());
			button.setText(mLayerNine + classNum);
		}
		mHorizontalScrollView.post(new Runnable() {
			@Override
			public void run() {
				mHorizontalScrollView.scrollTo(
						mHorizontalScrollView.getRight() + 1000,
						mHorizontalScrollView.getTop());
			}
		});
		mChooseLayer.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				RadioButton button = (RadioButton) group
						.findViewById(checkedId);
				int layer = Integer.parseInt((String) button.getTag());
				RadioButton buttonTwelve = (RadioButton) mChooseClass
						.findViewById(mSelectClassIds[mSelectClassIds.length - 1]);
				if (layer == 9) {
					buttonTwelve.setVisibility(View.VISIBLE);
					mHorizontalScrollView.post(new Runnable() {
						@Override
						public void run() {
							mHorizontalScrollView.scrollTo(
									mHorizontalScrollView.getRight() + 1000,
									mHorizontalScrollView.getTop());
						}
					});
				} else {
					buttonTwelve.setVisibility(View.GONE);
					buttonTwelve.setChecked(false);

				}
				inflateClasses(layer);
				mChooseClass.updateBackground();
			}
		});
	}

	@Click(R.id.cancel_button)
	void cancelOnClick() {
		startActivity(new Intent(this, MainActivity_.class));
		finish();
	}

	@Click(R.id.finish_button)
	void finishOnClick() {
		if (mChooseLayer.getCheckedRadioButtonId() == -1
				|| mChooseClass.getCheckedRadioButtonId() == -1
				|| mChooseAlert.getCheckedRadioButtonId() == -1) {
			Toast.makeText(this, R.string.fill_all, Toast.LENGTH_SHORT).show();
		} else {
			int mLayer = Integer.parseInt((String) findViewById(
					mChooseLayer.getCheckedRadioButtonId()).getTag());
			int mClass = Integer.parseInt((String) findViewById(
					mChooseClass.getCheckedRadioButtonId()).getTag());
			boolean alert = (Integer.parseInt((String) findViewById(
					mChooseClass.getCheckedRadioButtonId()).getTag()) == 1 ? true
					: false);
			mPrefs.getLayer().put(mLayer);
			mPrefs.getMotherClass().put(mClass);
			mPrefs.getNotification().put(alert);
			Toast.makeText(this, R.string.saved, Toast.LENGTH_SHORT).show();
			startActivity(new Intent(this, MainActivity_.class));
			if (mApp.isNetworkAvailable())
				mApp.updateChanges(mLayer);
			if (alert) {
				// Create intent that the AlarmReceiver will catch
				Intent mIntent = new Intent("UPDATE_CHANGES");
				PendingIntent mPendingIntent = PendingIntent.getBroadcast(this,
						0, mIntent, 0);
				// Set alarm for 21:05
				Calendar cur_cal = new GregorianCalendar();
				cur_cal.setTimeInMillis(System.currentTimeMillis());
				Calendar cal = new GregorianCalendar();
				cal.set(Calendar.HOUR_OF_DAY, 21);
				cal.set(Calendar.MINUTE, 05);
				((AlarmManager) this.getSystemService(Context.ALARM_SERVICE))
						.setRepeating(AlarmManager.RTC_WAKEUP,
								cal.getTimeInMillis(), 86400000l,
								mPendingIntent);
			}
			finish();
		}
	}

	@Override
	public void onBackPressed() {
		if (mIsSettings) {
			startActivity(new Intent(this, MainActivity_.class));
			finish();
		} else
			super.onBackPressed();
	}

	private void inflateClasses(int layer) {
		String layerS = "";
		switch (layer) {
		case 9:
			layerS = mLayerNine;
			break;
		case 10:
			layerS = mLayerTen;
			break;
		case 11:
			layerS = mLayerEleven;
			break;
		case 12:
			layerS = mLayerTwelve;
			break;
		}
		for (int id : mSelectClassIds) {
			RadioButton button = (RadioButton) mChooseClass.findViewById(id);
			int classNum = Integer.parseInt((String) button.getTag());
			button.setText(layerS + classNum);
		}
	}
}
