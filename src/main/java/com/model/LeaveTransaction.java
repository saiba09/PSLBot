package com.model;

import java.util.ArrayList;

public class LeaveTransaction {
private long employeeId;
private String employeeName;
private String approver;
private String approvarComment;
private Leave date;
private String leaveType;
private ArrayList<String> hirerachy;
public long getEmployeeId() {
	return employeeId;
}
public void setEmployeeId(long employeeId) {
	this.employeeId = employeeId;
}
public String getEmployeeName() {
	return employeeName;
}
public void setEmployeeName(String employeeName) {
	this.employeeName = employeeName;
}
public String getApprover() {
	return approver;
}
public void setApprover(String approver) {
	this.approver = approver;
}
public String getApprovarComment() {
	return approvarComment;
}
public void setApprovarComment(String approvarComment) {
	this.approvarComment = approvarComment;
}
public Leave getDate() {
	return date;
}
public void setDate(Leave date) {
	this.date = date;
}
public String getLeaveType() {
	return leaveType;
}
public void setLeaveType(String leaveType) {
	this.leaveType = leaveType;
}
public ArrayList<String> getHirerachy() {
	return hirerachy;
}
public void setHirerachy(ArrayList<String> hirerachy) {
	this.hirerachy = hirerachy;
}

}
