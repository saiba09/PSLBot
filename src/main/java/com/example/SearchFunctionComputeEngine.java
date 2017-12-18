package com.example;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Query;
import com.google.cloud.datastore.QueryResults;
import com.google.cloud.datastore.StringValue;

public class SearchFunctionComputeEngine {
	
	public static JSONObject fetchAnswerFromDatastore(String question){
		
		System.out.println("Searching for similar questions in knowledge base");
		
		JSONObject result = new JSONObject();
		
		String[] inputKeywords = question.split(" ");
		
		String projectId = "dummyproject-05042017"; //ServiceOptions.getDefaultProjectId();
		Datastore datastore = DatastoreOptions.newBuilder().setProjectId(projectId).build().getService();
		
		//Datastore datastore = DatastoreOptions.getDefaultInstance().getService();

		
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
					result.put("answer", currentEntity.getString("answer") + ". What else can I do for you #usr#" );
					result.put("question", currentEntity.getString("question"));
					result.put("keywords matched", count);
					resultCount = count;
				} catch (JSONException e) {
					System.err.println("ERROR : "+e);
				}
			}
		}
		
		return result;
	}
}
