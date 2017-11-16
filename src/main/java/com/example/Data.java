package com.example;

import java.util.Calendar;
import java.util.Date;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class Data {

	@SuppressWarnings({ "unused", "deprecation", "unchecked" })
	public static JSONObject getHolidays() {
		// TODO Auto-generated method stub
    	int leaveBalance = 5;
		int oh = 1;
		int ol = 0;
		int cf = 3;
		JSONObject responseObject = new JSONObject();
		
		String event_date="21/11/2017";  
		responseObject.put("birthday", event_date);
		
		JSONObject holidays = new JSONObject();
		event_date="25/12/2017";  
		holidays.put(event_date, "christmas");
		
		event_date="31/12/2017";  
		holidays.put(event_date, "new year eve");
		
		responseObject.put("holidays", holidays);
		responseObject.put("optional_leave", oh);
		responseObject.put("optional_holiday", ol);
		responseObject.put("compensatiory_off", cf);
		responseObject.put("leave_balance", leaveBalance);

		return responseObject;
	}

}
