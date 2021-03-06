package com.yoavst.changesystemohelshem.activities;

import java.util.ArrayList;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.App;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.StringArrayRes;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.TypedArray;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.graphics.BitmapFactory;
import android.widget.AbsListView;
import android.widget.ImageView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.OnNavigationListener;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.yoavst.changesystemohelshem.ChangeObject;
import com.yoavst.changesystemohelshem.CustomSpinnerAdapter;
import com.yoavst.changesystemohelshem.MyApp;
import com.yoavst.changesystemohelshem.Prefs_;
import com.yoavst.changesystemohelshem.R;
import com.yoavst.changesystemohelshem.fragments.ChangesFragment;
import com.yoavst.changesystemohelshem.fragments.ChangesFragment_;
import com.yoavst.changesystemohelshem.fragments.HelpFragment_;
import com.yoavst.changesystemohelshem.views.BetterNavigationDrawer;
import com.yoavst.changesystemohelshem.views.BetterNavigationDrawer.NavigationDrawerOptions;

import fr.nicolaspomepuy.discreetapprate.AppRate;
import fr.nicolaspomepuy.discreetapprate.RetryPolicy;

/**
 * The Main Activity, where everything that the user should know about it
 * happens (exclude the fragments)...
 * 
 * @author Yoav Sternberg
 * 
 */
@EActivity(R.layout.activity_main)
@OptionsMenu(R.menu.main)
public class MainActivity extends SherlockFragmentActivity implements
		MyApp.UpdateChanges {
	@App
	MyApp mApp;
	@ViewById(R.id.navigation_drawer_container)
	BetterNavigationDrawer mDrawerLayout;
	@ViewById(R.id.last_title)
	TextView mLastTitle;
	@ViewById(R.id.last_title_date)
	TextView mLastTitleDate;
	@ViewById(R.id.last_subtitle)
	TextView mLastSubtitle;
	@ViewById(R.id.last_subtitle_date)
	TextView mLastSubtitleDate;
	@ViewById(R.id.slide_pane_banner)
	LinearLayout mPane;
	@StringArrayRes(R.array.navigation_titles)
	String[] mNavigationTitles;
	@StringArrayRes(R.array.secondery_navigation_titles)
	String[] mSeconderyNavigationTitles;
	@InstanceState
	ActivityModes mActivityInfo = ActivityModes.NotSetYet;
	@InstanceState
	int mCurrentClass;
	@InstanceState
	int mCurrentLayer;
	@Extra("shouldRefresh")
	boolean mShouldRefresh = false;
	ActionBar mActionBar;
	FragmentManager mFragmentManager;
	NavigationDrawerOptions mNavDrawerOptions;
	static boolean sShouldOpenDrawer = false;

	enum ActivityModes {
		NotSetYet, User, Layer, Help
	}

	@SuppressLint("NewApi")
	@AfterViews
	void init() {
		// Init global variables
		mActionBar = getSupportActionBar();
		mFragmentManager = getSupportFragmentManager();
		// Check if user's class and layer exists on the preferences.
		Prefs_ mPrefs = mApp.getPreferences();
		if (!mPrefs.getLayer().exists() || !mPrefs.getMotherClass().exists()
				|| mPrefs.getLayer().getOr(0) < 9
				|| mPrefs.getLayer().getOr(0) > 12
				|| mPrefs.getMotherClass().getOr(0) < 1
				|| mPrefs.getMotherClass().getOr(0) > 13) {
			overridePendingTransition(0, 0);
			startActivity(new Intent(this, WizardActivity_.class));
			finish();
		}
		// Set listener for download changes
		mApp.setListener(this);
		// Configure ActionBar & Navigation Drawer
		mActionBar.setDisplayHomeAsUpEnabled(true);
		mActionBar.setHomeButtonEnabled(true);
		ActionBarDrawerToggle mDrawerToggle = new ActionBarDrawerToggle(this,
				mDrawerLayout, R.drawable.ic_drawer, R.string.app_name,
				R.string.app_name) {
			// Do nothing
			@Override
			public void onDrawerClosed(View view) {
			}

			// Do nothing
			@Override
			public void onDrawerOpened(View drawerView) {
			}
		};
		ImageView imageHeaderView = new ImageView(this);
		imageHeaderView.setImageBitmap(BitmapFactory.decodeResource(
				getResources(), R.drawable.logo));
		imageHeaderView.setScaleType(ImageView.ScaleType.FIT_XY);
		imageHeaderView.setLayoutParams(new AbsListView.LayoutParams(
				AbsListView.LayoutParams.MATCH_PARENT,
				AbsListView.LayoutParams.WRAP_CONTENT));
		TypedArray ids = getResources().obtainTypedArray(
				R.array.navigation_icons);
		int[] drawables = new int[ids.length()];
		for (int i = 0; i < ids.length(); i++) {
			drawables[i] = ids.getResourceId(i, -1);
		}
		ids.recycle();
		TypedArray secondaryIds = getResources().obtainTypedArray(
				R.array.secondery_navigation_icons);
		int[] secondaryDrawables = new int[secondaryIds.length()];
		for (int i = 0; i < secondaryIds.length(); i++) {
			secondaryDrawables[i] = secondaryIds.getResourceId(i, -1);
		}
		secondaryIds.recycle();
		mNavDrawerOptions = new NavigationDrawerOptions().canItemFocus(false)
				.setHeaderView(imageHeaderView, false)
				.setMainSectionsEntries(mNavigationTitles)
				.setMainSectionsDrawables(drawables)
				.setSecondaryEntries(mSeconderyNavigationTitles)
				.setSecondarySectionsDrawables(secondaryDrawables);
		mDrawerLayout.setOptions(mNavDrawerOptions);
		mDrawerLayout.setListViewSections();
		mDrawerLayout.setDrawerListener(mDrawerToggle);
		mDrawerLayout
				.setOnNavigationSectionSelected(new BetterNavigationDrawer.OnNavigationSectionSelected() {

					@Override
					public void onSectionSelected(View v, int position, long id) {
						showItem(position);
					}
				});
		// If first time running the activity
		if (mActivityInfo == ActivityModes.NotSetYet) {
			// Show user Changes
			showItem(1);
			// If Activity recreated and user was on its changes
		} else if (mActivityInfo == ActivityModes.User) {

			setLastUpdated(mApp.getLastUpdateDay(mPrefs.getLayer().getOr(0)),
					mApp.getsLastTime(mPrefs.getLayer().getOr(0)));
			// If Activity recreated and user was on the help fragment
		} else if (mActivityInfo == ActivityModes.Help) {
			// Hide the panel of last update
			mPane.setVisibility(View.GONE);
			// If the user was on one of the layer's fragment
		} else {
			// Show spinner
			showSpinner(mCurrentLayer);
			setLastUpdated(mApp.getLastUpdateDay(mCurrentLayer),
					mApp.getsLastTime(mCurrentLayer));
		}
		if (mShouldRefresh)
			refreshSelected();
		// Discreet app rate request
		AppRate.with(this).initialLaunchCount(5)
				.retryPolicy(RetryPolicy.INCREMENTAL)
				.listener(new AppRate.OnShowListener() {
					@Override
					public void onRateAppClicked() {
						// Don't show it anymore
						AppRate.with(MainActivity.this).neverShowAgain();
					}

					@Override
					public void onRateAppDismissed() {
					}

					@Override
					public void onRateAppShowing() {

					}
				}).checkAndShow();
	}

	/**
	 * Show selected option from Navigation Drawer
	 */
	void showItem(int position) {
		// The Fragment to show
		switch (position) {
		// User class
		case 1:
			// The fragment's builder
			ChangesFragment_.FragmentBuilder_ fragmentBuilder = ChangesFragment_
					.builder();
			// Insert the user's layer and class
			fragmentBuilder.mLayer(mApp.getPreferences().getLayer().getOr(0))
					.mClass(mApp.getPreferences().getMotherClass().getOr(0));
			// If there is saved changes
			if (mApp.getChangesForLayer(mApp.getPreferences().getLayer()
					.getOr(0)) != null) {
				// Insert them to the fragment
				ArrayList<ArrayList<ChangeObject>> changes = mApp
						.getChangesForLayer(mApp.getPreferences().getLayer()
								.getOr(0));
				int mClass = mApp.getPreferences().getMotherClass().getOr(1) - 1;
				fragmentBuilder.mChanges(changes.get(mClass));
			}
			// Remove Navgiation Spinner
			mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
			mActionBar.setDisplayShowTitleEnabled(true);
			// Clean the variables
			mCurrentLayer = mApp.getPreferences().getLayer().get();
			mCurrentClass = 0;
			mActivityInfo = ActivityModes.User;
			// Build and show the Fragment
			SherlockFragment mFragment = fragmentBuilder.build();
			mFragmentManager.beginTransaction()
					.replace(R.id.content_frame, mFragment).commit();
			// Show the panel of last update
			mPane.setVisibility(View.VISIBLE);
			setLastUpdated(mApp.getLastUpdateDay(mApp.getPreferences()
					.getLayer().getOr(0)), mApp.getsLastTime(mApp
					.getPreferences().getLayer().getOr(0)));
			break;
		case 2:
		case 3:
		case 4:
		case 5:
			// Set global variable to the current layer
			mCurrentLayer = position + 7;
			// Update global state
			mActivityInfo = ActivityModes.Layer;
			// Show the spinner
			showSpinner(mCurrentLayer);
			// Select Item
			showItemFromSpinner(1);
			// Show the panel of last update
			mPane.setVisibility(View.VISIBLE);
			setLastUpdated(mApp.getLastUpdateDay(position + 7),
					mApp.getsLastTime(position + 7));
			break;
		case 6:
			// Hide the panel of last update
			mPane.setVisibility(View.GONE);
			// Update global state
			mActivityInfo = ActivityModes.Help;
			// Create the Fragment and show it
			SherlockFragment fragment = HelpFragment_.builder().build();
			mFragmentManager.beginTransaction()
					.replace(R.id.content_frame, fragment).commit();
			break;
		case 7:
			settingsSelected();
			break;
		}
		mDrawerLayout.check(position);
		// Close the drawer
		if (sShouldOpenDrawer) {
			if (position > 2 || position > 5)
				sShouldOpenDrawer = true;
			else
				mDrawerLayout.openDrawerMenu();
		} else
			mDrawerLayout.closeDrawerMenu();

	}

	/**
	 * Show the given class that selected from the spinner
	 */
	void showItemFromSpinner(int mClass) {
		// Create Fragment builder
		ChangesFragment_.FragmentBuilder_ fragmentBuilder = ChangesFragment_
				.builder();
		// Insert the layer
		fragmentBuilder.mLayer(mCurrentLayer);
		// Insert the class
		fragmentBuilder.mClass(mClass);
		// Save the class
		mCurrentClass = mClass;
		// If there is saved changes
		if (mApp.getChangesForLayer(mCurrentLayer) != null) {
			// Insert them to the fragment
			ArrayList<ArrayList<ChangeObject>> changes = mApp
					.getChangesForLayer(mCurrentLayer);
			fragmentBuilder.mChanges(changes.get(mClass - 1));
		} else {
			refreshSelected();
		}
		// Build the fragment
		SherlockFragment mFragment = fragmentBuilder.build();
		// Show the fragment
		mFragmentManager.beginTransaction()
				.replace(R.id.content_frame, mFragment).commit();
		// Close drawer if opened
		if (sShouldOpenDrawer)
			mDrawerLayout.openDrawerMenu();
		else
			mDrawerLayout.closeDrawerMenu();
		sShouldOpenDrawer = false;

	}

	/**
	 * Show the spinner with number of items as the given layer's classes
	 */
	void showSpinner(int layer) {
		// Enable Spinner in ActionBar
		mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		mActionBar.setDisplayShowTitleEnabled(false);
		// Get number of classes in current layer
		int numberOfClasses = getResources()
				.getIntArray(R.array.layers_classes)[layer - 9];
		// Make a String array with the classes' numbers
		String[] SpinnerTitles = new String[numberOfClasses];
		String mLayer = getResources().getStringArray(R.array.layers)[layer - 9];
		for (int i = 0; i < numberOfClasses; i++) {
			SpinnerTitles[i] = mLayer + "'" + (i + 1);
		}
		// Create Custom Adapter
		CustomSpinnerAdapter mSpinnerAdapter = new CustomSpinnerAdapter(this,
				mLayer, numberOfClasses);

		// Create on item selected listener
		OnNavigationListener mOnNavigationListener = new OnNavigationListener() {

			@Override
			public boolean onNavigationItemSelected(int position, long itemId) {
				// Show item
				showItemFromSpinner(position + 1);
				return true;
			}
		};
		// Set the listener
		mActionBar.setListNavigationCallbacks(mSpinnerAdapter,
				mOnNavigationListener);
	}

	/**
	 * On android.R.id.home (where the app name and icon are) selected.
	 */
	@OptionsItem
	void homeSelected() {
		// If there is Navigation Drawer
		if (mDrawerLayout != null) {
			// If Navigation Drawer opened
			if (mDrawerLayout.isDrawerMenuOpen())
				// Close it
				mDrawerLayout.closeDrawerMenu();
			else
				// Open it
				mDrawerLayout.openDrawerMenu();
		}
	}

	/**
	 * On settings selected
	 */
	void settingsSelected() {
		// Start settings (setup but with back button to this) Activity.
		WizardActivity_.intent(this).mIsSettings(true).start();
		// Finish this to avoid this activity being shown twice
		finish();
	}

	/**
	 * On refresh on menu selected
	 */
	@OptionsItem(R.id.action_refresh)
	public void refreshSelected() {
		// Get the current shown fragment
		SherlockFragment fragment = (SherlockFragment) getSupportFragmentManager()
				.findFragmentById(R.id.content_frame);
		if (fragment instanceof ChangesFragment_ || mShouldRefresh) {
			// If there is internet connection
			if (mApp.isNetworkAvailable()) {
				if (fragment instanceof ChangesFragment_) {
					// Show loading message
					((ChangesFragment) fragment)
							.showLoadingMessage(getResources().getString(
									R.string.loading_message));
					// Hide the "no changes" textview
					((ChangesFragment) fragment)
							.setVisibiltyForNoChanges(View.GONE);
				}
				// Download the changes
				mApp.updateChanges(mCurrentLayer);
			} else {
				if (fragment instanceof ChangesFragment_) {
					// Show error of no connection
					((ChangesFragment) fragment).showErrorMessage(
							getResources().getString(R.string.no_connection),
							false, null);
				} else {
					Toast.makeText(this, R.string.no_connection,
							Toast.LENGTH_SHORT).show();
				}
			}
		}
	}

	@Override
	public void onUpdateChangesCompleted(
			ArrayList<ArrayList<ChangeObject>> changes, int layer) {
		// Get the fragment
		SherlockFragment fragment = (SherlockFragment) getSupportFragmentManager()
				.findFragmentById(R.id.content_frame);
		// If the fragment is ChangesFragment_ (not help Fragment)
		if (fragment != null && fragment instanceof ChangesFragment_) {
			// Show the changes
			((ChangesFragment) fragment).showChanges(changes);
			// Show last upate time
			setLastUpdated(mApp.getLastUpdateDay(layer),
					mApp.getsLastTime(layer));
			// mRefreshLayout.setRefreshing(false);
		}

	}

	/**
	 * @param dateOfDownload
	 *            the hour when the changes where downloaded
	 * @param date
	 *            the date of the changes
	 */
	private void setLastUpdated(String dateOfDownload, String date) {
		if (!dateOfDownload.equals(getResources().getString(
				R.string.never_updated))) {
			mLastTitle.setText(getResources().getString(R.string.last_updated));
			mLastTitleDate.setText(date);
			mLastSubtitle.setText(getResources().getString(
					R.string.last_updated_subtitle));
			mLastSubtitleDate.setText(dateOfDownload);
		} else {
			mLastTitle.setText(dateOfDownload);
			mLastTitleDate.setText("");
			mLastSubtitle.setText("");
			mLastSubtitleDate.setText("");
		}
	}

	@Override
	public void showConnectionError() {
		// Get the fragment
		SherlockFragment fragment = (SherlockFragment) getSupportFragmentManager()
				.findFragmentById(R.id.content_frame);
		// If the fragment is ChangesFragment_ (not help Fragment)
		if (fragment != null && fragment instanceof ChangesFragment_) {
			((ChangesFragment_) fragment).showErrorMessage(
					getString(R.string.no_connection), false, null);
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		// Save the drawer state
		sShouldOpenDrawer = mDrawerLayout.isDrawerMenuOpen();
	}
}
