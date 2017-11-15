package com.example;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Iterator;
import java.util.logging.Logger;

import org.json.simple.JSONObject;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import ai.api.model.AIEvent;

import ai.api.model.AIOutputContext;
import ai.api.model.Fulfillment;
import ai.api.web.AIWebhookServlet;

// [START example]
@SuppressWarnings("serial")
public class MyWebhookServlet extends AIWebhookServlet {
	private static final Logger log = Logger.getLogger(MyWebhookServlet.class.getName());

	@Override
	protected void doWebhook(AIWebhookRequest input, Fulfillment output) {
		log.info("webhook call");
		String action = input.getResult().getAction();
		HashMap<String, JsonElement> parameter = input.getResult().getParameters();
		// add constants in file
		try {
			switch (action) {
			case "QUERY_LEAVE":
				log.info("in action : query_leave");
				output = queryLeave(output, parameter);
				break;
			case "SYSTEM_SUGESTION_SATISFIED_YES":
				log.info(" intent SYSTEM_SUGESTION_SATISFIED_YES ");
				output = getConfirmationMessage(output, parameter);
				break;
			case "SYSTEM_SUGESTION_SATISFIED_NO":
				log.info("intent : SYSTEM_SUGESTION_SATISFIED_NO");
				output = getConfirmationMessage(output, parameter);
				break;
			case "CONFIRM_LEAVE_APPLY":
				log.info("intent APPLY_LEAVE_CUSTOM");
				output = applyLeave(output, parameter);
				break;
			case "OPT_CUSTOM_REQ":
				log.info("intent CONFIRM_LEAVE_YES");
				output = redirectToCustomApply(output, parameter);
				break;
			case "SYST_SUG_NOT_SATISFIED_CUST_CONFIRM":
				log.info("SYST_SUG_NOT_SATISFIED_CUST_CONFIRM");
				output = redirectToCustomApply(output,parameter); // response if yes replan goto custom
				break;
		/*	case "RESTART":
				log.info("intent : restart");
				output = fallbackCustomApply(output, parameter);
				break;
			case "EXIT":
				log.info("exit");
				output = exitFlow(output);
				break;
			case "input.welcome":
				log.info("input.welcome");
				output = eventTriggered(output);
				break;*/
			default:
				output.setSpeech("Default case");
				break;
			}
		} catch (Exception e) {
			log.info("exception : " + e);
		}

		
	}

	private Fulfillment exitFlow(Fulfillment output) {
		// TODO Auto-generated method stub
		return null;
	}

	private Fulfillment redirectToCustomApply(Fulfillment output, HashMap<String, JsonElement> parameter) {
		//Trigger event for custom leave apply
		log.info("redirectToCustomApply event trig fun");
		AIEvent followupEvent = new AIEvent("CUSTOM_FORM");
		log.info("rerouting to event : evt trg");
		output.setFollowupEvent(followupEvent);
		return output;
	}

	private Fulfillment applyLeave(Fulfillment output, HashMap<String, JsonElement> parameter) {
		log.info("apply leave function");
		String startDate = parameter.get("startDate").getAsString();
		String endDate = parameter.get("endDate").getAsString();
		String comment = parameter.get("comment").getAsString();
		log.info("parms :"+startDate+" "+endDate+" comment : "+comment);
		String message = "";
		JSONObject sugestion = Suggest(parameter);
		int leave_balance = Integer.parseInt(sugestion.get("leave_balance").toString());
		// check bal if allow apply
		int noOfLeaves = getDays(startDate, endDate);
		if (leave_balance <= 0) {
			log.info("bal < 0");
			message = "Sorry dear, you have insufficient leave balance, you will need DP approval If want to apply for leave.";
			//Trigger dp approval intent
			log.info("DP APPROVAL REQ event trig ");
			AIEvent followupEvent = new AIEvent("DP_APPROVAL");
			log.info("rerouting to event : evt trg");
			output.setFollowupEvent(followupEvent);
		}
		else if(leave_balance > noOfLeaves) {
			log.info("bal < no Leaves ");
			message = "Here you go! Your leaves had been applied in the system.";
		}			
		else {
			message = "Your leave balance is less than :" + noOfLeaves
					+ ". You will need Delivery partner approval.";		
			//set event trigg.
			log.info("DP APPROVAL REQ event trig ");
			AIEvent followupEvent = new AIEvent("DP_APPROVAL");
			log.info("rerouting to event : evt trg");
			output.setFollowupEvent(followupEvent);
		}
		//else abort
		log.info(message);
		output.setSpeech(message);
		output.setDisplayText(message);
		return output;
	}

	private Fulfillment queryLeave(Fulfillment output, HashMap<String, JsonElement> parameter) throws ParseException {
		log.info("querry leave function");
		String startDate = parameter.get("startDate").getAsString();
		String endDate = parameter.get("endDate").getAsString();
		String event = parameter.get("event").getAsString();
		log.info("parms :"+startDate+" "+endDate+" event: "+event);
		String message ="";
		JSONObject sugestion = Suggest(parameter);
		if (Boolean.parseBoolean(sugestion.get("present").toString())) {
			log.info("do have a suggestion");
			if (event.isEmpty()) {
				log.info("event suggested");
				message = sugestion.get("message").toString();
				//set cont.
			}else {
				log.info("already had an event");
				message = "you have sufficient leave balance. apply";
				//triggre event 
				log.info("redirectToCustomApply event trig fun");
				AIEvent followupEvent = new AIEvent("CUSTOM_FORM");
				log.info("rerouting to event : evt trg");
				output.setFollowupEvent(followupEvent);
			}
		}else {
			log.info("already had an event");
			message = "you have sufficient leave balance. apply";
			//triggre event 
			log.info("redirectToCustomApply event trig fun");
			AIEvent followupEvent = new AIEvent("CUSTOM_FORM");
			log.info("rerouting to event : evt trg");
			output.setFollowupEvent(followupEvent);
		}
		log.info(message);
		output.setSpeech(message);
		output.setDisplayText(message);
		return output;
	}

	
	private Fulfillment getConfirmationMessage(Fulfillment output, HashMap<String, JsonElement> parameter) {
		log.info("getConfirmationMessage");
		String startDate = parameter.get("startDate").getAsString();
		String endDate = parameter.get("endDate").getAsString();
		String comment =""; String event ="";
		JSONObject sugestion = Suggest(parameter);
		String message ="";
		log.info("parms :"+startDate+" "+endDate);
		if (parameter.containsKey("comment")) {
			comment = parameter.get("comment").getAsString();
			log.info("comment "+comment);
		}else {
		event =  parameter.get("event").getAsString();
		comment = getMessage(event);
		log.info("event :"+event +" comment :"+comment);
		}
		//check leave balance > days to apply
		int leave_balance = Integer.parseInt(sugestion.get("leave_balance").toString());
		int noOfLeaves = getDays(startDate, endDate);
		log.info("balance :"+leave_balance+" required :"+ noOfLeaves);
		if (leave_balance <= 0) {
			log.info("bal < 0");
			message = "Sorry dear, you have insufficient leave balance, you will need DP approval If want to apply for leave.";
			//triggre dp approval inteent
			log.info("DP APPROVAL REQ event trig ");
			AIEvent followupEvent = new AIEvent("DP_APPROVAL");
			log.info("rerouting to event : evt trg");
			output.setFollowupEvent(followupEvent);
		}
		else if(leave_balance > noOfLeaves) {
			log.info("req > bal");
			message = "Hurry you have " + leave_balance + " leaves remaining. You can apply for leave. Shall we proceed or you have a second thought?";
			output.setSpeech(message);
			output.setDisplayText(message);
		}			
		else {
			message = "Your leave balance is less than :" + noOfLeaves
					+ ". You will need Delivery partner approval if you will apply. Or dear if you say shall I apply for "
					+ leave_balance + " days.";
			log.info(message);
			output.setSpeech(message);
			output.setDisplayText(message);
			//IMP : set out parmas end date- diff
		}
		log.info(message);

		return output;
	}
	
	private static String getMessage(String event) {
		// TODO Auto-generated method stub
		return "Leave for "+event;
	}
	
	private static JSONObject Suggest(HashMap<String, JsonElement> parameter)  {
		log.info("suggest called");
		JSONObject holidayData = Data.getHolidays();
		String bday = holidayData.get("birthday").toString();
		JSONObject response = new JSONObject();
		try {
		Date birthday = new SimpleDateFormat("dd/MM/yyyy").parse(bday);
		String msg = "";
		String event = "";
		Boolean check = false;
		if (isEventWithinRange(birthday)) {
			Calendar cal = Calendar.getInstance();
			cal.setTime(birthday);
			msg = "Hey! Its your birthday on " + cal.DATE +"/"+cal.MONTH + ". Want to go out??";
			event = "birthday";
			check = true;
		} /*else {
			JSONObject holidays = (JSONObject) holidayData.get("holidays");
			for (Iterator iterator = holidays.keySet().iterator(); iterator.hasNext();) {
				String key = (String) iterator.next();
				Date date1 = new SimpleDateFormat("dd/MM/yyyy").parse(key);
				if (isEventWithinRange(date1)) {
					msg = holidays.get(key).toString() + " is coming up.. Wanna apply leave for that?"
							+ holidays.get(key).toString();
					event = (String) holidays.get(key);
				}
			}
		}*/
		response.put("event", event);
		response.put("message", msg);
		response.put("present", check);
		log.info("returns from function");
		log.info(response.toJSONString());
		return response;
		}catch(Exception e) {
			log.severe("error "+e);
		}
		return response;
	}
	

	public static boolean isEventWithinRange(Date testDate)  {
		log.info("isEventWithRange ");
		Date event_date = new Date();
		try{
		Date today = new SimpleDateFormat("dd/MM/yyyy").parse(new SimpleDateFormat("dd/MM/yyyy").format(event_date));
		String date2 = "31/01/2018";
		Date last = new SimpleDateFormat("dd/MM/yyyy").parse(date2);
		log.info("method returns");
		return testDate.before(today) && last.after(testDate);
		}catch(Exception e){
			log.severe("exception "+e);
		}
		return false;
	}
	
	private static int  getDays(String startDate, String endDate) {
		log.info("get days");
		int days = 0;
		log.info("start date " + startDate + " end date "+endDate);
		if ( (startDate.isEmpty() && endDate.isEmpty())) {
			return 0;
		}
		try {
			Date start = new SimpleDateFormat("yyyy-MM-dd").parse(startDate);
			Date end = new SimpleDateFormat("yyyy-MM-dd").parse(endDate);
			log.info("s :"+ start +" e: "+endDate);
			Calendar calS = Calendar.getInstance();
			calS.setTime(start);
			Calendar calE = Calendar.getInstance();
			calE.setTime(end);
			log.info("cal s :"+ calS +" cal e: "+calE);

			while(calS.compareTo(calE) != 0){
				if (calS.DAY_OF_WEEK != Calendar.SATURDAY || calS.DAY_OF_WEEK != Calendar.SUNDAY) {
					days ++;
					log.info("inc date");
					calS.add(Calendar.DATE, 1);
					log.info("date inc : " + calS);
				}
			}
			days ++;
			System.out.println("days :" + days);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
log.severe("exception getting days count :" + e);		}

		return days;
	}

	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	/*
	private static final Logger log = Logger.getLogger(MyWebhookServlet.class.getName());

	@Override
	protected void doWebhook(AIWebhookRequest input, Fulfillment output) {

		log.info("webhook call");
		String action = input.getResult().getAction();
		HashMap<String, JsonElement> parameter = input.getResult().getParameters();
		// add constants in file
		try {
			switch (action) {
			case "QUERY_LEAVE":
				log.info("in action : query_leave");
				output = queryLeave(output, parameter);
				break;
			case "SYSTEM_SUGESTION_SATISFIED_YES":
				log.info(" intent SYSTEM_SUGESTION_SATISFIED_YES ");
				output = submitFeilds(output, parameter);
				break;
			case "SYSTEM_SUGESTION_SATISFIED_NO":
				log.info("intent : SYSTEM_SUGESTION_SATISFIED_NO");
				output = fallbackCustomApply(output, parameter);
				break;
			case "APPLY_LEAVE_CUSTOM":
				log.info("intent APPLY_LEAVE_CUSTOM");
				output = submitFeilds(output, parameter);
				break;
			case "CONFIRM_LEAVE_YES":
				log.info("intent CONFIRM_LEAVE_YES");
				output = confirmLeave(output, parameter);
				break;
			case "CONFIRM_LEAVE_NO":
				log.info("CONFIRM_LEAVE_NO");
				output = exitFlow(output); // response if yes replan goto custom
				break;
			case "RESTART":
				log.info("intent : restart");
				output = fallbackCustomApply(output, parameter);
				break;
			case "EXIT":
				log.info("exit");
				output = exitFlow(output);
				break;
			case "input.welcome":
				log.info("input.welcome");
				output = eventTriggered(output);
				break;
			default:
				output.setSpeech("Default case");
				break;
			}
		} catch (Exception e) {
			log.info("exception : " + e);
		}
		// output.setSpeech(input.getResult().toString());

		// output.setSpeech("from webhook");
	}

	private Fulfillment eventTriggered(Fulfillment output) {
		
		 * with all params except event go to custom leave apply
		 * 
		 * 
		 
		log.info("event trig fun");
		Map<String, String> outParameter = new HashMap<>();
		AIEvent followupEvent = new AIEvent("event_triggered");
		String message = "Wanna do it yourself?  Okay! I would not give my suggestion, just let me know the details. I will apply for you.";
		followupEvent.setData(outParameter);

		log.info("rerouting to event : evt trg");
		output.setFollowupEvent(followupEvent);
		
		 * output.setSpeech(message); output.setDisplayText(message);
		 
		return output;
	}

	private Fulfillment queryLeave(Fulfillment output, HashMap<String, JsonElement> parameter) throws ParseException {
		// TODO Auto-generated method stub
		log.info("inside queryLeave");
		HashMap<String, Integer> holidayData = new HashMap<>(Data.getHolidays());
		log.info("holiday " + holidayData.toString());
		AIOutputContext contextOut = new AIOutputContext();
		String message = "";
		int balance = holidayData.get("leave_balance");
		log.info("bal :" + balance);
		int days = 0;
		log.info("parms : " + parameter.get("startDate") + " , "+ parameter.get("endDate"));
		HashMap<String, JsonElement> outParameters = new HashMap<String, JsonElement>();
		log.info("parms equal :"+parameter.get("noOfDays").getAsString().isEmpty());
		if (parameter.containsKey("noOfDays") && !(parameter.get("noOfDays").getAsString().isEmpty())) {
			log.info("contains no of days");
			// days = Integer.parseInt(parameter.get("noOfDays"));
			JsonElement contextOutParameter;
			contextOutParameter = new JsonPrimitive(days);
			outParameters.put("noOfDays", contextOutParameter);
		}
		if (parameter.containsKey("startDate") && parameter.containsKey("endDate")) {
			if (! parameter.get("startDate").getAsString().isEmpty()) {
				log.info("start date");
				JsonElement startDate = new JsonPrimitive(parameter.get("startDate").toString());
				outParameters.put("startDate", startDate);

			}
			if (! parameter.get("endDate").getAsString().isEmpty()) {
				log.info("endDate");
				JsonElement endDate = new JsonPrimitive(parameter.get("endDate").toString());
				outParameters.put("endDate", endDate);
			}
			log.info("resp " + ! (parameter.get("endDate").getAsString().isEmpty() && parameter.get("startDate").getAsString().isEmpty()));
			if (!(parameter.get("endDate").getAsString().isEmpty() && parameter.get("startDate").getAsString().isEmpty())) {
				days = getDays(parameter.get("startDate").toString(), parameter.get("endDate").toString());
				JsonElement noOfDay = new JsonPrimitive(days);// fetched no of
				outParameters.put("noOfDays", noOfDay);
				// fetch no of days
			}

		}
		if (parameter.containsKey("event") && ! parameter.get("event").getAsString().isEmpty()) {
			JsonElement contextOutParameter;
			contextOutParameter = new JsonPrimitive(parameter.get("event").getAsString());
			outParameters.put("event", contextOutParameter);
		}
		if (balance <= 0) {
			message = "Sorry dear, you have insufficient leave balance, you will need DP approval If want to apply for leave.";
			contextOut.setLifespan(2);
			contextOut.setName("InsufficientBalance");
			contextOut.setParameters(outParameters);
			output.setContextOut(contextOut);
			log.info("insufficent balance");
		} else if (balance < 3 && days < 3) {
			message = "Your leave balance is low. You are having only " + balance
					+ ". Do you still want to apply for leave ?";
			List<AIOutputContext> contextOutList = new LinkedList<AIOutputContext>();
			AIOutputContext contextOut1 = new AIOutputContext();
			contextOut1.setLifespan(2);
			contextOut1.setName("SystemSugestionSatisfied-Yes");
			contextOut1.setParameters(outParameters);
			contextOutList.add(contextOut1);
			AIOutputContext contextOut2 = new AIOutputContext();
			contextOut2.setLifespan(2);
			contextOut2.setName("SystemSugestionSatisfied-no");
			contextOut2.setParameters(outParameters);
			contextOutList.add(contextOut2);
			log.info("Context out parameters : if low balance");
			output.setContextOut(contextOutList);

		} else if (balance < days) {
			message = "Your leave balance is less than :" + days
					+ ". You will need Delivery partner approval if you will apply. Still wanna apply? Or dear you can apply for "
					+ days + " days.";
			List<AIOutputContext> contextOutList = new LinkedList<AIOutputContext>();
			AIOutputContext contextOut1 = new AIOutputContext();
			contextOut1.setLifespan(2);
			contextOut1.setName("SystemSugestionSatisfied-Yes");
			contextOut1.setParameters(outParameters);
			contextOutList.add(contextOut1);
			AIOutputContext contextOut2 = new AIOutputContext();
			contextOut2.setLifespan(2);
			contextOut2.setName("SystemSugestionSatisfied-no");
			contextOut2.setParameters(outParameters);
			contextOutList.add(contextOut2);
			log.info("Context out parameters : if low balance");
			output.setContextOut(contextOutList);
			log.info("balance < req days");

		} else {
			// api call to check for event
			JSONObject eventResponse = Suggest(parameter);
			if (Boolean.parseBoolean(eventResponse.get("present").toString())) {
				outParameters.put("event", new JsonPrimitive(eventResponse.get("event").toString()));
				message = eventResponse.get("message").toString();
				message += "You have "+ balance + " leaves available";
			}else{
			message = "Hurry you have " + balance + " leaves remaining. You can apply for leave. Shall we proceed or you have a second thought?";
			}
			AIOutputContext contextOut1 = new AIOutputContext();
			contextOut1.setLifespan(2);
			contextOut1.setName("SystemSugestionSatisfied-Yes");
			contextOut1.setParameters(outParameters);
			List<AIOutputContext> contextOutList = new LinkedList<>();
			contextOutList .add(contextOut1);
			AIOutputContext contextOut2 = new AIOutputContext();
			contextOut2.setLifespan(2);
			contextOut2.setName("SystemSugestionSatisfied-no");
			contextOut2.setParameters(outParameters);
			contextOutList.add(contextOut2);
			log.info("Context out parameters : if low balance");
			output.setContextOut(contextOutList);

		}
		output.setDisplayText(message);
		output.setSpeech(message);
		return output;
	}

	private Fulfillment submitFeilds(Fulfillment output, HashMap<String, JsonElement> parameter) {
		log.info("submit feilds");
		String message = "";
		String event = "";
		String comment = "";
		HashMap<String, JsonElement> outParameter = parameter;
		if (! parameter.get("event").getAsString().isEmpty()) {
			event = parameter.get("event").getAsString();
			comment = "Leave for " + event;
			log.info("comment : " + comment);
		}
		if (! parameter.get("comment").getAsString().isEmpty()) {
			comment = parameter.get("comment").getAsString();
		}
		int days = getDays(parameter.get("startDate").getAsString(), parameter.get("endDate").getAsString());
		JSONObject hist = Data.getHolidays();
		if (days < Integer.parseInt(hist.get("leave_balance").toString())) {
			message = "You want to apply for leave from " + parameter.get("startDate").getAsString() + " to "
					+ parameter.get("endDate").getAsString() + comment;
			outParameter.put("comment", new JsonPrimitive(comment));
			List<AIOutputContext> contextOutList = new LinkedList<AIOutputContext>();
			AIOutputContext contextOut1 = new AIOutputContext();
			contextOut1.setLifespan(2);
			contextOut1.setName("CONFIRM_LEAVE_YES");
			contextOut1.setParameters(outParameter);
			contextOutList.add(contextOut1);
			AIOutputContext contextOut2 = new AIOutputContext();
			contextOut2.setLifespan(2);
			contextOut2.setName("CONFIRM_LEAVE_NO");
			contextOut2.setParameters(outParameter);
			contextOutList.add(contextOut2);
			log.info("Context out parameters set");
			output.setContextOut(contextOutList);
		}else{
			message = "Your leave balance is less than :" + days
					+ ". You will need Delivery partner approval if you will apply. Still wanna apply? Or dear you can apply for "
					+ days + " days.";
			List<AIOutputContext> contextOutList = new LinkedList<AIOutputContext>();
			AIOutputContext contextOut1 = new AIOutputContext();
			contextOut1.setLifespan(2);
			contextOut1.setName("SystemSugestionSatisfied-Yes");
			contextOut1.setParameters(outParameter);
			contextOutList.add(contextOut1);
			AIOutputContext contextOut2 = new AIOutputContext();
			contextOut2.setLifespan(2);
			contextOut2.setName("SystemSugestionSatisfied-no");
			contextOut2.setParameters(outParameter);
			contextOutList.add(contextOut2);
			log.info("Context out parameters : if low balance");
			output.setContextOut(contextOutList);
			log.info("balance < req days");
		}
		

		message += " \n please confirm ";
		log.info("message");
		output.setSpeech(message);
		output.setDisplayText(message);
		log.info("setting context");
		
		return output;
	}

	private Fulfillment exitFlow(Fulfillment output) {

		AIOutputContext contextOut = new AIOutputContext();
		output.setContextOut(contextOut); // context reset to ""
		output.setDisplayText("Okay! no issues.");
		return output;
	}

	@SuppressWarnings("unchecked")
	private Fulfillment confirmLeave(Fulfillment output, HashMap<String, JsonElement> parameter) {

		log.info("in confirm leave");
		HashMap<String, JsonElement> outParameters = parameter;
		String message = "";
		AIOutputContext contextOut = new AIOutputContext();
		output.setContextOut(contextOut); // context reset to ""
		HashMap<String, Integer> holidayData = new HashMap<>(Data.getHolidays());
		int leaveBalance = (int) holidayData.get("leave_balance");
		log.info("leave balance : " + leaveBalance);
		int days = getDays(parameter.get("startDate").getAsString(), parameter.get("endDate").getAsString());
		if (leaveBalance < days) {
			message = "Your leave balance is less than :" + days
					+ ". You will need Delivery partner approval for applying. Still wanna apply? Or dear you can apply for "
					+ days + " days or less.";
			List<AIOutputContext> contextOutList = new LinkedList<AIOutputContext>();
			AIOutputContext contextOut1 = new AIOutputContext();
			contextOut1.setLifespan(2);
			contextOut1.setName("SystemSugestionSatisfied-Yes");
			contextOut1.setParameters(outParameters);
			contextOutList.add(contextOut1);
			AIOutputContext contextOut2 = new AIOutputContext();
			contextOut2.setLifespan(2);
			contextOut2.setName("SystemSugestionSatisfied-no");
			contextOut2.setParameters(outParameters);
			contextOutList.add(contextOut2);
			log.info("Context out parameters : if low balance : while confirmation");
			output.setContextOut(contextOutList);

		} else {
			message = "Yeah! your leave has been applied :) ";
			holidayData.put("leave_balane", leaveBalance - 1);

		}

		output.setDisplayText(message);
		output.setSpeech(message);
		return output;
	}

	private Fulfillment fallbackCustomApply(Fulfillment output, HashMap<String, JsonElement> parameter) {
		
		 * with all params except event go to custom leave apply
		 * 
		 * 
		 
		log.info("fallback custom apply");
		HashMap<String, JsonElement> outParameter = parameter;
		String message = "Wanna do it yourself?  Okay! I would not give my suggestion, just let me know the details. I will apply for you. Does this sound good ?";
		List<AIOutputContext> contextOutList = new LinkedList<AIOutputContext>();
		AIOutputContext contextOut1 = new AIOutputContext();
		contextOut1.setLifespan(2);
		contextOut1.setName("confirmLeave-followup"); // send to confirm leave - no
		contextOut1.setParameters(outParameter);
		contextOutList.add(contextOut1);
		AIOutputContext contextOut2 = new AIOutputContext();
		contextOut2.setLifespan(2);
		contextOut2.setName("applyForLeave-custom");
		contextOut2.setParameters(outParameter);
		contextOutList.add(contextOut2);

		output.setContextOut(contextOutList);
		output.setSpeech(message);
		output.setDisplayText(message);
		return output;
	}

	private static JSONObject Suggest(HashMap<String, JsonElement> parameter) throws ParseException {
		JSONObject holidayData = Data.getHolidays();
		String bday = holidayData.get("birthday").toString();
		Date birthday = new SimpleDateFormat("dd/MM/yyyy").parse(bday);
		String msg = "";
		String event = "";
		JSONObject response = new JSONObject();
		if (isEventWithinRange(birthday)) {
			Calendar cal = Calendar.getInstance();
			cal.setTime(birthday);
			msg = "Hey! Its your birthday on " + cal.DATE +"/"+cal.MONTH + ". Want to go out??";
			event = "birthday";
		} else {
			JSONObject holidays = (JSONObject) holidayData.get("holidays");
			for (Iterator iterator = holidays.keySet().iterator(); iterator.hasNext();) {
				String key = (String) iterator.next();
				Date date1 = new SimpleDateFormat("dd/MM/yyyy").parse(key);
				if (isEventWithinRange(date1)) {
					msg = holidays.get(key).toString() + " is coming up.. Wanna apply leave for that?"
							+ holidays.get(key).toString();
					event = (String) holidays.get(key);
				}
			}
		}
		response.put("event", event);
		response.put("message", msg);
		response.put("present", "true");
		return response;
	}

	private static int  getDays(String startDate, String endDate) {
		log.info("get days");
		int days = 0;
		log.info("start date " + startDate + " end date "+endDate);
		if ( (startDate.isEmpty() && endDate.isEmpty())) {
			return 0;
		}
		try {
			Date start = new SimpleDateFormat("yyyy-MM-dd").parse(startDate);
			Date end = new SimpleDateFormat("yyyy-MM-dd").parse(endDate);
			log.info("s :"+ start +" e: "+endDate);
			Calendar calS = Calendar.getInstance();
			calS.setTime(start);
			Calendar calE = Calendar.getInstance();
			calE.setTime(end);
			log.info("cal s :"+ calS +" cal e: "+calE);

			while(calS.compareTo(calE) != 0){
				if (calS.DAY_OF_WEEK != Calendar.SATURDAY || calS.DAY_OF_WEEK != Calendar.SUNDAY) {
					days ++;
					log.info("inc date");
					calS.add(Calendar.DATE, 1);
					log.info("date inc : " + calS);
				}
			}
			days ++;
			System.out.println("days :" + days);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
log.severe("exception getting days count :" + e);		}

		return days;
	}

	
	public static boolean isEventWithinRange(Date testDate)  {
		log.info("isEventWithRange ");
		Date event_date = new Date();
		try{
		Date today = new SimpleDateFormat("dd/MM/yyyy").parse(new SimpleDateFormat("dd/MM/yyyy").format(event_date));
		String date2 = "31/01/2018";
		Date last = new SimpleDateFormat("dd/MM/yyyy").parse(date2);
		System.out.println("method returns");
		return testDate.before(today) && last.after(testDate);
		}catch(Exception e){
			log.severe("exception "+e);
		}
		return false;
	}

*/}
