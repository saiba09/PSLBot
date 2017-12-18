package com.util;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;


import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class AccessProperty {
	final String fileName = "AccessToken.json";
	
	private static final Logger log = Logger.getLogger(AccessProperty.class.getName());
	ReentrantLock lock;
	public AccessProperty() {
		// TODO Auto-generated constructor stub
		lock = new ReentrantLock(true);
	}
public void writeToFile(String userName, String accessToken, String fileName){
	File read = new File(fileName);
	JSONObject fileContent;
	if (read.exists()) {
		fileContent  = this.readFile(fileName);
	}
	else{
		fileContent = new JSONObject();
	}
	log.info("File content "+fileContent); 
	fileContent.put(userName, accessToken);
	try {
		if (lock.tryLock(10, TimeUnit.SECONDS)) {
			log.info("lock accquired writting");
			
			 try {
				Files.write(Paths.get(fileName), fileContent.toJSONString().getBytes());
				lock.unlock();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				log.severe("Exception writing to file");
			}
		/* try (FileWriter file = new FileWriter(fileName)) {
			 
		     file.write(fileContent.toJSONString());
		     file.flush();

		 } catch (IOException e) {
			 log.severe("error writting to file");
		 }*/
		}
	} catch (InterruptedException e) {
		// TODO Auto-generated catch block
		log.severe("Exception while locking");
	}
}
public JSONObject readFile(String fileName){
	JSONObject file = new JSONObject();
	JSONParser jsonParser = new JSONParser();
	try {
		if (lock.tryLock(10, TimeUnit.SECONDS)) {
			log.info("Lock accquired while reading");
    try (FileReader reader = new FileReader(fileName))
    {
      file = (JSONObject) jsonParser.parse(reader);
      lock.unlock();
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

public String getAccessToken(String userName,String fileName){
	JSONObject file = new JSONObject();
	String accessToken = null;
	JSONParser jsonParser = new JSONParser();
	try {
		if (lock.tryLock(10, TimeUnit.SECONDS)) {
			log.info("Lock accquired while reading");
			File read = new File(fileName);
			
    try (FileReader reader = new FileReader(read))
    {
      file = (JSONObject) jsonParser.parse(reader);
      accessToken = file.get(userName).toString();
      lock.unlock();
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
