package com.example;

import java.util.HashMap;
import java.util.logging.Logger;

import org.json.simple.JSONObject;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.util.DateDetails;

import ai.api.model.AIEvent;
import ai.api.model.AIOutputContext;
import ai.api.model.Fulfillment;

public class Redirections {
	private static final Logger log = Logger.getLogger(Redirections.class.getName());
	
	public static Fulfillment redirectToDisplayMessage(Fulfillment output, HashMap<String, JsonElement> parameter){
		log.info("Display text event triggred ");
		AIEvent followupEvent = new AIEvent("DISPLAY_MESSAGE");
		log.info("rerouting to event : evt trg");
		output.setFollowupEvent(followupEvent);
		AIOutputContext contextOut = new AIOutputContext();
		contextOut.setLifespan(1);
		contextOut.setName("displayMessage");
		contextOut.setParameters(parameter);
		output.setContextOut(contextOut);
		return output;
	}
	protected static Fulfillment redirectToTerminate(Fulfillment output){
		log.info("exit event trig fun");
		AIEvent followupEvent = new AIEvent("TERMINATE");
		log.info("rerouting to event TERMINATE: evt trg");
		output.setFollowupEvent(followupEvent);
		return output;
	}
	protected static Fulfillment redirectToDPApproval(Fulfillment output, HashMap<String, JsonElement> parameter){
		AIEvent followupEvent = new AIEvent("DP_APPROVAL");
		log.info("rerouting to event DP_APPROVAL: evt trg");
		output.setFollowupEvent(followupEvent);
		return output;
	}
	
	protected static Fulfillment redirectToSuggestLeaveOption(Fulfillment output, HashMap<String, JsonElement> parameter){
		log.info("redirect to event without asking dates event trig fun");
		log.info("parms : " + parameter.get("startDate") + " end date "+parameter.get("endDate") +" comment "+ parameter.get("comment"));
		AIEvent followupEvent = new AIEvent("SUGGEST_LEAVES_OPTION");
		log.info("rerouting to event : evt trg");
		output.setFollowupEvent(followupEvent);
		AIOutputContext contextOut = new AIOutputContext();
		contextOut.setLifespan(1);
		contextOut.setName("leaveParms");
		contextOut.setParameters(parameter);
		output.setContextOut(contextOut);
		return output;
	}
	protected static Fulfillment redirectToCustomApply(Fulfillment output, HashMap<String, JsonElement> parameter) {
		// Trigger event for custom leave apply
		log.info("redirectToCustomApply event trig fun");
		AIEvent followupEvent = new AIEvent("CUSTOM_FORM");
		log.info("rerouting to event : evt trg");
		AIOutputContext contextOut = new AIOutputContext();
		contextOut.setLifespan(1);
		contextOut.setName("customFormContext");
		contextOut.setParameters(parameter);
		output.setContextOut(contextOut);
		output.setFollowupEvent(followupEvent);
		return output;
	}
	protected static Fulfillment redirectToQueryLeaveWithParms(Fulfillment output, HashMap<String, JsonElement> parameter) {
		// Trigger event for custom leave apply
		log.info("redirectToQueryLeaveWithParms event trig fun");
		AIEvent followupEvent = new AIEvent("QUERY_LEAVE_PARMS");
		log.info("rerouting to event : evt trg");
		AIOutputContext contextOut = new AIOutputContext();
		contextOut.setLifespan(1);
		contextOut.setName("leaveParms");
		contextOut.setParameters(parameter);
		output.setContextOut(contextOut);
		output.setFollowupEvent(followupEvent);
		return output;
	}
	protected static Fulfillment redirectToComboLeaveForm(Fulfillment output, HashMap<String, JsonElement> parameter,
			String sessionId) {
		log.info("redirectToComboLeaveForm event trig fun");
		String startDate = parameter.get("startDate").getAsString().trim();
		String endDate = parameter.get("endDate").getAsString().trim();
		String comment = parameter.get("comment").getAsString().trim();
		JSONObject data = Data.getHolidays(sessionId);
		int PL = Integer.parseInt(data.get("privillage_leave").toString());
		int OH = Integer.parseInt(data.get("optional_holiday").toString());
		int OL = Integer.parseInt(data.get("optional_leave").toString());
		int CF = Integer.parseInt(data.get("compensatiory_off").toString());
		
		int noOfLeave = Integer.parseInt(DateDetails.getDays(startDate, endDate).get("days").toString().trim());
		log.info("no of leaves: " + noOfLeave + " triggering event combo leave");
		AIEvent followupEvent = new AIEvent("CUSTOMIZE_LEAVE_TYPE");
		log.info("re-routing to event : evt trg");
		output.setFollowupEvent(followupEvent);
		AIOutputContext contextOut = new AIOutputContext();
		HashMap<String, JsonElement> outParms = new HashMap<>();
		outParms.put("comment", new JsonPrimitive(comment));
		outParms.put("startDate", new JsonPrimitive(startDate));
		outParms.put("endDate", new JsonPrimitive(endDate));
		outParms.put("presentPL", new JsonPrimitive(PL));
		outParms.put("presentOL", new JsonPrimitive(OL));
		outParms.put("presentOH", new JsonPrimitive(OH));
		outParms.put("presentCF", new JsonPrimitive(CF));
		outParms.put("noOfLeave", new JsonPrimitive(noOfLeave));
		contextOut.setLifespan(1);
		contextOut.setName("sugestLeaveOption");
		contextOut.setParameters(outParms);
		// set cont.
		output.setContextOut(contextOut);
		return output;
	}
	public static Fulfillment redirectToDisplayMessage(Fulfillment output, String message) {
		// TODO Auto-generated method stub
		log.info("Display text event triggred ");
		AIEvent followupEvent = new AIEvent("DISPLAY_MESSAGE");
		log.info("rerouting to event : evt trg");
		output.setFollowupEvent(followupEvent);
		AIOutputContext contextOut = new AIOutputContext();
		HashMap<String, JsonElement> outParms = new HashMap<>();
		outParms.put("comment", new JsonPrimitive(message));
		contextOut.setLifespan(1);
		contextOut.setName("displayMessage");
		contextOut.setParameters(outParms);
		return null;
	}
}
