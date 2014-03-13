package com.yoavst.changesystemohelshem.fragments;

import java.util.ArrayList;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.App;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.ViewById;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ListView;

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
		if (mLayer < 9 || mLayer > 12 || mClass < 1 || mClass > 13) {
			showErrorMessage(
					getActivity().getResources().getString(R.string.error),
					false, null);
		} else {
			if (mApp.isLayerEmpty(mLayer)) {
				showEmptyMessage(getActivity().getResources().getString(
						R.string.no_changes));
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
			showEmptyMessage(getActivity().getResources().getString(
					R.string.no_changes));
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

	public void showEmptyMessage(String emptyMessage) {
		mListView.setAdapter(null);
		mEmptyLayout.setEmptyMessage(emptyMessage);
		mEmptyLayout.setShowEmptyButton(false);
		mEmptyLayout.showEmpty();
	}

}
