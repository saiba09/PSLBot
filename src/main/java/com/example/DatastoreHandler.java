package com.example;

import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.model.LeaveTransaction;

/**
 * Servlet implementation class DatastoreHandler
 */
public class DatastoreHandler extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger log = Logger.getLogger(DatastoreHandler.class.getName());

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		log.info("in do get");
		ArrayList<LeaveTransaction> transaction = null;
		//add check if in hirerachy 
		if (request.getParameter("employeeName") != null) {
			log.info("employee name :"+ request.getParameter("employeeName"));
			transaction = new LeaveTransactionDao().getrecordByName(request.getParameter("employeeName").toString());
		}
		if (transaction != null) {
			response.getWriter().write(transaction.get(0).getEmployeeName());

		}else{
			response.getWriter().write("no sub oridinate");

		}
	}

	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
