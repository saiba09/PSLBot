package com.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;

public class Formator {
	private static final Logger log = Logger.getLogger(Formator.class.getName());
	public static String getFormatedDate(String dateString){
		Date date = new Date(dateString);
		return getFormatedDate(date);
	}
	public static String getFormatedDate(Date date){

		String fDate = new SimpleDateFormat("MMM d").format(date);
		log.info("formated Date : "+fDate);
		return fDate;
	}
	}

