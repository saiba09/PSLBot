package com.example;

import static org.hamcrest.CoreMatchers.is;

import java.sql.Array;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Iterator;
import java.util.logging.Logger;

import javax.swing.text.AbstractDocument.LeafElement;
import javax.xml.stream.events.StartDocument;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.util.DateDetails;
import com.util.Formator;
import com.util.LeaveMessageFormator;
import com.util.PropertyLoader;

import ai.api.model.AIEvent;
import ai.api.model.AIOutputContext;
import ai.api.model.Fulfillment;
import ai.api.model.Result;
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
		// String sessionId = input.getSessionId();
		String sessionId = "milind_pingale";
		log.info(sessionId);
		// add constants in file
		log.info("action : " + action);
		try {
			switch (action) {
			case "QUERY_LEAVE":
				log.info("in action : query_leave");
				output = queryLeave(output, parameter, sessionId, action, null);
				break;
			case "SYSTEM_SUGESTION_SATISFIED_YES":
				log.info(" intent SYSTEM_SUGESTION_SATISFIED_YES ");
				output = getConfirmationMessage(output, parameter, "SYSTEM_SUGESTION_SATISFIED_YES", sessionId);
				break;
			case "QUERY_LEAVE_PARMS":
				log.info("intent : QUERY_LEAVE_PARMS");
				output = queryLeave(output, parameter, sessionId, action, input);
				break;
			case "SYSTEM_SUGESTION_SATISFIED_NO":
				log.info("intent : SYSTEM_SUGESTION_SATISFIED_NO");
				// output = getConfirmationMessage(output, parameter,
				// "SYSTEM_SUGESTION_SATISFIED_NO",sessionId);
				output = exitFlow(output);
				break;
			case "SUGGEST_LEAVES_OPTION":
				log.info("intent : SUGGEST_LEAVES_OPTION");
				output = getLeaveComboSuggestion(output, parameter, "SUGGEST_LEAVES_OPTION", sessionId);
				// give suggestion for oh pl combo, calc no of days,
				break;
			// case not found
			case "OPT_CUSTOM_REQ":
				log.info("intent: OPT_CUSTOM_REQ");
				output = Redirections.redirectToCustomApply(output, parameter);
				break;
			case "CONFIRM_APPLY":
				log.info("intent : CONFIRM_APPLY");
				output = applyLeave(output, parameter, sessionId);
				break;
			// not found
			case "SYST_SUG_NOT_SATISFIED_CUST_CONFIRM":
				log.info("SYST_SUG_NOT_SATISFIED_CUST_CONFIRM");
				output = applyLeave(output, parameter, sessionId);
				break;

			case "CUSTOM_FORM_SUBMIT":
				log.info("intent : CUSTOM_FORM_SUBMIT");
				output = getConfirmationMessage(output, parameter, "CUSTOM_FORM_SUBMIT", sessionId);
				break;

			case "APPLY_COMBO_LEAVE":
				log.info("intent : APPLY_COMBO_LEAVE");
				output = Redirections.redirectToComboLeaveForm(output, parameter, sessionId);
				break;

			case "CUSTOM_FORM_SUBMIT_CONFIRM":
				log.info("INTENT : CUSTOM_FORM_SUBMIT_CONFIRM");
				output = applyLeave(output, parameter, sessionId);
				break;

			case "LEAVE_TYPE_SELECTION":
				log.info("intent : LEAVE_TYPE_SELECTION");
				output = getLeaveBreakup(output, parameter, sessionId);
				// message for confirmation returns leave brk up
				break;
			// case not found
			case "ONE_DAY_LEAVE":
				log.info("intent : ONE_DAY_LEAVE");
				output = queryLeave(output, parameter, sessionId, action, input);
				break;
			case "TO_SUGGEST_LEAVE_TYPE":
				log.info("intent : LeaveQueryWithParms - yes");
				output = Redirections.redirectToSuggestLeaveOption(output, parameter);
				break;
				
			case "CONFIRM_LEAVE_APPLY":
				log.info("intent : CONFIRM_LEAVE_APPLY");
				output = applyLeaveWithTypes(output, parameter);
				break;

			case "AGAIN_SELECT_LEAVE_TYPE":
				log.info("intent :AGAIN_SELECT_LEAVE_TYPE");
				output = Redirections.redirectToComboLeaveForm(output, parameter, sessionId);
				break;
			// not found
			case "SYST_SUG_NOT_SATISFIED_CUST_CONFIRM_SUGGEST_TYPES":
				log.info("intent : SYST_SUG_NOT_SATISFIED_CUST_CONFIRM_SUGGEST_TYPES");
				output = getLeaveComboSuggestion(output, parameter, "SYST_SUG_NOT_SATISFIED_CUST_CONFIRM_SUGGEST_TYPES",
						sessionId);
				break;

			case "GET_LEAVE_BALANCE":
				log.info("intent : getLeaveBalance");
				output = getLeaveBalance(output, parameter, sessionId);
				break;
			case "FALLBACK":
				log.info("intent : fallback");
				output = getFallBackResponse(output, input);
				break;
			case "FAQ":
				log.info("intent : FAQ");
				output = getFallBackResponse(output, input);
				break;
			case "ONE_DAY_LEAVE_YES_FOLLOWUP":
				log.info("intent : ONE_DAY_LEAVE_YES_FOLLOWUP");
				output = getResponseForOneDayLeaveIntent(output, parameter, sessionId, action);
				break;
			case "ONE_DAY_TYPE":
				log.info("intent : ONE_DAY_TYPE");
				output = applyLeaveWithTypes(output, parameter);
				break;
			case "APPLY_ONE_DAY":
				log.info("intent : APPLY_ONE_DAY");
				output = getResponseForOneDayLeaveIntent(output, parameter, sessionId, action);
				break;
			default:
				output.setSpeech("Default case");
				break;
			}
		} catch (Exception e) {
			log.info("exception : " + e);
		}

	}

	private Fulfillment getResponseForOneDayLeaveIntent(Fulfillment output, HashMap<String, JsonElement> parameter,
			String sessionId, String action) {
		// TODO Auto-generated method stub

		log.info("getResponseForOneDayLeaveIntent");
		String startDate = parameter.get("startDate").getAsString().trim();
		String endDate = parameter.get("endDate").getAsString().trim();
		String comment = parameter.get("comment").getAsString().trim();
		Boolean isFestival = Boolean.parseBoolean(parameter.get("isFestival").getAsString().trim());
		Boolean isHoliday = Boolean.parseBoolean(parameter.get("isHoliday").getAsString().trim());
		Boolean isOneDay = Boolean.parseBoolean(parameter.get("isOneDay").getAsString().trim());
		String message = "";
		log.info("Parms : " + startDate + " " + endDate + " " + comment + " isFest " + isFestival + " isHoliday "
				+ isHoliday + " isOne " + isOneDay);
		HashMap<String, JsonElement> outParms = new HashMap<>();
		outParms.put("comment", new JsonPrimitive(comment));
		if (action.equals("APPLY_ONE_DAY") || isOneDay) {
			// Ask leave type
			AIOutputContext contextOut = new AIOutputContext();
			outParms.put("startDate", new JsonPrimitive(startDate));
			outParms.put("endDate", new JsonPrimitive(endDate));
			contextOut.setParameters(outParms);
			output.setContextOut(contextOut);
			message = LeaveMessageFormator.getLeaveDetailMessage(sessionId);
			message += "Which type of leave you want to opt for?";
		} else if (isHoliday || isFestival) {
			// redirect to custom form
			output = Redirections.redirectToCustomApply(output, outParms);
		}

		return output;
	}

	private Fulfillment getLeaveBalance(Fulfillment output, HashMap<String, JsonElement> parameter, String sessionId) {

		JSONObject employeeData = Data.getHolidays(sessionId);
		int PL = Integer.parseInt(employeeData.get("privillage_leave").toString());
		int OH = Integer.parseInt(employeeData.get("optional_holiday").toString());
		int OL = Integer.parseInt(employeeData.get("optional_leave").toString());
		int CF = Integer.parseInt(employeeData.get("compensatiory_off").toString());

		output.setDisplayText("You have " + PL + " privileged leave, " + OL + " optional leave, " + OH
				+ " optional holiday and " + CF + " compensatory Off" + " #false");
		output.setSpeech("You have " + PL + " privileged leave, " + OL + " optional leave, " + OH
				+ " optional holiday and " + CF + " compensatory Off" + " #false");

		return output;
	}

	private Fulfillment getFallBackResponse(Fulfillment output, AIWebhookRequest input) {

		String question = input.getResult().getResolvedQuery();
		JSONObject response = SearchFunction.fetchAnswerFromDatastore(question);
		log.info(response.toJSONString());
		String answer = (String) response.get("answer");
		output.setDisplayText(answer);
		output.setSpeech(answer);
		return output;
	}

	private Fulfillment applyLeaveWithTypes(Fulfillment output, HashMap<String, JsonElement> parameter) {
		String startDate = parameter.get("startDate").getAsString().trim();
		String endDate = parameter.get("endDate").getAsString().trim();
		String comment = parameter.get("comment").getAsString().trim();
		String leaveBreakUp = parameter.get("leaveBreakUp").getAsString().trim();
		String message = "Your leaves have been applied successfully in the system. Let me know what can I do else for you.#false";
		output.setDisplayText(message);
		output.setSpeech(message);
		return output;
	}

	private Fulfillment getLeaveBreakup(Fulfillment output, HashMap<String, JsonElement> parameter, String sessionId) {
		// TODO Auto-generated method stub
		log.info("getLeavebreakup");
		String startDate = parameter.get("startDate").getAsString().trim();
		String endDate = parameter.get("endDate").getAsString().trim();
		String comment = parameter.get("comment").getAsString().trim();
		String leaveType = parameter.get("leaveType").getAsString().trim();
		String leave = parameter.get("noOfLeave").getAsString().trim();
		log.info(leave + " no of leaves");
		leave = leave.substring(0, leave.indexOf("."));
		log.info("leave post sub string :" + leave);
		int noOfLeave = Integer.parseInt(leave.trim());
		JSONObject data = Data.getHolidays(sessionId);
		/*
		 * int noPL = Integer.parseInt(parameter.get("noPL").toString().trim());
		 * int noOL = Integer.parseInt(parameter.get("noOL").toString().trim());
		 * int noOH = Integer.parseInt(parameter.get("noOH").toString().trim());
		 * int noCF = Integer.parseInt(parameter.get("noCF").toString().trim());
		 * log.info("applied for : PL " + noPL + " CF "+noCF + " OH  "+ noOH +
		 * "  OL  "+noOL );
		 */
		int PL = Integer.parseInt(data.get("privillage_leave").toString());
		int OH = Integer.parseInt(data.get("optional_holiday").toString());
		int OL = Integer.parseInt(data.get("optional_leave").toString());
		int CF = Integer.parseInt(data.get("compensatiory_off").toString());
		log.info(" pressent values : PL " + PL + " CF " + CF + " OH " + OH + "  OL  " + OL);
		String message = "";
		JSONObject leaveJson = new JSONObject();
		int balance = 0;
		String type = "";
		switch (leaveType) {
		case "PL":
			balance = PL;
			type = "privilaged leave";
			break;
		case "OH":
			balance = OH;
			type = "optional holiday";
			break;
		case "OL":
			balance = OL;
			type = "optional leave";
			break;
		case "CF":
			balance = CF;
			type = "compensatiory off";
			break;
		default:
			log.severe("UNKNOWN LEAVE TYPE");
			break;
		}
		String day = "day";
		if (noOfLeave > 1) {
			day += "s";
		}
		if (noOfLeave <= balance) {
			if (noOfLeave == 1) {
				message += "You want to apply 1 " + type + " on " + Formator.getFormatedDate(startDate) + " as "
						+ comment + ". Should I confirm?";

			} else {
				message += "So want to apply : " + noOfLeave + " " + day + " " + type + " from "
						+ Formator.getFormatedDate(startDate) + " to " + Formator.getFormatedDate(endDate)
						+ ". Shall I confirm?";
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
		} else {
			output = Redirections.redirectToDPApproval(output, parameter);
		}
		/*
		 * int sum = noCF+noOH+noOL+noPL; log.info("sum : "+ sum +
		 * " days : "+days);
		 * 
		 * if (sum == days) { //APPLY LEAVE message =
		 * "So you want to apply from "+startDate.toString() + " to " +
		 * endDate.toString() + " as " +comment+" of which ";
		 * 
		 * if (noPL != 0 && noPL <= PL) { leaveJson.put("PL", noPL); message +=
		 * noPL +" are privileged leave"; } if (noCF != 0 && noCF <= CF) {
		 * leaveJson.put("CF", noCF); message += noCF
		 * +" are compensatory offs "; } if (noOH != 0 && noOH <= OH) {
		 * leaveJson.put("OH", noOH); message += noOH +" are optional holiday ";
		 * } if (noOL != 0 && noOL <= OL) { leaveJson.put("OL", noOL); message
		 * += noOL +" are optional leave "; } message += " Please confirm.";
		 * }else{ //sorry someting went wrong. //Go back to message =
		 * "You have entered wrong combination. Try again!";
		 * 
		 * }
		 */

		return output;
	}

	private Fulfillment getLeaveComboSuggestion(Fulfillment output, HashMap<String, JsonElement> parameter,
			String action, String sessionId) {
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
		int leave_balance = Integer.parseInt(DateDetails.getLeaveInfo(sessionId).get("count").toString());
		JSONObject jsonDays = DateDetails.getDays(startDate, endDate);
		int noOfLeaves = Integer.parseInt(jsonDays.get("days").toString());
		JSONObject data = Data.getHolidays(sessionId);
		int PL = Integer.parseInt(data.get("privillage_leave").toString());
		int OH = Integer.parseInt(data.get("optional_holiday").toString());
		int OL = Integer.parseInt(data.get("optional_leave").toString());
		int CF = Integer.parseInt(data.get("compensatiory_off").toString());
		log.info("balance :" + leave_balance + " required :" + noOfLeaves);
		if (PL < noOfLeaves) {
			// check if any leave >= req . if give option else go to dp
			if (CF < noOfLeaves) {
				if (OH < noOfLeaves) {
					if (OL < noOfLeaves) {
						output = Redirections.redirectToDPApproval(output, parameter);
					}
				}
			} else {
				message += "You have in-sufficient privillaged leave, still want to apply for it? You will need toh apply for LWP in that case.";
			}
		} else {
			if (CF != 0 || OH != 0 || OL != 0) {
				message += "You have ";
			}
			if (CF != 0) {
				message += CF + " compensatory off, ";

			}
			if (OH != 0) {
				message += OH + " optional holiday, ";
			}
			if (OL != 0) {
				message += OL + " optional leave.";
			}
			if (CF != 0 || OH != 0 || OL != 0) {
				// message += "Do you still want to apply for privilage leave ?
				// Don't forget these leaves won't carry forward.";
				message += " Do you still want to apply for privilaged leave?";
			}
		}
		log.info(message);
		// context : system-sugestion-satisfied-yes-yes-suggestOptions-followup
		/*
		 * AIOutputContext contextOut = new AIOutputContext(); HashMap<String,
		 * JsonElement> outParms = new HashMap<>(); outParms.put("comment", new
		 * JsonPrimitive(comment)); outParms.put("startDate", new
		 * JsonPrimitive(startDate)); outParms.put("endDate", new
		 * JsonPrimitive(endDate)); contextOut.setLifespan(1);
		 * contextOut.setName(
		 * "system-sugestion-satisfied-yes-yes-suggestOptions-followup");
		 * contextOut.setParameters(outParms); // set cont.
		 * output.setContextOut(contextOut);
		 */
		output.setSpeech(message);
		output.setDisplayText(message);
		return output;

	}

	private Fulfillment exitFlow(Fulfillment output) {
		// TODO Auto-generated method stub TERMINATE
		return Redirections.redirectToTerminate(output);

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
		JSONObject jsonDays = DateDetails.getDays(startDate, endDate);
		int noOfLeaves = Integer.parseInt(jsonDays.get("days").toString());
		if (leave_balance <= 0) {
			log.info("bal < 0");
			output = Redirections.redirectToDPApproval(output, parameter);
		} else if (leave_balance >= noOfLeaves) {
			log.info("bal < no Leaves ");
			message = "Your leaves have been applied successfully in the system. Let me know what can I do else for you.";
		} else {
			log.info("Your leave balance is less than :" + noOfLeaves + ". You will need Delivery partner approval.");
			output = Redirections.redirectToDPApproval(output, parameter);

		}
		// else abort
		log.info(message);
		message += "#false";
		output.setSpeech(message);
		output.setDisplayText(message);
		return output;
	}

	private Fulfillment queryLeave(Fulfillment output, HashMap<String, JsonElement> parameter, String sessionId,
			String action, AIWebhookRequest input) throws ParseException {
		log.info("querry leave function");
		String startDate;
		String endDate;
		String comment;
		int leave_balance = Integer.parseInt(DateDetails.getLeaveInfo(sessionId).get("count").toString());
		String message = "";
		if (action.equals("ONE_DAY_LEAVE")) {
			log.info("one day leave apply");
			startDate = parameter.get("date").getAsString().trim();
			String event = parameter.get("dateEvent").getAsString().trim();
			comment = parameter.get("comment").getAsString().trim();
			JSONObject responseMessageObject = LeaveMessageFormator.getMessageForFestival(event, startDate, comment);
			Boolean isHoliday = Boolean.parseBoolean(responseMessageObject.get("isHoliday").toString());
			Boolean isFestival = Boolean.parseBoolean(responseMessageObject.get("isFestival").toString());
			Boolean isOneDay = Boolean.parseBoolean(responseMessageObject.get("isOneDay").toString());
			message += responseMessageObject.get("message").toString();
			// isHoliday no long weekend || if isfestival no long weekend ||
			// with long weekend ==>> same intent yes/No || set a boolean
			// isHoliday
			if (leave_balance <= 0 && isHoliday) {
				// intent to display text and break;
				message = "No leave balance however its holiday for " + event;
				HashMap<String, JsonElement> outParms = new HashMap<>();
				outParms.put("message", new JsonPrimitive(message));
				output = Redirections.redirectToDisplayMessage(output, outParms);

			} else if (leave_balance > 0 && isFestival || isHoliday) {

				message += responseMessageObject.get("longVaccationSugestion");
				comment = getMessage(event);
				AIOutputContext contextOut = new AIOutputContext();
				HashMap<String, JsonElement> outParms = new HashMap<>();
				outParms.put("comment", new JsonPrimitive(comment));
				outParms.put("startDate", new JsonPrimitive(startDate));
				outParms.put("endDate", new JsonPrimitive(startDate));
				outParms.put("isHoliday", new JsonPrimitive(isHoliday));
				outParms.put("isFestival", new JsonPrimitive(isFestival));
				outParms.put("isOneDay", new JsonPrimitive(isOneDay));
				contextOut.setLifespan(1);
				contextOut.setName("oneDayLeaveFollowup");
				contextOut.setParameters(outParms);
				output.setContextOut(contextOut);
			}
			// if is oneday go to SUGGEST_lEAVES_OPTION
			else if (isOneDay) {
				JSONObject jsonDays = DateDetails.getDays(startDate, startDate);
				Boolean isWeekend = Boolean.parseBoolean(jsonDays.get("isWeekEnd").toString());
				if (isWeekend) {
					if (event.equalsIgnoreCase("today")) {
						comment += " Its weekend today.";
						isOneDay = true;

					}
					if (event.equalsIgnoreCase("tomorrow")) {
						comment += "It was weekend yesterday.";
						isOneDay = true;
					}
					message = comment;

					HashMap<String, JsonElement> outParms = new HashMap<>();
					outParms.put("message", new JsonPrimitive(message));
					output = Redirections.redirectToDisplayMessage(output, outParms);
				}
				if (leave_balance > 0) {
					HashMap<String, JsonElement> outParms = new HashMap<>();
					outParms.put("comment", new JsonPrimitive(comment));
					outParms.put("startDate", new JsonPrimitive(startDate));
					outParms.put("endDate", new JsonPrimitive(startDate));
					output = Redirections.redirectToCustomApply(output, outParms);
				} else {
					output = Redirections.redirectToDPApproval(output, parameter);
				}

			} else if (leave_balance <= 0) {
				log.info("bal < req & no holiday");
				output = Redirections.redirectToDPApproval(output, parameter);
			}

		}
		if (action.equals("QUERY_LEAVE")) {
			startDate = parameter.get("startDate").getAsString().trim();
			endDate = parameter.get("endDate").getAsString().trim();
			comment = parameter.get("comment").getAsString().trim();
			if (comment.isEmpty()) {
				String event = parameter.get("event").getAsString().trim();
				if (!event.isEmpty()) {
					comment = "leave for " + event;
				}
			}
			log.info("parms :" + startDate + " " + endDate + " comment: " + comment);
			if (leave_balance > 0) {
				if (!comment.isEmpty() || !startDate.isEmpty() || !endDate.isEmpty()) {

					HashMap<String, JsonElement> outParms = new HashMap<>();

					outParms.put("comment", new JsonPrimitive(comment));
					outParms.put("startDate", new JsonPrimitive(startDate));
					outParms.put("endDate", new JsonPrimitive(endDate));
					output = Redirections.redirectToQueryLeaveWithParms(output, outParms);
				} else {

					message += "Hey I just checked you have sufficient leave balance, shall we proceed?";

				}
			} else {
				// message = "Your l You will need Delivery partner approval.";
				// set event trigg.
				output = Redirections.redirectToDPApproval(output, parameter);
			}

		}

		if (action.equalsIgnoreCase("QUERY_LEAVE_PARMS")) {
			startDate = parameter.get("startDate").getAsString().trim();
			endDate = parameter.get("endDate").getAsString().trim();
			comment = parameter.get("comment").getAsString().trim();
			output = Formator.getLeaveConfirmationMessage(startDate, endDate, comment, leave_balance, output);
		}
	}

	private Fulfillment getConfirmationMessage(Fulfillment output, HashMap<String, JsonElement> parameter,
			String action, String sessionId) throws ParseException {
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
		int leave_balance = Integer.parseInt(DateDetails.getLeaveInfo(sessionId).get("count").toString());
		JSONObject jsonDays = DateDetails.getDays(startDate, endDate);
		int noOfLeaves = Integer.parseInt(jsonDays.get("days").toString());
		Boolean isWeekend = Boolean.parseBoolean(jsonDays.get("isWeekEnd").toString());
		int totalDays = DateDetails.getDaysBetweenDates(startDate, endDate);
		log.info("balance :" + leave_balance + " required :" + noOfLeaves +" total days : "+ totalDays);
		if (leave_balance <= 0 || leave_balance < noOfLeaves) {
			log.info("bal < 0");
			log.info(
					"Sorry, you have insufficient leave balance, you will need DP approval If want to apply for leave.");
			output = Redirections.redirectToDPApproval(output, parameter);
		} else if (leave_balance >= noOfLeaves) {
			log.info("req > bal");
			
			if (noOfLeaves == 1) {
				message += "You want to apply leave on " + Formator.getFormatedDate(startDate) + " as " + comment+".";
				message += Formator.getWeekendContainsMessage(startDate, endDate,noOfLeaves);

//						+ ". Should I confirm?";
			}else if (totalDays == 2 && Boolean.parseBoolean(jsonDays.get("isWeekEnd").toString().trim())) {
				Date start = new SimpleDateFormat("yyyy-MM-dd").parse(startDate);
				Date end = new SimpleDateFormat("yyyy-MM-dd").parse(endDate);
				JSONObject holidayMap = (JSONObject) jsonDays.get("holidayTrack");
				if (((String) holidayMap.get(start)).equalsIgnoreCase("Saturday")
						&& ((String) holidayMap.get(end)).equalsIgnoreCase("Sunday")) {
					// redirect to send message
					message = "Its weekend from " + Formator.getFormatedDate(start) + " to "
							+ Formator.getFormatedDate(end) + ". No need to apply for leave. Enjoy!";

					HashMap<String, JsonElement> outParms = new HashMap<>();
					outParms.put("message", new JsonPrimitive(message));
					output = Redirections.redirectToDisplayMessage(output, outParms);
				} else if (leave_balance >= noOfLeaves) {
					message = "Hey I just checked you have sufficient leave balance, shall we proceed?";
					message += Formator.getWeekendContainsMessage(startDate, endDate,noOfLeaves);
				}

			} else if (leave_balance >= noOfLeaves) {
				// give suggestion that if weekend
				message = "Hey I just checked you have sufficient leave balance.";
				message += Formator.getWeekendContainsMessage(startDate, endDate, noOfLeaves);
			} 
			
			if (action.equalsIgnoreCase("SYSTEM_SUGESTION_SATISFIED_YES")) {
				log.info("for action :  SYSTEM_SUGESTION_SATISFIED_YES");
				AIOutputContext contextOut = new AIOutputContext();
				HashMap<String, JsonElement> outParms = new HashMap<>();
				outParms.put("comment", new JsonPrimitive(comment));
				outParms.put("startDate", new JsonPrimitive(startDate));
				outParms.put("endDate", new JsonPrimitive(endDate));

				contextOut.setLifespan(1);
				contextOut.setName("system-sugestion-satisfied-yes-follow-up");
				contextOut.setParameters(outParms);
				// set cont.
				output.setContextOut(contextOut);
			
			}
			if (action.equalsIgnoreCase("SYSTEM_SUGESTION_SATISFIED_NO")) {
				/*
				 * log.info("for action :  SYSTEM_SUGESTION_SATISFIED_NO");
				 * AIOutputContext contextOut = new AIOutputContext();
				 * HashMap<String, JsonElement> outParms = new HashMap<>();
				 * outParms.put("comment", new JsonPrimitive(comment));
				 * outParms.put("startDate",new JsonPrimitive(startDate));
				 * outParms.put("endDate",new JsonPrimitive(endDate));
				 * 
				 * contextOut.setLifespan(1);
				 * contextOut.setName("system-sugestion-satisfied-yes-follow-up"
				 * ); contextOut.setParameters(outParms); // set cont.
				 * output.setContextOut(contextOut);
				 */}
			/*			
			*/
			
		} else {
			output = Redirections.redirectToDPApproval(output, parameter);
			// IMP : set out parmas end date- diff
		}
		log.info(message);
		output.setDisplayText(message);
		output.setSpeech(message);
		return output;
	}

	private static String getMessage(String event) {
		// TODO Auto-generated method stub
		return "Leave for " + event;
	}

}
