package com.yoavst.changesystemohelshem.activities;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.App;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.StringArrayRes;

import android.os.Bundle;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

import com.actionbarsherlock.app.SherlockFragment;
import com.tooleap.sdk.TooleapActivities.Sherlock.SherlockFragmentActivity;
import com.yoavst.changesystemohelshem.MyApp;
import com.yoavst.changesystemohelshem.R;
import com.yoavst.changesystemohelshem.fragments.ChangesFragment_;
import com.yoavst.changesystemohelshem.views.BetterSegmentedGroup;

@EActivity(R.layout.activity_floating)
public class FloatingActivity extends SherlockFragmentActivity implements
		OnCheckedChangeListener {
	@StringArrayRes(R.array.layers)
	String[] mLayers;
	@ViewById(R.id.segmentedclassfloating)
	BetterSegmentedGroup mSegmentedGroup;
	@ViewById(R.id.hoscrollviewfloating)
	HorizontalScrollView mHorizontalScrollView;
	@App
	MyApp mApp;
	int mLayer;
	int mClass;
	int mUserClass;
	int[] mSelectClassIds = new int[] { R.id.radioclass1, R.id.radioclass2,
			R.id.radioclass3, R.id.radioclass4, R.id.radioclass5,
			R.id.radioclass6, R.id.radioclass7, R.id.radioclass8,
			R.id.radioclass9, R.id.radioclass10, R.id.radioclass11,
			R.id.radioclass12 };

	@Override
	public void onCreate(Bundle savedBundle) {
		super.onCreate(savedBundle);
		setTheme(R.style.Theme_Sherlock_Light);
	}

	@AfterViews
	void init() {
		mLayer = mApp.getPreferences().getLayer().get();
		mUserClass = mApp.getPreferences().getMotherClass().get();
		mClass = mUserClass;
		mSegmentedGroup.setOnCheckedChangeListener(this);
		mSegmentedGroup.check(mSelectClassIds[mClass - 1]);
		mHorizontalScrollView.post(new Runnable() {
			@Override
			public void run() {
				mHorizontalScrollView.scrollTo(
						findViewById(mSegmentedGroup.getCheckedRadioButtonId())
								.getRight() - 200, mHorizontalScrollView
								.getTop());
			}
		});
		inflateClasses(mApp.getPreferences().getLayer().get());
	}

	@Override
	public void onResume() {
		super.onResume();
		if (mLayer != mApp.getPreferences().getLayer().get()
				|| mUserClass != mApp.getPreferences().getMotherClass().get())
			init();
	}

	private void inflateClasses(int layer) {
		String mLayer = mLayers[layer - 9];
		int i = 1;
		for (int id : mSelectClassIds) {
			RadioButton radioButton = (RadioButton) findViewById(id);
			radioButton.setText(mLayer + i);
			i++;
		}
		if (layer != 9) {
			findViewById(mSelectClassIds[mSelectClassIds.length - 1])
					.setVisibility(View.GONE);
			mSegmentedGroup.updateBackground();
		}
	}

	private void showChanges() {
		// The fragment's builder
		ChangesFragment_.FragmentBuilder_ fragmentBuilder = ChangesFragment_
				.builder();
		// Insert the user's layer and class
		fragmentBuilder.mLayer(mLayer).mClass(mClass);
		SherlockFragment mFragment = fragmentBuilder.build();
		getSupportFragmentManager().beginTransaction()
				.replace(R.id.content_frame_floating, mFragment).commit();
	}

	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		mClass = Integer.parseInt((String) findViewById(checkedId).getTag());
		showChanges();
	}
}
