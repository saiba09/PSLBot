package com.example;


import java.util.ArrayList;
import java.util.Iterator;

import org.json.simple.JSONObject;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;



public class SearchFunction {

	public static JSONObject fetchAnswerFromDatastore(String question){
		
		System.out.println("Searching for similar questions in knowledge base");
		
		JSONObject result = new JSONObject();
		
		
		
		String[] inputKeywords = question.split(" ");


		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		
		Query q = new Query("LeaveInformation");
		PreparedQuery pq = datastore.prepare(q);
		
		Iterator<Entity> iterator = pq.asIterator();
		
		int resultCount = 0;

		while(iterator.hasNext()){
			Entity currentEntity = iterator.next();
			
			ArrayList<String> targetKeywordsList = (ArrayList<String>) currentEntity.getProperty("keywords");
			
			Object[] targetKeywords = targetKeywordsList.toArray();
			
			int count = 0;
			for(int i=0;i<inputKeywords.length;i++){
	            for(int j=0;j<targetKeywords.length;j++){
	            	
	            	String curentKeyword = targetKeywords[j].toString();
	            	//System.out.println(curentKeyword.get());
	            	
	                if( inputKeywords[i].equalsIgnoreCase(curentKeyword) ){
	                    count++;
	                }
	            }
	        }
			
			if(count > resultCount){
				try {
					result.put("answer", currentEntity.getProperty("answer").toString());
					result.put("question", currentEntity.getProperty("question").toString());
					resultCount = count;
				} catch (Exception e) {
					System.err.println("ERROR : "+e);
				}
			}
		}
		
		return result;
	}
/*	
	public static void main(String[] args) {
		String question = args[0];
		//"Q: What is a leave year in LAMS?"
		System.out.println( fetchAnswerFromDatastore(question).toString() );
	}
*/
}
