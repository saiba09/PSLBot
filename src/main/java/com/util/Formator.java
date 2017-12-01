package com.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;

public class Formator {
	private static final Logger log = Logger.getLogger(Formator.class.getName());
	public static String getFormatedDate(String dateString){
		Date date = null;
		try {
			date = new SimpleDateFormat("yyyy-MM-dd").parse(dateString);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return getFormatedDate(date);
	}
	public static String getFormatedDate(Date date){

		String fDate = new SimpleDateFormat("MMM d").format(date);
		log.info("formated Date : "+fDate);
		return fDate;
	}
	}

