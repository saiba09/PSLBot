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
		// add constants in file
		try {
			switch (action) {
			case "QUERY_LEAVE":
				log.info("in action : query_leave");
				output = queryLeave(output, parameter); // check for oh , cf etc
				break;
			case "SYSTEM_SUGESTION_SATISFIED_YES":
				log.info(" intent SYSTEM_SUGESTION_SATISFIED_YES ");
				output = getConfirmationMessage(output, parameter, "SYSTEM_SUGESTION_SATISFIED_YES"); 
				break;
			case "SYSTEM_SUGESTION_SATISFIED_NO":
				log.info("intent : SYSTEM_SUGESTION_SATISFIED_NO");
				output = getConfirmationMessage(output, parameter, "SYSTEM_SUGESTION_SATISFIED_NO");
				break;
			case "SUGGEST_LEAVES_OPTION":
				log.info("intent : SUGGEST_LEAVES_OPTION");
				output = getLeaveComboSuggestion(output, parameter, "SUGGEST_LEAVES_OPTION"); 
				// give suggestion for oh pl combo, calc no of days,
				break;
			case "OPT_CUSTOM_REQ":
				log.info("intent: OPT_CUSTOM_REQ");
				output = redirectToCustomApply(output, parameter);
				break;
			case "CONFIRM_APPLY":
				log.info("intent : CONFIRM_APPLY");
				output = applyLeave(output, parameter);
			case "SYST_SUG_NOT_SATISFIED_CUST_CONFIRM":
				log.info("SYST_SUG_NOT_SATISFIED_CUST_CONFIRM");
				output = applyLeave(output, parameter); // APPLY LEAVE IN SYSTEM
				break;
			case "CUSTOM_FORM_SUBMIT":
				log.info("intent : CUSTOM_FORM_SUBMIT");
				output = getConfirmationMessage(output, parameter,"CUSTOM_FORM_SUBMIT");
				break;
			case "APPLY_COMBO_LEAVE" :
				log.info("intent : APPLY_COMBO_LEAVE");
				output = redirectToComboLeaveForm(output, parameter);
				break;
			case "CUSTOM_FORM_SUBMIT_CONFIRM":
				log.info("INTENT : CUSTOM_FORM_SUBMIT_CONFIRM");
				output = applyLeave(output, parameter);
				break;
			case "LEAVE_TYPE_SELECTION":
				log.info("intent : LEAVE_TYPE_SELECTION_CONFRIM_MESSAGE");
				// message for confirmation returns leave brk up
				break;
			case "LEAVE_TYPE_SELECTION_CONFRIM_MESSAGE":
				log.info("intent : LEAVE_TYPE_SELECTION_CONFRIM_MESSAGE");
				// apply leave
				/*
				 * case "input.welcome": log.info("input.welcome"); output =
				 * eventTriggered(output); break;
				 */
			default:
				output.setSpeech("Default case");
				break;
			}
		} catch (Exception e) {
			log.info("exception : " + e);
		}

	}

	private Fulfillment redirectToComboLeaveForm(Fulfillment output, HashMap<String, JsonElement> parameter) {
		log.info("redirectToComboLeaveForm event trig fun");
		String startDate = parameter.get("startDate").getAsString();
		String endDate = parameter.get("endDate").getAsString();
		String comment = parameter.get("comment").getAsString();
		JSONObject data = Data.getHolidays();
		int PL = Integer.parseInt(data.get("leave_balance").toString());
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
			String string) {
		log.info("getLeaveComboSuggestion");
		String startDate = parameter.get("startDate").getAsString();
		String endDate = parameter.get("endDate").getAsString();
		String comment = "";
		log.info("parms :" + startDate + " " + endDate);
		comment = parameter.get("comment").getAsString();
		log.info("comment " + comment);
		JSONObject sugestion = Data.getHolidays();
		String message = "";
		// check leave balance > days to apply
		int leave_balance = Integer.parseInt(getLeaveInfo().get("count").toString());
		JSONObject jsonDays = getDays(startDate, endDate);
		int noOfLeaves = Integer.parseInt(jsonDays.get("days").toString());
		JSONObject data = Data.getHolidays();
		int PL = Integer.parseInt(data.get("leave_balance").toString());
		int OH = Integer.parseInt(data.get("optional_holiday").toString());
		int OL = Integer.parseInt(data.get("optional_leave").toString());
		int CF = Integer.parseInt(data.get("compensatiory_off").toString());
		log.info("balance :" + leave_balance + " required :" + noOfLeaves);
		if (PL < noOfLeaves ) {
			message = "Your PL balance is less than what is you opt. for "+noOfLeaves+ "and applying for same, will need DP approval if you wish to continue.";
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
				message += CF+" comp. off, ";

			}
			if (OH != 0) {
				message += OH+ " optional leave, ";
			}
			if (OL != 0) {
				message += OL+ " optional leave.";
			}
			if (CF != 0 || OH != 0 || OL != 0) {
				message += "Do you still want to apply for privilage leave ? Don't forget these leaves won't carry forward.";
			}
		}
		log.info(message);
		//context : system-sugestion-satisfied-yes-yes-suggestOptions-followup
		AIOutputContext contextOut = new AIOutputContext();
		HashMap<String, JsonElement> outParms = new HashMap<>();
		outParms.put("comment", new JsonPrimitive(comment));
		outParms.put("startDate", new JsonPrimitive(startDate));
		outParms.put("endDate", new JsonPrimitive(endDate));
		contextOut.setLifespan(1);
		contextOut.setName("system-sugestion-satisfied-yes-yes-suggestOptions-followup");
		contextOut.setParameters(outParms);
		// set cont.
		output.setContextOut(contextOut);
		
		return output;

	}

	private Fulfillment exitFlow(Fulfillment output) {
		// TODO Auto-generated method stub
		return null;
	}

	private Fulfillment redirectToCustomApply(Fulfillment output, HashMap<String, JsonElement> parameter) {
		// Trigger event for custom leave apply
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
		log.info("parms :" + startDate + " " + endDate + " comment : " + comment);
		String message = "";
		int leave_balance = Integer.parseInt(Data.getHolidays().get("PL").toString());
		// check bal if allow apply
		JSONObject jsonDays = getDays(startDate, endDate);
		int noOfLeaves = Integer.parseInt(jsonDays.get("days").toString());
		if (leave_balance <= 0) {
			log.info("bal < 0");
			message = "Sorry dear, you have insufficient leave balance, you will need DP approval If want to apply for leave.";
			// Trigger dp approval intent
			log.info("DP APPROVAL REQ event trig ");
			AIEvent followupEvent = new AIEvent("DP_APPROVAL");
			log.info("rerouting to event : evt trg");
			output.setFollowupEvent(followupEvent);
		} else if (leave_balance > noOfLeaves) {
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
		output.setSpeech(message);
		output.setDisplayText(message);
		return output;
	}

	private Fulfillment queryLeave(Fulfillment output, HashMap<String, JsonElement> parameter) throws ParseException {
		log.info("querry leave function");
		String startDate = parameter.get("startDate").getAsString();
		String endDate = parameter.get("endDate").getAsString();
		String event = parameter.get("event").getAsString();
		log.info("parms :" + startDate + " " + endDate + " event: " + event);
		String message = "";
		message += " " + getLeaveInfo().get("message") + "  ";
		JSONObject sugestion = Suggest(parameter);
		int leave_balance = Integer.parseInt(getLeaveInfo().get("count").toString());
		if (leave_balance > 0) {
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
					message += "you have sufficient leave balance. apply";
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
		}
		log.info(message);
		output.setSpeech(message);
		output.setDisplayText(message);
		return output;
	}

	private Fulfillment getConfirmationMessage(Fulfillment output, HashMap<String, JsonElement> parameter , String action) {
		log.info("getConfirmationMessage");
		String startDate = parameter.get("startDate").getAsString();
		String endDate = parameter.get("endDate").getAsString();
		String comment = "";
		String event = "";
		JSONObject sugestion = Data.getHolidays();
		String message = "";
		log.info("parms :" + startDate + " " + endDate);
		if (parameter.containsKey("comment")) {
			comment = parameter.get("comment").getAsString();
			log.info("comment " + comment);
		} else {
			event = parameter.get("event").getAsString();
			comment = getMessage(event);
			log.info("event :" + event + " comment :" + comment);
		}
		// check leave balance > days to apply
		int leave_balance = Integer.parseInt(getLeaveInfo().get("count").toString());
		JSONObject jsonDays = getDays(startDate, endDate);
		int noOfLeaves = Integer.parseInt(jsonDays.get("days").toString()); 
		log.info("balance :" + leave_balance + " required :" + noOfLeaves);
		if (leave_balance <= 0) {
			log.info("bal < 0");
			message = "Sorry dear, you have insufficient leave balance, you will need DP approval If want to apply for leave.";
			// triggre dp approval inteent
			log.info("DP APPROVAL REQ event trig ");
			AIEvent followupEvent = new AIEvent("DP_APPROVAL");
			log.info("rerouting to event : evt trg");
			output.setFollowupEvent(followupEvent);
		} else if (leave_balance > noOfLeaves) {
			log.info("req > bal");
			if (action.equalsIgnoreCase("SUGGEST_LEAVES_OPTION")) {
				
				if (Boolean.parseBoolean(jsonDays.get("isWeekEnd").toString())) {
					HashMap<String, String> holidayMap = (HashMap<String, String>) jsonDays.get("holidayTrack");
					message += "However its, ";
					for (String date : holidayMap.keySet()) {
						String day = holidayMap.get(date).toString();
						message += "  " +day+" on "+new SimpleDateFormat("MMM d").format(date);
					}
					message += "shall we continue the plan ?";
				}else{
					message += "No weekends or holidays in between. Are you sure you wanna plan this vaccation ?";
				}
				
				
/*				if (Boolean.parseBoolean(getLeaveInfo().get("isAvailable").toString())) {
					message += "And"+getLeaveInfo().get("message") +" Do you still want to apply privilage leave ?";
				}*/
			}
			// suggestion for ol cf if present
			if (action.equalsIgnoreCase("SYSTEM_SUGESTION_SATISFIED_YES")) {
				/**/
				message = "So you want to apply from "+startDate + " to " + endDate + "as " +comment; 
				if (Boolean.parseBoolean(jsonDays.get("isWeekEnd").toString())) {
					HashMap<String, String> holidayMap = (HashMap<String, String>) jsonDays.get("holidayTrack");
					message += "However its, ";
					for (String date : holidayMap.keySet()) {
						String day = holidayMap.get(date).toString();
						message += "  " +day+" on "+new SimpleDateFormat("MMM d").format(date);
					}
				}
				message += "Should I confirm?";
				AIOutputContext contextOut = new AIOutputContext();
				HashMap<String, JsonElement> outParms = new HashMap<>();
				outParms.put("comment", new JsonPrimitive(comment));
				outParms.put("startDate",new JsonPrimitive(startDate));
				outParms.put("endDate",new JsonPrimitive(endDate));

				contextOut.setLifespan(1);
				contextOut.setName("system-sugestion-satisfied-no-follow-up");
				contextOut.setParameters(outParms);
				// set cont.
				output.setContextOut(contextOut);
			}
			/*			
	*/
			output.setSpeech(message);
			output.setDisplayText(message);
		} else {
			message = "Your leave balance is less than :" + noOfLeaves
					+ ". You will need Delivery partner approval if you will apply. Or dear if you say shall I apply for "
					+ leave_balance + " days.";

			log.info(message);
			output.setSpeech(message);
			output.setDisplayText(message);
			// IMP : set out parmas end date- diff
		}
		log.info(message);

		return output;
	}

	private static String getMessage(String event) {
		// TODO Auto-generated method stub
		return "Leave for " + event;
	}

	private static JSONObject Suggest(HashMap<String, JsonElement> parameter) {
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
				msg = "And! Its your birthday on " + new SimpleDateFormat("MMM d").format(birthday)
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

	private static JSONObject getLeaveInfo() {
		String message = "";
		JSONObject data = Data.getHolidays();
		int PL = Integer.parseInt(data.get("leave_balance").toString());
		int OH = Integer.parseInt(data.get("optional_holiday").toString());
		int OL = Integer.parseInt(data.get("optional_leave").toString());
		int CF = Integer.parseInt(data.get("compensatiory_off").toString());
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
						message = "Buddy! you have " + PL + "previlage leave with you."
								+ "But I would suggest you should take compensatiory leaves as you have " + CF
								+ " comp. off available Do consume it, I know you don't wanna lose them.";
					} else {
						message = "Buddy! you have " + PL + "previlage leave with you."
								+ "But I would suggest you should take compensatiory leaves as you have " + CF
								+ " comp. off available. However you can also opt for optional leave as you have " + OL
								+ " optional leave available.Do consume it, I know you don't wanna lose them.";
					}
				} else {
					if (OL <= 0) {
						log.info("no OL");
						message = "Buddy! you have " + PL + "previlage leave with you."
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
			String date2 = "31/01/2018";
			Date last = new SimpleDateFormat("dd/MM/yyyy").parse(date2);
			log.info("method returns");
			return testDate.before(today) || testDate.after(today);
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

			while (calS.compareTo(calE) != 0) {
				if (calS.DAY_OF_WEEK != Calendar.SATURDAY || calS.DAY_OF_WEEK != Calendar.SUNDAY) {
					days++;
					log.info("inc date");
					calS.add(Calendar.DATE, 1);
					log.info("date inc : " + calS);
				} else {
					if (calS.DAY_OF_WEEK == Calendar.SATURDAY) {
						holidayTrack.put(calS, "Saturday");

					} else {
						holidayTrack.put(calS, "Sunday");
					}
					isWeekEnd = true;
				}
			}
			days++;
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

	/*
	 * private static final Logger log =
	 * Logger.getLogger(MyWebhookServlet.class.getName());
	 * 
	 * @Override protected void doWebhook(AIWebhookRequest input, Fulfillment
	 * output) {
	 * 
	 * log.info("webhook call"); String action = input.getResult().getAction();
	 * HashMap<String, JsonElement> parameter =
	 * input.getResult().getParameters(); // add constants in file try { switch
	 * (action) { case "QUERY_LEAVE": log.info("in action : query_leave");
	 * output = queryLeave(output, parameter); break; case
	 * "SYSTEM_SUGESTION_SATISFIED_YES":
	 * log.info(" intent SYSTEM_SUGESTION_SATISFIED_YES "); output =
	 * submitFeilds(output, parameter); break; case
	 * "SYSTEM_SUGESTION_SATISFIED_NO":
	 * log.info("intent : SYSTEM_SUGESTION_SATISFIED_NO"); output =
	 * fallbackCustomApply(output, parameter); break; case "APPLY_LEAVE_CUSTOM":
	 * log.info("intent APPLY_LEAVE_CUSTOM"); output = submitFeilds(output,
	 * parameter); break; case "CONFIRM_LEAVE_YES":
	 * log.info("intent CONFIRM_LEAVE_YES"); output = confirmLeave(output,
	 * parameter); break; case "CONFIRM_LEAVE_NO": log.info("CONFIRM_LEAVE_NO");
	 * output = exitFlow(output); // response if yes replan goto custom break;
	 * case "RESTART": log.info("intent : restart"); output =
	 * fallbackCustomApply(output, parameter); break; case "EXIT":
	 * log.info("exit"); output = exitFlow(output); break; case "input.welcome":
	 * log.info("input.welcome"); output = eventTriggered(output); break;
	 * default: output.setSpeech("Default case"); break; } } catch (Exception e)
	 * { log.info("exception : " + e); } //
	 * output.setSpeech(input.getResult().toString());
	 * 
	 * // output.setSpeech("from webhook"); }
	 * 
	 * private Fulfillment eventTriggered(Fulfillment output) {
	 * 
	 * with all params except event go to custom leave apply
	 * 
	 * 
	 * 
	 * log.info("event trig fun"); Map<String, String> outParameter = new
	 * HashMap<>(); AIEvent followupEvent = new AIEvent("event_triggered");
	 * String message =
	 * "Wanna do it yourself?  Okay! I would not give my suggestion, just let me know the details. I will apply for you."
	 * ; followupEvent.setData(outParameter);
	 * 
	 * log.info("rerouting to event : evt trg");
	 * output.setFollowupEvent(followupEvent);
	 * 
	 * output.setSpeech(message); output.setDisplayText(message);
	 * 
	 * return output; }
	 * 
	 * private Fulfillment queryLeave(Fulfillment output, HashMap<String,
	 * JsonElement> parameter) throws ParseException { // TODO Auto-generated
	 * method stub log.info("inside queryLeave"); HashMap<String, Integer>
	 * holidayData = new HashMap<>(Data.getHolidays()); log.info("holiday " +
	 * holidayData.toString()); AIOutputContext contextOut = new
	 * AIOutputContext(); String message = ""; int balance =
	 * holidayData.get("leave_balance"); log.info("bal :" + balance); int days =
	 * 0; log.info("parms : " + parameter.get("startDate") + " , "+
	 * parameter.get("endDate")); HashMap<String, JsonElement> outParameters =
	 * new HashMap<String, JsonElement>();
	 * log.info("parms equal :"+parameter.get("noOfDays").getAsString().isEmpty(
	 * )); if (parameter.containsKey("noOfDays") &&
	 * !(parameter.get("noOfDays").getAsString().isEmpty())) {
	 * log.info("contains no of days"); // days =
	 * Integer.parseInt(parameter.get("noOfDays")); JsonElement
	 * contextOutParameter; contextOutParameter = new JsonPrimitive(days);
	 * outParameters.put("noOfDays", contextOutParameter); } if
	 * (parameter.containsKey("startDate") && parameter.containsKey("endDate"))
	 * { if (! parameter.get("startDate").getAsString().isEmpty()) {
	 * log.info("start date"); JsonElement startDate = new
	 * JsonPrimitive(parameter.get("startDate").toString());
	 * outParameters.put("startDate", startDate);
	 * 
	 * } if (! parameter.get("endDate").getAsString().isEmpty()) {
	 * log.info("endDate"); JsonElement endDate = new
	 * JsonPrimitive(parameter.get("endDate").toString());
	 * outParameters.put("endDate", endDate); } log.info("resp " + !
	 * (parameter.get("endDate").getAsString().isEmpty() &&
	 * parameter.get("startDate").getAsString().isEmpty())); if
	 * (!(parameter.get("endDate").getAsString().isEmpty() &&
	 * parameter.get("startDate").getAsString().isEmpty())) { days =
	 * getDays(parameter.get("startDate").toString(),
	 * parameter.get("endDate").toString()); JsonElement noOfDay = new
	 * JsonPrimitive(days);// fetched no of outParameters.put("noOfDays",
	 * noOfDay); // fetch no of days }
	 * 
	 * } if (parameter.containsKey("event") && !
	 * parameter.get("event").getAsString().isEmpty()) { JsonElement
	 * contextOutParameter; contextOutParameter = new
	 * JsonPrimitive(parameter.get("event").getAsString());
	 * outParameters.put("event", contextOutParameter); } if (balance <= 0) {
	 * message =
	 * "Sorry dear, you have insufficient leave balance, you will need DP approval If want to apply for leave."
	 * ; contextOut.setLifespan(2); contextOut.setName("InsufficientBalance");
	 * contextOut.setParameters(outParameters);
	 * output.setContextOut(contextOut); log.info("insufficent balance"); } else
	 * if (balance < 3 && days < 3) { message =
	 * "Your leave balance is low. You are having only " + balance +
	 * ". Do you still want to apply for leave ?"; List<AIOutputContext>
	 * contextOutList = new LinkedList<AIOutputContext>(); AIOutputContext
	 * contextOut1 = new AIOutputContext(); contextOut1.setLifespan(2);
	 * contextOut1.setName("SystemSugestionSatisfied-Yes");
	 * contextOut1.setParameters(outParameters);
	 * contextOutList.add(contextOut1); AIOutputContext contextOut2 = new
	 * AIOutputContext(); contextOut2.setLifespan(2);
	 * contextOut2.setName("SystemSugestionSatisfied-no");
	 * contextOut2.setParameters(outParameters);
	 * contextOutList.add(contextOut2);
	 * log.info("Context out parameters : if low balance");
	 * output.setContextOut(contextOutList);
	 * 
	 * } else if (balance < days) { message =
	 * "Your leave balance is less than :" + days +
	 * ". You will need Delivery partner approval if you will apply. Still wanna apply? Or dear you can apply for "
	 * + days + " days."; List<AIOutputContext> contextOutList = new
	 * LinkedList<AIOutputContext>(); AIOutputContext contextOut1 = new
	 * AIOutputContext(); contextOut1.setLifespan(2);
	 * contextOut1.setName("SystemSugestionSatisfied-Yes");
	 * contextOut1.setParameters(outParameters);
	 * contextOutList.add(contextOut1); AIOutputContext contextOut2 = new
	 * AIOutputContext(); contextOut2.setLifespan(2);
	 * contextOut2.setName("SystemSugestionSatisfied-no");
	 * contextOut2.setParameters(outParameters);
	 * contextOutList.add(contextOut2);
	 * log.info("Context out parameters : if low balance");
	 * output.setContextOut(contextOutList); log.info("balance < req days");
	 * 
	 * } else { // api call to check for event JSONObject eventResponse =
	 * Suggest(parameter); if
	 * (Boolean.parseBoolean(eventResponse.get("present").toString())) {
	 * outParameters.put("event", new
	 * JsonPrimitive(eventResponse.get("event").toString())); message =
	 * eventResponse.get("message").toString(); message += "You have "+ balance
	 * + " leaves available"; }else{ message = "Hurry you have " + balance +
	 * " leaves remaining. You can apply for leave. Shall we proceed or you have a second thought?"
	 * ; } AIOutputContext contextOut1 = new AIOutputContext();
	 * contextOut1.setLifespan(2);
	 * contextOut1.setName("SystemSugestionSatisfied-Yes");
	 * contextOut1.setParameters(outParameters); List<AIOutputContext>
	 * contextOutList = new LinkedList<>(); contextOutList .add(contextOut1);
	 * AIOutputContext contextOut2 = new AIOutputContext();
	 * contextOut2.setLifespan(2);
	 * contextOut2.setName("SystemSugestionSatisfied-no");
	 * contextOut2.setParameters(outParameters);
	 * contextOutList.add(contextOut2);
	 * log.info("Context out parameters : if low balance");
	 * output.setContextOut(contextOutList);
	 * 
	 * } output.setDisplayText(message); output.setSpeech(message); return
	 * output; }
	 * 
	 * private Fulfillment submitFeilds(Fulfillment output, HashMap<String,
	 * JsonElement> parameter) { log.info("submit feilds"); String message = "";
	 * String event = ""; String comment = ""; HashMap<String, JsonElement>
	 * outParameter = parameter; if (!
	 * parameter.get("event").getAsString().isEmpty()) { event =
	 * parameter.get("event").getAsString(); comment = "Leave for " + event;
	 * log.info("comment : " + comment); } if (!
	 * parameter.get("comment").getAsString().isEmpty()) { comment =
	 * parameter.get("comment").getAsString(); } int days =
	 * getDays(parameter.get("startDate").getAsString(),
	 * parameter.get("endDate").getAsString()); JSONObject hist =
	 * Data.getHolidays(); if (days <
	 * Integer.parseInt(hist.get("leave_balance").toString())) { message =
	 * "You want to apply for leave from " +
	 * parameter.get("startDate").getAsString() + " to " +
	 * parameter.get("endDate").getAsString() + comment;
	 * outParameter.put("comment", new JsonPrimitive(comment));
	 * List<AIOutputContext> contextOutList = new LinkedList<AIOutputContext>();
	 * AIOutputContext contextOut1 = new AIOutputContext();
	 * contextOut1.setLifespan(2); contextOut1.setName("CONFIRM_LEAVE_YES");
	 * contextOut1.setParameters(outParameter); contextOutList.add(contextOut1);
	 * AIOutputContext contextOut2 = new AIOutputContext();
	 * contextOut2.setLifespan(2); contextOut2.setName("CONFIRM_LEAVE_NO");
	 * contextOut2.setParameters(outParameter); contextOutList.add(contextOut2);
	 * log.info("Context out parameters set");
	 * output.setContextOut(contextOutList); }else{ message =
	 * "Your leave balance is less than :" + days +
	 * ". You will need Delivery partner approval if you will apply. Still wanna apply? Or dear you can apply for "
	 * + days + " days."; List<AIOutputContext> contextOutList = new
	 * LinkedList<AIOutputContext>(); AIOutputContext contextOut1 = new
	 * AIOutputContext(); contextOut1.setLifespan(2);
	 * contextOut1.setName("SystemSugestionSatisfied-Yes");
	 * contextOut1.setParameters(outParameter); contextOutList.add(contextOut1);
	 * AIOutputContext contextOut2 = new AIOutputContext();
	 * contextOut2.setLifespan(2);
	 * contextOut2.setName("SystemSugestionSatisfied-no");
	 * contextOut2.setParameters(outParameter); contextOutList.add(contextOut2);
	 * log.info("Context out parameters : if low balance");
	 * output.setContextOut(contextOutList); log.info("balance < req days"); }
	 * 
	 * 
	 * message += " \n please confirm "; log.info("message");
	 * output.setSpeech(message); output.setDisplayText(message);
	 * log.info("setting context");
	 * 
	 * return output; }
	 * 
	 * private Fulfillment exitFlow(Fulfillment output) {
	 * 
	 * AIOutputContext contextOut = new AIOutputContext();
	 * output.setContextOut(contextOut); // context reset to ""
	 * output.setDisplayText("Okay! no issues."); return output; }
	 * 
	 * @SuppressWarnings("unchecked") private Fulfillment
	 * confirmLeave(Fulfillment output, HashMap<String, JsonElement> parameter)
	 * {
	 * 
	 * log.info("in confirm leave"); HashMap<String, JsonElement> outParameters
	 * = parameter; String message = ""; AIOutputContext contextOut = new
	 * AIOutputContext(); output.setContextOut(contextOut); // context reset to
	 * "" HashMap<String, Integer> holidayData = new
	 * HashMap<>(Data.getHolidays()); int leaveBalance = (int)
	 * holidayData.get("leave_balance"); log.info("leave balance : " +
	 * leaveBalance); int days =
	 * getDays(parameter.get("startDate").getAsString(),
	 * parameter.get("endDate").getAsString()); if (leaveBalance < days) {
	 * message = "Your leave balance is less than :" + days +
	 * ". You will need Delivery partner approval for applying. Still wanna apply? Or dear you can apply for "
	 * + days + " days or less."; List<AIOutputContext> contextOutList = new
	 * LinkedList<AIOutputContext>(); AIOutputContext contextOut1 = new
	 * AIOutputContext(); contextOut1.setLifespan(2);
	 * contextOut1.setName("SystemSugestionSatisfied-Yes");
	 * contextOut1.setParameters(outParameters);
	 * contextOutList.add(contextOut1); AIOutputContext contextOut2 = new
	 * AIOutputContext(); contextOut2.setLifespan(2);
	 * contextOut2.setName("SystemSugestionSatisfied-no");
	 * contextOut2.setParameters(outParameters);
	 * contextOutList.add(contextOut2);
	 * log.info("Context out parameters : if low balance : while confirmation");
	 * output.setContextOut(contextOutList);
	 * 
	 * } else { message = "Yeah! your leave has been applied :) ";
	 * holidayData.put("leave_balane", leaveBalance - 1);
	 * 
	 * }
	 * 
	 * output.setDisplayText(message); output.setSpeech(message); return output;
	 * }
	 * 
	 * private Fulfillment fallbackCustomApply(Fulfillment output,
	 * HashMap<String, JsonElement> parameter) {
	 * 
	 * with all params except event go to custom leave apply
	 * 
	 * 
	 * 
	 * log.info("fallback custom apply"); HashMap<String, JsonElement>
	 * outParameter = parameter; String message =
	 * "Wanna do it yourself?  Okay! I would not give my suggestion, just let me know the details. I will apply for you. Does this sound good ?"
	 * ; List<AIOutputContext> contextOutList = new
	 * LinkedList<AIOutputContext>(); AIOutputContext contextOut1 = new
	 * AIOutputContext(); contextOut1.setLifespan(2);
	 * contextOut1.setName("confirmLeave-followup"); // send to confirm leave -
	 * no contextOut1.setParameters(outParameter);
	 * contextOutList.add(contextOut1); AIOutputContext contextOut2 = new
	 * AIOutputContext(); contextOut2.setLifespan(2);
	 * contextOut2.setName("applyForLeave-custom");
	 * contextOut2.setParameters(outParameter); contextOutList.add(contextOut2);
	 * 
	 * output.setContextOut(contextOutList); output.setSpeech(message);
	 * output.setDisplayText(message); return output; }
	 * 
	 * private static JSONObject Suggest(HashMap<String, JsonElement> parameter)
	 * throws ParseException { JSONObject holidayData = Data.getHolidays();
	 * String bday = holidayData.get("birthday").toString(); Date birthday = new
	 * SimpleDateFormat("dd/MM/yyyy").parse(bday); String msg = ""; String event
	 * = ""; JSONObject response = new JSONObject(); if
	 * (isEventWithinRange(birthday)) { Calendar cal = Calendar.getInstance();
	 * cal.setTime(birthday); msg = "Hey! Its your birthday on " + cal.DATE
	 * +"/"+cal.MONTH + ". Want to go out??"; event = "birthday"; } else {
	 * JSONObject holidays = (JSONObject) holidayData.get("holidays"); for
	 * (Iterator iterator = holidays.keySet().iterator(); iterator.hasNext();) {
	 * String key = (String) iterator.next(); Date date1 = new
	 * SimpleDateFormat("dd/MM/yyyy").parse(key); if (isEventWithinRange(date1))
	 * { msg = holidays.get(key).toString() +
	 * " is coming up.. Wanna apply leave for that?" +
	 * holidays.get(key).toString(); event = (String) holidays.get(key); } } }
	 * response.put("event", event); response.put("message", msg);
	 * response.put("present", "true"); return response; }
	 * 
	 * private static int getDays(String startDate, String endDate) {
	 * log.info("get days"); int days = 0; log.info("start date " + startDate +
	 * " end date "+endDate); if ( (startDate.isEmpty() && endDate.isEmpty())) {
	 * return 0; } try { Date start = new
	 * SimpleDateFormat("yyyy-MM-dd").parse(startDate); Date end = new
	 * SimpleDateFormat("yyyy-MM-dd").parse(endDate); log.info("s :"+ start
	 * +" e: "+endDate); Calendar calS = Calendar.getInstance();
	 * calS.setTime(start); Calendar calE = Calendar.getInstance();
	 * calE.setTime(end); log.info("cal s :"+ calS +" cal e: "+calE);
	 * 
	 * while(calS.compareTo(calE) != 0){ if (calS.DAY_OF_WEEK !=
	 * Calendar.SATURDAY || calS.DAY_OF_WEEK != Calendar.SUNDAY) { days ++;
	 * log.info("inc date"); calS.add(Calendar.DATE, 1); log.info("date inc : "
	 * + calS); } } days ++; System.out.println("days :" + days); } catch
	 * (ParseException e) { // TODO Auto-generated catch block
	 * log.severe("exception getting days count :" + e); }
	 * 
	 * return days; }
	 * 
	 * 
	 * public static boolean isEventWithinRange(Date testDate) {
	 * log.info("isEventWithRange "); Date event_date = new Date(); try{ Date
	 * today = new SimpleDateFormat("dd/MM/yyyy").parse(new
	 * SimpleDateFormat("dd/MM/yyyy").format(event_date)); String date2 =
	 * "31/01/2018"; Date last = new
	 * SimpleDateFormat("dd/MM/yyyy").parse(date2);
	 * System.out.println("method returns"); return testDate.before(today) &&
	 * last.after(testDate); }catch(Exception e){ log.severe("exception "+e); }
	 * return false; }
	 * 
	 */}
