package com.example;

import java.util.logging.Logger;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletContext;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.model.User;
import com.util.AccessProperty;

public class UserHandler {
	private static final Logger log = Logger.getLogger(UserHandler.class.getName());
	public User getUser(String userName,String fileName){
		log.info("inside get user");
	
		 String accessToken = new AccessProperty().getAccessToken(userName, fileName);
		 log.info("Accesss Token " + accessToken); 
		User user = new User(userName, accessToken);

		JSONObject employeeDetails = PiHandler.getLeaveBalance(accessToken);
		
		JSONArray leaveTypes = (JSONArray)employeeDetails.get("LeavesList");
		log.info(leaveTypes.toString());
		
		JSONObject leave = (JSONObject)leaveTypes.get(0);
		log.info(leave.toString());
		float privillage_leave = Float.parseFloat(leave.get("AvailableLeaveCount").toString());
		
		leave = (JSONObject)leaveTypes.get(1);
		float ol = Float.parseFloat(leave.get("AvailableLeaveCount").toString());
		
		leave = (JSONObject)leaveTypes.get(2);
		float oh = Float.parseFloat(leave.get("AvailableLeaveCount").toString());
		
		leave = (JSONObject)leaveTypes.get(3);
		float cf = Float.parseFloat(leave.get("AvailableLeaveCount").toString());
		user.setPrivilagedLeave(privillage_leave);
		user.setCompensatioryOff(cf);
		user.setOptionalHoliday(oh);
		user.setOptionalLeave(ol);
		log.info(user.toString());
		return user;
	}
	private  User setUserLeaveData(User user) {
		// TODO Auto-generated method stub
		
		
		JSONObject employeeDetails = PiHandler.getLeaveBalance(user.getSession().getAccessToken());
		
		JSONArray leaveTypes = (JSONArray)employeeDetails.get("LeavesList");
		log.info(leaveTypes.toString());
		
		JSONObject leave = (JSONObject)leaveTypes.get(0);
		log.info(leave.toString());
		float privillage_leave = Float.parseFloat(leave.get("AvailableLeaveCount").toString());
		
		leave = (JSONObject)leaveTypes.get(1);
		float ol = Float.parseFloat(leave.get("AvailableLeaveCount").toString());
		
		leave = (JSONObject)leaveTypes.get(2);
		float oh = Float.parseFloat(leave.get("AvailableLeaveCount").toString());
		
		leave = (JSONObject)leaveTypes.get(3);
		float cf = Float.parseFloat(leave.get("AvailableLeaveCount").toString());
		user.setPrivilagedLeave(privillage_leave);
		user.setCompensatioryOff(cf);
		user.setOptionalHoliday(oh);
		user.setOptionalLeave(ol);
		/*JSONObject responseObject = new JSONObject();
		
		String event_date= "22/05/1995";  
		responseObject.put("birthday", event_date);
		
		JSONObject holidays = new JSONObject();
		event_date="25/12/2017";  
		holidays.put(event_date, "christmas");
		
		event_date="31/12/2017";  
		holidays.put(event_date, "new year eve");
		
		responseObject.put("holidays", holidays);
		responseObject.put("optional_leave", ol);
		responseObject.put("optional_holiday", oh);
		responseObject.put("compensatiory_off", cf);
		responseObject.put("privillage_leave", privillage_leave);*/

		log.info(user.toString());
		return user;
	
	}
	
}
