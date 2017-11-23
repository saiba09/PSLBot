package com.example;


import org.json.simple.JSONObject;

import com.google.cloud.ServiceOptions;
import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Query;
import com.google.cloud.datastore.QueryResults;
import com.google.cloud.datastore.StringValue;


public class SearchFunction {

	public static JSONObject fetchAnswerFromDatastore(String question){
		
		System.out.println("Searching for similar questions in knowledge base");
		
		JSONObject result = new JSONObject();
		
		String[] inputKeywords = question.split(" ");
		
		//Datastore datastore = DatastoreOptions.newBuilder().setProjectId("dummyproject-05042017").build().getService();
		String projectId = ServiceOptions.getDefaultProjectId(); // "dummyproject-05042017"
		Datastore datastore = DatastoreOptions.newBuilder().setProjectId(projectId).build().getService();
		Query<Entity> query = Query.newEntityQueryBuilder().setKind("LeaveInformation").build();
		QueryResults<Entity> tasks = datastore.run(query);
		
		int resultCount = 0;

		while(tasks.hasNext()){	
			Entity currentEntity = tasks.next();
			//System.out.println(currentEntity.getList("keywords").toString());
			
			Object[] targetKeywords = currentEntity.getList("keywords").toArray();
			
			int count = 0;
			for(int i=0;i<inputKeywords.length;i++){
	            for(int j=0;j<targetKeywords.length;j++){
	            	
	            	StringValue curentKeyword = (StringValue)targetKeywords[j];
	            	//System.out.println(curentKeyword.get());
	            	
	                if( inputKeywords[i].equalsIgnoreCase(curentKeyword.get()) ){
	                    count++;
	                }
	            }
	        }
			
			if(count > resultCount){
				try {
					result.put("answer", currentEntity.getString("answer"));
					result.put("question", currentEntity.getString("question"));
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
