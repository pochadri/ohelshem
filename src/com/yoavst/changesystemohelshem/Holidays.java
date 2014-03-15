package com.yoavst.changesystemohelshem;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

/**
 * The dates of holdays until the end of this academic year. Used for check if
 * tomorrow is a holiday for the notification.
 * 
 * @author Yoav Sternberg
 */
public class Holidays {
	/**
	 * All the holidays
	 */
	private ArrayList<Holiday> mHolidays = new ArrayList<Holiday>();
	/**
	 * The date format of the holidays
	 */
	private SimpleDateFormat mDataFormat = new SimpleDateFormat("dd/MM/yyyy",
			Locale.ROOT);

	/**
	 * Add the holidays
	 */
	public Holidays() {
		mHolidays.add(new Holiday("15/03/2014", "17/03/2014")); // Purim 2014
		mHolidays.add(new Holiday("06/04/2014", "22/04/2014")); // Passover 2014
		mHolidays.add(new Holiday("06/05/2014")); // Yom Ha'atzmaut 2014
		mHolidays.add(new Holiday("19/05/2014")); // Lag BaOmer 2014
		mHolidays.add(new Holiday("03/05/2014", "06/05/2014")); // Shavuot 2014
		mHolidays.add(new Holiday("21/06/2014", "26/08/2014")); // Summer
																// vacation 2014
	}

	/**
	 * @return true if tomorrow is holiday (please!)
	 */
	public boolean isTomorrowHoliday() {
		try {
			// Create calander
			Calendar calendar = new GregorianCalendar();
			// Add one day
			calendar.add(Calendar.DAY_OF_MONTH, 1);
			long tomorrowTime = getDayOnly(calendar).getTimeInMillis();
			for (Holiday holiday : mHolidays) {
				if (holiday.isOneDay()) {
					Calendar cal = new GregorianCalendar();
					cal.setTime((mDataFormat.parse(holiday.getStartDay())));
					/*
					 * If tomorrow is the same day as the one time holiday
					 * Because it parse the date, it come with no hours,
					 * minutes... like our calendar, so they can be equals.
					 */
					if (tomorrowTime == cal.getTimeInMillis()) {
						// Tomorrow is officialy holiday
						return true;
					}
				} else {
					Calendar startCal = new GregorianCalendar();
					startCal.setTime((mDataFormat.parse(holiday.getStartDay())));
					Calendar endCal = new GregorianCalendar();
					endCal.setTime((mDataFormat.parse(holiday.getEndDay())));
					/*
					 * If tomorrow is between or is the start and the end day
					 * then tomorrow is holiday.
					 */
					if (tomorrowTime >= startCal.getTimeInMillis()
							&& tomorrowTime <= endCal.getTimeInMillis()) {
						// Tomorrow is officialy holiday
						return true;
					}
				}
			}
		} catch (ParseException e) {
			// Oh no! something imposible happend!
			e.printStackTrace();
		}
		// Tomorrow... Isn't... Holiday! :|
		return false;
	}

	/**
	 * @return Calander without the hours, minutes, seconds and miliseconds
	 */
	public Calendar getDayOnly(Calendar cal) {
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		return cal;
	}

	public class Holiday {
		/**
		 * The first day of the holiday.
		 */
		private String mStartDay;
		/**
		 * The last day of the holiday.
		 */
		private String mEndDay;
		/**
		 * True if one day holiday.
		 */
		private boolean mOneDayHoliday = false;

		public Holiday(String startDay, String endDay) {
			mStartDay = startDay;
			mEndDay = endDay;
		}

		/**
		 * One day holiday like Yom Ha'atzmaut
		 */
		public Holiday(String oneDay) {
			mStartDay = oneDay;
			mEndDay = oneDay;
			mOneDayHoliday = true;
		}

		/**
		 * @return The first day of the holiday
		 */
		public String getStartDay() {
			return mStartDay;
		}

		/**
		 * @return The last day of the holiday
		 */
		public String getEndDay() {
			return mEndDay;
		}

		/**
		 * @return true if the the holiday is one day only
		 */
		public boolean isOneDay() {
			return mOneDayHoliday;
		}
	}
}
