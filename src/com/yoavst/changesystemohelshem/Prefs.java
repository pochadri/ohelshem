package com.yoavst.changesystemohelshem;

import org.androidannotations.annotations.sharedpreferences.DefaultBoolean;
import org.androidannotations.annotations.sharedpreferences.DefaultInt;
import org.androidannotations.annotations.sharedpreferences.DefaultLong;
import org.androidannotations.annotations.sharedpreferences.SharedPref;

/**
 * The interface that the app use to connect the shared preferences. Annoated by
 * AndroidAnnotations.
 * 
 * @author Yoav Sternberg
 * 
 */
@SharedPref(value = SharedPref.Scope.UNIQUE)
public interface Prefs {

	/**
	 * True if the user want to get notification on 21:05 daily.
	 */
	@DefaultBoolean(false)
	boolean getNotification();

	/**
	 * The student's layer.
	 */
	@DefaultInt(9)
	int getLayer();

	/**
	 * The student's class number.
	 */
	@DefaultInt(4)
	int getMotherClass();

	/**
	 * Last day the user got notification.
	 */
	@DefaultLong(0)
	long getLastNotificationDay();

	/**
	 * The changes of the nine layer cached.
	 */
	String getJsonOfLayerNine();

	/**
	 * The changes of the ten layer cached.
	 */
	String getJsonOfLayerTen();

	/**
	 * The changes of the eleven layer cached.
	 */
	String getJsonOfLayerEleven();

	/**
	 * The changes of the twelve layer cached.
	 */
	String getJsonOfLayerTwelve();

	/**
	 * The day of the cached json of layer nine in miliseconds.
	 */
	long LayerNineUpdatedFrom();

	/**
	 * The day of the cached json of layer ten in miliseconds.
	 */
	long LayerTenUpdatedFrom();

	/**
	 * The day of the cached json of layer eleven in miliseconds.
	 */
	long LayerElevenUpdatedFrom();

	/**
	 * The day of the cached json of layer twelve in miliseconds.
	 */
	long LayerTwelveUpdatedFrom();

	/**
	 * The time of the cached json of layer nine in miliseconds.
	 */
	long LayerNineUpdatedWhen();

	/**
	 * The time of the cached json of layer ten in miliseconds.
	 */
	long LayerTenUpdatedWhen();

	/**
	 * The time of the cached json of layer eleven in miliseconds.
	 */
	long LayerElevenUpdatedWhen();

	/**
	 * The time of the cached json of layer twelve in miliseconds.
	 */
	long LayerTwelveUpdatedWhen();

}
