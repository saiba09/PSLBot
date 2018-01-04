package com.example;

import java.util.ArrayList;
import java.util.logging.Logger;

import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Query;
import com.google.cloud.datastore.QueryResults;
import com.google.cloud.datastore.StructuredQuery.PropertyFilter;
import com.model.Leave;
import com.model.LeaveTransaction;

public class LeaveTransactionDao {
	private static final Logger log = Logger.getLogger(LeaveTransactionDao.class.getName());

	private final String projectId = "dummyproject-05042017"; // ServiceOptions.getDefaultProjectId();
	private final Datastore datastore = DatastoreOptions.newBuilder().setProjectId(projectId).build().getService();

	public int addRecord(LeaveTransaction transaction) {
		int response = 0;

		return response;
	}

	public ArrayList<LeaveTransaction> getrecordByName(String employeeName) {
		log.info("get record by name : "+employeeName);
		ArrayList<LeaveTransaction> response = new ArrayList<>();
		Query<Entity> query = Query.newEntityQueryBuilder().setKind("LeaveTransaction")
															.setNamespace("PSL-DEMO")
															.setFilter(PropertyFilter.eq("employee_name", employeeName)).setLimit(10)
															.build();
		log.info("query build");
		QueryResults<Entity> tasks = datastore.run(query);
		log.info("query executed");
		while (tasks.hasNext()) {
			log.info("fetching in if");
			Entity entity = tasks.next();
			// System.out.println(currentEntity.getList("keywords").toString());
			LeaveTransaction transaction = new LeaveTransaction();
			transaction.setApprovarComment(entity.getString("approver_comment"));
			transaction.setApprover(entity.getString("Approver"));
			transaction.setEmployeeName(entity.getString("employee_name"));
			transaction.setLeaveType(entity.getString("leave_type"));
			Leave date= new Leave(entity.getString("Leave_start_date"), entity.getString("leave_end_date"), entity.getString("Leave_application_reason")); 
			transaction.setDate(date);
			transaction.setEmployeeId(entity.getLong("employee_id"));
		//	Object[] hirerachy = entity.getList("hirerachy").toArray() ;
			response.add(transaction);
		
		}

		return response;
	}
}
