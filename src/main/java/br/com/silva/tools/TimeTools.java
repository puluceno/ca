package br.com.silva.tools;

import java.text.SimpleDateFormat;
import java.util.Date;

public class TimeTools {

	public static String formatTime(int elapsed) {
		int ss = elapsed % 60;
		elapsed /= 60;
		int min = elapsed % 60;
		elapsed /= 60;
		int hh = elapsed % 24;
		return strZero(hh) + ":" + strZero(min) + ":" + strZero(ss);
	}

	private static String strZero(int n) {
		if (n < 10)
			return "0" + String.valueOf(n);
		return String.valueOf(n);
	}

	public static String formatDateTime(Date date) {
		if (date != null)
			return getDateOnlyFormat().format(date);
		return null;
	}

	public static SimpleDateFormat getDateOnlyFormat() {
		return new SimpleDateFormat("dd/MM/yyyy");
	}

	public static String convertDate(String dateYMD) {
		if (dateYMD.contains("T")) {
			dateYMD.substring(0, dateYMD.indexOf("T"));
			String y = dateYMD.substring(0, 4);
			String m = dateYMD.substring(5, 7);
			String d = dateYMD.substring(8, 10);
			return d + "/" + m + "/" + y;
		} else {
			return dateYMD;
		}
	}

	/**
	 * 
	 * @param date1
	 * @param date2
	 * @return true if date1 is bigger than date2. False if the opposite or
	 *         equals
	 */
	public static boolean compareStringDates(String date1, String date2) {
		String[] period1 = date1.split("/");
		String[] period2 = date2.split("/");
		// Compare year
		if (Integer.valueOf(period1[2]) > Integer.valueOf(period2[2]))
			return true;

		// Compare month
		if (Integer.valueOf(period1[2]).equals(Integer.valueOf(period2[2]))
				&& Integer.valueOf(period1[1]) > Integer.valueOf(period2[1]))
			return true;

		// Compare day
		if (Integer.valueOf(period1[2]).equals(Integer.valueOf(period2[2]))
				&& Integer.valueOf(period1[1]).equals(Integer.valueOf(period2[1]))
				&& Integer.valueOf(period1[0]) >= Integer.valueOf(period2[0]))
			return true;

		return false;
	}
}
