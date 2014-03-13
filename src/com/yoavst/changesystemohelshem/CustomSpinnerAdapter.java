package com.yoavst.changesystemohelshem;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

/**
 * This spinner enable to show the class name currently on versions 2.3-4.0
 * using 2 textViews instead of one.
 * 
 * @author Yoav Sternberg
 */
public class CustomSpinnerAdapter extends BaseAdapter implements SpinnerAdapter {
	/**
	 * The class letter like "ט"
	 */
	String mClassName;
	String mDelimiter = "\'";
	/**
	 * Number of classes in the layer
	 */
	int mClassLength;
	Context mContext;

	public CustomSpinnerAdapter(Context context, String className,
			int classLength) {
		mClassName = className;
		mClassLength = classLength;
		mContext = context;
	}

	@Override
	public int getCount() {
		return mClassLength;
	}

	/**
	 * Since you have the position, the method return the class name.
	 * {@inheritDoc}
	 */
	@Override
	public Object getItem(int position) {
		return mClassName;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	/**
	 * Inflating the view that always shown
	 */
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// Inflate the Custom laout
		LinearLayout layout = (LinearLayout) View.inflate(mContext,
				R.layout.spinner_dropdown_title, null);
		TextView className = (TextView) layout.findViewById(android.R.id.text1);
		TextView classNumber = (TextView) layout
				.findViewById(android.R.id.text2);
		// If RTL problem was fixed
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
			// Show also the delimiter
			className.setText(mClassName + mDelimiter);
		} else {
			// Show only the layer letter
			className.setText(mClassName);
		}
		// Set class number
		classNumber.setText("" + (position + 1));
		return layout;
	}

	/**
	 * Inflating the drop down items
	 */
	@Override
	public View getDropDownView(int position, View convertView, ViewGroup parent) {
		// Inflate the Custom laout
		LinearLayout layout = (LinearLayout) View.inflate(mContext,
				R.layout.spinner_dropdown_item, null);
		TextView className = (TextView) layout.findViewById(android.R.id.text1);
		TextView classNumber = (TextView) layout
				.findViewById(android.R.id.text2);
		// If RTL problem was fixed
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
			// Show also the delimiter
			className.setText(mClassName + mDelimiter);
		} else {
			// Show only the layer letter
			className.setText(mClassName);
		}
		// Set class number
		classNumber.setText("" + (position + 1));
		return layout;
	}

}