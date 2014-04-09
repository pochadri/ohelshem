package com.yoavst.changesystemohelshem.navigation;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.yoavst.changesystemohelshem.R;

/**
 * The adapter for the drawer navigation ListView (was taken from my another app
 * - Hebrew Jokes).
 * 
 * @author Yoav Sternberg
 * 
 */
public class NavDrawerListAdapter extends BaseAdapter {

	private Context mContext;
	/**
	 * The items
	 */
	private ArrayList<NavDrawerItem> mNavDrawerItems;

	public NavDrawerListAdapter(Context context,
			ArrayList<NavDrawerItem> navDrawerItems) {
		this.mContext = context;
		this.mNavDrawerItems = navDrawerItems;
	}

	@Override
	public int getCount() {
		return mNavDrawerItems.size();
	}

	@Override
	public Object getItem(int position) {
		return mNavDrawerItems.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public int getViewTypeCount() {
		return 2;
	}

	@Override
	public int getItemViewType(int position) {
		return ((NavDrawerItem) getItem(position)).isHeader() ? 0 : 1;
	}

	@Override
	public boolean isEnabled(int position) {
		return ((NavDrawerItem) getItem(position)).isHeader() ? false : true;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder;
		if (convertView == null) {
			LayoutInflater mInflater = (LayoutInflater) mContext
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = mInflater
					.inflate(R.layout.drawer_line, parent, false);
			viewHolder = new ViewHolder();
			viewHolder.icon = (ImageView) convertView.findViewById(R.id.image);
			viewHolder.title = (TextView) convertView.findViewById(R.id.text);
			viewHolder.count = (TextView) convertView.findViewById(R.id.count);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}

		viewHolder.icon.setImageResource(mNavDrawerItems.get(position)
				.getIcon());
		viewHolder.title.setText(mNavDrawerItems.get(position).getTitle());

		// displaying count
		// check whether it set visible or not
		if (mNavDrawerItems.get(position).getCounterVisibility()) {
			viewHolder.count.setText(mNavDrawerItems.get(position).getCount());
		} else {
			// hide the counter view
			viewHolder.count.setVisibility(View.GONE);
		}
		return convertView;
	}

	static class ViewHolder {
		ImageView icon;
		TextView title;
		TextView count;
	}

}
