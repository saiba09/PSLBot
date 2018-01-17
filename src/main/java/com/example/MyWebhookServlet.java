 package com.example;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.logging.Logger;

import javax.servlet.ServletContext;

import org.json.simple.JSONObject;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.model.Leave;
import com.model.LeaveTransaction;
import com.model.User;
import com.util.DateDetails;
import com.util.Formator;
import com.util.LeaveMessageFormator;
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
		log.info("sessionId =" + sessionId);
		String userName = sessionId.substring(0, sessionId.lastIndexOf("_"));
		ServletContext conetxt = getServletContext();
		String fileName = conetxt.getRealPath("/WEB-INF/accessToken.json");
		User user = new UserHandler().getUser(userName,fileName);
		log.info("user name : " + userName);
		log.info("action : " + action);
		try {
			switch (action) {
			case "QUERY_LEAVE":
				log.info("in action : query_leave");
				output = queryLeave(output, parameter, user, action, null);
				break;
			case "SYSTEM_SUGESTION_SATISFIED_YES":
				log.info(" intent SYSTEM_SUGESTION_SATISFIED_YES ");
				output = getConfirmationMessage(output, parameter, "SYSTEM_SUGESTION_SATISFIED_YES", user);
				break;
			case "QUERY_LEAVE_PARMS":
				log.info("intent : QUERY_LEAVE_PARMS");
				output = queryLeave(output, parameter, user, action, input);
				break;
			case "SYSTEM_SUGESTION_SATISFIED_NO":
				log.info("intent : SYSTEM_SUGESTION_SATISFIED_NO");
				// output = getConfirmationMessage(output, parameter,
				// "SYSTEM_SUGESTION_SATISFIED_NO",user);
				output = exitFlow(output);
				break;
			case "SUGGEST_LEAVES_OPTION":
				log.info("intent : SUGGEST_LEAVES_OPTION");
				output = getLeaveComboSuggestion(output, parameter, "SUGGEST_LEAVES_OPTION", user);
				// give suggestion for oh pl combo, calc no of days,
				break;
			// case not found
			case "OPT_CUSTOM_REQ":
				log.info("intent: OPT_CUSTOM_REQ");
				output = Redirections.redirectToCustomApply(output, parameter);
				break;
			case "CONFIRM_APPLY":
				log.info("intent : CONFIRM_APPLY");
				output = applyLeave(output, parameter, user);
				log.info("output "+ output.getDisplayText());
				break;
			// not found
			case "SYST_SUG_NOT_SATISFIED_CUST_CONFIRM":
				log.info("SYST_SUG_NOT_SATISFIED_CUST_CONFIRM");
				output = applyLeave(output, parameter, user);
				break;

			case "CUSTOM_FORM_SUBMIT":
				log.info("intent : CUSTOM_FORM_SUBMIT");
				output = getConfirmationMessage(output, parameter, "CUSTOM_FORM_SUBMIT", user);
				break;

			case "APPLY_COMBO_LEAVE":
				log.info("intent : APPLY_COMBO_LEAVE");
				output = Redirections.redirectToComboLeaveForm(output, parameter, user);
				break;

			case "CUSTOM_FORM_SUBMIT_CONFIRM":
				log.info("INTENT : CUSTOM_FORM_SUBMIT_CONFIRM");
				output = applyLeave(output, parameter, user);
				break;

			case "LEAVE_TYPE_SELECTION":
				log.info("intent : LEAVE_TYPE_SELECTION");
				output = getLeaveBreakup(output, parameter, user);
				// message for confirmation returns leave brk up
				break;
			// case not found
			case "ONE_DAY_LEAVE":
				log.info("intent : ONE_DAY_LEAVE");
				output = queryLeave(output, parameter, user, action, input);
				break;
			case "TO_SUGGEST_LEAVE_TYPE":
				log.info("intent : LeaveQueryWithParms - yes");
				output = Redirections.redirectToSuggestLeaveOption(output, parameter);
				break;

			case "CONFIRM_LEAVE_APPLY":
				log.info("intent : CONFIRM_LEAVE_APPLY");
				output = applyLeaveWithTypes(output, parameter, user);
				break;

			case "AGAIN_SELECT_LEAVE_TYPE":
				log.info("intent :AGAIN_SELECT_LEAVE_TYPE");
				output = getResponseForLeaveTypeSelectionResponseNo(output, parameter, user);
				break;
			// not found
			case "SYST_SUG_NOT_SATISFIED_CUST_CONFIRM_SUGGEST_TYPES":
				log.info("intent : SYST_SUG_NOT_SATISFIED_CUST_CONFIRM_SUGGEST_TYPES");
				output = getLeaveComboSuggestion(output, parameter, "SYST_SUG_NOT_SATISFIED_CUST_CONFIRM_SUGGEST_TYPES",
						user);
				break;

			case "GET_LEAVE_BALANCE":
				log.info("intent : getLeaveBalance");
				output = getLeaveBalance(output, parameter, user);
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
				output = getResponseForOneDayLeaveIntent(output, parameter, user, action);
				break;
			case "ONE_DAY_TYPE":
				log.info("intent : ONE_DAY_TYPE");
				output = applyLeaveWithTypes(output, parameter, user);
				break;
			case "APPLY_ONE_DAY":
				log.info("intent : APPLY_ONE_DAY");
				output = getResponseForOneDayLeaveIntent(output, parameter, user, action);
				break;
			case "FEELING_SICK":
				log.info("intent FEELING_SICK");
				output = getResponseForNotWell(output, parameter, user);
				break;
			case "FEELING_SICK_CUSTOM_NO_FOLLOWUP":
				log.info("for intent FEELING_SICK_CUSTOM_NO_FOLLOWUP");
				output = getResponseForFollowUpNotWell(output, parameter, user);
				break;
			case "SUGGEST_TIME":
				log.info("Event triggred : suggest time");
				output = GetResponseForSuggestTime(output, parameter, user);
				break;
			case "FEELING_SICK_APPLY_LEAVE":
				log.info("intent : FEELING_SICK_APPLY_LEAVE");
				output = getResponseFeelingSickApplyLeave(output,parameter,user);
				break;
			case "SELECT_LEAVE_TYPE":
				log.info("intent SELECT_LEAVE_TYPE");
			
				output = getResponseForLeaveTypeSelectionResponseNo(output, parameter, user);
				break;
			case "CALANDER":
				log.info("intent CALANDER");
				output =getNextHoliday(output, parameter, user);
				break;
			case "CKECK_STATUS":
				log.info("intent CALANDER");
				output =getLeaveStatus(output, parameter, user);
				break;
			default:
				output.setSpeech("Default case");
				break;
			}
		} catch (Exception e) {
			log.info("exception : " + e);
		}

	}

	private Fulfillment getLeaveStatus(Fulfillment output, HashMap<String, JsonElement> parameter, User user) {
		log.info("get leave status ");
		String userName = user.getUserName();
		String message = "";
		
		userName = userName.substring(0, userName.indexOf("_")) + " " + userName.substring(userName.indexOf("_")+1);
		userName = userName.substring(0,1).toUpperCase() + userName.substring(1, userName.indexOf(" ")) + " " + userName.substring(userName.indexOf(" ")+1,userName.indexOf(" ")+2).toUpperCase() +userName.substring(userName.indexOf(" ")+2);
		//comment
		ArrayList<LeaveTransaction> transaction = new LeaveTransactionDao().getrecordByName(userName);
		if (transaction.size() > 0) {
			String todayString = DateDetails.getCurrentDate();
			DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
			DateFormat tf = new SimpleDateFormat("dd-MMM-yyyy");
			try {
				Date today = df.parse(todayString);
				todayString = tf.format(today);
				/*if (tf.parse(transaction.get(0).getDate().getStartDate()).before(tf.parse(todayString))) {
					message = "No pending transaction";
				}else{*/
					if ( transaction.get(0).getIsApproved()) {
						message = "Your leave application from "+transaction.get(0).getDate().getStartDate()+" has been approved.";
					}else{
						if (transaction.get(0).getApprovarComment().equals(" ")) {
							message = "Your leave application from "+transaction.get(0).getDate().getStartDate()+" is not yet approved.";
						}else{
							message = "Your leave application from "+transaction.get(0).getDate().getStartDate()+" is not yet approved, with "+transaction.get(0).getApprover()+"'s comments saying "+transaction.get(0).getApprovarComment();

						}
					} 
					output.setDisplayText(message);
					output.setSpeech(message);
				
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				log.severe("exception parsing date : "+e);
			}
		}
		return output;
	}

	private Fulfillment getNextHoliday(Fulfillment output, HashMap<String, JsonElement> parameter, User user) {
		// TODO Auto-generated method stub
		String message = DateDetails.getNextHoliday();
		output.setDisplayText(message);
		output.setSpeech(message);
		return output;
	}

	private Fulfillment getResponseForLeaveTypeSelectionResponseNo(Fulfillment output,
			HashMap<String, JsonElement> parameter, User user) {
		Boolean haveOther = Boolean.parseBoolean(parameter.get("haveOther").getAsString());
		if (haveOther) {
			output = Redirections.redirectToComboLeaveForm(output, parameter, user);

		}
		else{
			output = Redirections.redirectToDisplayMessage(output, "Okay #usr#. As you say.");
		}
		return output;
	}

	private Fulfillment getResponseFeelingSickApplyLeave(Fulfillment output, HashMap<String, JsonElement> parameter,
			User user) {
		// TODO Auto-generated method stub
		log.info("feeling sick apply leave now");
		String timeConstraint = parameter.get("timeConstraint").getAsString().trim();
		HashMap<String, JsonElement> outParms = new HashMap<>();
		outParms.put("startDate", new JsonPrimitive(DateDetails.getCurrentDate()));
		outParms.put("endDate", new JsonPrimitive(DateDetails.getCurrentDate()));
		outParms.put("comment", new JsonPrimitive("Not feeling well"));
		outParms.put("timeConstraint", new JsonPrimitive(timeConstraint));
		output = Redirections.redirectToSuggestLeaveOption(output, outParms);
		return output;
	}

	private Fulfillment GetResponseForSuggestTime(Fulfillment output, HashMap<String, JsonElement> parameter,
			User user) {
		// TODO Auto-generated method stub
		String disease = parameter.get("disease").getAsString();
		String userSays = parameter.get("userSays").getAsString();
		String token = parameter.get("token").getAsString();
		log.info("Parms : "+ disease + "  : "+userSays +" : "+token);
		HashMap<String, JsonElement> outParms = new HashMap<>();
		outParms.put("token", new JsonPrimitive(token));
		String message = "";
		if (token.equals("ILL")) {
			message = "Oh! Do you want me to apply leave for you?";
		}
		if (token.equals("UNWELL")) {
			log.info("unwell token");
			JSONObject response = Formator.getOptionForLeave();
			message = response.get("message").toString();
			outParms.put("disease", new JsonPrimitive(disease));
			outParms.put("userSays", new JsonPrimitive(userSays));
			outParms.put("isFullDay", new JsonPrimitive(response.get("isFullDay").toString()));
			outParms.put("isHalfDay", new JsonPrimitive(response.get("isHalfDay ").toString()));
			outParms.put("canBeHalfDay", new JsonPrimitive(response.get("canBeHalfDay").toString()));
			outParms.put("time", new JsonPrimitive(response.get("time").toString()));
			outParms.put("token", new JsonPrimitive("UNWELL"));

		}
		AIOutputContext contextOut = new AIOutputContext();
		contextOut.setLifespan(1);
		contextOut.setParameters(outParms);
		contextOut.setName("suggest-time-follow-up");
		output.setContextOut(contextOut);
		output.setSpeech(message);
		output.setDisplayText(message);
		return output;
	}

	private Fulfillment getResponseForFollowUpNotWell(Fulfillment output, HashMap<String, JsonElement> parameter,
			User user) {
		// TODO Auto-generated method stub
		String timeConstraint = parameter.get("timeConstraint").getAsString().trim();
		/*
		 * String disease = parameter.get("disease").getAsString(); String
		 * userSays = parameter.get("userSays").getAsString(); String time =
		 * parameter.get("time").getAsString();
		 * 
		 * Boolean isFullDay =
		 * Boolean.parseBoolean(parameter.get("isFullDay").getAsString().trim())
		 * ; Boolean isHalfDay =
		 * Boolean.parseBoolean(parameter.get("isHalfDay").getAsString().trim())
		 * ; Boolean canBeHalfDay =
		 * Boolean.parseBoolean(parameter.get("canBeHalfDay").getAsString().trim
		 * ()); Boolean forLeave =
		 * Boolean.parseBoolean(parameter.get("forLeave").getAsString().trim());
		 */
		JSONObject res = Formator.getOptionForLeave();
		String message = "";
		float leave_balance = user.getTotalLeaveBalance();
		// follow up for user said I m not feeling well
		if (leave_balance > 0) {
			if (!timeConstraint.isEmpty()) {
				// user specified want leave
				message = "Okay #usr#. You take rest. ";
				switch (timeConstraint) {
				case "full":
					message += "I will apply leave for you.";
					break;
				case "half":
					message += "I will apply half day leave for you.";
					break;
				case "now":

					if (Boolean.parseBoolean(res.get("isFullDay").toString())
							|| Boolean.parseBoolean(res.get("canBeHalfDay").toString())) {
						message += "I will apply leave for you";
					} else {
						message += "I will apply half day leave for you";
						timeConstraint = "half";
					}
					break;

				}
				output.setSpeech(message);
				output.setDisplayText(message);

			} else {
				// give suggestion on time constraint
				// triggre time suggestion event
				/*HashMap<String, JsonElement> outParms = new HashMap<>();
				outParms.put("token", new JsonPrimitive("UNWELL"));
				output = Redirections.redirectToSuggestTime(output, outParms);*/
				message = "I will apply leave for you. You take care :)" ;
				output.setSpeech(message);
				output.setDisplayText(message);
			}
		} else {
			message = "I’m sorry that you’re not feeling well, for leave you need to ask your manager, as you don't have sufficient leave balance";
			output = Redirections.redirectToDisplayMessage(output, message);
		}

		return output;
	}

	private Fulfillment getResponseForNotWell(Fulfillment output, HashMap<String, JsonElement> parameter, User user) {
		// TODO Auto-generated method stub
		String disease = parameter.get("disease").getAsString();
		String userSays = parameter.get("userSays").getAsString();
		String message = "";
		Boolean forLeave = false;
		HashMap<String, JsonElement> outParms = new HashMap<>();
		float leave_balance = user.getTotalLeaveBalance();

		if (disease.isEmpty()) {
			//if no disease mention get generic response and set (feeling-sick-followup)
			message = Formator.getNotWellresponse();
			outParms.put("token", new JsonPrimitive("UNWELL"));
			AIOutputContext contextOut = new AIOutputContext();
			contextOut.setParameters(outParms);
			contextOut.setLifespan(1);
			contextOut.setName("feeling-sick-followup");
			output.setSpeech(message);
			output.setDisplayText(message);
		} else {
			//if leave bal suggest for leave (REDIRECT TO SUGGEST TIME INTENT)otherwise go to ask manager
			if (leave_balance > 0) {
				// apply check for leave

				// redirect to time suggestion
				// trigre event suggesting time options
				/*
				 * feeling-sick-follow JSONObject response =
				 * Formator.getOptionForLeave(); message =
				 * "I’m sorry that you’re not feeling well, maybe you should take rest at home."
				 * + response.get("message"); outParms.put("disease", new
				 * JsonPrimitive(disease)); outParms.put("userSays", new
				 * JsonPrimitive(userSays)); outParms.put("isFullDay", new
				 * JsonPrimitive(response.get("isFullDay").toString()));
				 * outParms.put("isHalfDay", new
				 * JsonPrimitive(response.get("isHalfDay ").toString()));
				 * outParms.put("canBeHalfDay", new
				 * JsonPrimitive(response.get("canBeHalfDay").toString()));
				 * outParms.put("time", new
				 * JsonPrimitive(response.get("time").toString()));
				 */
				outParms.put("disease", new JsonPrimitive(disease));
				outParms.put("userSays", new JsonPrimitive(userSays));
				outParms.put("token", new JsonPrimitive("ILL"));
				output = Redirections.redirectToSuggestTime(output, outParms);
			} else {
				// redirect to display message with message saying talk to your
				// manager
				message = "I’m sorry that you’re not feeling well, for leave you need to ask your manager, as you don't have sufficient leave balance";
				output = Redirections.redirectToDisplayMessage(output, message);
			}

		}

		return output;
	}

	private Fulfillment getResponseForOneDayLeaveIntent(Fulfillment output, HashMap<String, JsonElement> parameter,
			User user, String action) {
		// TODO Auto-generated method stub
		log.info("getResponseForOneDayLeaveIntent");
		String startDate = parameter.get("startDate").getAsString().trim();
		String endDate = parameter.get("endDate").getAsString().trim();
		String comment = parameter.get("comment").getAsString().trim();
		String event = parameter.get("dateEvent").getAsString().trim();
		Boolean isFestival = Boolean.parseBoolean(parameter.get("isFestival").getAsString().trim());
		Boolean isHoliday = Boolean.parseBoolean(parameter.get("isHoliday").getAsString().trim());
		Boolean isOneDay = Boolean.parseBoolean(parameter.get("isOneDay").getAsString().trim());
		String message = "";
		log.info("Parms : " + startDate + " " + endDate + " " + comment + " isFest " + isFestival + " isHoliday "
				+ isHoliday + " isOne " + isOneDay);
		HashMap<String, JsonElement> outParms = new HashMap<>();
		if (action.equalsIgnoreCase("ONE_DAY_LEAVE_YES_FOLLOWUP")) {
			// redirect to custom form with comment as event

			outParms.put("comment", new JsonPrimitive(comment));
			output = Redirections.redirectToCustomApply(output, outParms);
		}
		if (action.equals("APPLY_ONE_DAY")) {
			if (isHoliday) {
				message = "Have fun! Happy " + event;
				outParms.put("message", new JsonPrimitive(message));
				output = Redirections.redirectToDisplayMessage(output, outParms);
			} else if (isFestival) {
				outParms.put("comment", new JsonPrimitive(comment));
				output = Redirections.redirectToCustomApply(output, outParms);
			} else {
				message = LeaveMessageFormator.getLeaveDetailMessage(user);
				message += "Which type of leave would you like to apply?";
				AIOutputContext contextOut = new AIOutputContext();
				outParms.put("startDate", new JsonPrimitive(startDate));
				outParms.put("endDate", new JsonPrimitive(endDate));
				outParms.put("comment", new JsonPrimitive(comment));
				contextOut.setParameters(outParms);
				output.setContextOut(contextOut);
				output.setSpeech(message);
				output.setDisplayText(message);
			}
		}
		return output;
	}

	private Fulfillment getLeaveBalance(Fulfillment output, HashMap<String, JsonElement> parameter, User user) {
		log.info("get leave balance function");
		float PL = user.getPrivilagedLeave();
		log.info("PL :" + PL);
		float OH = user.getOptionalHoliday();
		log.info("OH :" + OH);
		float OL = user.getOptionalLeave();
		log.info("OL :" + OL);
		float CF = user.getCompensatioryOff();
		log.info("CF :" + CF);
		output.setDisplayText("You have " + PL + " privileged leave, " + OL + " optional leave, " + OH
				+ " optional holiday and " + CF + " compensatory Off" + " #false");
		output.setSpeech("You have " + PL + " privileged leave, " + OL + " optional leave, " + OH
				+ " optional holiday and " + CF + " compensatory Off" + " #false");

		return output;
	}

	private Fulfillment getFallBackResponse(Fulfillment output, AIWebhookRequest input) {

		String question = input.getResult().getResolvedQuery();
		//JSONObject response = SearchFunction.fetchAnswerFromDatastore(question); // call fetch answerFromDatastore
		JSONObject response = SearchFunctionComputeEngine.fetchAnswerFromDatastore(question); // call fetch answerFromDatastore
		log.info(response.toJSONString());
		String answer = (String) response.get("answer");
		output.setDisplayText(answer);
		output.setSpeech(answer);
		return output;
	}

	private Fulfillment applyLeaveWithTypes(Fulfillment output, HashMap<String, JsonElement> parameter, User user) {
		String startDate = parameter.get("startDate").getAsString().trim();
		String endDate = parameter.get("endDate").getAsString().trim();
		String comment = parameter.get("comment").getAsString().trim();
		String leaveType = parameter.get("leaveType").getAsString().trim();
		//String leave = parameter.get("noOfLeave").getAsString().trim();
		Leave leave = new Leave(startDate, endDate, comment);
		log.info(leave + " no of leaves");
		//float noOfLeave = Float.parseFloat(leave);
		/*int response = Server.applyLeaveInSystem(startDate, endDate, user.getSession().getUserName(), comment,
				leaveType, noOfLeave);
		String message = "";
		if (response == 200) {
			message = "Your leaves are applied successfully in the system.#false";

		} else {
			message = "Sorry #usr#. Unable to apply leave. Please try after sometime.";
		}*/
		JSONObject respponse  = PiHandler.applyLeave(user,leaveType,leave);
		String message = respponse.get("message").toString();
		output.setDisplayText(message);
		output.setSpeech(message);
		return output;
	}

	private Fulfillment getLeaveBreakup(Fulfillment output, HashMap<String, JsonElement> parameter, User user) {
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
		float noOfLeave = Float.parseFloat(leave.trim());
		float PL = user.getPrivilagedLeave();
		float OH = user.getOptionalHoliday();
		float OL = user.getOptionalLeave();
		float CF = user.getCompensatioryOff();
		log.info(" pressent values : PL " + PL + " CF " + CF + " OH " + OH + "  OL  " + OL);
		String message = "";
		JSONObject leaveJson = new JSONObject();
		float balance = 0;
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
				message += "1 " + type + " on " + Formator.getFormatedDate(startDate) + ". Is it correct?";

			} else {
				message += noOfLeave + " " + day + " " + type + " from " + Formator.getFormatedDate(startDate) + " to "
						+ Formator.getFormatedDate(endDate) + ". Is it correct?";
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
			String action, User user) {
		log.info("getLeaveComboSuggestion");
		String startDate = parameter.get("startDate").getAsString().trim();
		String endDate = parameter.get("endDate").getAsString().trim();
		String comment = "";
		log.info("parms :" + startDate + " " + endDate);
		comment = parameter.get("comment").getAsString().trim();
		log.info("comment " + comment);
		String timeConstraint = parameter.get("timeConstraint").getAsString().trim();
		Boolean haveOther = false;
		String message = "";
		// check leave balance > days to apply
		float leave_balance = user.getTotalLeaveBalance();
		float noOfLeaves = 0.0f;
		float PL = user.getPrivilagedLeave();
		float OH = user.getOptionalHoliday();
		float OL = user.getOptionalLeave();
		float CF = user.getCompensatioryOff();
		if (!timeConstraint.isEmpty()) {
			switch (timeConstraint) {
			case "full":
				noOfLeaves = 1;
				break;
			case "half":
				break;
			case "now":
				noOfLeaves = 1;
				break;

			}
		} else {
			JSONObject jsonDays = DateDetails.getDays(startDate, endDate);
			noOfLeaves = Integer.parseInt(jsonDays.get("days").toString());
		}
		log.info("balance :" + leave_balance + " required :" + noOfLeaves);

		if (PL >= 1 && !timeConstraint.isEmpty()) {
			// apply leave and go to display text
			output = applyLeaveUrgentCase(output, parameter);
		} else if (PL < noOfLeaves) {
			log.info("PL < REQ");
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
			if (CF >= noOfLeaves || OH >= noOfLeaves || OL >= noOfLeaves) {
				haveOther = true;
				message += "You have ";
				if (CF >= noOfLeaves) {
					message += CF + " compensatory off, ";

				}
				if (OH >= noOfLeaves) {
					message += OH + " optional holiday, ";
				}
				if (OL >= noOfLeaves) {
					message += OL + " optional leave.";
				}
				if (CF >= noOfLeaves || OH >= noOfLeaves || OL >= noOfLeaves) {
					// message += "Do you still want to apply for privilage
					// leave ?
					// Don't forget these leaves won't carry forward.";
					message += " Do you still want to apply for privilaged leave?";
				}
			} else {
				message = "You have " + PL + "privilaged leave. Shall we proceed?";
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
		AIOutputContext contextOut = new AIOutputContext();
		HashMap<String, JsonElement> outParms = new HashMap<>();
		outParms.put("haveOther", new JsonPrimitive(haveOther)); 
		contextOut.setLifespan(1);
		contextOut.setName("webhook-parms");
		contextOut.setParameters(outParms);
		output.setContextOut(contextOut);
		//SuggestLeaveOptions-followup
		output.setSpeech(message);
		output.setDisplayText(message);
		return output;

	}

	private Fulfillment applyLeaveUrgentCase(Fulfillment output, HashMap<String, JsonElement> parameter) {
		String startDate = parameter.get("startDate").getAsString().trim();
		String endDate = parameter.get("endDate").getAsString().trim();
		String comment = parameter.get("comment").getAsString().trim();
		String timeConstraint = parameter.get("timeConstraint").getAsString().trim();

		// function call for applying leave
		
		
		String message = "Your leave has been applied, take care";
		
		output = Redirections.redirectToDisplayMessage(output, message);
		return output;
	}

	private Fulfillment exitFlow(Fulfillment output) {
		// TODO Auto-generated method stub TERMINATE comment
		return Redirections.redirectToTerminate(output);

	}

	private Fulfillment applyLeave(Fulfillment output, HashMap<String, JsonElement> parameter, User user) {
		log.info("apply leave function");
		String startDate = parameter.get("startDate").getAsString().trim();
		String endDate = parameter.get("endDate").getAsString().trim();
		String comment = parameter.get("comment").getAsString().trim();
		log.info("parms :" + startDate + " " + endDate + " comment : " + comment);
		String message = "";
		Leave leave = new Leave(startDate, endDate, comment);
		float leave_balance = user.getTotalLeaveBalance();
		// check bal if allow apply
		JSONObject jsonDays = DateDetails.getDays(startDate, endDate);
		float noOfLeaves = Integer.parseInt(jsonDays.get("days").toString());
		if (leave_balance <= 0) {
			log.info("bal < 0");
			output = Redirections.redirectToDPApproval(output, parameter);
		} else if (leave_balance >= noOfLeaves) {
			log.info("bal > no Leaves ");
			String leaveType = "PL";
			/*int response = Server.applyLeaveInSystem(startDate, endDate, user.getUserName(), comment, leaveType,
					noOfLeaves);
			if (response == 200) {
				message = "Your leaves are applied successfully in the system.#false";

			} else {
				message = "Sorry #usr#. Unable to apply leave. Please try after sometime.";
			}*/
			JSONObject respponse  = PiHandler.applyLeave(user,"PL",leave);
			 message = respponse.get("Message").toString();
			 
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

	private Fulfillment queryLeave(Fulfillment output, HashMap<String, JsonElement> parameter, User user, String action,
			AIWebhookRequest input) throws ParseException {
		log.info("querry leave function");
		String startDate = null;
		String endDate = null;
		String comment;
		float leave_balance = user.getTotalLeaveBalance();
		String message = "";
		if (action.equals("ONE_DAY_LEAVE")) {
			log.info("one day leave apply");
			startDate = parameter.get("date").getAsString().trim();
			String event = parameter.get("dateEvent").getAsString().trim();
			comment = parameter.get("comment").getAsString().trim();
			log.info("parms :" + startDate + " " + endDate + " comment : " + comment);
			JSONObject responseMessageObject = LeaveMessageFormator.getMessageForFestival(event, startDate, comment);
			log.info("resp : " + responseMessageObject);
			Boolean isHoliday = Boolean.parseBoolean(responseMessageObject.get("isHoliday").toString());
			Boolean isFestival = Boolean.parseBoolean(responseMessageObject.get("isFestival").toString());
			Boolean isOneDay = Boolean.parseBoolean(responseMessageObject.get("isOneDay").toString());
			log.info("boolean : fetched");
			message += responseMessageObject.get("message").toString();
			log.info("msg : " + message);
			if (leave_balance <= 0 && isHoliday) {
				// intent to display text and break;
				log.info("bal < 0 & holiday");
				message = "Its holiday for " + event;
				HashMap<String, JsonElement> outParms = new HashMap<>();
				outParms.put("message", new JsonPrimitive(message));
				output = Redirections.redirectToDisplayMessage(output, outParms);

			} else if (leave_balance > 0 && isFestival || isHoliday) {
				log.info("bal > 0 && festival or holiday");
				message += responseMessageObject.get("longVaccationSugestion");
				comment = getMessage(event);
				AIOutputContext contextOut = new AIOutputContext();
				HashMap<String, JsonElement> outParms = new HashMap<>();
				outParms.put("comment", new JsonPrimitive(comment));
				outParms.put("startDate", new JsonPrimitive(startDate));
				outParms.put("endDate", new JsonPrimitive(startDate));
				outParms.put("dateEvent", new JsonPrimitive(event));

				outParms.put("isHoliday", new JsonPrimitive(isHoliday));
				outParms.put("isFestival", new JsonPrimitive(isFestival));
				outParms.put("isOneDay", new JsonPrimitive(isOneDay));
				contextOut.setLifespan(1);
				contextOut.setName("oneDayLeaveFollowup");
				contextOut.setParameters(outParms);
				output.setContextOut(contextOut);
				output.setDisplayText(message);
				output.setSpeech(message);
			}
			// if is oneday go to SUGGEST_lEAVES_OPTION
			else if (isOneDay) {
				log.info("is one day leave");
				JSONObject jsonDays = DateDetails.getDays(startDate, startDate);
				log.info("getDays : " + jsonDays);
				Boolean isWeekend = Boolean.parseBoolean(jsonDays.get("isWeekEnd").toString());
				if (isWeekend) {
					log.info("is week-end");
					if (event.equalsIgnoreCase("today")) {
						message = " Its weekend today.";
						isOneDay = true;

					}
					else if (event.equalsIgnoreCase("tomorrow")) {
						message = "It was weekend yesterday.";
						isOneDay = true;
					}else{
						message = "Its weekend on "+Formator.getFormatedDate(startDate)+". Have Fun";
					}
					

					HashMap<String, JsonElement> outParms = new HashMap<>();
					outParms.put("message", new JsonPrimitive(message));
					output = Redirections.redirectToDisplayMessage(output, outParms);
				}
				else if (leave_balance > 0) {
					log.info("bal > 0 ask leave type");
					HashMap<String, JsonElement> outParms = new HashMap<>();
					AIOutputContext contextOut = new AIOutputContext();
					outParms.put("startDate", new JsonPrimitive(startDate));
					outParms.put("endDate", new JsonPrimitive(startDate));
					outParms.put("comment", new JsonPrimitive(comment));
					output = Redirections.redirectToQueryLeaveWithParms(output, outParms);
					// output = Redirections.redirectToCustomApply(output,
					// outParms);
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
			log.info("parms :" + startDate + " " + endDate + " comment : " + comment);
			if (comment.isEmpty()) {
				String event = parameter.get("event").getAsString().trim();
				if (!event.isEmpty()) {
					comment = "leave for " + event;
				}
			}
			log.info("parms :" + startDate + " " + endDate + " comment: " + comment);
			if (leave_balance > 0) {
				HashMap<String, JsonElement> outParms = new HashMap<>();
				outParms.put("comment", new JsonPrimitive(comment));
				outParms.put("startDate", new JsonPrimitive(startDate));
				outParms.put("endDate", new JsonPrimitive(endDate));
				if (!comment.isEmpty() || !startDate.isEmpty() || !endDate.isEmpty()) {

				} else {
					String msg = "Great you have sufficient Leave balance. From when you want leave?";
					outParms.put("msg", new JsonPrimitive(msg));
				}
				output = Redirections.redirectToQueryLeaveWithParms(output, outParms);
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
			log.info("parms :" + startDate + " " + endDate + " comment : " + comment);
			output = Formator.getLeaveConfirmationMessage(startDate, endDate, comment, leave_balance, output);
		}
		return output;
	}

	private Fulfillment getConfirmationMessage(Fulfillment output, HashMap<String, JsonElement> parameter,
			String action, User user) throws ParseException {
		log.info("getConfirmationMessage");
		String startDate = parameter.get("startDate").getAsString().trim();
		String endDate = parameter.get("endDate").getAsString().trim();
		String comment = "";
		String event = "";
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
		float leave_balance = user.getTotalLeaveBalance();
		JSONObject jsonDays = DateDetails.getDays(startDate, endDate);
		float noOfLeaves = Integer.parseInt(jsonDays.get("days").toString());
		Boolean isWeekend = Boolean.parseBoolean(jsonDays.get("isWeekEnd").toString());
		float totalDays = DateDetails.getDaysBetweenDates(startDate, endDate);
		log.info("balance :" + leave_balance + " required :" + noOfLeaves + " total days : " + totalDays);
		if (leave_balance <= 0 || leave_balance < noOfLeaves) {
			log.info("bal < 0");
			log.info(
					"Sorry, you have insufficient leave balance, you will need DP approval If want to apply for leave.");
			output = Redirections.redirectToDPApproval(output, parameter);
		} else if (leave_balance >= noOfLeaves) {
			log.info("req > bal");
			if (noOfLeaves == 1) {
				message += "You want to apply leave on " + Formator.getFormatedDate(startDate) + ".";
				message += Formator.getWeekendContainsMessage(startDate, endDate, noOfLeaves);

				// + ". Should I confirm?";
			} else if (totalDays == 2 && Boolean.parseBoolean(jsonDays.get("isWeekEnd").toString().trim())) {
				Date start = new SimpleDateFormat("yyyy-MM-dd").parse(startDate);
				Date end = new SimpleDateFormat("yyyy-MM-dd").parse(endDate);
				JSONObject holidayMap = (JSONObject) jsonDays.get("holidayTrack");
				// check for contains
				if (((String) holidayMap.get(start)).equalsIgnoreCase("Saturday")
						&& ((String) holidayMap.get(end)).equalsIgnoreCase("Sunday")) {
					// redirect to send message
					message = "Its weekend from " + Formator.getFormatedDate(start) + " to "
							+ Formator.getFormatedDate(end) + ". No need to apply for leave. Enjoy!";
					HashMap<String, JsonElement> outParms = new HashMap<>();
					outParms.put("message", new JsonPrimitive(message));
					output = Redirections.redirectToDisplayMessage(output, outParms);
				}
			} else if (leave_balance >= noOfLeaves) {
				if (noOfLeaves == 1 && Boolean.parseBoolean(jsonDays.get("isWeekEnd").toString().trim())) {
					message = "Hey #usr#! Its holiday.";
					message += Formator.getWeekendContainsMessage(startDate, endDate, noOfLeaves);
					output = Redirections.redirectToDisplayMessage(output, message);
				} else if (noOfLeaves == 1)
					message = "You want to apply leave on " + Formator.getFormatedDate(startDate);
				else {
					message = "So you want to apply leave from " + Formator.getFormatedDate(startDate) + " to "
							+ Formator.getFormatedDate(endDate) + ". ";
					message += Formator.getWeekendContainsMessage(startDate, endDate, noOfLeaves);
				}

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
