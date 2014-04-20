package com.yoavst.changesystemohelshem.fragments;

import java.util.ArrayList;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.App;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.ViewById;

import android.content.res.TypedArray;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;
import com.kanak.emptylayout.EmptyLayout;
import com.yoavst.changesystemohelshem.ChangeObject;
import com.yoavst.changesystemohelshem.ChangesListViewAdapter;
import com.yoavst.changesystemohelshem.MyApp;
import com.yoavst.changesystemohelshem.MyApp.LessonTime;
import com.yoavst.changesystemohelshem.R;

/**
 * The most used fragment - the fragment that showing the changes.
 * 
 * @author Yoav Sternberg
 * 
 */
@EFragment(R.layout.fragment_changes)
public class ChangesFragment extends SherlockFragment
		 {
	@ViewById(R.id.lessons)
	ListView mListView;
	@ViewById(R.id.nochanges)
	TextView mTextViewNoChanges;
	@ViewById(R.id.cached)
	TextView mTextViewCached;
	@ViewById(R.id.showtimetable)
	Button mButtonTimetable;
	@FragmentArg("layer")
	@InstanceState
	int mLayer;
	@FragmentArg("class")
	@InstanceState
	int mClass;
	@App
	MyApp mApp;
	/**
	 * Cached version. Will be updated each time by the Activity.
	 */
	@FragmentArg("changes")
	@InstanceState
	ArrayList<ChangeObject> mChanges;
	static String mNoChanges;
	ChangesListViewAdapter mAdapter;
	EmptyLayout mEmptyLayout;
	/**
	 * Showing the loading animation for at least 1.5 seconds
	 */
	long mMilisForAnimation;
	/**
	 * The class's timetable
	 */
	String[][] mTimetable;
	/**
	 * The date of the changes or today
	 */
	int day;
	static final int ANIMATION_LENGTH = 1500;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(false);
	}

	@AfterViews
	void init() {
		// Save the day of the last updated time
		day = mApp.getDayOfWeekFromLayerLastUpdateChangeTime(mLayer);
		// Init the empty layout
		mEmptyLayout = new EmptyLayout(getActivity(), mListView);
		// Set the static variable one time only
		if (mNoChanges == null) {
			mNoChanges = getActivity().getString(R.string.no_changes);
		}
		// Set visibility of the "no changes" & "cached" views to not visible
		setVisibiltyForNoChanges(View.GONE);
		setVisibiltyForCached(View.GONE);
		// Set Visibility of the "show timetable" button to not visible
		// Setup show timetable button
		mButtonTimetable.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				day = mApp.getDayOfWeekFromLayerLastUpdateChangeTime(mLayer);
				if (day == 0) {
					showErrorMessage(getActivity().getString(R.string.error),
							false, null);
				} else {
					// Show timetable
					String[] timetable = mTimetable[day - 1];
					ChangeObject[] changes = new ChangeObject[timetable.length];
					TypedArray colors = getActivity().getResources()
							.obtainTypedArray(R.array.holo_colors);
					for (int i = 0; i < timetable.length; i++) {
						if (timetable[i] != null && !timetable[i].equals(" "))
							changes[i] = new ChangeObject(i + 1, timetable[i],
									mNoChanges, colors.getColor(i % 5, 0), mApp
											.getLessonTime(i + 1,
													LessonTime.Start), mApp
											.getLessonTime(i + 1,
													LessonTime.End));
						else {
							changes[i] = new ChangeObject(i + 1, " ", " ",
									colors.getColor(i % 5, 0), mApp
											.getLessonTime(i + 1,
													LessonTime.Start), mApp
											.getLessonTime(i + 1,
													LessonTime.End));
						}
					}
					colors.recycle();
					mAdapter = new ChangesListViewAdapter(getActivity(),
							changes);
					mListView.setAdapter(mAdapter);
					setVisibiltyForNoChanges(View.VISIBLE);
					// Hide the button
					mButtonTimetable.setVisibility(View.GONE);
				}
			}
		});
		if (mLayer < 9 || mLayer > 12 || mClass < 1 || mClass > 13) {
			showErrorMessage(
					getActivity().getResources().getString(R.string.error),
					false, null);
		} else {
			// Save the timetable to the class
			mTimetable = mApp.getTimetable()[mLayer - 9][mClass - 1];
			// If layer is empty
			if (mApp.isLayerEmpty(mLayer)) {
				// Show empty or timetable
				showEmptyMessageOrTimetable();
			} else if (!mApp.isNetworkAvailable()
					&& mApp.getChangesForLayer(mLayer) == null) {
				showErrorMessage(getActivity()
						.getString(R.string.no_connection), false, null);
				mButtonTimetable.setVisibility(View.GONE);
			} else if (mChanges == null
					&& mApp.getChangesForLayer(mLayer) == null) {
				showLoadingMessage(getActivity().getResources().getString(
						R.string.loading_message));
			} else if (mChanges == null
					&& mApp.getChangesForLayer(mLayer) != null) {
				//
			} else {
				showChanges();
			}
		}
	}

	public void showErrorMessage(String errorMessage, boolean showButton,
			OnClickListener listener) {
		mEmptyLayout.setErrorMessage(errorMessage);
		mEmptyLayout.setShowErrorButton(showButton);
		mEmptyLayout.setErrorButtonClickListener(listener);
		if (mAdapter != null) {
			mAdapter = null;
		}
		mListView.setAdapter(null);
		mEmptyLayout.showError();
	}

	public void showLoadingMessage(String loadingMessage) {
		mListView.setAdapter(null);
		mEmptyLayout.setLoadingMessage(loadingMessage);
		mEmptyLayout.setShowLoadingButton(false);
		mEmptyLayout.showLoading();
	}

	public void showChanges(ArrayList<ArrayList<ChangeObject>> changes) {
		if (changes == null)
			mChanges = null;
		else if (mChanges != changes.get(mClass - 1)) {
			mChanges = changes.get(mClass - 1);
		}
		showChanges();
	}

	private void showChanges() {
		if (mChanges == null || mApp.isChangesEmpty(mChanges)) {
			showEmptyMessageOrTimetable();
		} else {
			mAdapter = new ChangesListViewAdapter(getActivity(),
					mChanges.toArray(new ChangeObject[mChanges.size()]));
			mListView.setAdapter(mAdapter);
		}
	}

	public int getLayer() {
		return mLayer;
	}

	public int getMotherClass() {
		return mClass;
	}

	public void showEmptyMessage(final String emptyMessage,
			final boolean showEmptyButton, final OnClickListener listener) {
		mListView.setAdapter(null);
		mEmptyLayout.setEmptyMessage(emptyMessage);
		mEmptyLayout.setShowEmptyButton(showEmptyButton);
		mEmptyLayout.setEmptyButtonClickListener(listener);
		mEmptyLayout.showEmpty();

	}

	public void showEmptyMessage(String emptyMessage) {
		showEmptyMessage(emptyMessage, false, null);
	}

	public void showEmptyMessageOrTimetable() {
				day = mApp.getDayOfWeekFromLayerLastUpdateChangeTime(mLayer);
				if (day == 0) {
					showErrorMessage(
							getActivity().getString(R.string.no_connection),
							false, null);
				} else if (day != 7) {
					String[] timetable = mTimetable[day - 1];
					if (MyApp.isTimetableEmpty(timetable)) {
						showEmptyMessage(getResources().getString(
								R.string.no_changes));
					} else {
						showEmptyMessage(getActivity().getString(
								R.string.no_changes));
						mButtonTimetable.setVisibility(View.VISIBLE);
					}
				} else {
					showEmptyMessage(mNoChanges);
				}
	}

	public void setVisibiltyForNoChanges(int status) {
		mTextViewNoChanges.setVisibility(status);
	}

	public void setVisibiltyForCached(int status) {
		mTextViewCached.setVisibility(status);
	}
}