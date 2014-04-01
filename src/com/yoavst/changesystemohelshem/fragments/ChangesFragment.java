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
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;
import com.kanak.emptylayout.EmptyLayout;
import com.yoavst.changesystemohelshem.ChangeObject;
import com.yoavst.changesystemohelshem.ChangesListViewAdapter;
import com.yoavst.changesystemohelshem.MyApp;
import com.yoavst.changesystemohelshem.R;
import com.yoavst.changesystemohelshem.activities.MainActivity_;

/**
 * The most used fragment - the fragment that showing the changes.
 * 
 * @author Yoav Sternberg
 * 
 */
@EFragment(R.layout.fragment_changes)
public class ChangesFragment extends SherlockFragment implements
		OnClickListener {
	@ViewById(R.id.lessons)
	ListView mListView;
	@ViewById(R.id.nochanges)
	TextView mTextViewNoChanges;
	@ViewById(R.id.cached)
	TextView mTextViewCached;
	@ViewById(R.id.refresh_layout)
	SwipeRefreshLayout mRefreshLayout;
	@ViewById(R.id.showtimetable)
	Button mButtonTimetable;
	@ViewById(R.id.refreshbutton)
	Button mButtonRefresh;
	@ViewById(R.id.layout)
	RelativeLayout mLayout;
	@ViewById(R.id.refreshbuttonoutside)
	Button mButtonRefreshOutside;
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
	final int ANIMATION_LENGTH = 1500;

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
		mLayout.setVisibility(View.GONE);
		setVisiblityForTryAgainOutside(View.GONE);
		// Setup pull to refresh
		mRefreshLayout.setEnabled(true);
		mRefreshLayout.setColorScheme(R.color.holo_green, R.color.holo_red,
				R.color.holo_blue, R.color.holo_yellow);
		mRefreshLayout.setOnRefreshListener(new OnRefreshListener() {
			@Override
			public void onRefresh() {
				// Save the time
				setMiliForAnimation(System.currentTimeMillis());
				// Refresh the changes
				((MainActivity_) getActivity()).refreshSelected();
			}
		});
		// Setup refresh buttons
		mButtonRefresh.setOnClickListener(this);
		mButtonRefreshOutside.setOnClickListener(this);
		// Setup show timetable button
		mButtonTimetable.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				day = mApp.getDayOfWeekFromLayerLastUpdateChangeTime(mLayer);
				if (day == 0) {
					showErrorMessage(getActivity().getString(R.string.error),
							false, null);
					setVisiblityForTryAgainOutside(View.VISIBLE);
				} else {
					// Show timetable
					String[] timetable = mTimetable[day - 1];
					ChangeObject[] changes = new ChangeObject[timetable.length];
					TypedArray colors = getActivity().getResources()
							.obtainTypedArray(R.array.holo_colors);
					for (int i = 0; i < timetable.length; i++) {
						if (timetable[i] != null && !timetable[i].equals(" "))
							changes[i] = new ChangeObject(i + 1, timetable[i],
									mNoChanges, colors.getColor(i % 5, 0));
						else {
							changes[i] = new ChangeObject(i + 1, " ", " ",
									colors.getColor(i % 5, 0));
						}
					}
					colors.recycle();
					mAdapter = new ChangesListViewAdapter(getActivity(),
							changes);
					mListView.setAdapter(mAdapter);
					setVisibiltyForNoChanges(View.VISIBLE);
					// Hide the buttons
					mLayout.setVisibility(View.GONE);
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
				setVisiblityForTryAgainOutside(View.VISIBLE);
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
		mRefreshLayout.setRefreshing(false);
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
			stopRefreshingAnimation(new StopAnimationInterface() {
				@Override
				public void DoOnEnd() {
					mAdapter = new ChangesListViewAdapter(getActivity(),
							mChanges.toArray(new ChangeObject[mChanges.size()]));
					mListView.setAdapter(mAdapter);
				}
			});
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
		stopRefreshingAnimation(new StopAnimationInterface() {
			@Override
			public void DoOnEnd() {
				mListView.setAdapter(null);
				mEmptyLayout.setEmptyMessage(emptyMessage);
				mEmptyLayout.setShowEmptyButton(showEmptyButton);
				mEmptyLayout.setEmptyButtonClickListener(listener);
				mEmptyLayout.showEmpty();
			}
		});
	}

	public void showEmptyMessage(String emptyMessage) {
		showEmptyMessage(emptyMessage, false, null);
	}

	public void showEmptyMessageOrTimetable() {
		stopRefreshingAnimation(new StopAnimationInterface() {

			@Override
			public void DoOnEnd() {
				day = mApp.getDayOfWeekFromLayerLastUpdateChangeTime(mLayer);
				if (day == 0) {
					showErrorMessage(getActivity().getString(R.string.error),
							false, null);
					setVisiblityForTryAgainOutside(View.VISIBLE);
				} else if (day != 7) {
					String[] timetable = mTimetable[day - 1];
					if (MyApp.isTimetableEmpty(timetable)) {
						showEmptyMessage(getResources().getString(
								R.string.no_changes));
						setVisiblityForTryAgainOutside(View.VISIBLE);
					} else {
						showEmptyMessage(getActivity().getString(
								R.string.no_changes));
						mLayout.setVisibility(View.VISIBLE);
					}
				} else {
					showEmptyMessage(mNoChanges);
					setVisiblityForTryAgainOutside(View.VISIBLE);
				}

			}
		});
	}

	public void setVisiblityForTryAgainOutside(int status) {
		mButtonRefreshOutside.setVisibility(status);
	}

	public void setVisibiltyForNoChanges(int status) {
		mTextViewNoChanges.setVisibility(status);
	}

	public void setVisibiltyForCached(int status) {
		mTextViewCached.setVisibility(status);
	}

	public void setRefreshing(boolean refreshing) {
		mRefreshLayout.setRefreshing(refreshing);
	}

	private void stopRefreshingAnimation(final StopAnimationInterface sai) {
		if (mRefreshLayout.isRefreshing()) {
			// Check the current time
			long cTime = System.currentTimeMillis();
			// Check how many milisecond check the refreshing
			int subtract = (int) (cTime - mMilisForAnimation);
			// If it took less then 2 seconds
			if (subtract <= ANIMATION_LENGTH) {
				// Do what need to be done 2 second after start of animation
				final Handler handler = new Handler();
				Runnable task = new Runnable() {
					public void run() {
						mRefreshLayout.setRefreshing(false);
						sai.DoOnEnd();
					}
				};
				handler.postDelayed(task, ANIMATION_LENGTH - subtract);
			} else {
				// Do what need to be done
				mRefreshLayout.setRefreshing(false);
				sai.DoOnEnd();
			}
		} else
			sai.DoOnEnd();
	}

	public void setMiliForAnimation(long milis) {
		mMilisForAnimation = milis;
	}

	private interface StopAnimationInterface {
		void DoOnEnd();
	}

	/**
	 * On click listener for refresh buttons
	 */
	@Override
	public void onClick(View v) {
		if (mApp.isNetworkAvailable()) {
			// Save the time
			setMiliForAnimation(System.currentTimeMillis());
			// Refresh the changes
			((MainActivity_) getActivity()).refreshSelected();
			// Hide the buttons
			mLayout.setVisibility(View.GONE);
			setVisiblityForTryAgainOutside(View.GONE);
		} else {
			Toast.makeText(getActivity(), R.string.no_connection,
					Toast.LENGTH_SHORT).show();
			showEmptyMessageOrTimetable();
		}
	}
}
