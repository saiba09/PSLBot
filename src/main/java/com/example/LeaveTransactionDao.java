package com.example;

import java.util.ArrayList;
import java.util.logging.Logger;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.FullEntity;
import com.google.cloud.datastore.IncompleteKey;
import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.KeyFactory;
import com.google.cloud.datastore.Query;
import com.google.cloud.datastore.QueryResults;
import com.google.cloud.datastore.StringValue;
import com.google.cloud.datastore.StructuredQuery.OrderBy;
import com.google.cloud.datastore.StructuredQuery.PropertyFilter;
import com.model.Leave;
import com.model.LeaveTransaction;

public class LeaveTransactionDao {
	private static final Logger log = Logger.getLogger(LeaveTransactionDao.class.getName());

	private static final String projectId = "dummyproject-05042017"; // ServiceOptions.getDefaultProjectId();
	private static final Datastore datastore = DatastoreOptions.newBuilder().setProjectId(projectId).build().getService();

	public int addRecord(LeaveTransaction transaction) {
		log.info("adding to datastore");
		int response = 0;
		JSONObject hirerachy = new JSONObject();
		JSONArray hirerachyArray = new JSONArray();
		
		for (String emp : transaction.getHirerachy()) {
			JSONObject manager = new JSONObject();
			manager.put("stringValue", emp);
			hirerachyArray.add(manager);
		}
	
		hirerachy.put("value", hirerachyArray);
		KeyFactory keyFactory = datastore.newKeyFactory().setNamespace("PSL-DEMO").setKind("LeaveTransaction");
		IncompleteKey key = keyFactory.newKey();
		FullEntity<IncompleteKey> entity = Entity.newBuilder(key)
		    .set("Approver", transaction.getApprover())
		    .set("Leave_application_reason", transaction.getDate().getReason())
		    .set("Leave_start_date", transaction.getDate().getStartDate("dd-MMM-yyyy"))		   
		    .set("leave_end_date", transaction.getDate().getEndDate("dd-MMM-yyyy"))
		    .set("approver_comment"," ")
		    .set("employee_id", transaction.getEmployeeId())
		    .set("employee_name", transaction.getEmployeeName())
		    .set("leave_type", transaction.getLeaveType())
		    .set("no_of_leave", transaction.getDate().getNoOfDays())
		    .set("hirerachy", hirerachy.toJSONString())
		    .set("approved", "false")
		    .build();
		datastore.put(entity);
		log.info("added");
		return response;
	}
/*	public ArrayList<LeaveTransaction> getLeaveTransaction(String employeeName) {
		log.info("get leave transaction by name : "+employeeName);
		Query<Entity> query = Query.newEntityQueryBuilder().setKind("LeaveTransaction")
															.setNamespace("PSL-DEMO")
															//.setFilter(CompositeFilter.and(PropertyFilter.eq("employee_name", employeeName),PropertyFilter.eq("hirerachy", user)))
															.setFilter(PropertyFilter.eq("employee_name", employeeName))
															.setLimit(10).build();
		
		ArrayList<LeaveTransaction> response = executeQuery(query);
		return response;
	}*/
	public ArrayList<LeaveTransaction> getrecordByName(String employeeName) {
		log.info("get record by name : "+employeeName);
		Query<Entity> query = Query.newEntityQueryBuilder().setKind("LeaveTransaction")
															.setNamespace("PSL-DEMO")
															//.setFilter(CompositeFilter.and(PropertyFilter.eq("employee_name", employeeName),PropertyFilter.eq("hirerachy", user)))
															.setFilter(PropertyFilter.eq("employee_name", employeeName))
															.setOrderBy(OrderBy.desc("Leave_start_date"))
															.setLimit(10).build();
		
		ArrayList<LeaveTransaction> response = executeQuery(query);
		return response;
	}
	public ArrayList<LeaveTransaction> getrecordById(long employeeId) {
		log.info("get record by id : "+employeeId);
		Query<Entity> query = Query.newEntityQueryBuilder().setKind("LeaveTransaction")
															.setNamespace("PSL-DEMO")
															//.setFilter(CompositeFilter.and(PropertyFilter.eq("employee_id", employeeId),PropertyFilter.eq("hirerachy", user)))
															.setFilter(PropertyFilter.eq("employee_id", employeeId))
															.setOrderBy(OrderBy.desc("Leave_start_date"))
															.setLimit(10).build();
		
		ArrayList<LeaveTransaction> response = executeQuery(query);
		return response;
	}
	public ArrayList<LeaveTransaction> getrecordByLeaveType(String leaveType) {
		log.info("get record by id : "+leaveType);
		Query<Entity> query = Query.newEntityQueryBuilder().setKind("LeaveTransaction")
															.setNamespace("PSL-DEMO")
															//.setFilter(CompositeFilter.and(PropertyFilter.eq("employee_id", employeeId),PropertyFilter.eq("hirerachy", user)))
															.setFilter(PropertyFilter.eq("leave_type", leaveType))
															.setOrderBy(OrderBy.desc("Leave_start_date"))
															.setLimit(10).build();
		
		ArrayList<LeaveTransaction> response = executeQuery(query);
		return response;
	}
	
	private static ArrayList<LeaveTransaction> executeQuery(Query<Entity> query){
		log.info("execute Query");
		ArrayList<LeaveTransaction> response = new ArrayList<>();
		try{
		QueryResults<Entity> tasks = datastore.run(query);
		log.info("query executed");
		while (tasks.hasNext()) {
			log.info("fetching in if");
			Entity entity = tasks.next();
			// System.out.println(currentEntity.getList("keywords").toString());
			log.info("entity :"+entity);
			LeaveTransaction transaction = new LeaveTransaction();
			transaction.setApprovarComment(entity.getString("approver_comment"));
			transaction.setApprover(entity.getString("Approver"));
			transaction.setEmployeeName(entity.getString("employee_name"));
			transaction.setLeaveType(entity.getString("leave_type"));
			Leave date= new Leave(entity.getString("Leave_start_date"), entity.getString("leave_end_date"), entity.getString("Leave_application_reason"),entity.getLong("no_of_leave")); 
			transaction.setDate(date);
			transaction.setEmployeeId(entity.getString("employee_id"));
			transaction.setIsApproved(entity.getBoolean("approved"));
			ArrayList<String> list = new ArrayList<>();
			Object[] hirerachy = entity.getList("hirerachy").toArray() ;
			for (Object object : hirerachy) {
				StringValue higher = (StringValue)object;
				log.info("hirerachy : "+(StringValue)object);
				
				list.add(higher.get());
			}
			transaction.setHirerachy(list);
			response.add(transaction);
			log.info("added to response");
		}
		}catch(Exception e){
			response = null;
			log.info("Exception executin query : "+ e);
		}
		log.info("function ended : "+response);
		return response;
	}
	public ArrayList<LeaveTransaction> getSubordinateRecords(String user) {
		log.info("get record of all sub ordinate " + user);
		Query<Entity> query = Query.newEntityQueryBuilder().setKind("LeaveTransaction")
															.setNamespace("PSL-DEMO")
//															.setFilter(CompositeFilter.and(PropertyFilter.eq("employee_id", employeeId),PropertyFilter.eq("hirerachy", user)))
															.setFilter(PropertyFilter.eq("hirerachy", user))
															.setOrderBy(OrderBy.desc("Leave_start_date"))
															.setLimit(10).build();
		
		ArrayList<LeaveTransaction> response = executeQuery(query);
		return response;
	}
}
