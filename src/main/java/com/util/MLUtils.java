package com.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;
import java.util.Properties;

import org.json.JSONObject;



public class MLUtils {
	private static final Logger log = Logger.getLogger(MLUtils.class.getName());


	public static String leaveCategoryPrediction(String empId) {
		String apiUrl = "http://127.0.0.1:5000/leave_category_prediction";
		
		return postRequestMl(empId, apiUrl);
	}

	
	public static ArrayList<String> longLeavePrediction(Map<String, String> subordinates) {
		
		String apiUrl = "http://127.0.0.1:5000/long_leave_prediction";
		
		ArrayList<String> possibleSubordinatesList = new ArrayList<String>();
		
		for(Entry<String, String> subordinate : subordinates.entrySet()) {
			String empId = subordinate.getKey();
			
			if(postRequestMl(empId, apiUrl).equalsIgnoreCase("yes"))
				possibleSubordinatesList.add(subordinate.getValue()); 
		}
		
		return possibleSubordinatesList;
	}
	

	public static ArrayList<String> longLeavePrediction() {
		
		ArrayList<String> possibleSubordinatesList = new ArrayList<String>();
		
		try {
			
			//Load the properties file storing the list of GD&T symbols
			Properties ListOfSubordinates = new Properties();
			InputStream inputStream =  MLUtils.class.getResourceAsStream("/subordinates.properties");
			
			ListOfSubordinates.load(inputStream);
			
			//initialize Map of subordinates
			Map<String, String> subordinates = new HashMap<String, String>();
			
			for(String key : ListOfSubordinates.stringPropertyNames()) { //Iterate over every property
				subordinates.put(key, ListOfSubordinates.getProperty(key));
			}
			
			
			String apiUrl = "http://127.0.0.1:5000/long_leave_prediction";
			
			for(Entry<String, String> subordinate : subordinates.entrySet()) {
				String empId = subordinate.getKey();
				
				if(postRequestMl(empId, apiUrl).equalsIgnoreCase("yes"))
					possibleSubordinatesList.add(subordinate.getValue()); 
			}
			
		
		} catch (IOException e) {
			log.warning(e.getMessage());
		}
	
		
		return possibleSubordinatesList;
	}
	
	
	private static String postRequestMl(String empId, String apiUrl) {
		
		String prediction_value = null;
		
		try{
			
			JSONObject data = new JSONObject();
			data.put("EmpID", empId);
			
			URL url = new URL(apiUrl);
			
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setDoOutput(true);
			conn.setDoInput(true);
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type", "application/json");
			conn.setRequestProperty("Accept", "application/json");

			
			OutputStream outputStream = conn.getOutputStream();
			outputStream.write(data.toString().getBytes("UTF-8"));
			outputStream.flush();
			
			BufferedReader bufferedReaderObject = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"));			
			StringBuilder output = new StringBuilder();			
			
			String op;
			while ((op = bufferedReaderObject.readLine()) != null) {
				output.append(op);
			}

			JSONObject responseData = new JSONObject(output.toString());
			
			conn.disconnect();
			
			prediction_value = responseData.getString("predicted_value");
		}
		catch(Exception e){
			log.warning(e.getMessage());
		}

		return prediction_value;
	}
	
	
	public static void main(String[] args) {
		
		System.out.println(leaveCategoryPrediction("10071"));
		
		Object possibleSubordinates[] = longLeavePrediction().toArray();
		
		for(Object o : possibleSubordinates) {
			System.out.println(o);
		}
	}
	
}
