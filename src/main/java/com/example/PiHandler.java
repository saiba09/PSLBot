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
import com.model.Leave;
import com.model.User;


public class PiHandler {
	private static final Logger log = Logger.getLogger(PiHandler.class.getName());
	public static JSONObject getLeaveCalander(){
		log.info("get leave calander");
		JSONObject responseData = null ;
		try{
			log.info("inside getting response of api for holidays");
			String apiurl = "https://1-dot-dummyproject-05042017.appspot.com/getHolidayData";
			URL url = new URL(apiurl);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setDoOutput(true);
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Content-Type", "application/json");
		
			BufferedReader bufferedReaderObject = new BufferedReader(new InputStreamReader(conn.getInputStream(),"UTF-8"));			
			StringBuilder output = new StringBuilder();			
			
			String op;
			while ((op = bufferedReaderObject.readLine()) != null) {
				output.append(op);
			}
			conn.disconnect();
			JSONParser parser = new JSONParser();
			responseData = (JSONObject) parser.parse(output.toString());
			log.info(responseData.toString());
		}catch(Exception e){
			log.severe("error accessing leave balance api:"+e);
		}

		return responseData;
	
		
	}
	static JSONObject getLeaveBalance(String accessToken){
		log.info("access token : " + accessToken);
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
	static JSONObject applyLeave(User user, String val,Leave leave){
		log.info("inside get test1");
		String userName = user.getUserName();
		String accessToken = user.getSession().getAccessToken();
		
		
		String leaveType="";
		switch (val) {
		case "PL":
			leaveType = "Privileged Leave";
			break;
		case "OL":
			leaveType = "Optional Leave";
				break;
		case "ML":
			leaveType = "Maternity Leave";
				break;
		case "OH":
			leaveType = "Optional Holiday";
				break;
		case "CF":
			leaveType = "Compensatory Off";
				break;
		case "PAT" :
			leaveType = "Paternity Leave";
			break;
		case "CAL" :
			leaveType = "Child Adoption Leave";
			break;
		
		}
		
		JSONArray leaveTypes = getResponseFromLeaveTypeAPI(accessToken, userName);
		String leaveYear = leave.getFinnancialYear();
		log.info("user : "+userName + " acces token "+accessToken);	
		int leaveYearCid = 0;	
		int leaveTypeCid = 0;
		for (Object object : leaveTypes) {
			JSONObject current = (JSONObject)object;
			if (leaveYearCid == 0) {
				if (current.get("field").toString().equals("LeaveYear")) {
					log.info("leave year value : "+current.get("value").toString());
					if (current.get("value").toString().trim().equalsIgnoreCase(leaveYear)) {
						log.info("true");
						leaveYearCid = Integer.parseInt(current.get("key").toString());
					}
				}
			}
			if (leaveTypeCid == 0) {
				if (current.get("field").toString().trim().equals("LeaveType")) {
					if (current.get("value").toString().equals(leaveType)) {
						leaveTypeCid = Integer.parseInt(current.get("key").toString());
					}
				}
			}
		}
		//leaveTypeCid need to be set based on leave type
		
		JSONArray LeaveInfo = getResponseFromLeaveInfoAPI(accessToken, userName, leaveTypeCid);
		String employeeHRISCid = "";
		String employeeName = "";
		String approverID = "";
		for (Object object : LeaveInfo) {
			
			JSONObject current = (JSONObject)object;
			if (current.get("field").toString().equals("Name")) {
				employeeHRISCid = current.get("key").toString();
				employeeName = current.get("value").toString();
				log.info("Employee Name :"+employeeName + " EmployeeHRISCid : "+employeeHRISCid);

			}
			if (approverID.equals("")) {
				if (current.get("field").toString().equals("Approvers")) {
					approverID = current.get("key").toString();
					log.info("Approver "+approverID);
				}
			}
			
		}
	
		//setting parameters for leave days api
		String fromDate = leave.getStartDate(); //"15-Dec-2017"; 
		String toDate = leave.getEndDate();
		boolean isHalfDaySession = leave.getIsHalfDaySession();
		
		float leaves = getResponseFromLeaveDaysAPI(accessToken, leaveTypeCid,leave, employeeHRISCid);
		log.info("getResponseFromLeaveDaysAPI(accessToken, leaveTypeCid,leave, employeeHRISCid) : "+leaves);
		
		boolean isAfterNoon = leave.getIsAfterNoon();
		boolean isAdvancedLeave = leave.getIsAdvancedLeave();
		String Reason = leave.getReason();
		
		JSONObject res = applyLeave(employeeName, leaveTypeCid, fromDate, toDate, isHalfDaySession, isAfterNoon, leaveYearCid, isAdvancedLeave, approverID, Reason, accessToken);
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
			log.severe("error accessing leave balance :"+e);
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
			log.severe("error accessing leave balance :"+e);
		}

		return leaveInfo;
	}
	
	static float getResponseFromLeaveDaysAPI(String accessToken, int leaveTypeCid, Leave leave, String employeeHRISCid){
		float leaves = 0;
		try{
			log.info("inside getting response of api for leave days");
			String apiurl = "https://api.persistent.com:9020/hr/leavedays/"+leave.getEndDate()+"/"+leave.getStartDate()+"/"+leaveTypeCid+"/"+leave.getIsHalfDaySession()+"/"+employeeHRISCid;
			log.info("url : "+apiurl); 
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
			log.severe("error accessing leave balance :"+e);
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
			
			requestBody.put("AddToCcList", "");
			requestBody.put("OptionalHolidayDate", "");
			requestBody.put("LeaveStatusCid", 3);
			
			log.info("raw post data one :"+requestBody);
			byte[] out = requestBody.toJSONString().getBytes(StandardCharsets.UTF_8);
			log.info(requestBody.toJSONString());

			URL u = new URL("https://api.persistent.com:9020/hr/leave");
			HttpURLConnection conn = (HttpURLConnection) u.openConnection();
			conn.setDoOutput(true);
			conn.setRequestMethod("POST");
			conn.setRequestProperty( "Content-Type", "application/json" );
			conn.setRequestProperty("Authorization", "Bearer " + accessToken);
			//conn.setRequestProperty( "Content-Length", String.valueOf(out.length));
			log.info("setting output stream");
			OutputStream os = conn.getOutputStream();
			log.info("os : "+os);
			os.write(out);
			log.info("out "+out); 
			os.flush();
			log.info("flushed");
			BufferedReader bufferedReaderObject = new BufferedReader(new InputStreamReader((conn.getInputStream())));						
			log.info("buff reader : "+bufferedReaderObject);
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
			log.severe("exception in applying leave" + e);
		}
		return responseData;
	}
}
