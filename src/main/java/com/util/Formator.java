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
import com.example.Redirections;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import ai.api.model.Fulfillment;

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
	public static String getWeekendContainsMessage(String startDate , String endDate,int check){
		String message= "";
		JSONObject jsonDays = DateDetails.getDays(startDate, endDate);
		Boolean isWeekend = Boolean.parseBoolean(jsonDays.get("isWeekEnd").toString());
		Boolean flag = false;
		if (isWeekend) {
			log.info(" dates contains weekend in Between");
			HashMap<Date, String> holidayMap = (HashMap<Date, String>) jsonDays.get("holidayTrack");
			log.info("holiday map fetched");
			if (check == 1) {
				message += "As its,";
			}else{
			message += " However its,";
			}
			for (Date date : holidayMap.keySet()) {
				String day = holidayMap.get(date).toString();
				if (!flag) {
					message += " " + day + " on " + Formator.getFormatedDate(date);
					flag = true;
				}
				message += " and " + day + " on " + Formator.getFormatedDate(date);

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
	public static Fulfillment getLeaveConfirmationMessage(String startDate, String endDate, String comment ,int leave_balance, Fulfillment output){
		Date start = null;;
		Date end =null;
		try {
			start = new SimpleDateFormat("yyyy-MM-dd").parse(startDate);
			end = new SimpleDateFormat("yyyy-MM-dd").parse(endDate);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			log.severe("Exception : "+ e);
		}
		String message = "";
		
		JSONObject jsonDays = DateDetails.getDays(startDate, endDate);
		int noOfLeaves = DateDetails.getDaysBetweenDates(startDate, endDate);
		log.info("Days between days : "+ noOfLeaves);
		if (noOfLeaves == 2 && Boolean.parseBoolean(jsonDays.get("isWeekEnd").toString().trim())) {
			JSONObject holidayMap = (JSONObject) jsonDays.get("holidayTrack");
			if (((String) holidayMap.get(start)).equalsIgnoreCase("Saturday")
					&& ((String) holidayMap.get(end)).equalsIgnoreCase("Sunday")) {
				// redirect to send message
				message = "Its weekend from " + Formator.getFormatedDate(start) + " to "
						+ Formator.getFormatedDate(end) + ". No need to apply for leave. Enjoy!";

				HashMap<String, JsonElement> outParms = new HashMap<>();
				outParms.put("message", new JsonPrimitive(message));
				output = Redirections.redirectToDisplayMessage(output, outParms);
			} else if (leave_balance >= noOfLeaves) {
				message = "Hey I just checked you have sufficient leave balance, shall we proceed?";
				message += Formator.getWeekendContainsMessage(startDate, endDate,noOfLeaves);
			}

		} else if (leave_balance >= noOfLeaves) {
			// give suggestion that if weekend
			message = "Hey I just checked you have sufficient leave balance.";
			message += Formator.getWeekendContainsMessage(startDate, endDate, noOfLeaves);
		}
		log.info(message);
		output.setSpeech(message);
		output.setDisplayText(message);
		return output;
	}
		}

