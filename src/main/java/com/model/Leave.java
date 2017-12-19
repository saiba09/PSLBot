package com.model;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;

public class Leave {
	private static final Logger log = Logger.getLogger(Leave.class.getName());

	String startDate, endDate;
	String reason;

	public Leave(String startDate, String endDate, String reason) {
		SimpleDateFormat formator = new SimpleDateFormat("dd-MMM-yyyy");
		try {
			Date start = new SimpleDateFormat("yyyy-MM-dd").parse(startDate);
			Date end = new SimpleDateFormat("yyyy-MM-dd").parse(endDate);
			this.startDate = formator.format(start);
			this.endDate = formator.format(end);
		} catch (ParseException e) {
			log.severe("Exception parsing while creating leave object");
		}

	}

	public String getStartDate() {
		return startDate;
	}

	public String getStartDate(String format) { //
		SimpleDateFormat formator = new SimpleDateFormat("dd-MMM-yyyy");
		SimpleDateFormat newFormat = new SimpleDateFormat(format);
		String result = null;
		try {
			Date start = formator.parse(startDate);
			result = newFormat.format(start);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			log.severe("Exception parsing " + e);
		}
		return result;
	}
	public String getEndDate() {
		return endDate;
	}

	public String getEndDate(String format) { //
		SimpleDateFormat formator = new SimpleDateFormat("dd-MMM-yyyy");
		SimpleDateFormat newFormat = new SimpleDateFormat(format);
		String result = null;
		try {
			Date end = formator.parse(endDate);
			result = newFormat.format(end);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			log.severe("Exception parsing " + e);
		}
		return result;
	}
}
