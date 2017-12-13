package com.model;

public class User {
	MySession session;
	float privilagedLeave, compensatioryOff, optionalHoliday,optionalLeave;
	
	public User(String userName, String accessToken) {
		this.session = new MySession(userName, accessToken);
		
	}
	public MySession getSession() {
		return session;
	}
	public void setSession(MySession session) {
		this.session = session;
	}
	public float getPrivilagedLeave() {
		return privilagedLeave;
	}
	public void setPrivilagedLeave(float privilagedLeave) {
		this.privilagedLeave = privilagedLeave;
	}
	public float getCompensatioryOff() {
		return compensatioryOff;
	}
	public void setCompensatioryOff(float compensatioryOff) {
		this.compensatioryOff = compensatioryOff;
	}
	public float getOptionalHoliday() {
		return optionalHoliday;
	}
	public void setOptionalHoliday(float optionalHoliday) {
		this.optionalHoliday = optionalHoliday;
	}
	public float getOptionalLeave() {
		return optionalLeave;
	}
	public void setOptionalLeave(float optionalLeave) {
		this.optionalLeave = optionalLeave;
	}
	public float getTotalLeaveBalance() {
		float balance= this.getCompensatioryOff()+this.getOptionalHoliday()+this.getOptionalLeave()+this.getPrivilagedLeave();
		
		return balance;
	}
	public String getUserName(){
		return this.getSession().getUserName();
	}
	@Override
	public String toString() {
		return "User [session=" + session + ", privilagedLeave=" + privilagedLeave + ", compensatioryOff="
				+ compensatioryOff + ", optionalHoliday=" + optionalHoliday + ", optionalLeave=" + optionalLeave + "]";
	}
	
}
