package com.example;

import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.model.LeaveTransaction;

/**
 * Servlet implementation class DatastoreHandler
 */
public class DatastoreHandler extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger log = Logger.getLogger(DatastoreHandler.class.getName());

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		log.info("in do get");
		String user = request.getParameter("user");
		log.info("user : "+ user);
		ArrayList<LeaveTransaction> transaction = null;
		//add check if in hirerachy 
		
		if (request.getParameter("employeeName") != null) {
			log.info("employee name :"+ request.getParameter("employeeName"));
			transaction = new LeaveTransactionDao().getrecordByName(request.getParameter("employeeName").toString());
		}if (request.getParameter("employeeId") != null) {
			log.info("employee id :"+ request.getParameter("employeeId"));
			transaction = new LeaveTransactionDao().getrecordById(request.getParameter("employeeId").toString());
		}if (request.getParameter("leaveType") != null) {
			log.info("leaveType :"+ request.getParameter("leaveType"));
			transaction = new LeaveTransactionDao().getrecordByLeaveType(request.getParameter("leaveType").toString());
		}
		String empData =  foramteResponse(transaction,user).toJSONString();
		if (transaction != null) {
			response.getWriter().write(empData);

		}else{
			response.getWriter().write("no sub oridinate");

		}
	}

	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}
	
	private static JSONObject foramteResponse(ArrayList<LeaveTransaction> transaction,String user){
		JSONObject response = new JSONObject();
		JSONArray data = new JSONArray();
		//add check if not exists
		
		if (! transaction.isEmpty()) {
			for (LeaveTransaction leaveTransaction : transaction) {
				if(leaveTransaction.getHirerachy().contains(user)){
				JSONObject tran = new JSONObject();
				tran.put("employeeName", leaveTransaction.getEmployeeName());
				tran.put("leaveStartDate", leaveTransaction.getDate().getStartDate("dd-MMM-yyyy"));
				tran.put("leaveEndDate", leaveTransaction.getDate().getEndDate("dd-MMM-yyyy"));
				tran.put("noOfLeave", leaveTransaction.getDate().getNoOfDays());
				tran.put("reasonForLeave", leaveTransaction.getDate().getReason());
				tran.put("leaveType", leaveTransaction.getLeaveType());
				data.add(tran);
				}
			}
			if(data.size() != 0){
				response.put("responseCode", 200);
				response.put("data", data);
			}else{
				response.put("responseCode", 401);
				response.put("message", "You can only view details of your sub ordinate. #emp# is not your sub ordinate");
				response.put("data", data);
			}
			
			
		}else {
			response.put("responseCode", 400);
			response.put("message", "No record found");
			response.put("data", data);
		}
		
		return response;
	}

}
