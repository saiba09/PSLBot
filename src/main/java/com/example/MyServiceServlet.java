package com.example;

import java.io.*;
import java.util.logging.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.json.simple.JSONObject;

import ai.api.AIServiceException;
import ai.api.model.AIResponse;
import ai.api.web.AIServiceServlet;



// [START example]
@SuppressWarnings("serial")
public class MyServiceServlet extends AIServiceServlet {
	
	private static final Logger log = Logger.getLogger(MyServiceServlet.class.getName());

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    
	log.info("service Servlet :");
	String sessionId = req.getParameter("sessionId");
	try{
	
		AIResponse aiResponse = request(req.getParameter("query"), sessionId);
		String action = aiResponse.getResult().getAction();
		log.info("action : " + action);
		
		
		
		resp.setContentType("text/plain");
		resp.getWriter().append(aiResponse.getResult().getFulfillment().getSpeech());
	}
	
	catch(AIServiceException e){
	
	}
  }
}