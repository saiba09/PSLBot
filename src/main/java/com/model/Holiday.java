package com.model;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;

public class Holiday {
	private static final Logger log = Logger.getLogger(Holiday.class.getName());

	String date;
	String name;
	public Holiday(String date, String reason) {
		SimpleDateFormat formator = new SimpleDateFormat("dd-MMM-yyyy");
		try {
			Date start = new SimpleDateFormat("yyyy-MM-dd").parse(date);
			this.date = formator.format(start);
		} catch (ParseException e) {
			log.severe("Exception parsing while creating leave object");
		}
		this.name = reason;
	}
	
	
}
