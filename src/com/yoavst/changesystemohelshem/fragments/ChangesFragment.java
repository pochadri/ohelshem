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
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;
import com.kanak.emptylayout.EmptyLayout;
import com.yoavst.changesystemohelshem.ChangeObject;
import com.yoavst.changesystemohelshem.ChangesListViewAdapter;
import com.yoavst.changesystemohelshem.MyApp;
import com.yoavst.changesystemohelshem.R;

/**
 * The most used fragment - the fragment that showing the changes.
 * 
 * @author Yoav Sternberg
 * 
 */
@EFragment(R.layout.fragment_changes)
public class ChangesFragment extends SherlockFragment {
	@ViewById(R.id.lessons)
	ListView mListView;
	@ViewById(R.id.nochanges)
	TextView mTextView;
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
	ChangesListViewAdapter mAdapter;
	EmptyLayout mEmptyLayout;

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return null;
	}

	@AfterViews
	void init() {
		mEmptyLayout = new EmptyLayout(getActivity(), mListView);
		// Set visibility of the "no changes" view to not visible
		setVisibiltyForNoChanges(View.GONE);
		if (mLayer < 9 || mLayer > 12 || mClass < 1 || mClass > 13) {
			showErrorMessage(
					getActivity().getResources().getString(R.string.error),
					false, null);
		} else {
			if (mApp.isLayerEmpty(mLayer)) {
				showEmptyMessageOrTimetable();
			} else if (!mApp.isNetworkAvailable()
					&& mApp.getChangesForLayer(mLayer) == null) {
				showErrorMessage(
						getActivity().getResources().getString(
								R.string.no_connection), false, null);
			} else if (mChanges == null) {
				showLoadingMessage(getActivity().getResources().getString(
						R.string.loading_message));
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
		if (mChanges != changes.get(mClass - 1)) {
			mChanges = changes.get(mClass - 1);
		}
		showChanges();
	}

	private void showChanges() {
		if (mApp.isChangesEmpty(mChanges)) {
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

	public void showEmptyMessage(String emptyMessage, boolean showEmptyButton,
			OnClickListener listener) {
		mListView.setAdapter(null);
		mEmptyLayout.setEmptyMessage(emptyMessage);
		mEmptyLayout.setShowEmptyButton(showEmptyButton);
		mEmptyLayout.setEmptyButtonClickListener(listener);
		mEmptyLayout.showEmpty();
	}

	public void showEmptyMessageOrTimetable() {
		String emptyMessage = getActivity().getResources().getString(
				R.string.no_changes);
		int day = mApp.getDayOfWeekFromLayerLastUpdateChangeTime(mLayer);
		if (day != 7) {
			String[] timetable = mApp.getTimetable()[mLayer - 9][mClass - 1][day - 1];
			ChangeObject[] changes = new ChangeObject[timetable.length];
			TypedArray colors = getActivity().getResources().obtainTypedArray(
					R.array.holo_colors);
			for (int i = 0; i < timetable.length; i++) {
				if (timetable[i] != null && !timetable[i].equals(" "))
					changes[i] = new ChangeObject(i + 1, timetable[i],
							emptyMessage, colors.getColor(i % 5, 0));
				else {
					changes[i] = new ChangeObject(i + 1, " ", " ",
							colors.getColor(i % 5, 0));
				}
			}
			colors.recycle();
			mAdapter = new ChangesListViewAdapter(getActivity(), changes);
			mListView.setAdapter(mAdapter);
			setVisibiltyForNoChanges(View.VISIBLE);
		} else
			showEmptyMessage(emptyMessage, false, null);
	}
	
	public void setVisibiltyForNoChanges(int status) {
		mTextView.setVisibility(status);
	}

}
