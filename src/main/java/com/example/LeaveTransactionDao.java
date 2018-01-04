package com.example;

import java.util.ArrayList;
import java.util.logging.Logger;

import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Query;
import com.google.cloud.datastore.QueryResults;
import com.google.cloud.datastore.StringValue;
import com.google.cloud.datastore.StructuredQuery.PropertyFilter;
import com.model.Leave;
import com.model.LeaveTransaction;

public class LeaveTransactionDao {
	private static final Logger log = Logger.getLogger(LeaveTransactionDao.class.getName());

	private static final String projectId = "dummyproject-05042017"; // ServiceOptions.getDefaultProjectId();
	private static final Datastore datastore = DatastoreOptions.newBuilder().setProjectId(projectId).build().getService();

	public int addRecord(LeaveTransaction transaction) {
		int response = 0;

		return response;
	}

	public ArrayList<LeaveTransaction> getrecordByName(String employeeName) {
		log.info("get record by name : "+employeeName);
		Query<Entity> query = Query.newEntityQueryBuilder().setKind("LeaveTransaction")
															.setNamespace("PSL-DEMO")
															//.setFilter(CompositeFilter.and(PropertyFilter.eq("employee_name", employeeName),PropertyFilter.eq("hirerachy", user)))
															.setFilter(PropertyFilter.eq("employee_name", employeeName))
															.setLimit(10).build();
		
		ArrayList<LeaveTransaction> response = executeQuery(query);
		return response;
	}
	public ArrayList<LeaveTransaction> getrecordById(String employeeId) {
		log.info("get record by id : "+employeeId);
		Query<Entity> query = Query.newEntityQueryBuilder().setKind("LeaveTransaction")
															.setNamespace("PSL-DEMO")
															//.setFilter(CompositeFilter.and(PropertyFilter.eq("employee_id", employeeId),PropertyFilter.eq("hirerachy", user)))
															.setFilter(PropertyFilter.eq("employee_id", employeeId))
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
															.setLimit(10).build();
		
		ArrayList<LeaveTransaction> response = executeQuery(query);
		return response;
	}
	
	private static ArrayList<LeaveTransaction> executeQuery(Query<Entity> query){
		log.info("execute Query");
		ArrayList<LeaveTransaction> response = new ArrayList<>();
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
		log.info("function ended : "+response);
		return response;
	}
}
