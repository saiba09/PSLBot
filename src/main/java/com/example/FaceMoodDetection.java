package com.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
public class FaceMoodDetection extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger log = Logger.getLogger(FaceMoodDetection.class.getName());

       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public FaceMoodDetection() {
        super();
    }

	
	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("application/json");
		
		String requestBody = ReadParameters.readPostParameter(request);
		JSONObject bodyJson = new JSONObject();
		
		try {
			bodyJson =  (JSONObject) new JSONParser().parse(requestBody);
		
			String imageInBase64 = bodyJson.get("imageInBase64").toString();
		
			JSONObject detectedMoodData = callToVisionAPI(imageInBase64);
			
			response.getWriter().write(detectedMoodData.toJSONString());
		}
		catch(Exception e) {
			log.severe("Error in FaceMoodDetection "+e);
			response.setStatus(401);
			response.getWriter().write("{ \\\"error\\\" : \\\"invalid request format\\\"}");
		}
	}

	
	private JSONObject callToVisionAPI(String imageInBase64) throws Exception {
		JSONObject responseData = null;
		
		log.info("inside postRequest");
		try{
			
			//System.out.println("apiUrl: "+apiurl);
			String apiurl = "https://vision.googleapis.com/v1/images:annotate?key=AIzaSyCtxCU83hl9_PZKmJXQK4P1nW_gOiGxOIk";
			URL url = new URL(apiurl);
			
			log.info("url :"+url);
			
			
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setDoOutput(true);
			conn.setRequestProperty("Content-Length", "0");
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type", "application/json");
			
			//Prepare request data 
			JSONArray requests = new JSONArray();
			
			JSONObject image = new JSONObject(); 
			image.put("content", imageInBase64);
			
			JSONObject type = new JSONObject();
			type.put("type", "FACE_DETECTION");
			
			JSONArray features = new JSONArray();
			features.add(type);
			
			JSONObject requestData = new JSONObject();
			requestData.put("image", image);
			requestData.put("features", features);
			
			requests.add(requestData);
			
			JSONObject requestBody = new JSONObject();
			requestBody.put("requests", requests);
			
			OutputStream outputStream = conn.getOutputStream();
			
			outputStream.write(requestBody.toString().getBytes());
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
			
			JSONObject responseObject = new JSONObject();
			JSONArray moods = new JSONArray();
			
			JSONArray responses = (JSONArray) responseData.get("responses");
			JSONObject response = (JSONObject) responses.get(0);
			
			JSONArray faceAnnotations = (JSONArray) response.get("faceAnnotations");
			JSONObject moodData = (JSONObject) faceAnnotations.get(0);
			
			Map<String, String> moodLikelihoods = new HashMap<String, String>();
			
			moodLikelihoods.put("happy", moodData.get("joyLikelihood").toString());
			moodLikelihoods.put("sad", moodData.get("sorrowLikelihood").toString());
			moodLikelihoods.put("angry", moodData.get("angerLikelihood").toString());
			moodLikelihoods.put("surprised", moodData.get("surpriseLikelihood").toString());
			
			for (Map.Entry<String, String> mood : moodLikelihoods.entrySet())
				if(! mood.getValue().contains("UNLIKELY"))
					moods.add(mood.getKey());
			
			responseObject.put("moods", moods);
			return responseObject;
		}
		catch(Exception e){
			log.severe("exception post req :" + e);
			throw new Exception(e);
		}
		
	}
}
