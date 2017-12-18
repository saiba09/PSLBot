package com.example;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.model.User;

public class PiHandler {
	private static final Logger log = Logger.getLogger(PiHandler.class.getName());
	static JSONObject getLeaveBalance(String accessToken){
		JSONObject responseData=null;
		try{
			log.info("inside getting response of api for leave balance");
			String apiurl = "https://api.persistent.com:9020/hr/leaveattendanceself";
			URL url = new URL(apiurl);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setDoOutput(true);
			conn.setRequestProperty("Content-Length", "0");
			conn.setRequestProperty("Authorization", "Bearer "+accessToken);
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Content-Type", "application/json");
			
			BufferedReader bufferedReaderObject = new BufferedReader(new InputStreamReader(conn.getInputStream(),"UTF-8"));			
			StringBuilder output = new StringBuilder();			
			
			String op;
			while ((op = bufferedReaderObject.readLine()) != null) {
				output.append(op);
			}
			
			JSONParser parser = new JSONParser();
			responseData = (JSONObject) parser.parse(output.toString());
			conn.disconnect();
			log.info(responseData.toString());
		}catch(Exception e){
			log.severe("error accessing leave balance api:"+e);
		}

		return responseData;
	}
	static JSONObject applyLeave(User user, String val){
		log.info("inside get test1");
		String userName = user.getUserName();
		String accessToken = user.getSession().getAccessToken();
		
		
		int index = -1;
		switch (val) {
		case "PL":
				index = 0;
			break;
		case "OL":
				index = 1;
				break;
		case "ML":
				index = 2;
				break;
		case "CAL":
				index = 3;
				break;
		case "OH":
				index = 4;
				break;
		case "CF":
				index = 5;
				break;
		}
		
		JSONArray leaveTypes = getResponseFromLeaveTypeAPI(accessToken, userName);
		JSONObject obj = (JSONObject) leaveTypes.get(6);
		int leaveYearCid = Integer.parseInt(obj.get("key").toString());
		obj = (JSONObject) leaveTypes.get(index);
		int leaveTypeCid = Integer.parseInt(obj.get("key").toString());
		
		///leaveTypeCid need to be set based on leave type
		
		JSONArray LeaveInfo = getResponseFromLeaveInfoAPI(accessToken, userName, leaveTypeCid);
		JSONObject employee = (JSONObject) LeaveInfo.get(9);
		String employeeHRISCid = employee.get("key").toString();
		String empName = employee.get("value").toString();
		log.info("employee name :"+empName);
		
		JSONObject approver = (JSONObject)LeaveInfo.get(4);
		String approverID = approver.get("key").toString();
		
		//setting parameters for leave days api
		String fromDate = "15-Dec-2017";
		String toDate = "15-Dec-2017";
		boolean isHalfDaySession = false;
		
		float leaves = getResponseFromLeaveDaysAPI(accessToken, fromDate, toDate, leaveTypeCid, isHalfDaySession, employeeHRISCid);
		log.info("Leave types, info and days api tested successfully");
		
		//setting parameters for leave apply parameters
		boolean isAfterNoon = false;
		boolean isAdvancedLeave = false;
		String Reason = "testing leave apply api";
		
		JSONObject res = applyLeave(empName, leaveTypeCid, fromDate, toDate, isHalfDaySession, isAfterNoon, leaveYearCid, isAdvancedLeave, approverID, Reason, accessToken);
		return res;
	}
	static JSONArray getResponseFromLeaveTypeAPI(String accessToken,String userName){
		JSONArray leaveTypes = null;
		try{
			log.info("inside getting response of api for type");
			String apiurl = "https://api.persistent.com:9020/hr/leavetypes/"+userName;
			URL url = new URL(apiurl);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setDoOutput(true);
			conn.setRequestProperty("Content-Length", "0");
			conn.setRequestProperty("Authorization", "Bearer "+accessToken);
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Content-Type", "application/json");
			
			BufferedReader bufferedReaderObject = new BufferedReader(new InputStreamReader(conn.getInputStream(),"UTF-8"));			
			StringBuilder output = new StringBuilder();			
			
			String op;
			while ((op = bufferedReaderObject.readLine()) != null) {
				output.append(op);
			}
			
			JSONParser parser = new JSONParser();
			leaveTypes = (JSONArray) parser.parse(output.toString());
			conn.disconnect();
			log.info(leaveTypes.toString());
		}catch(Exception e){
			log.info("error accessing leave balance :"+e);
		}

		return leaveTypes;
	}

	static JSONArray getResponseFromLeaveInfoAPI(String accessToken,String userName, int leaveTypeCid){
		JSONArray leaveInfo=null;
		try{
			log.info("inside getting response of api for leave info");
			String apiurl = "https://api.persistent.com:9020/hr/leaveinfo/"+userName+"/"+leaveTypeCid;
			URL url = new URL(apiurl);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setDoOutput(true);
			conn.setRequestProperty("Content-Length", "0");
			conn.setRequestProperty("Authorization", "Bearer "+accessToken);
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Content-Type", "application/json");
			
			BufferedReader bufferedReaderObject = new BufferedReader(new InputStreamReader(conn.getInputStream(),"UTF-8"));			
			StringBuilder output = new StringBuilder();			
			
			String op;
			while ((op = bufferedReaderObject.readLine()) != null) {
				output.append(op);
			}
			
			JSONParser parser = new JSONParser();
			leaveInfo= (JSONArray) parser.parse(output.toString());
			conn.disconnect();
			log.info(leaveInfo.toString());
		}catch(Exception e){
			log.info("error accessing leave balance :"+e);
		}

		return leaveInfo;
	}
	
	static float getResponseFromLeaveDaysAPI(String accessToken,String fromDate, String toDate, int leaveTypeCid, boolean isHalfDaySession, String employeeHRISCid){
		float leaves = 0;
		try{
			log.info("inside getting response of api for leave days");
			String apiurl = "https://api.persistent.com:9020/hr/leavedays/"+fromDate+"/"+toDate+"/"+leaveTypeCid+"/"+isHalfDaySession+"/"+employeeHRISCid;
			URL url = new URL(apiurl);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setDoOutput(true);
			conn.setRequestProperty("Content-Length", "0");
			conn.setRequestProperty("Authorization", "Bearer "+accessToken);
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Content-Type", "application/json");
			
			BufferedReader bufferedReaderObject = new BufferedReader(new InputStreamReader(conn.getInputStream(),"UTF-8"));			
			StringBuilder output = new StringBuilder();			
			
			String op;
			while ((op = bufferedReaderObject.readLine()) != null) {
				output.append(op);
			}
			
			leaves = Float.parseFloat(output.toString());
			conn.disconnect();
			log.info("no of leaves applicable"+leaves);
		}catch(Exception e){
			log.info("error accessing leave balance :"+e);
		}

		return leaves;
	}
	
	@SuppressWarnings({ "unchecked", "null" })
	public static JSONObject applyLeave(String empName, int leaveTypeCid,
			String fromDate, String toDate, boolean isHalfDaySession,
			boolean isAfterNoon, int leaveYearCid, boolean isAdvancedLeave,
			String approverID, String Reason, String accessToken) {
		JSONObject responseData = null;
		try {

			JSONObject requestBody = new JSONObject();
			requestBody.put("EmployeeUserName", empName);
			requestBody.put("LeaveTypeCid", leaveTypeCid);
			requestBody.put("FromDate", fromDate);
			requestBody.put("ToDate", toDate);
			requestBody.put("IsHalfDaySession", isHalfDaySession);
			requestBody.put("IsAfterNoon", isAfterNoon);
			requestBody.put("LeaveYearCid", leaveYearCid);
			requestBody.put("IsAdvanceLeave", isAdvancedLeave);
			requestBody.put("ApproverId", approverID);
			requestBody.put("Reason", Reason);
			log.info("raw post data :"+requestBody);
			byte[] out = requestBody.toJSONString().getBytes(StandardCharsets.UTF_8);
			
			URL u = new URL("https://api.persistent.com:9020/hr/leave");
			HttpURLConnection conn = (HttpURLConnection) u.openConnection();
			conn.setDoOutput(true);
			conn.setRequestMethod("POST");
			conn.setRequestProperty( "Content-Type", "application/json" );
			conn.setRequestProperty("Authorization", "Bearer " + accessToken);
			//conn.setRequestProperty( "Content-Length", String.valueOf(out.length));
			log.info("setting output stream");
			OutputStream os = conn.getOutputStream();
			os.write(out);
			os.flush();
			
			BufferedReader bufferedReaderObject = new BufferedReader(new InputStreamReader((conn.getInputStream())));			
			StringBuilder output = new StringBuilder();			
			
			log.info("getting the output");
			String op;
			while ((op = bufferedReaderObject.readLine()) != null) {
				output.append(op);
			}
			log.info("output of api :"+output);
			JSONParser parser = new JSONParser();
			log.info("parsing the output");
			responseData = (JSONObject) parser.parse(output.toString());
			//responseData.put("output", output.toString());
			log.info("resposne from apply leave API:"+responseData);
			conn.disconnect();
		} catch (Exception e) {
			log.info("exception in applying leave" + e);
		}
		return responseData;
	}
}
