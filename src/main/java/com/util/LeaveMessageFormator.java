package com.util;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import org.json.simple.JSONObject;

import com.model.User;

public class LeaveMessageFormator {
	private static final Logger log = Logger.getLogger(LeaveMessageFormator.class.getName());

 public static String getLeaveDetailMessage(User user){
	 log.info("get leave detail msg");
		Boolean prev = false;
		String message ="";
		float PL = user.getPrivilagedLeave();
		float OH = user.getOptionalHoliday();
		float OL = user.getOptionalLeave();
		float CF = user.getCompensatioryOff();
		if (PL != 0 || CF != 0 || OH != 0 || OL != 0) {
			message += "You have ";
		
		if (PL != 0 ) {
			message += PL + " privilaged leaves";
			prev = true;
		}
		if (CF != 0) {
			if (prev) {
				message += ", ";
			}
			message += CF + " compensatory off.";
			prev = true;
		}
		if (OH != 0) {
			if (prev) {
				message += ", ";
			}
			message += OH + " optional holiday";
			prev = true;
		}
		if (OL != 0) {
			if (prev) {
				message += ", ";
			}
			message += OL + " optional leave";
		}
		message +=".";
		}
	 return message;
 }
 public static JSONObject getMessageForFestival(String event, String date, String comment) {
		// TODO Auto-generated method stub
		log.info("inside get message");
		JSONObject response = new JSONObject();
		String longVaccSugestion = "Do you want to make it a long vaccation?";
		String message = "";
		Boolean isHoliday = false;
		Boolean isOneDay = false;
		Boolean isFestival = false;
	
		try {
			String holidays = PropertyLoader.getList("INDIA_HOLIDAY");
			log.info("INDIA_HOLIDAY loaded : " + holidays);
			String[] arrayHolidays = holidays.split(",");
			List<String> listOfHoliday = Arrays.asList(arrayHolidays);
			for (String holiday : listOfHoliday) {
				if (holiday.equalsIgnoreCase(event)) {
					message += "Its holiday on " + date + " for " + event + ". ";
					isHoliday = true;
					isFestival = true;
					break;
				}
			}
			String festivals = PropertyLoader.getList("INDIA_OCCASSION");
			log.info("festivals loaded " + festivals);
			String[] arrayFestivals = festivals.split(",");
			List<String> listOfFestival = Arrays.asList(arrayFestivals);
			for (String festival : listOfFestival) {
				if (festival.equalsIgnoreCase(event)) {
					message += "Oh! Great so you want to apply leave for on "
							+ Formator.getFormatedDate(date) + ". ";
					isFestival = true;
					break;
				}
			}
			if (!isFestival) {
				message += "You want to apply leave on " + date;
				if (event.equalsIgnoreCase("today")) {
					comment += " as you are " + comment;
					

				}
				else if (event.equalsIgnoreCase("tomorrow")) {
					comment += "as you was " + comment;
//					isOneDay = true;
				}
				isOneDay = true;

			}
			log.info("message : " + message);
			response.put("message", message);
			response.put("isFestival", isFestival);
			response.put("isOneDay", isOneDay);
			response.put("isHoliday", isHoliday);
			response.put("longVaccationSugestion", longVaccSugestion);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return response;
	}
}
