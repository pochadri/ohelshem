package com.yoavst.changesystemohelshem;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * The adapter for the ListView that show the changes
 * 
 * @author Yoav Sternberg
 * 
 */
public class ChangesListViewAdapter extends ArrayAdapter<ChangeObject> {
	Context mContext;
	ChangeObject[] values;

	public ChangesListViewAdapter(Context context, ChangeObject[] values) {
		super(context, R.layout.listview_view, values);
		this.mContext = context;
		this.values = values;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder;
		if (convertView == null) {
			LayoutInflater inflater = (LayoutInflater) mContext
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(R.layout.listview_view, parent,
					false);
			viewHolder = new ViewHolder();
			viewHolder.colorBorder = (ImageView) convertView
					.findViewById(R.id.colorBorder);
			viewHolder.lessonNumber = (TextView) convertView
					.findViewById(R.id.time);
			viewHolder.change = (TextView) convertView
					.findViewById(R.id.change);
			viewHolder.lesson = (TextView) convertView
					.findViewById(R.id.lesson);
			viewHolder.lessonDate = (TextView) convertView
					.findViewById(R.id.timedate);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		String noChanges = mContext.getResources().getString(
				R.string.no_changes);
		String lessonNumber = mContext.getResources().getString(
				R.string.lesson_number);
		viewHolder.colorBorder.setBackgroundColor(values[position].getColor());
		viewHolder.lessonNumber.setText(lessonNumber + " "
				+ values[position].getLessonNumber());
		viewHolder.lessonDate.setText(values[position].getStartTime() + " - "
				+ values[position].getEndTime());
		if (values[position].getChangeText().equals("-"))
			viewHolder.change.setText(noChanges);
		else
			viewHolder.change.setText(values[position].getChangeText());
		viewHolder.lesson.setText(values[position].getLesson());
		return convertView;
	}

	/**
	 * Using viewHolder pattern
	 */
	static class ViewHolder {
		TextView lessonNumber;
		TextView change;
		TextView lesson;
		TextView lessonDate;
		ImageView colorBorder;
	}

}
