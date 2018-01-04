package com.model;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Logger;

import com.util.DateDetails;

public class Leave {
	private static final Logger log = Logger.getLogger(Leave.class.getName());

	String startDate, endDate;
	String reason;
	Boolean isHalfDaySession, isAfterNoon, isAdvancedLeave;
	int noOfDays;
	public int getNoOfDays() {
		return noOfDays;
	}

	public void setNoOfDays(int noOfDays) {
		this.noOfDays = noOfDays;
	}

	public Boolean getIsHalfDaySession() {
		return isHalfDaySession;
	}

	public void setIsHalfDaySession(Boolean isHalfDaySession) {
		this.isHalfDaySession = isHalfDaySession;
	}

	public Boolean getIsAfterNoon() {
		return isAfterNoon;
	}

	public void setIsAfterNoon(Boolean isAfterNoon) {
		this.isAfterNoon = isAfterNoon;
	}

	public Boolean getIsAdvancedLeave() {
		return isAdvancedLeave;
	}

	public void setIsAdvancedLeave(Boolean isAdvancedLeave) {
		this.isAdvancedLeave = isAdvancedLeave;
	}

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

	public Leave(String startDate, String endDate, String reason) {
		SimpleDateFormat formator = new SimpleDateFormat("dd-MMM-yyyy");
		try {
			Date start = new SimpleDateFormat("yyyy-MM-dd").parse(startDate);
			Date end = new SimpleDateFormat("yyyy-MM-dd").parse(endDate);
			this.startDate = formator.format(start);
			this.endDate = formator.format(end);
		} catch (ParseException e) {
			log.severe("Exception parsing while creating leave object");
		}
		this.reason = reason;
		isHalfDaySession = false;
		isAfterNoon = false;
		isAdvancedLeave = false;
		noOfDays =Integer.parseInt(DateDetails.getDays(startDate, endDate).get("days").toString());
	}

	public String getStartDate() {
		return startDate;
	}

	public String getStartDate(String format) { //
		SimpleDateFormat formator = new SimpleDateFormat("dd-MMM-yyyy");
		SimpleDateFormat newFormat = new SimpleDateFormat(format);
		String result = null;
		try {
			Date start = formator.parse(startDate);
			result = newFormat.format(start);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			log.severe("Exception parsing " + e);
		}
		return result;
	}
	public String getEndDate() {
		return endDate;
	}

	public String getEndDate(String format) { //
		SimpleDateFormat formator = new SimpleDateFormat("dd-MMM-yyyy");
		SimpleDateFormat newFormat = new SimpleDateFormat(format);
		String result = null;
		try {
			Date end = formator.parse(endDate);
			result = newFormat.format(end);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			log.severe("Exception parsing " + e);
		}
		return result;
	}
	public String getFinnancialYear(){
		String year = "" ;
		Date start;
		try {
			start = new SimpleDateFormat("dd-MMM-yyyy").parse(this.startDate);
			Date end = new SimpleDateFormat("dd-MMM-yyyy").parse(this.endDate);
			log.info("s :" + start + " e: " + endDate);
			Calendar calS = Calendar.getInstance();
			calS.setTime(start);
			Calendar calE = Calendar.getInstance();
			calE.setTime(end);
			Calendar today = Calendar.getInstance();
			if (calS.get(Calendar.MONTH) >= 4 && calS.get(Calendar.MONTH) <=12) {
				year += calS.get(Calendar.YEAR) + "-"+(calS.get(Calendar.YEAR)+1); //2017-2018
						
			}else{
				year += calS.get(Calendar.YEAR)-1 + "-"+(calS.get(Calendar.YEAR));
			}
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			log.severe("exception "+e); 
		}
		log.info("Financial Year : "+year);
		return year;
	}
}
