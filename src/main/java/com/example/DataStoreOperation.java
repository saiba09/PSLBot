package com.example;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Logger;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.model.LeaveTransaction;

public class DataStoreOperation {
	private static final Logger log = Logger.getLogger(DataStoreOperation.class.getName());

public static void addTransaction(LeaveTransaction leaveTransaction, JSONObject hirearchy) {
	// TODO Auto-generated method stub

	try{
		
		String apiUrl = "146.148.66.111/PSLBot/DatastoreHandler";
		log.info("urL : "+apiUrl);
		URL url = new URL(apiUrl);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setDoOutput(true);
		conn.setRequestProperty("Content-Length", "0");
		conn.setRequestMethod("POST");
//		conn.setRequestProperty("Content-Type", "application/json");
		JSONObject requestObject = getRequestObject(leaveTransaction, hirearchy);
		OutputStream outputStream = conn.getOutputStream();
		outputStream.write(requestObject.toJSONString().getBytes());
		outputStream.flush();
		
		BufferedReader bufferedReaderObject = new BufferedReader(new InputStreamReader((conn.getInputStream())));			
		StringBuilder output = new StringBuilder();			
		
		String op;
		while ((op = bufferedReaderObject.readLine()) != null) {
			output.append(op);
		}

		JSONParser parser = new JSONParser();
		JSONObject responseData = (JSONObject) parser.parse(output.toString());
		
		conn.disconnect();
		
		log.info(responseData.toJSONString());
	}
	catch(Exception e){
		log.severe("exception writing to data store : "+e);
	}

}

private static JSONObject getRequestObject(LeaveTransaction leaveTransaction, JSONObject hirearchy) {
	// TODO Auto-generated method stub
JSONObject requestObject = new JSONObject();
requestObject.put("approverComment", leaveTransaction.getApprovarComment());
requestObject.put("approver", leaveTransaction.getApprover());
requestObject.put("employeeId", leaveTransaction.getEmployeeId());
requestObject.put("employeeName", leaveTransaction.getEmployeeName());
requestObject.put("leaveType", leaveTransaction.getLeaveType());
requestObject.put("leaveStartDate", leaveTransaction.getDate().getStartDate());
requestObject.put("leaveEndDate", leaveTransaction.getDate().getEndDate());
requestObject.put("reason", leaveTransaction.getDate().getReason());
requestObject.put("noOfLeaves", leaveTransaction.getDate().getNoOfDays());
requestObject.put("hirerachy", hirearchy);
return requestObject;
}
}
