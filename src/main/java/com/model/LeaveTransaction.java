package com.model;

import java.util.ArrayList;

import com.google.cloud.datastore.StringValue;

public class LeaveTransaction {
private String employeeId;
private String employeeName;
private String approver;
private String approvarComment;
private Leave date;
private String leaveType;
private ArrayList<String> hirerachy;
private Boolean isApproved;
public Boolean getIsApproved() {
	return isApproved;
}
public void setIsApproved(Boolean isApproved) {
	this.isApproved = isApproved;
}
public String getEmployeeId() {
	return employeeId;
}
public void setEmployeeId(String employeeId) {
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
