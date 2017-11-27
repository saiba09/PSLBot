package com.example;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.logging.Logger;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class Data {
	private static final Logger log = Logger.getLogger(Data.class.getName());

	@SuppressWarnings({ "unused", "deprecation", "unchecked" })
	public static JSONObject getHolidays(String userName) {
		// TODO Auto-generated method stub
		
		JSONObject employeeDetails = getResponseFromAPI(userName);
		
    	int privillage_leave = Integer.parseInt(employeeDetails.get("PrivilegedLeave").toString().trim()); 
		int oh = Integer.parseInt(employeeDetails.get("OptionalHoliday").toString().trim()); 
		int ol = Integer.parseInt(employeeDetails.get("OptionalLeave").toString().trim()); 
		int cf = Integer.parseInt(employeeDetails.get("CompensatoryOff").toString().trim()); 
		JSONObject responseObject = new JSONObject();
		
		String event_date= employeeDetails.get("DateOfBirth").toString();  
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
		responseObject.put("privillage_leave", privillage_leave);

	
		return responseObject;
	
	}
	
	static JSONObject getResponseFromAPI(String userName){
		JSONObject obj = null ;

		try{
			
			URL url = new URL("https://1-dot-dummyproject-05042017.appspot.com/getData?EmployeeName="+userName);
			
			
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setDoOutput(true);
			conn.setRequestProperty("Content-Length", "0");
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Content-Type", "application/json");
			
			BufferedReader bufferedReaderObject = new BufferedReader(new InputStreamReader(conn.getInputStream(),"UTF-8"));			
			StringBuilder output = new StringBuilder();			
			
			String op;
			while ((op = bufferedReaderObject.readLine()) != null) {
				output.append(op);
			}

			JSONParser parser = new JSONParser();
			 obj = (JSONObject) parser.parse(output.toString());
			
			conn.disconnect();
			
			log.info(obj.toJSONString());
		}
		catch(Exception e){
			e.printStackTrace();
		}

		return obj;
	}

}
