package com.util;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class AccessProperty {
	private static final Logger log = Logger.getLogger(AccessProperty.class.getName());
	ReentrantLock lock;
	public AccessProperty() {
		// TODO Auto-generated constructor stub
		lock = new ReentrantLock(true);
	}
public void writeToFile(String userName, String accessToken){
	JSONObject fileContent = this.readFile();
	log.info("File content "+fileContent); 
	fileContent.put(userName, accessToken);
	try {
		if (lock.tryLock(10, TimeUnit.SECONDS)) {
			log.info("lock accquired writting");
		 try (FileWriter file = new FileWriter("accessToken.json")) {
			 
		     file.write(fileContent.toJSONString());
		     file.flush();

		 } catch (IOException e) {
			 log.severe("error writting to file");
		 }
		}
	} catch (InterruptedException e) {
		// TODO Auto-generated catch block
		log.severe("Exception while locking");
	}
}
public JSONObject readFile(){
	JSONObject file = new JSONObject();
	JSONParser jsonParser = new JSONParser();
	try {
		if (lock.tryLock(10, TimeUnit.SECONDS)) {
			log.info("Lock accquired while reading");
    try (FileReader reader = new FileReader("accessToken.json"))
    {
      file = (JSONObject) jsonParser.parse(reader);

    } catch (IOException | org.json.simple.parser.ParseException e) {
       log.severe("error reading from file");
    }
		}
	}catch (InterruptedException e) {
		// TODO Auto-generated catch block
		log.severe("Exception while locking");
	}
	return file;
}

public String getAccessToken(String userName){
	JSONObject file = new JSONObject();
	String accessToken = null;
	JSONParser jsonParser = new JSONParser();
	try {
		if (lock.tryLock(10, TimeUnit.SECONDS)) {
			log.info("Lock accquired while reading");
    try (FileReader reader = new FileReader("accessToken.json"))
    {
      file = (JSONObject) jsonParser.parse(reader);
      accessToken = file.get(userName).toString();

    } catch (IOException | org.json.simple.parser.ParseException e) {
       log.severe("error reading from file");
    }
		}
	}catch (InterruptedException e) {
		// TODO Auto-generated catch block
		log.severe("Exception while locking");
	}
	return accessToken;
}
}
