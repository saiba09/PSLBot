package com.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.logging.Logger;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.util.AccessProperty;


/**
 * Servlet implementation class ChatbotControl
 */
public class ChatbotControl extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger log = Logger.getLogger(ChatbotControl.class.getName());
	private static String gcp_access_token = "AIzaSyCtxCU83hl9_PZKmJXQK4P1nW_gOiGxOIk";

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		//HttpSession session = request.getSession();
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		request.setCharacterEncoding("UTF-8");
		
		try {
			
			// GET PARAMETERS
			String q = request.getParameter("q");
			String sessionID = request.getParameter("session_id");

			log.severe("q"+q);
			log.severe("sessionID"+sessionID);
			
			// LANGUAGE TRANSLATION OF q TO ENGLISH
			JSONObject translationResult = languageTranslation(q, "en");
							
			
			// PERFORM SENTIMENT ANALYSIS 
			String qEng = (String) translationResult.get("translatedText");
			double sentimentValue = performSentimentAnalysis(qEng);
			
			log.severe("sentiment : "+sentimentValue);
				
			//Write accessToken to File
			ServletContext conetxt = getServletContext();
			String fileName = conetxt.getRealPath("/WEB-INF/accessToken.json");
			String userName = sessionID.substring(0, sessionID.lastIndexOf("_"));
			String accessToken = sessionID.substring(sessionID.indexOf("-")+1 );
			new AccessProperty().writeToFile(userName, accessToken,fileName);
			sessionID = sessionID.substring(0, sessionID.indexOf("-"));
			
			log.severe("username :" +userName);
			log.severe("accesstoken : "+accessToken);
				
			// PASS INPUT AND GET RESPONSE FROM API.AI
			String input = (String) translationResult.get("translatedText");
			log.severe("input :"+input);
			JSONObject apiaiResponse = apiaiCall(input, sessionID);

			String speech = (String) apiaiResponse.get("speech");
			String displayText = (String) apiaiResponse.get("displayText");
			
			log.severe("speech "+speech);
			log.severe("displayText "+displayText);


			JSONObject responseObject = new JSONObject();
			
			if(! translationResult.get("detectedSourceLanguage").toString().equalsIgnoreCase("en") ) {
				//LANGUAGE TRANSLATION OF RESPONSE TO SOURCE LANGUAGE
				String sourceLanguage = (String) translationResult.get("detectedSourceLanguage");

				responseObject.put("detectedSourceLanguage", sourceLanguage);
				
				JSONObject reverseTranslationResult = languageTranslation(speech, sourceLanguage);
				responseObject.put("speech", (String) reverseTranslationResult.get("translatedText") );
				
				reverseTranslationResult = languageTranslation(displayText, sourceLanguage);
				responseObject.put("displayText", (String) reverseTranslationResult.get("translatedText") );
			}
			else { 
				responseObject.put("speech", speech);
				responseObject.put("displayText", displayText);						
			}
					
			responseObject.put("usersSentiment", sentimentValue);
			
			PrintWriter out = response.getWriter();
			out.print(responseObject);	
		}
		catch (Exception e) {
			log.severe("Exception in doGet : "+ e);
			JSONObject responseObject = new JSONObject();
			responseObject.put("message", "There is some error");
			PrintWriter out = response.getWriter();
			out.print(responseObject);	
		}
	}


	private JSONObject apiaiCall(String input, String sessionID) {

		String CLIENT_ACCESS_TOKEN = "3e74b8c508614678b9576ef714dcd4a5";
		
		// PREPARE DATA
		JSONObject data = new JSONObject();

		data.put("query", input);
		data.put("sessionId", sessionID);
		data.put("lang", "en");
		
		String apiurl = "https://api.api.ai/v1/query?v=20150910";
		
		log.severe("data : "+data.toJSONString());
		
		// DO POST REQUEST TO API.AI 
	 	JSONObject apiaiResponse =  postRequest(apiurl, data, CLIENT_ACCESS_TOKEN);
		
	 	String speech = (String) ((JSONObject) ((JSONObject)apiaiResponse.get("result")).get("fulfillment") ).get("speech"); 
	 	
	 	JSONObject response = new JSONObject();

	 	response.put("speech", speech);
	 	response.put("displayText", speech);
	 	
		return response;
	}


	private int performSentimentAnalysis(String q) {
		log.info("inside performSentimentAnalysis");
		JSONObject document = new JSONObject();
		document.put("type", "PLAIN_TEXT");
		document.put("content", q);
		
		JSONObject data = new JSONObject();
		data.put("encodingType", "UTF8");
		data.put("document", document);
		
		log.info(data.toJSONString());
		
		String apiurl = "https://language.googleapis.com/v1/documents:analyzeSentiment?key="+gcp_access_token;
		JSONObject apiResponse = postRequest(apiurl, data, "NO HEADER");
		
		log.info(apiResponse.toJSONString());
		
		JSONObject documentSentiment = (JSONObject) apiResponse.get("documentSentiment");
		
		double score = Double.parseDouble(documentSentiment.get("score").toString());
		double magnitude = Double.parseDouble( documentSentiment.get("magnitude").toString());
	
		int sentimentValue;

		if(score < -0.5)
			sentimentValue = 0;
		else if(score < -0.25) 
			sentimentValue = 1;
		else if (score < 0.25)
			sentimentValue = 2;
		else if(score < 0.7)
			sentimentValue = 3;
		else
			sentimentValue = 4;

		return sentimentValue;
	}


	private JSONObject languageTranslation(String input, String target) {
		log.info("inside lang translation");
		input = input.replaceAll("#usr#", "<usr>");
				
		try {
			input = URLEncoder.encode(input, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			log.severe("Error in parsing url "+e);
		}
		
		String model = "base";
		String traslationURL = "https://translation.googleapis.com/language/translate/v2?model="+model+"&key="+gcp_access_token+"&target="+target+"&q="+input;
		
		JSONObject traslationResult = getRequest(traslationURL);
		

		JSONObject data = (JSONObject)traslationResult.get("data");
		JSONObject translations = (JSONObject) ((JSONArray) data.get("translations") ).get(0);
		
		
		String detectedSourceLanguage = (String) translations.get("detectedSourceLanguage");
		String translatedText = (String) translations.get("translatedText");

		JSONObject result = new JSONObject();

		result.put("detectedSourceLanguage", detectedSourceLanguage.replaceAll("<usr>", "#usr#"));
		result.put("translatedText", translatedText.replaceAll("<usr>", "#usr#"));
		
		return result;
	}
	
	private JSONObject getRequest(String apiurl) {
		
		log.severe("inside getRequest");
		JSONObject responseData = null;
		try{
			
			URL url = new URL(apiurl);
			
			log.info("url "+url);
			//setProxy();
			
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setDoOutput(true);
			conn.setRequestProperty("Content-Length", "0");
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Content-Type", "application/json");
			
			BufferedReader bufferedReaderObject = new BufferedReader(new InputStreamReader(conn.getInputStream(),"UTF-8"));			
			StringBuilder output = new StringBuilder();			
			
			String op;
			while ((op = bufferedReaderObject.readLine()) != null) {
				output.append(op);
			}

			JSONParser parser = new JSONParser();
			responseData = (JSONObject) parser.parse(output.toString());
			
			conn.disconnect();
						
			return responseData;
		}
		catch(Exception e){
			log.severe("Exception in getRequest: "+ e);
		}

		return responseData;
	}
	
	
	private JSONObject postRequest(String apiurl, JSONObject data, String CLIENT_ACCESS_TOKEN) {
		JSONObject responseData = null;
		
		log.info("inside postRequest");
		try{
			
			//System.out.println("apiUrl: "+apiurl);
			URL url = new URL(apiurl);
			
			log.info("url :"+url);
			
			//setProxy();
			
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setDoOutput(true);
			conn.setRequestProperty("Content-Length", "0");
			if (! CLIENT_ACCESS_TOKEN.equals("NO HEADER")) {
				conn.setRequestProperty("Authorization", "Bearer "+CLIENT_ACCESS_TOKEN);
			}
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type", "application/json");
			
			OutputStream outputStream = conn.getOutputStream();
			
			outputStream.write(data.toString().getBytes());
			outputStream.flush();
			
			BufferedReader bufferedReaderObject = new BufferedReader(new InputStreamReader(conn.getInputStream(),"UTF-8"));			
			StringBuilder output = new StringBuilder();			
			
			String op;
			while ((op = bufferedReaderObject.readLine()) != null) {
				output.append(op);
			}

			JSONParser parser = new JSONParser();
			responseData = (JSONObject) parser.parse(output.toString());
			
			conn.disconnect();
			
			return responseData;
		}
		catch(Exception e){
			log.severe("exception post req :" + e);
		}
		
		return responseData;
	}
}
