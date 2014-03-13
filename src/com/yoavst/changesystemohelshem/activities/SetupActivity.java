package com.yoavst.changesystemohelshem.activities;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.App;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.StringArrayRes;
import org.androidannotations.annotations.sharedpreferences.Pref;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import android.content.Context;

import co.juliansuarez.libwizardpager.wizard.model.AbstractWizardModel;
import co.juliansuarez.libwizardpager.wizard.model.ModelCallbacks;
import co.juliansuarez.libwizardpager.wizard.model.Page;
import co.juliansuarez.libwizardpager.wizard.model.ReviewItem;
import co.juliansuarez.libwizardpager.wizard.ui.PageFragmentCallbacks;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.yoavst.changesystemohelshem.MyApp;
import com.yoavst.changesystemohelshem.Prefs_;
import com.yoavst.changesystemohelshem.R;
import com.yoavst.changesystemohelshem.Wizard;

@EActivity
public class SetupActivity extends SherlockFragmentActivity implements
		PageFragmentCallbacks, ModelCallbacks {
	@ViewById(R.id.pager)
	ViewPager mPager;
	@ViewById(R.id.next_button)
	Button mNextButton;
	@ViewById(R.id.prev_button)
	Button mPrevButton;
	@Pref
	Prefs_ mPrefs;
	@StringArrayRes(R.array.layers)
	String[] mClasses;
	@Extra("settings")
	boolean mIsSettings;
	@App
	MyApp mApp;
	static AbstractWizardModel sWizardModel;
	List<Page> mCurrentPageSequence;
	WizardPagerAdapter mPagerAdapter;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_wizard);
		if (savedInstanceState != null) {
			sWizardModel.load(savedInstanceState.getBundle("model"));
		}

	}

	@AfterViews
	void startWizard() {
		sWizardModel = new Wizard(getApplicationContext());
		sWizardModel.registerListener(this);
		mPagerAdapter = new WizardPagerAdapter(getSupportFragmentManager());
		mPager.setAdapter(mPagerAdapter);
		mCurrentPageSequence = sWizardModel.getCurrentPageSequence();
		mNextButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (mPager.getCurrentItem() == 2) {
					ArrayList<ReviewItem> reviewItems = new ArrayList<ReviewItem>();
					for (Page page : sWizardModel.getCurrentPageSequence()) {
						page.getReviewItems(reviewItems);
					}
					String classAndLayer = reviewItems.get(1).getDisplayValue();
					String mLayerString = reviewItems.get(0).getDisplayValue();
					int mLayer = 9;
					for (int i = 0; i < mClasses.length; i++) {
						if (mLayerString.equals(mClasses[i])) {
							mLayer = 9 + i;
							break;
						}
					}
					mPrefs.getLayer().put(mLayer);
					mPrefs.getMotherClass().put(
							Integer.parseInt(classAndLayer
									.substring(classAndLayer.length() - 1)));
					mPrefs.getNotification().put(
							(reviewItems
									.get(2)
									.getDisplayValue()
									.equals(getResources().getString(
											R.string.yes)) ? true : false));
					Toast.makeText(SetupActivity.this, R.string.saved,
							Toast.LENGTH_SHORT).show();
					startActivity(new Intent(SetupActivity.this,
							MainActivity_.class));
					if (mIsSettings && mApp.isNetworkAvailable())
						mApp.updateChanges(mLayer);
					if (mPrefs.getNotification().getOr(false)) {
						// Create intent that the AlarmReceiver will catch
						Intent mIntent = new Intent("UPDATE_CHANGES");
						PendingIntent mPendingIntent = PendingIntent
								.getBroadcast(SetupActivity.this, 0, mIntent, 0);
						// Set alarm for 21:05
						Calendar cur_cal = new GregorianCalendar();
						cur_cal.setTimeInMillis(System.currentTimeMillis());
						Calendar cal = new GregorianCalendar();
						cal.set(Calendar.HOUR_OF_DAY, 21);
						cal.set(Calendar.MINUTE, 05);
						((AlarmManager) SetupActivity.this
								.getSystemService(Context.ALARM_SERVICE)).setRepeating(
								AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), 86400000l,
								mPendingIntent);
					}
					finish();

				} else {
					mPager.setCurrentItem(mPager.getCurrentItem() + 1);
					updateBottomBar();
				}
			}
		});

		mPrevButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (mIsSettings && mPager.getCurrentItem() == 0) {
					startActivity(new Intent(SetupActivity.this,
							MainActivity_.class));
					finish();
				}
				mPager.setCurrentItem(mPager.getCurrentItem() - 1);
				updateBottomBar();

			}
		});
		onPageTreeChanged();
		updateBottomBar();
	}

	private void updateBottomBar() {
		int position = mPager.getCurrentItem();
		if (mIsSettings && position == 0) {
			mPrevButton.setVisibility(View.VISIBLE);
			mPrevButton.setText(R.string.cancel_button);
			return;
		} else
			mPrevButton.setVisibility(position <= 0 ? View.INVISIBLE
					: View.VISIBLE);
		if (position == 2) {
			mNextButton.setText(R.string.finish_button);
			mNextButton.setBackgroundResource(R.drawable.finish_background);
			mNextButton.setTextColor(Color.WHITE);
		} else {
			mNextButton.setText(R.string.next_button);
			mNextButton
					.setBackgroundResource(R.drawable.selectable_item_background);
			mNextButton.setTextColor(Color.BLACK);
		}

	}

	@Override
	public Page onGetPage(String key) {
		return sWizardModel.findByKey(key);
	}

	public class WizardPagerAdapter extends FragmentStatePagerAdapter {
		private int mCutOffPage;
		private Fragment mPrimaryItem;

		public WizardPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int i) {
			return mCurrentPageSequence.get(i).createFragment();
		}

		@Override
		public int getItemPosition(Object object) {
			if (object == mPrimaryItem) {
				return POSITION_UNCHANGED;
			}

			return POSITION_NONE;
		}

		@Override
		public void setPrimaryItem(ViewGroup container, int position,
				Object object) {
			super.setPrimaryItem(container, position, object);
			mPrimaryItem = (Fragment) object;
		}

		@Override
		public int getCount() {
			return Math.min(mCutOffPage + 1, mCurrentPageSequence == null ? 1
					: mCurrentPageSequence.size() + 1);
		}

		public void setCutOffPage(int cutOffPage) {
			if (cutOffPage < 0) {
				cutOffPage = Integer.MAX_VALUE;
			}
			mCutOffPage = cutOffPage;
		}

		public int getCutOffPage() {
			return mCutOffPage;
		}
	}

	@Override
	public void onPageDataChanged(Page page) {
		if (page.isRequired()) {
			if (recalculateCutOffPage()) {
				mPagerAdapter.notifyDataSetChanged();
				updateBottomBar();
			}
		}
	}

	@Override
	public void onPageTreeChanged() {
		mCurrentPageSequence = sWizardModel.getCurrentPageSequence();
		mPagerAdapter.notifyDataSetChanged();
	}

	private boolean recalculateCutOffPage() {
		// Cut off the pager adapter at first required page that isn't completed
		int cutOffPage = mCurrentPageSequence.size() + 1;
		for (int i = 0; i < mCurrentPageSequence.size(); i++) {
			Page page = mCurrentPageSequence.get(i);
			if (page.isRequired() && !page.isCompleted()) {
				cutOffPage = i;
				break;
			}
		}

		if (mPagerAdapter.getCutOffPage() != cutOffPage) {
			mPagerAdapter.setCutOffPage(cutOffPage);
			return true;
		}

		return false;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		sWizardModel.unregisterListener(this);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBundle("model", sWizardModel.save());
	}

}
