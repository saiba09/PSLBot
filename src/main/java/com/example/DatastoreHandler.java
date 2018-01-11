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
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.model.Leave;
import com.model.LeaveTransaction;

/**
 * Servlet implementation class DatastoreHandler
 */
public class DatastoreHandler extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger log = Logger.getLogger(DatastoreHandler.class.getName());

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		log.info("in do get");
		String user = request.getParameter("user");
		log.info("user : " + user);
		ArrayList<LeaveTransaction> transaction = null;
		String token = request.getParameter("token");
		// add check if in hirerachy
		String empData ="";
		switch (token) {
		case "user":

			if (request.getParameter("employeeName") != null) {
				log.info("employee name :" + request.getParameter("employeeName"));
				transaction = new LeaveTransactionDao()
						.getrecordByName(request.getParameter("employeeName").toString());
			}
			 empData = foramteResponse(transaction, transaction.get(0).getApprover()).toJSONString();
			if (transaction != null) {
				response.getWriter().write(empData);

			} else {
				response.getWriter().write("no sub oridinate");

			}

			break;
		case "subordinate":

			if (request.getParameter("employeeName") != null) {
				log.info("employee name :" + request.getParameter("employeeName"));
				transaction = new LeaveTransactionDao()
						.getrecordByName(request.getParameter("employeeName").toString());
			}
			if (request.getParameter("employeeId") != null) {
				log.info("employee id :" + request.getParameter("employeeId"));
				transaction = new LeaveTransactionDao()
						.getrecordById(Long.parseLong(request.getParameter("employeeId").toString()));
			}
			if (request.getParameter("leaveType") != null) {
				log.info("leaveType :" + request.getParameter("leaveType"));
				transaction = new LeaveTransactionDao()
						.getrecordByLeaveType(request.getParameter("leaveType").toString());
			}
			empData = foramteResponse(transaction, user).toJSONString();
			if (transaction != null) {
				response.getWriter().write(empData);

			} else {
				response.getWriter().write("no sub oridinate");

			}

			break;
		case "allsubordinate":
			log.info("employee name :" + user +" get details of all employees");
			if (user.equals(null)) {
				response.getWriter().write("Invalid query");

			}else{
			transaction = new LeaveTransactionDao()
					.getSubordinateRecords(user);
			 if (transaction != null) {
				 empData = foramteResponse(transaction, transaction.get(0).getApprover()).toJSONString();

					response.getWriter().write(empData);

				} else {
					response.getWriter().write("no sub oridinate");

				}
			}
			break;
		default:
			response.getWriter().write("Invalid query");
			break;
		}
	

	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		try {
			JSONObject parameter = (JSONObject) new JSONParser().parse(new ReadParameters().readPostParameter(request));
			LeaveTransaction leaveTransaction = new LeaveTransaction();
			leaveTransaction.setApprovarComment(parameter.get("approverComment").toString());
			leaveTransaction.setApprover(parameter.get("approver").toString());
			leaveTransaction.setEmployeeId(parameter.get("employeeId").toString());
			leaveTransaction.setEmployeeName(parameter.get("employeeName").toString());
			leaveTransaction.setLeaveType(parameter.get("leaveType").toString());
			leaveTransaction.setDate(new Leave(parameter.get("leaveStartDate").toString(),
					parameter.get("leaveEndDate").toString(), parameter.get("reason").toString(),
					Integer.parseInt(parameter.get("noOfLeaves").toString())));

			JSONArray hirerachy = (JSONArray) parameter.get("hirerachy");
			ArrayList<String> list = new ArrayList<>();
			for (Object manager : hirerachy) {
				list.add(manager.toString());
			}
			leaveTransaction.setHirerachy(list);
			new LeaveTransactionDao().addRecord(leaveTransaction);

		} catch (ParseException e) {
			// TODO Auto-generated catch block
			log.severe("exception adding tot datastore : " + e);
		}
		response.getWriter().write("successfully added to data store");

	}

	private static JSONObject foramteResponse(ArrayList<LeaveTransaction> transaction, String user) {
		JSONObject response = new JSONObject();
		JSONArray data = new JSONArray();
		// add check if not exists

		if (!transaction.isEmpty()) {
			for (LeaveTransaction leaveTransaction : transaction) {
				if (leaveTransaction.getHirerachy().contains(user)) {
					JSONObject tran = new JSONObject();
					tran.put("employeeName", leaveTransaction.getEmployeeName());
					tran.put("leaveStartDate", leaveTransaction.getDate().getStartDate("dd-MMM-yyyy"));
					tran.put("leaveEndDate", leaveTransaction.getDate().getEndDate("dd-MMM-yyyy"));
					tran.put("noOfLeave", leaveTransaction.getDate().getNoOfDays());
					tran.put("reasonForLeave", leaveTransaction.getDate().getReason());
					tran.put("leaveType", leaveTransaction.getLeaveType());
					tran.put("approved", leaveTransaction.getIsApproved());
					data.add(tran);
				}
			}
			if (data.size() != 0) {
				response.put("responseCode", 200);
				response.put("data", data);
			} else {
				response.put("responseCode", 401);
				response.put("message",
						"You can only view details of your sub ordinate. #emp# is not your sub ordinate");
				response.put("data", data);
			}

		} else {
			response.put("responseCode", 400);
			response.put("message", "No record found");
			response.put("data", data);
		}

		return response;
	}

}
