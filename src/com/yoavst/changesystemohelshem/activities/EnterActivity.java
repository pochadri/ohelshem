package com.yoavst.changesystemohelshem.activities;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.sharedpreferences.Pref;

import android.app.Activity;

import com.yoavst.changesystemohelshem.Prefs_;
import com.yoavst.changesystemohelshem.R;
/**
 * The entering class. Navigate you to the setup Activity or to the Main
 * Activity.
 * 
 * @author Yoav Sternberg
 */
@EActivity(R.layout.empty)
public class EnterActivity extends Activity {
	@Pref
	Prefs_ mPrefs;

	@AfterViews
	void chooseActivity() {
		// Disable animation
		overridePendingTransition(0, 0);
		// If user's layer or user's class are missing
		if (!mPrefs.getLayer().exists() || !mPrefs.getMotherClass().exists()
				|| mPrefs.getLayer().getOr(0) < 9
				|| mPrefs.getLayer().getOr(0) > 12
				|| mPrefs.getMotherClass().getOr(0) < 1
				|| mPrefs.getMotherClass().getOr(0) > 13) {
			// Open setup Activity
			WizardActivity_.intent(this).mIsSettings(false).start();
			finish();
		} else {
			// Open Main Activity
			MainActivity_.intent(this).start();
			finish();
		}
	}

	/**
	 * Overriding finsh so it won't show any animation on finish
	 */
	@Override
	public void finish() {
		// Disable animation
		overridePendingTransition(0, 0);
		super.finish();
	}
}
