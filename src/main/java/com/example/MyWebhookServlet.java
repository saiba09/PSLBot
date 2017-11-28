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
import org.json.simple.parser.JSONParser;

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
		String sessionId = input.getSessionId();
		log.info(sessionId);
		// add constants in file
		log.info("action : " +action);
		try {
			switch (action) {
			case "QUERY_LEAVE":
				log.info("in action : query_leave");
				output = queryLeave(output, parameter,sessionId); // check for oh , cf etc
				break;
			case "SYSTEM_SUGESTION_SATISFIED_YES":
				log.info(" intent SYSTEM_SUGESTION_SATISFIED_YES ");
				output = getConfirmationMessage(output, parameter, "SYSTEM_SUGESTION_SATISFIED_YES",sessionId); 
				break;
			case "SYSTEM_SUGESTION_SATISFIED_NO":
				log.info("intent : SYSTEM_SUGESTION_SATISFIED_NO");
				//output = getConfirmationMessage(output, parameter, "SYSTEM_SUGESTION_SATISFIED_NO",sessionId);
				output = exitFlow(output);
				break;
			case "SUGGEST_LEAVES_OPTION":
				log.info("intent : SUGGEST_LEAVES_OPTION");
				output = getLeaveComboSuggestion(output, parameter, "SUGGEST_LEAVES_OPTION",sessionId); 
				// give suggestion for oh pl combo, calc no of days,
				break;
			//case not found
			case "OPT_CUSTOM_REQ":
				log.info("intent: OPT_CUSTOM_REQ");
				output = redirectToCustomApply(output, parameter);
				break;
			case "CONFIRM_APPLY":
				log.info("intent : CONFIRM_APPLY");
				output = applyLeave(output, parameter,sessionId);
				break;
			//not found
			case "SYST_SUG_NOT_SATISFIED_CUST_CONFIRM":
				log.info("SYST_SUG_NOT_SATISFIED_CUST_CONFIRM");
				output = applyLeave(output, parameter,sessionId); // APPLY LEAVE IN SYSTEM
				break;
				
			case "CUSTOM_FORM_SUBMIT":
				log.info("intent : CUSTOM_FORM_SUBMIT");
				output = getConfirmationMessage(output, parameter,"CUSTOM_FORM_SUBMIT",sessionId);
				break;
				
			case "APPLY_COMBO_LEAVE" :
				log.info("intent : APPLY_COMBO_LEAVE");
				output = redirectToComboLeaveForm(output, parameter,sessionId);
				break;
				
			case "CUSTOM_FORM_SUBMIT_CONFIRM":
				log.info("INTENT : CUSTOM_FORM_SUBMIT_CONFIRM");
				output = applyLeave(output, parameter,sessionId);
				break;
				
			case "LEAVE_TYPE_SELECTION":
				log.info("intent : LEAVE_TYPE_SELECTION_CONFRIM_MESSAGE");
				output = getLeaveBreakup(output, parameter,sessionId);
				// message for confirmation returns leave brk up
				break;
			//case not found
			case "LEAVE_TYPE_SELECTION_CONFRIM_MESSAGE":
				log.info("intent : LEAVE_TYPE_SELECTION_CONFRIM_MESSAGE");
				// apply leave
				/*
				 * case "input.welcome": log.info("input.welcome"); output =
				 * eventTriggered(output); break;
				 */
				break;
			
			case "CONFIRM_LEAVE_APPLY":
				log.info("intent : CONFIRM_LEAVE_APPLY");
				output = applyLeaveWithTypes(output,parameter);
				break;
			
			case "AGAIN_SELECT_LEAVE_TYPE": 
				log.info("intent :AGAIN_SELECT_LEAVE_TYPE");
				output = redirectToComboLeaveForm(output, parameter,sessionId);
				break;
			//not found
			case "SYST_SUG_NOT_SATISFIED_CUST_CONFIRM_SUGGEST_TYPES":
				log.info("intent : SYST_SUG_NOT_SATISFIED_CUST_CONFIRM_SUGGEST_TYPES");
				output =  getLeaveComboSuggestion(output, parameter, "SYST_SUG_NOT_SATISFIED_CUST_CONFIRM_SUGGEST_TYPES" ,sessionId); 
				break;
			case "GET_LEAVE_BALANCE":
				log.info("intent : getLeaveBalance");
				output = getLeaveBalance(output, parameter, sessionId);
				break;
			case "FALLBACK":
				log.info("intent : fallback");
				output = getFallBackResponse(output,input);
				break;
			default:
				output.setSpeech("Default case");
				break;
			}
		} catch (Exception e) {
			log.info("exception : " + e);
		}

	}

	private Fulfillment getLeaveBalance(Fulfillment output, HashMap<String, JsonElement> parameter, String sessionId) {
		
		JSONObject employeeData =  Data.getHolidays(sessionId);
		int PL = Integer.parseInt(employeeData.get("privillage_leave").toString());
		int OH = Integer.parseInt(employeeData.get("optional_holiday").toString());
		int OL = Integer.parseInt(employeeData.get("optional_leave").toString());
		int CF = Integer.parseInt(employeeData.get("compensatiory_off").toString());

		output.setDisplayText("You have "+PL+" privileged leave, "+OL+" optional leave, "+OH+" optional holiday and "+CF+" compensatory Off"+" #false");
		output.setSpeech("You have "+PL+" privileged leave, "+OL+" optional leave, "+OH+" optional holiday and "+CF+" compensatory Off"+" #false");
		
		return output;
	}

	private Fulfillment getFallBackResponse(Fulfillment output, AIWebhookRequest input) {
		// TODO Auto-generated method stub
		String question = input.getResult().getResolvedQuery();
		JSONObject response = SearchFunction.fetchAnswerFromDatastore(question);
		log.info(response.toJSONString());
		String answer = (String) response.get("answer") + "#false";
		output.setDisplayText(answer);
		output.setSpeech(answer);
		return output;
	}

	private Fulfillment applyLeaveWithTypes(Fulfillment output, HashMap<String, JsonElement> parameter) {
		String startDate = parameter.get("startDate").getAsString().trim();
		String endDate = parameter.get("endDate").getAsString().trim();
		String comment = parameter.get("comment").getAsString().trim();
		String leaveBreakUp = parameter.get("leaveBreakUp").getAsString().trim();
		String message = "Your leaves had been applied.#false";
		output.setDisplayText(message);
		output.setSpeech(message);
		return output;
	}

	private Fulfillment getLeaveBreakup(Fulfillment output, HashMap<String, JsonElement> parameter , String sessionId) {
		// TODO Auto-generated method stub
		String startDate = parameter.get("startDate").getAsString().trim();
		String endDate = parameter.get("endDate").getAsString().trim();
		String comment = parameter.get("comment").getAsString().trim();
		JSONObject data = Data.getHolidays(sessionId);
		int noPL = Integer.parseInt(parameter.get("noPL").toString().trim());
		int noOL = Integer.parseInt(parameter.get("noOL").toString().trim());
		int noOH = Integer.parseInt(parameter.get("noOH").toString().trim());
		int noCF = Integer.parseInt(parameter.get("noCF").toString().trim());
		log.info("applied for : PL " + noPL + " CF "+noCF + " OH  "+ noOH + "  OL  "+noOL  );
		int PL = Integer.parseInt(data.get("privillage_leave").toString());
		int OH = Integer.parseInt(data.get("optional_holiday").toString());
		int OL = Integer.parseInt(data.get("optional_leave").toString());
		int CF = Integer.parseInt(data.get("compensatiory_off").toString());
		log.info(" pressent values : PL "+PL + " CF "+CF +" OH "+OH +"  OL  "+OL);
		String message = "";
		JSONObject leaveJson = new JSONObject();
		int sum = noCF+noOH+noOL+noPL;
		int days = Integer.parseInt(getDays(startDate, endDate).get("days").toString().trim());
		log.info("sum : "+ sum + " days : "+days);
		if (sum == days) {
			//APPLY LEAVE
			message = "So you want to apply from "+startDate.toString() + " to " + endDate.toString() + " as " +comment+" of which "; 	

			if (noPL != 0 && noPL <= PL) {
				leaveJson.put("PL", noPL);
				message += noPL +" are privileged leave";
			}
			if (noCF != 0 && noCF <= CF) {
				leaveJson.put("CF", noCF);
				message += noCF +" are compensatory offs ";
			}
			if (noOH != 0 && noOH <= OH) {
				leaveJson.put("OH", noOH);
				message += noOH +" are optional holiday ";
			}
			if (noOL != 0 && noOL <= OL) {
				leaveJson.put("OL", noOL);
				message += noOL +" are optional leave ";
			}
			message += " Please confirm.";
		}else{
			//sorry someting went wrong.
			//Go back to  
			message = "You have entered wrong combination. Try again!";
			
		}
		AIOutputContext contextOut = new AIOutputContext();
		HashMap<String, JsonElement> outParms = new HashMap<>();
		outParms.put("leaveBreakUp", new JsonPrimitive(leaveJson.toJSONString()));
		contextOut.setLifespan(1);
		contextOut.setName("allOtherPresent-followup");
		contextOut.setParameters(outParms);
		output.setContextOut(contextOut);
		output.setDisplayText(message);
		output.setSpeech(message);
		return output;
	}

	private Fulfillment redirectToComboLeaveForm(Fulfillment output, HashMap<String, JsonElement> parameter, String sessionId) {
		log.info("redirectToComboLeaveForm event trig fun");
		String startDate = parameter.get("startDate").getAsString().trim();
		String endDate = parameter.get("endDate").getAsString().trim();
		String comment = parameter.get("comment").getAsString().trim();
		JSONObject data = Data.getHolidays(sessionId);
		int PL = Integer.parseInt(data.get("privillage_leave").toString());
		int OH = Integer.parseInt(data.get("optional_holiday").toString());
		int OL = Integer.parseInt(data.get("optional_leave").toString());
		int CF = Integer.parseInt(data.get("compensatiory_off").toString());
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

		contextOut.setLifespan(1);
		contextOut.setName("sugestLeaveOption");
		contextOut.setParameters(outParms);
		// set cont.
		output.setContextOut(contextOut);
		return output;
	}

	private Fulfillment getLeaveComboSuggestion(Fulfillment output, HashMap<String, JsonElement> parameter,
			String action , String sessionId) {
		log.info("getLeaveComboSuggestion");
		String startDate = parameter.get("startDate").getAsString().trim();
		String endDate = parameter.get("endDate").getAsString().trim();
		String comment = "";
		log.info("parms :" + startDate + " " + endDate);
		comment = parameter.get("comment").getAsString().trim();
		log.info("comment " + comment);
		JSONObject sugestion = Data.getHolidays(sessionId);
		String message = "";
		// check leave balance > days to apply
		int leave_balance = Integer.parseInt(getLeaveInfo(sessionId).get("count").toString());
		JSONObject jsonDays = getDays(startDate, endDate);
		int noOfLeaves = Integer.parseInt(jsonDays.get("days").toString());
		JSONObject data = Data.getHolidays(sessionId);
		int PL = Integer.parseInt(data.get("privillage_leave").toString());
		int OH = Integer.parseInt(data.get("optional_holiday").toString());
		int OL = Integer.parseInt(data.get("optional_leave").toString());
		int CF = Integer.parseInt(data.get("compensatiory_off").toString());
		log.info("balance :" + leave_balance + " required :" + noOfLeaves);
		if (PL < noOfLeaves ) {
			message = "Your privileged leave balance is less than what is you opt. for "+noOfLeaves+ " days and applying for same will need DP approval,However You can take a combination of other leaves type."+
						" Do you want to continue with privilage leaves?";
			/*
			if (leave_balance >= noOfLeaves) {
				message = "Your privillage leave balance is low, you can "; /////
				if (PL != 0) {
					int diff = noOfLeaves - PL;
					if (CF != 0) {
						if (CF >= diff) {
							message += "You have "+ PL+ "privilage leave and "+ CF+" comp. offs. Do you want to consume both in a go? ";
						}
						else{
							if (OH != 0 && diff<= OH) {
								
							}
						}
					}
				}
				if (noOfLeaves <= CF) {
					message += "apply for comp. offs. As you have  "+CF +" available.";
				}
				else{
					
				}
			}
		*/} 
		else{
			if (CF != 0 || OH != 0 || OL != 0) {
				message += "You also have ";
			}
			if (CF != 0) {
				message += CF+" compensatory off, ";

			}
			if (OH != 0) {
				message += OH+ " optional holiday, ";
			}
			if (OL != 0) {
				message += OL+ " optional leave.";
			}
			if (CF != 0 || OH != 0 || OL != 0) {
			//	message += "Do you still want to apply for privilage leave ? Don't forget these leaves won't carry forward.";
				message += "Do you still want to apply for privilaged leave?.";
			}
		}
		log.info(message);
		//context : system-sugestion-satisfied-yes-yes-suggestOptions-followup
		/*AIOutputContext contextOut = new AIOutputContext();
		HashMap<String, JsonElement> outParms = new HashMap<>();
		outParms.put("comment", new JsonPrimitive(comment));
		outParms.put("startDate", new JsonPrimitive(startDate));
		outParms.put("endDate", new JsonPrimitive(endDate));
		contextOut.setLifespan(1);
		contextOut.setName("system-sugestion-satisfied-yes-yes-suggestOptions-followup");
		contextOut.setParameters(outParms);
		// set cont.
		output.setContextOut(contextOut);*/
		output.setSpeech(message);
		output.setDisplayText(message);
		return output;

	}

	private Fulfillment exitFlow(Fulfillment output) {
		// TODO Auto-generated method stub TERMINATE
		log.info("redirectToCustomApply event trig fun");
		AIEvent followupEvent = new AIEvent("TERMINATE");
		log.info("rerouting to event TERMINATE: evt trg");
		output.setFollowupEvent(followupEvent);
		return output;
		
	}

	private Fulfillment redirectToCustomApply(Fulfillment output, HashMap<String, JsonElement> parameter) {
		// Trigger event for custom leave apply
		log.info("redirectToCustomApply event trig fun");
		AIEvent followupEvent = new AIEvent("CUSTOM_FORM");
		log.info("rerouting to event : evt trg");
		output.setFollowupEvent(followupEvent);
		return output;
	}

	private Fulfillment applyLeave(Fulfillment output, HashMap<String, JsonElement> parameter, String sessionId) {
		log.info("apply leave function");
		String startDate = parameter.get("startDate").getAsString().trim();
		String endDate = parameter.get("endDate").getAsString().trim();
		String comment = parameter.get("comment").getAsString().trim();
		log.info("parms :" + startDate + " " + endDate + " comment : " + comment);
		String message = "";
		int leave_balance = Integer.parseInt(Data.getHolidays(sessionId).get("privillage_leave").toString());
		// check bal if allow apply
		JSONObject jsonDays = getDays(startDate, endDate);
		int noOfLeaves = Integer.parseInt(jsonDays.get("days").toString());
		if (leave_balance <= 0) {
			log.info("bal < 0");
			message = "Sorry, you have insufficient leave balance, you will need DP approval If want to apply for leave.";
			// Trigger dp approval intent
			log.info("DP APPROVAL REQ event trig ");
			AIEvent followupEvent = new AIEvent("DP_APPROVAL");
			log.info("rerouting to event : evt trg");
			output.setFollowupEvent(followupEvent);
		} else if (leave_balance >= noOfLeaves) {
			log.info("bal < no Leaves ");
			message = "Here you go! Your leaves had been applied in the system.";
		} else {
			message = "Your leave balance is less than :" + noOfLeaves + ". You will need Delivery partner approval.";
			// set event trigg.
			log.info("DP APPROVAL REQ event trig ");
			AIEvent followupEvent = new AIEvent("DP_APPROVAL");
			log.info("rerouting to event : evt trg");
			output.setFollowupEvent(followupEvent);
		}
		// else abort
		log.info(message);
		message += "#false";
		output.setSpeech(message);
		output.setDisplayText(message);
		return output;
	}

	private Fulfillment queryLeave(Fulfillment output, HashMap<String, JsonElement> parameter, String sessionId) throws ParseException {
		log.info("querry leave function");
		String startDate = parameter.get("startDate").getAsString().trim();
		String endDate = parameter.get("endDate").getAsString().trim();
		String event = parameter.get("event").getAsString().trim();
		log.info("parms :" + startDate + " " + endDate + " event: " + event);
		String message = "";
		//message += " " + getLeaveInfo(sessionId).get("message") + "  ";
		JSONObject sugestion = Suggest(parameter, sessionId);
		int leave_balance = Integer.parseInt(getLeaveInfo(sessionId).get("count").toString());
		if (leave_balance > 0) {
			if (event.isEmpty() && startDate.isEmpty() && endDate.isEmpty()) {
				message += "You have "+leave_balance+" Leave balance, shall we proceed?";
			}
			else {
				log.info("redirect to event without asking dates event trig fun");
				AIEvent followupEvent = new AIEvent("SUGGEST_LEAVES_OPTION");
				log.info("rerouting to event : evt trg");
				output.setFollowupEvent(followupEvent);
				}
			}
		else{
			//message = "Your l You will need Delivery partner approval.";
			// set event trigg.
			log.info("DP APPROVAL REQ event trig ");
			AIEvent followupEvent = new AIEvent("DP_APPROVAL");
			log.info("rerouting to event : evt trg");
			output.setFollowupEvent(followupEvent);
		}
			/*
			if (Boolean.parseBoolean(sugestion.get("present").toString())) {
				log.info("do have a suggestion");
				if (event.isEmpty()) {
					log.info("event suggested");
					event = sugestion.get("event").toString();
					message += sugestion.get("message").toString();
					AIOutputContext contextOut = new AIOutputContext();
					HashMap<String, JsonElement> outParms = new HashMap<>();
					outParms.put("comment", new JsonPrimitive("leave for " + event));
					outParms.put("event", new JsonPrimitive(event));

					contextOut.setLifespan(1);
					contextOut.setName("QueryLeave-followup");
					contextOut.setParameters(outParms);
					// set cont.
					output.setContextOut(contextOut);
				} else {
					log.info("already had an event");
					message += "You have sufficient leave balance. apply";
					// triggre event
					log.info("redirectToCustomApply event trig fun");
					AIEvent followupEvent = new AIEvent("CUSTOM_FORM");
					log.info("rerouting to event : evt trg");
					output.setFollowupEvent(followupEvent);
				}
			} else {
				log.info("already had an event");
				message += "So shall we proceed ?";
				// triggre event
				log.info("redirectToCustomApply event trig fun");
				AIEvent followupEvent = new AIEvent("CUSTOM_FORM");
				log.info("rerouting to event : evt trg");
				output.setFollowupEvent(followupEvent);
			}
		*/
			
		
		log.info(message);
		output.setSpeech(message);
		output.setDisplayText(message);
		return output;
	}

	private Fulfillment getConfirmationMessage(Fulfillment output, HashMap<String, JsonElement> parameter , String action, String sessionId) {
		log.info("getConfirmationMessage");
		String startDate = parameter.get("startDate").getAsString().trim();
		String endDate = parameter.get("endDate").getAsString().trim();
		String comment = "";
		String event = "";
		JSONObject sugestion = Data.getHolidays(sessionId);
		String message = "";
		log.info("parms :" + startDate + " " + endDate);
		if (parameter.containsKey("comment")) {
			comment = parameter.get("comment").getAsString().trim();
			log.info("comment " + comment);
		} else {
			event = parameter.get("event").getAsString().trim();
			comment = getMessage(event);
			log.info("event :" + event + " comment :" + comment);
		}
		// check leave balance > days to apply
		int leave_balance = Integer.parseInt(getLeaveInfo(sessionId).get("count").toString());
		JSONObject jsonDays = getDays(startDate, endDate);
		int noOfLeaves = Integer.parseInt(jsonDays.get("days").toString()); 
		log.info("balance :" + leave_balance + " required :" + noOfLeaves);
		if (leave_balance <= 0 || leave_balance < noOfLeaves) {
			log.info("bal < 0");
			message = "Sorry, you have insufficient leave balance, you will need DP approval If want to apply for leave.";
			// triggre dp approval inteent
			log.info("DP APPROVAL REQ event trig ");
			AIEvent followupEvent = new AIEvent("DP_APPROVAL");
			log.info("rerouting to event : evt trg");
			output.setFollowupEvent(followupEvent);
		} else if (leave_balance >= noOfLeaves) {
			log.info("req > bal");
			
			message = "So you want to apply from "+startDate.toString() + " to " + endDate.toString() + " as  " +comment; 
			if (Boolean.parseBoolean(jsonDays.get("isWeekEnd").toString())) {
				log.info(" dates contains weekend in Between");
				HashMap<Date, String> holidayMap = (HashMap<Date, String>) jsonDays.get("holidayTrack");
				log.info("holiday map fetched");
				message += " However its, ";
				for (Date date : holidayMap.keySet()) {
					String day = holidayMap.get(date).toString();
					message += "  " +day+" on "+new SimpleDateFormat("MMM d").format(date);
				}
				log.info("message for weekend addded");
				message += " shall we continue the plan ?";
			}else{
				log.info("no weekend in between ");
				//message += " No weekends or holidays in between. Are you sure you wanna plan this vaccation ?";
			}
			message += " Should I confirm?";
			if (action.equalsIgnoreCase("SYSTEM_SUGESTION_SATISFIED_YES")) {
				log.info("for action :  SYSTEM_SUGESTION_SATISFIED_YES");
				AIOutputContext contextOut = new AIOutputContext();
				HashMap<String, JsonElement> outParms = new HashMap<>();
				outParms.put("comment", new JsonPrimitive(comment));
				outParms.put("startDate",new JsonPrimitive(startDate));
				outParms.put("endDate",new JsonPrimitive(endDate));

				contextOut.setLifespan(1);
				contextOut.setName("system-sugestion-satisfied-yes-follow-up");
				contextOut.setParameters(outParms);
				// set cont.
				output.setContextOut(contextOut);
			}
			if (action.equalsIgnoreCase("SYSTEM_SUGESTION_SATISFIED_NO")) {/*
				log.info("for action :  SYSTEM_SUGESTION_SATISFIED_NO");
				AIOutputContext contextOut = new AIOutputContext();
				HashMap<String, JsonElement> outParms = new HashMap<>();
				outParms.put("comment", new JsonPrimitive(comment));
				outParms.put("startDate",new JsonPrimitive(startDate));
				outParms.put("endDate",new JsonPrimitive(endDate));

				contextOut.setLifespan(1);
				contextOut.setName("system-sugestion-satisfied-yes-follow-up");
				contextOut.setParameters(outParms);
				// set cont.
				output.setContextOut(contextOut);
			*/}
			/*			
	*/
			output.setSpeech(message);
			output.setDisplayText(message);
		} else {
			message = "Your leave balance is less than : " + noOfLeaves
					+ ". You will need Delivery partner approval if you will apply. ";
			log.info(message);
			output.setSpeech(message);
			output.setDisplayText(message);
			AIOutputContext contextOut = new AIOutputContext();
			HashMap<String, JsonElement> outParms = parameter;
			outParms.put("comment", new JsonPrimitive(comment));
			contextOut.setLifespan(1);
			contextOut.setName("system-sugestion-satisfied-no-follow-up");
			contextOut.setParameters(outParms);
			// set cont.
			output.setContextOut(contextOut);
			// IMP : set out parmas end date- diff
		}
		log.info(message);

		return output;
	}

	private static String getMessage(String event) {
		// TODO Auto-generated method stub
		return "Leave for " + event;
	}

	private static JSONObject Suggest(HashMap<String, JsonElement> parameter , String sessionId) {
		log.info("suggest called");
		JSONObject holidayData = Data.getHolidays(sessionId);
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
				msg = " And! Its your birthday on " + new SimpleDateFormat("MMM d").format(birthday)
						+ ". Want to go out??";
				event = "birthday";
				check = true;
			} else {
				JSONObject holidays = (JSONObject) holidayData.get("holidays");
				for (Iterator iterator = holidays.keySet().iterator(); iterator.hasNext();) {
					String key = (String) iterator.next();
					Date date1 = new SimpleDateFormat("dd/MM/yyyy").parse(key);
					if (isEventWithinRange(date1)) {
						msg = holidays.get(key).toString() + " is coming up.. Wanna apply leave for that?"
								+ holidays.get(key).toString();
						event = (String) holidays.get(key);
						check = true;
					}
				}
			}
			response.put("event", event);
			response.put("message", msg);
			response.put("present", check);
			log.info("returns from function");
			log.info(response.toJSONString());
			return response;
		} catch (Exception e) {
			log.severe("error " + e);
		}

		return response;
	}

	private static JSONObject getLeaveInfo(String sessionId) {
		String message = "";
		JSONObject data = Data.getHolidays(sessionId);
		
		log.info("data : "+data.toJSONString());
		
		int PL = Integer.parseInt(data.get("privillage_leave").toString());
		int OH = Integer.parseInt(data.get("optional_holiday").toString());
		int OL = Integer.parseInt(data.get("optional_leave").toString());
		int CF = Integer.parseInt(data.get("compensatiory_off").toString());
		
		log.info("recieved leaves");
		
		boolean isAvailable = false;
		int count = PL + CF + OH + OL;
		JSONObject response = new JSONObject();
		response.put("isAvailable", isAvailable);
		response.put("count", count);

		if (PL <= 0) {
			isAvailable = true;
			message = "You don't have any privilage leave available.";
			if (CF <= 0) {
				log.info("no cf");
				if (OH <= 0) {
					log.info("no oh");
					if (OL <= 0) {
						log.info("no OL");
						message = "Sorry dear! You don't have any leave balance. ";
						isAvailable = false;
					} else {
						message = "You only have " + OL + " optional leaves left. Are you sure you want to apply now ?";
					}
				} else {
					if (OL <= 0) {
						log.info("no OL");
						message = "Hey! Buddy. You only have " + OH
								+ " optional holidays left. Are you sure you want to apply now ? ";
					} else {
						message = "You have " + OL + " optional leaves, and " + OH
								+ " optional holiday left. Are you sure you want to apply now ?";
					}
				}
			} else {
				log.info("cf avl");
				if (OH <= 0) {
					log.info("no oh");
					if (OL <= 0) {
						log.info("no OL");
						message = "Great! you have " + CF
								+ " comp. off available Do consume it, you won't be able to carry forward it.";
					} else {
						message = "Great! you have " + CF
								+ " comp. off available Do consume it, you won't be able to carry forward it.Also, you have "
								+ OL + " optional leave. You can use it too.";
					}
				} else {
					if (OL <= 0) {
						log.info("no OL");
						message = "Great! you have " + CF + " comp. off and " + OH
								+ " optional leaves available Do consume it, they won't be carry forward. I know you don't wanna lose them";
					} else {
						message = "Great! you have " + CF + " comp. off , " + OH + " optional leaves" + OL
								+ " optional leave available Do consume it, they won't be carry forward. I know you don't wanna lose them";
					}
				}
			}
		} else {
			message = "You don't have any privilage leave available.";
			if (CF <= 0) {
				log.info("no cf");
				if (OH <= 0) {
					log.info("no oh");
					if (OL <= 0) {
						log.info("no OL");
						message = "Sorry dear! You don't have any leave balance. ";
					} else {
						message = "You only have " + OL + " optional leaves left. Are you sure you want to apply now ?";
					}
				} else {
					if (OL <= 0) {
						log.info("no OL");
						message = "Hey! Buddy. You only have " + OH
								+ " optional holidays left. Are you sure you want to apply now ? ";
					} else {
						message = "You have " + OL + " optional leaves, and " + OH
								+ " optional holiday left. Are you sure you want to apply now ?";
					}
				}
			} else {
				log.info("cf avl");
				if (OH <= 0) {
					log.info("no oh");
					if (OL <= 0) {
						log.info("no OL");
						message = "Buddy! you have " + PL + " previlage leave with you."
								+ "But I would suggest you should take compensatiory leaves as you have " + CF
								+ " comp. off available Do consume it, I know you don't wanna lose them.";
					} else {
						message = "Buddy! you have " + PL + " previlage leave with you."
								+ "But I would suggest you should take compensatiory leaves as you have " + CF
								+ " comp. off available. However you can also opt for optional leave as you have " + OL
								+ " optional leave available.Do consume it, I know you don't wanna lose them.";
					}
				} else {
					if (OL <= 0) {
						log.info("no OL");
						message = "Buddy! you have " + PL + " previlage leave with you."
								+ "But I would suggest you should take compensatiory leaves as you have " + CF
								+ " comp. off available. However you can also opt for optional leave as you have " + OL
								+ " optional leave available.Do consume it, I know you don't wanna lose them.";
					} else {
						message = "You have " + PL + " privilage leave " + CF + " comp. offs " + OH
								+ " optional holidays and " + OL
								+ " optional leaves with you. So its your call which you wanna consume.Still I would suggest do use out the leaves who don't carry forward.";
					}
				}
			}

		}
		response.put("message", message);
		return response;
	}

	public static boolean isEventWithinRange(Date testDate) {
		log.info("isEventWithRange ");
		Date event_date = new Date();
		try {
			Date today = new SimpleDateFormat("dd/MM/yyyy")
					.parse(new SimpleDateFormat("dd/MM/yyyy").format(event_date));
			String date2 = "31/04/2018";
			Date last = new SimpleDateFormat("dd/MM/yyyy").parse(date2);
			log.info("method returns");
			return testDate.after(today) || testDate.before(last);
		} catch (Exception e) {
			log.severe("exception " + e);
		}
		return false;
	}

	private static JSONObject getDays(String startDate, String endDate) {
		log.info("get days");
		int days = 0;
		boolean isWeekEnd = false;
		JSONObject response = new JSONObject();
		JSONObject holidayTrack = new JSONObject();
		response.put("days", days);
		response.put("holidayTrack", holidayTrack);
		response.put("isWeekEnd", isWeekEnd);
		log.info("start date " + startDate + " end date " + endDate);
		if ((startDate.isEmpty() && endDate.isEmpty())) {
			return response;
		}
		try {
			Date start = new SimpleDateFormat("yyyy-MM-dd").parse(startDate);
			Date end = new SimpleDateFormat("yyyy-MM-dd").parse(endDate);
			log.info("s :" + start + " e: " + endDate);
			Calendar calS = Calendar.getInstance();
			calS.setTime(start);
			Calendar calE = Calendar.getInstance();
			calE.setTime(end);
			log.info("cal s :" + calS + " cal e: " + calE);

			do {
				
				if (calS.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY){
					holidayTrack.put(calS.getTime(), "Saturday");
					log.info( Calendar.SATURDAY + " : on "+calS.DATE + " saturday");
					calS.add(Calendar.DATE, 1);
					isWeekEnd = true;
				}
				else if(calS.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
					holidayTrack.put(calS.getTime(), "Sunday");
					log.info( Calendar.SUNDAY + " : on "+calS.DATE + " sunday");
					calS.add(Calendar.DATE, 1);
					isWeekEnd = true;

				}else {
					
					days++;
					log.info("inc date");
					calS.add(Calendar.DATE, 1);
					log.info("date inc : " + calS.DATE + " " + calS.MONTH);
					/*if (calS.DAY_OF_WEEK == Calendar.SATURDAY) {
						holidayTrack.put(calS, "Saturday");
						log.info( Calendar.SATURDAY + " : on "+calS.DATE);

					} else if (calS.DAY_OF_WEEK == Calendar.SUNDAY) {
						holidayTrack.put(calS, "Sunday");
						log.info( Calendar.SUNDAY + " : on "+calS.DATE);

					}
					isWeekEnd = true;*/
				}
			}while (calS.compareTo(calE) <= 0); 
			
			System.out.println("days :" + days);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			log.severe("exception getting days count :" + e);
		}
		response.put("days", days);
		response.put("holidayTrack", holidayTrack);
		response.put("isWeekEnd", isWeekEnd);
		return response;
	}

	}
