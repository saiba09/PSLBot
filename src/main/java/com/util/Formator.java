package com.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;
import java.util.TreeMap;
import java.util.logging.Logger;

import org.json.simple.JSONObject;

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
	public static String getWeekendContainsMessage(String startDate , String endDate,float check){
		String message= "";
		JSONObject jsonDays = DateDetails.getDays(startDate, endDate);
		Boolean isWeekend = Boolean.parseBoolean(jsonDays.get("isWeekEnd").toString());
		Boolean flag = false;
		if (isWeekend) {
			log.info(" dates contains weekend in Between");
			TreeMap<Date, String> holidayMap = (TreeMap<Date, String>) jsonDays.get("holidayTrack");
			log.info("holiday map fetched");
			if (check == 1) {
				message += "Because its, weekend";
			}else{
			message += " However its, weekend";
			}
			for (Date date : holidayMap.keySet()) {
				String day = holidayMap.get(date).toString();
				if (!flag) {
					//message += " " + day + " on " + Formator.getFormatedDate(date);
					message += " on " + Formator.getFormatedDate(date);
					flag = true;
				}
				else{
				//message += " and " + day + " on " + Formator.getFormatedDate(date);
					message += " on " + Formator.getFormatedDate(date);
				}
			}
			log.info("message for weekend addded");
			message += ". . Should we proceed with the plan?";
		} else {
			log.info("No weekend in between ");
			message += ". Should we proceed with the plan?";

		}
		return message;
	}
	/*private static JSONObject Suggest(HashMap<String, JsonElement> parameter, String sessionId) {
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
	}*/
	public static Fulfillment getLeaveConfirmationMessage(String startDate, String endDate, String comment ,float leave_balance, Fulfillment output){
		Date start = null;;
		Date end =null;
		try {
			start = new SimpleDateFormat("yyyy-MM-dd").parse(startDate);
			end = new SimpleDateFormat("yyyy-MM-dd").parse(endDate);
			 log.info("casted to Date class");
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			log.severe("Exception : "+ e);
		}
		String message = "";
		
		JSONObject jsonDays = DateDetails.getDays(startDate, endDate);
		log.info("jsonDays : "+jsonDays);
		int noOfDays = DateDetails.getDaysBetweenDates(startDate, endDate);
		log.info("Days between days : "+ noOfDays);
		Boolean isWeekend = Boolean.parseBoolean(jsonDays.get("isWeekEnd").toString().trim());
		int noOfLeaves = Integer.parseInt(jsonDays.get("days").toString().trim());
		if (noOfLeaves == 0) {
			if (noOfDays == 1) {
				message = "Hey #usr#! Its holiday. No need to apply for leave. Enjoy!";
				message += Formator.getWeekendContainsMessage(startDate, endDate,noOfLeaves);
				output = Redirections.redirectToDisplayMessage(output, message);
			}
			if (noOfDays == 2) {
				message = "Its weekend from " + Formator.getFormatedDate(start) + " to "
						+ Formator.getFormatedDate(end) + ". No need to apply for leave. Enjoy!";
				log.info(message);
				HashMap<String, JsonElement> outParms = new HashMap<>();
				outParms.put("message", new JsonPrimitive(message));
				output = Redirections.redirectToDisplayMessage(output, outParms);
			}
		}else if (noOfLeaves <= leave_balance) {
			if (noOfLeaves == 1) {
				message = "You want to apply leave on "+Formator.getFormatedDate(start)+". Shall I confirm?";
				log.info("1 leave msg : "+ message);
			}
			else{
			message = "So you want to apply leave from "+Formator.getFormatedDate(start)+ " to "+Formator.getFormatedDate(end)+".";
			message += Formator.getWeekendContainsMessage(startDate, endDate, noOfLeaves);
			
			}
			log.info(message);
			output.setSpeech(message);
			output.setDisplayText(message);
		}
		else{
			output = Redirections.redirectToDPApproval(output, null);

		}
		log.info(message);
		
		return output;
	}
	public static String getNotWellresponse(){
		log.info("getNotWellresponse");
		String[] response = {
				//"What’s wrong with you?",
			//	"Are you sick?",
				//"How do you feel?",
				//"Do you feel weak / nauseous?",
				//"Do you have a headache?",
				//"Have you been feeling like this for a while?",
				//"Is something wrong?",
				//"Are you worried about something?",
				"Is there anything I can do to help?",
				/*"Did you eat something bad?",
				"Did you eat something that doesn’t agree with you?",
				"Did you eat something that you are allergic to?",
				"Did you forget to take your medication?",
				"Do you want to go to the doctor?",*/
				//"I’m sorry that you’re not feeling well, maybe I should bring you home.",
				//"I think we should bring you to the doctor / hospital.",
				"I hope you feel better soon.",
				"I think you need to drink some water and lie down for a while.",
				"I know you aren’t feeling well but hopefully the medicine will help.",
				"I’m sorry, I didn’t realise you were feeling sick. Let’s sit down for a while, shall we?"	
		};
		Random r = new Random();
		int select =  r.nextInt((response.length - 1) + 1) + 0;
		log.info("select : "+ select);
		return response[select]+"";
	}
	public static JSONObject getOptionForLeave(){
		String time = DateDetails.getCurrentTime();
		int hour = Integer.parseInt(time.substring(0, time.indexOf("/")));
		int min = Integer.parseInt(time.substring(time.indexOf("/"), time.indexOf(":")));
		String message ="";
		Boolean isHalfDay = false;
		Boolean canBeHalfDay = false;
		Boolean isFullDay = false;
		JSONObject response = new JSONObject();
		if (hour > 13) {
			message = "Its almost half day completed, Do you wish to apply for half day leave?";
			isHalfDay = true;
		}else{
			if (min > 30) {
				message = "Hey #usr# If you can just wait for "+(60-min)+"min more, we can apply half day leave else will need to apply for a full day leave.";
				canBeHalfDay = true;
			}
			else{
				message = "#usr# Its just "+time+" Do you wish to apply for a full day leave.";
				isFullDay = true;
			}
		}
		response .put("isFullDay", isFullDay);
		response.put("isHalfDay", isHalfDay);
		response.put("canBeHalfDay", canBeHalfDay);
		response.put("message", message);
		response.put("time", time);
		return response;
	}
		}

