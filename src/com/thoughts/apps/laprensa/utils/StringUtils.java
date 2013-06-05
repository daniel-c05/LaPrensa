package com.thoughts.apps.laprensa.utils;

import java.util.Calendar;

/**
 * Helper class to provide different variants of a date
 *
 */
public class StringUtils {	
	
	private static final CharSequence SMALL_IMAGE = "120x90";
	private static final CharSequence LARGE_IMAGE = "600x400";

	/**
	 * 
	 * Get today'date as a String in the format: /YYYY/MM/DD/
	 * 
	 * @see #getCustomDate(int, int, int)
	 * 
	 * @return The formatted date string.
	 */
	public static String getTodaysDate () {
		final Calendar c = Calendar.getInstance();
		int year = c.get(Calendar.YEAR);
		int month = c.get(Calendar.MONTH);
		int day = c.get(Calendar.DAY_OF_MONTH);
		return getCustomDate(year, month, day);		
	}

	/**
	 * 
	 * Get a date as a String in the format: /YYYY/MM/DD/
	 * This is especially important to query www.laprensa.com.ni for specific date articles.
	 * 
	 * @param year The year. 
	 * @param month The month, which must be zero-indexed: i.e January is 0. 
	 * @param day The day.
	 * @return The formatted date string.
	 */
	public static String getCustomDate (int year, int month, int day) {
		String monthStr = String.valueOf(month + 1);
		String dayStr = String.valueOf(day);
		if (monthStr.length() == 1) {
			monthStr = "0" + monthStr;
		}
		if (dayStr.length() == 1) {
			dayStr = "0" + dayStr;
		}
		return "/" + year + "/" + monthStr + "/" + dayStr + "/";
	}
		
	/**
	 * 
	 * Extract a Date String from a Url. 
	 * 
	 * @param link The Url you want to extract the Date String from. Links supplied <b>must</b> be similar to: "http://www.laprensa.com.ni/YYYY/MM/DD/portada" 
	 * @return A Date String, formatted as /YYYY/MM/DD/
	 */
	public static String getCustomDate(String link) {
		link = link.replace(HtmlHelper.WEB_PREFIX, "");
		return link.substring(0, 12);		
	}

	/**
	 * Get today'date as a String in the format: D-M-YYYY.
	 * @see #getReadableDate(int, int, int)
	 * @return
	 */
	public static String getTodaysReadableDate() {
		final Calendar c = Calendar.getInstance();
		int year = c.get(Calendar.YEAR);
		int month = c.get(Calendar.MONTH);
		int day = c.get(Calendar.DAY_OF_MONTH);
		return getReadableDate(year, month, day);
	}

	/**
	 * 
	 * Get a date as a String in the format: D-M-YYYY. 
	 * This is used as subtitle in the actionbar.
	 * 
	 * @param year The year. 
	 * @param month The month, which must be zero-indexed: i.e January is 0.
	 * @param day The day.
	 * @return The formatted date string.
	 */
	public static String getReadableDate(int year, int month, int day) {	
		String monthStr = String.valueOf(month + 1);
		String dayStr = String.valueOf(day);
		if (monthStr.length() == 1) {
			monthStr = "0" + monthStr;
		}
		if (dayStr.length() == 1) {
			dayStr = "0" + dayStr;
		}
		return dayStr + "-" + monthStr + "-" + year;
	}

	/**
	 * 
	 * Extract a Date String from a Url. 
	 * 
	 * @see #getGregorianDate(String)
	 * 
	 * @param link The Url you want to extract the Date String from. Links supplied <b>must</b> be similar to: "http://www.laprensa.com.ni/YYYY/MM/DD/section" 
	 * @return A Date String, formatted as DD-MM-YYYY
	 */
	public static String getReadableDate (String link) {
		link = getCustomDate(link);
		return reverseString(getGregorianDate(link));		
	}
	
	private static String reverseString (String string) {
		char[] chars = string.toCharArray();
		StringBuilder sBuilder = new StringBuilder();
		for (int i = 8; i <= 9; i++) {
			sBuilder.append(chars[i]);
		}		
		for (int i = 4; i <= 7; i++) {
			sBuilder.append(chars[i]);
		}
		for (int i = 0; i <= 3; i++) {
			sBuilder.append(chars[i]);
		}
		return sBuilder.toString();
	}

	/**
	 * Convert a Date String formatted as /YYYY/MM/DD/ to a String in the format: YYYY-MM-DD. 
	 * This is used as subtitle in the actionbar.
	 * 
	 * @param customDate The Custom Date String formatted as /YYYY/MM/DD/, you can pass null to have today's date returned. 
	 * @return A Date String, formatted as YYYY-MM-DD.
	 */
	public static String getGregorianDate (String customDate) {
		if (customDate != null) {
			//If a date we can transform is supplied, convert it. 
			customDate = customDate.substring(1, customDate.length() -1);
			return customDate = customDate.replace('/', '-');
		}
		//No date is supplied, then return today
		final Calendar c = Calendar.getInstance();
		int year = c.get(Calendar.YEAR);
		int month = c.get(Calendar.MONTH);
		int day = c.get(Calendar.DAY_OF_MONTH);

		String monthStr = String.valueOf(month + 1);
		String dayStr = String.valueOf(day);
		if (monthStr.length() == 1) {
			monthStr = "0" + monthStr;
		}
		if (dayStr.length() == 1) {
			dayStr = "0" + dayStr;
		}
		return year + "-" + monthStr + "-" + dayStr;

	}
	
	/**
	 * Some links from La Prensa are limited to preview sizes of 120*90 res. 
	 * This helps you replace the size, to show images in 600*400. 
	 *  
	 * @param link The link of the image to expand.
	 * @return The new link to the image in a higher resolution.
	 */
	public static String hackImage(String link) {
		if (link.contains(SMALL_IMAGE)) {
			link = link.replace(SMALL_IMAGE, LARGE_IMAGE);
		}		
		return link;
	}


}