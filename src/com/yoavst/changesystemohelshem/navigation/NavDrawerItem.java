package com.yoavst.changesystemohelshem.navigation;

/**
 * The item on the navigation drawer that support titles and count (was taken
 * from my another app - Hebrew Jokes).
 * 
 * @author Yoav Sternberg
 * 
 */
public class NavDrawerItem {

	private String title;
	private int icon;
	private String count = "0";
	// boolean to set visiblity of the counter
	private boolean isCounterVisible = false;
	// boolean to select if the item is header or regular item
	private boolean isHeader = false;

	public NavDrawerItem(String title, int icon) {
		this.title = title;
		this.icon = icon;
	}

	public NavDrawerItem(String title, boolean header) {
		this.title = title;
		this.isHeader = header;
	}

	public NavDrawerItem(String title, int icon, boolean isCounterVisible,
			String count) {
		this.title = title;
		this.icon = icon;
		this.isCounterVisible = isCounterVisible;
		this.count = count;
	}

	public String getTitle() {
		return this.title;
	}

	public int getIcon() {
		return this.icon;
	}

	public String getCount() {
		return this.count;
	}

	public boolean getCounterVisibility() {
		return this.isCounterVisible;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void setIcon(int icon) {
		this.icon = icon;
	}

	public void setCount(String count) {
		this.count = count;
	}

	public void setCounterVisibility(boolean isCounterVisible) {
		this.isCounterVisible = isCounterVisible;
	}

	public boolean isHeader() {
		return isHeader;
	}
}
