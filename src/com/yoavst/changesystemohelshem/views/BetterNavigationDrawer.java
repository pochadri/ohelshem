package com.yoavst.changesystemohelshem.views;

import org.arasthel.googlenavdrawermenu.adapters.GoogleNavigationDrawerAdapter;

import com.yoavst.changesystemohelshem.R;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v4.widget.DrawerLayout;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

public class BetterNavigationDrawer extends DrawerLayout {

	private NavigationDrawerOptions mOptions;
	private ListView mListView;
	private OnNavigationSectionSelected mSelectionListener;
	private DrawerLayout.DrawerListener drawerToggle;
	private static int checkPosition = 0;

	public BetterNavigationDrawer(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
	}

	public BetterNavigationDrawer(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public BetterNavigationDrawer(Context context) {
		super(context);
	}

	public void setOptions(NavigationDrawerOptions options) {
		this.mOptions = options;
	}

	@Override
	public void setDrawerListener(
			DrawerLayout.DrawerListener actionBarDrawerToggle) {
		super.setDrawerListener(actionBarDrawerToggle);
		this.drawerToggle = actionBarDrawerToggle;
	}

	private void configureList() {
		LayoutInflater inflater = (LayoutInflater) getContext()
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mListView = (ListView) inflater.inflate(R.layout.navigation_list, this,
				false);
		if (mOptions != null) {
			mListView.setItemsCanFocus(mOptions.canItemFocus());
			((DrawerLayout.LayoutParams) mListView.getLayoutParams()).gravity = gravityToGravityInt(mOptions
					.getDrawerGravity());
			if (mOptions.getHeaderView() != null) {
				mListView.addHeaderView(mOptions.getHeaderView(), null,
						mOptions.isHeaderClickable());
			}
			if (mOptions.getFooterView() != null) {
				mListView.addFooterView(mOptions.getFooterView(), null,
						mOptions.isFooterClickable());
			}
		} else {
			((DrawerLayout.LayoutParams) mListView.getLayoutParams()).gravity = gravityToGravityInt(Gravity.Left);
		}
		addView(mListView);
	}

	public void setListViewSections() {
		if (mListView == null) {
			configureList();
		}
		if (mOptions != null) {
			GoogleNavigationDrawerAdapter adapter = new GoogleNavigationDrawerAdapter(
					getContext(), mOptions.getMainSectionsEntries(),
					mOptions.getSecondarySectionsEntries(),
					mOptions.getMainSectionsDrawables(),
					mOptions.getSecondarySectionsDrawables());
			mListView.setAdapter(adapter);
			if (checkPosition != 0)
				check(checkPosition);
			else if (mOptions.getHeaderView() != null
					&& mOptions.isHeaderClickable()) {
				check(0);
			} else {
				check(1);
			}
		}
	}

	public void setOnNavigationSectionSelected(
			OnNavigationSectionSelected listener) {
		mSelectionListener = listener;
		if (mSelectionListener != null && mListView != null) {
			mListView
					.setOnItemClickListener(new AdapterView.OnItemClickListener() {
						@Override
						public void onItemClick(AdapterView<?> adapterView,
								View view, int i, long l) {

							if (mOptions.getHeaderView() != null
									&& !mOptions.isHeaderClickable() && i == 0) {
								return;
							}

							if (mOptions.getFooterView() != null
									&& !mOptions.isFooterClickable()
									&& i == mListView.getCount() - 1) {
								return;
							}
							check(i);
							mSelectionListener.onSectionSelected(view, i, l);
							closeDrawerMenu();
						}
					});
		} else {
			mListView.setOnItemClickListener(null);
		}
	}

	/**
	 * Helper to access the isDrawerOpen method of the ListView menu.
	 * 
	 * @return whether it's opened or not
	 */
	public boolean isDrawerMenuOpen() {
		return super.isDrawerOpen(mListView);
	}

	/**
	 * Helper to open the ListView menu.
	 */
	public void openDrawerMenu() {
		super.openDrawer(mListView);
	}

	/**
	 * Helper to close the ListView menu.
	 */
	public void closeDrawerMenu() {
		super.closeDrawer(mListView);
	}

	public void check(int position) {
		mListView.setItemChecked(checkPosition, false);
		mListView.setItemChecked(position, true);
		checkPosition = position;
	}

	public interface OnNavigationSectionSelected {

		public void onSectionSelected(View v, int i, long l);

	}

	public static class NavigationDrawerOptions {
		private Gravity drawerGravity = Gravity.Left;
		private String[] mainSectionsEntries;
		private int[] mainSectionsDrawables;
		private String[] SecondarySectionsEntries;
		private int[] SecondarySectionsDrawables;
		private View HeaderView;
		private boolean isHeaderClickable = true;
		private View FooterView;
		private boolean isFooterClickable = true;
		private boolean canItemFocus = true;

		public NavigationDrawerOptions setGravity(Gravity gravity) {
			drawerGravity = gravity;
			return this;
		}

		public NavigationDrawerOptions setMainSectionsEntries(String[] entries) {
			mainSectionsEntries = entries;
			return this;
		}

		public NavigationDrawerOptions setMainSectionsDrawables(int[] drawables) {
			mainSectionsDrawables = drawables;
			return this;
		}

		public NavigationDrawerOptions setSecondaryEntries(String[] entries) {
			SecondarySectionsEntries = entries;
			return this;
		}

		public NavigationDrawerOptions setSecondarySectionsDrawables(
				int[] drawables) {
			SecondarySectionsDrawables = drawables;
			return this;
		}

		public NavigationDrawerOptions setHeaderView(View headerView,
				boolean clickable) {
			HeaderView = headerView;
			isHeaderClickable = clickable;
			return this;
		}

		public NavigationDrawerOptions setFooterView(View footerView,
				boolean clickable) {
			FooterView = footerView;
			isFooterClickable = clickable;
			return this;
		}

		public NavigationDrawerOptions canItemFocus(boolean focusable) {
			canItemFocus = focusable;
			return this;
		}

		public Gravity getDrawerGravity() {
			return drawerGravity;
		}

		public String[] getMainSectionsEntries() {
			return mainSectionsEntries;
		}

		public int[] getMainSectionsDrawables() {
			return mainSectionsDrawables;
		}

		public String[] getSecondarySectionsEntries() {
			return SecondarySectionsEntries;
		}

		public int[] getSecondarySectionsDrawables() {
			return SecondarySectionsDrawables;
		}

		public View getHeaderView() {
			return HeaderView;
		}

		public boolean isHeaderClickable() {
			return isHeaderClickable;
		}

		public View getFooterView() {
			return FooterView;
		}

		public boolean isFooterClickable() {
			return isFooterClickable;
		}

		public boolean canItemFocus() {
			return canItemFocus;
		}
	}

	public enum Gravity {
		Left, Right, Start, End
	}

	@SuppressLint("InlinedApi")
	public static int gravityToGravityInt(Gravity gravity) {
		switch (gravity) {
		default:
		case Left:
			return android.view.Gravity.LEFT;
		case Right:
			return android.view.Gravity.RIGHT;
		case Start:
			return android.view.Gravity.START;
		case End:
			return android.view.Gravity.END;
		}
	}

	public DrawerLayout.DrawerListener getDrawerToggle() {
		return drawerToggle;
	}

}
