package com.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Logger;

import org.json.simple.JSONObject;

import com.example.Data;
import com.google.gson.JsonElement;

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
	public static String getWeekendContainsMessage(String startDate , String endDate){
		String message= "";
		JSONObject jsonDays = getDays(startDate, endDate);
		Boolean isWeekend = Boolean.parseBoolean(jsonDays.get("isWeekEnd").toString());

		if (isWeekend) {
			log.info(" dates contains weekend in Between");
			HashMap<Date, String> holidayMap = (HashMap<Date, String>) jsonDays.get("holidayTrack");
			log.info("holiday map fetched");
			message += " However its,";
			for (Date date : holidayMap.keySet()) {
				String day = holidayMap.get(date).toString();
				message += " " + day + " on " + Formator.getFormatedDate(date);
			}
			log.info("message for weekend addded");
			message += ". Shall we continue the plan?";
		} else {
			log.info("No weekend in between ");
			message += " Should I confirm?";

		}
		return message;
	}
	private static JSONObject Suggest(HashMap<String, JsonElement> parameter, String sessionId) {
		log.info("suggest called");
		JSONObject holidayData = Data.getHolidays(sessionId);
		String bday = holidayData.get("birthday").toString();
		JSONObject response = new JSONObject();
		try {
			Date birthday = new SimpleDateFormat("dd/MM/yyyy").parse(bday);
			String msg = "";
			String event = "";
			Boolean check = false;
			if (DateDetails.isEventWithinRange(birthday)) {
				Calendar cal = Calendar.getInstance();
				cal.setTime(birthday);
				msg = " And! Its your birthday on " + new SimpleDateFormat("MMM d").format(birthday)
						+ ". Want to go out??";
				event = "birthday";
				check = true;
			} else {
				JSONObject holidays = (JSONObject) holidayData.get("holidays");
				for (Iterator iterator = holidays.keySet().iterator(); iterator.hasNext();) {
					String key = (String) iterator.next();
					Date date1 = new SimpleDateFormat("dd/MM/yyyy").parse(key);
					if (DateDetails.isEventWithinRange(date1)) {
						msg = holidays.get(key).toString() + " is coming up.. Wanna apply leave for that?"
								+ holidays.get(key).toString();
						event = (String) holidays.get(key);
						check = true;
					}
				}
			}
			response.put("event", event);
			response.put("message", msg);
			response.put("present", check);
			log.info("returns from function");
			log.info(response.toJSONString());
			return response;
		} catch (Exception e) {
			log.severe("error " + e);
		}

		return response;
	}
		}

