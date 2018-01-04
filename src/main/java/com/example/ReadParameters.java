package com.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.servlet.http.HttpServletRequest;

public class ReadParameters {
public static String readPostParameter(HttpServletRequest request) {
	// TODO Auto-generated method stub
	StringBuilder stringBuilder = new StringBuilder();  
    BufferedReader bufferedReader = null;  
    
    try {  
        InputStream inputStream = request.getInputStream(); 

        if (inputStream != null) {  
            bufferedReader = new BufferedReader(new InputStreamReader(inputStream));  

            char[] charBuffer = new char[128];  
            int bytesRead = -1;  

            while ((bytesRead = bufferedReader.read(charBuffer)) > 0) {  
                stringBuilder.append(charBuffer, 0, bytesRead);  
            }  
        } else {  
            stringBuilder.append("");  
        }  
    } catch (IOException ex) {  
        System.out.println(("Error reading the request body..."));  
    } finally {  
        if (bufferedReader != null) {  
            try {  
                bufferedReader.close();  
            } catch (IOException ex) {  
              System.out.println("Error closing bufferedReader...");  
            }  
        }  
    }  
    System.out.println(stringBuilder.toString());
    return stringBuilder.toString();
}
}
