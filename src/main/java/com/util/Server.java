package com.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Logger;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


public class Server {
	private static final Logger log = Logger.getLogger(Server.class.getName());
	static String urlStr = "https://1-dot-dummyproject-05042017.appspot.com/applyLeave";
	
public static int applyLeaveInSystem(String startDate, String  endDate, String empName, String reason, String typeOfLeave, float noOfDays){
	log.info("creating post call");
	log.info("---------------->");
	int response = 400;
	
	try {
		
		URL url = new URL(urlStr);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setDoOutput(true);
		conn.setRequestProperty("Content-Length", "0");
		conn.setRequestMethod("GET");
		conn.setRequestProperty("Content-Type", "application/json");
		
		OutputStream outputStream = conn.getOutputStream();
		outputStream.write(getJsonStringEntityForElement(startDate, endDate, empName, reason, typeOfLeave, noOfDays).getBytes());
		outputStream.flush();
		BufferedReader bufferedReaderObject = new BufferedReader(new InputStreamReader(conn.getInputStream(),"UTF-8"));			
		StringBuilder output = new StringBuilder();			
		
		String op;
		while ((op = bufferedReaderObject.readLine()) != null) {
			output.append(op);
		}

		JSONParser parser = new JSONParser();
		 JSONObject obj = (JSONObject) parser.parse(output.toString());
		
		conn.disconnect();
		
		log.info(obj.toJSONString());
		response = Integer.parseInt(obj.get("error_code").toString());		
		
	} catch (IOException | ParseException e) {
		// TODO Auto-generated catch block
		log.severe("exception : "+ e);
	} 
	return response;
}
private static String getJsonStringEntityForElement(String startDate, String endDate, String empName, String reason, String typeOfLeave, float noOfDays) {
	JSONObject obj = new JSONObject();
	obj.put("startDate", startDate);
	obj.put("endDate", endDate);
	obj.put("employeeName", empName);
	obj.put("reason", reason);
	obj.put("typeOfLeave", typeOfLeave);
	obj.put("noOfDays", noOfDays);
	return obj.toJSONString();
}
}
