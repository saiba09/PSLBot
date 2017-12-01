package com.util;

import org.json.simple.JSONObject;

import com.example.Data;

public class LeaveMessageFormator {
 public static String getLeaveDetailMessage(String userName){
		JSONObject data = Data.getHolidays(userName);
		Boolean prev = false;
		String message ="";
		int PL = Integer.parseInt(data.get("privillage_leave").toString());
		int OH = Integer.parseInt(data.get("optional_holiday").toString());
		int OL = Integer.parseInt(data.get("optional_leave").toString());
		int CF = Integer.parseInt(data.get("compensatiory_off").toString());
		if (PL != 0 || CF != 0 || OH != 0 || OL != 0) {
			message += "You have ";
		
		if (PL != 0 ) {
			message += PL + " privilaged leaves";
			prev = true;
		}
		if (CF != 0) {
			if (prev) {
				message += ", ";
			}
			message += CF + " compensatory off.";
			prev = true;
		}
		if (OH != 0) {
			if (prev) {
				message += ", ";
			}
			message += OH + " optional holiday";
			prev = true;
		}
		if (OL != 0) {
			if (prev) {
				message += ", ";
			}
			message += OL + " optional leave";
		}
		message +=".";
		}
	 return message;
 }
}
