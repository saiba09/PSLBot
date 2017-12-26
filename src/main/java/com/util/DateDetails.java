package com.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.logging.Logger;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.example.PiHandler;
import com.model.User;


public class DateDetails {
	private static final Logger log = Logger.getLogger(DateDetails.class.getName());
	public static String getNextHoliday(){

		log.info("get next holiday");
		HashMap<String, String> calander = PiHandler.getLeaveCalander();
		String message = "";
		try {
			Date today =new SimpleDateFormat("yyyy-MM-dd").parse(getCurrentDate()) ;
			for (String date : calander.keySet()) {
				Date d1 = new SimpleDateFormat("dd/MM/yyyy").parse(date);
				if (d1.after(today)) {
					message = "Its "+calander.get(date)+" comming up on "+Formator.getFormatedDate(d1);
				}
			}
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			log.info("Exception : "+ e);
		}
		log.info("response : "+ message);
		return message;
	}
	/*
	public static JSONObject getLeaveInfo(String sessionId) {
		String message = "";
		JSONObject data = Data.getHolidays(sessionId);

		log.info("data : " + data.toJSONString());

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
*/
	public static JSONObject getDays(String startDate, String endDate) {
		log.info("get days");
		int days = 0;
		boolean isWeekEnd = false;
		JSONObject response = new JSONObject();
		TreeMap<Date, String> holidayTrack = new TreeMap<>(); 
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

				if (calS.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) {
					holidayTrack.put(calS.getTime(), "Saturday");
					log.info(Calendar.SATURDAY + " : on " + calS.DATE + " saturday");
					calS.add(Calendar.DATE, 1);
					isWeekEnd = true;
				} else if (calS.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
					holidayTrack.put(calS.getTime(), "Sunday");
					log.info(Calendar.SUNDAY + " : on " + calS.DATE + " sunday");
					calS.add(Calendar.DATE, 1);
					isWeekEnd = true;

				} else {

					days++;
					log.info("inc date");
					calS.add(Calendar.DATE, 1);
					log.info("date inc : " + calS.DATE + " " + calS.MONTH);
					/*
					 * if (calS.DAY_OF_WEEK == Calendar.SATURDAY) {
					 * holidayTrack.put(calS, "Saturday"); log.info(
					 * Calendar.SATURDAY + " : on "+calS.DATE);
					 * 
					 * } else if (calS.DAY_OF_WEEK == Calendar.SUNDAY) {
					 * holidayTrack.put(calS, "Sunday"); log.info(
					 * Calendar.SUNDAY + " : on "+calS.DATE);
					 * 
					 * } isWeekEnd = true;
					 */
				}
			} while (calS.compareTo(calE) <= 0);

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
	public static int getDaysBetweenDates(String startDate ,String endDate){
		int days = 0; 
		log.info("start date " + startDate + " end date " + endDate);
		if ((startDate.isEmpty() && endDate.isEmpty())) {
			log.info("startdate &7 endDate empty ");
			return days;
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
					days++;
					log.info("inc date");
					calS.add(Calendar.DATE, 1);
					log.info("date inc : " + calS.DATE + " " + calS.MONTH);
				
				} while (calS.compareTo(calE) <= 0);
			System.out.println("days :" + days);
			}		
		 catch (ParseException e) {
			// TODO Auto-generated catch block
			log.severe("exception getting days count :" + e);
		}
		return days;
	}
	protected static String getCurrentTime(){
		DateFormat df = new SimpleDateFormat("HH/mm:ss");
       Calendar calobj = Calendar.getInstance();
	     //(df.format(calobj.getTime()));
		String time = df.format(calobj.getTime());
		log.info("current time : "+ time);
       return time;
	}
	public static String getCurrentDate(){
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
       Calendar calobj = Calendar.getInstance();
	     //(df.format(calobj.getTime()));
		String date = df.format(calobj.getTime());
		log.info("current time : "+ date);
       return date;
	}
}
