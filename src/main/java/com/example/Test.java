package com.example;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.Logger;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.util.PropertyLoader;

/**
 * Servlet implementation class Test
 */
@WebServlet("/test")
public class Test extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger log = Logger.getLogger(Test.class.getName());

    /**
     * @see HttpServlet#HttpServlet()
     */
    public Test() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		//response.getWriter().append("hello world");
		ServletContext conetxt = getServletContext();
		log.info("context : " +conetxt);
		String fileName = conetxt.getRealPath("/WEB-INF/accessToken.json");
		log.info("file created");
		File file = new File(fileName);
		System.out.println("file : "+file);
        //FileWriter fr = null;
        //BufferedWriter br = null;
        String dataWithNewLine="hello world";
        try{
        	log.info("in ty");
        	FileWriter  fr = new FileWriter(fileName);
        	log.info("file writter created");
            log.warning(fr.toString());
            BufferedWriter br = new BufferedWriter(fr);
            log.info(br.toString());
          System.out.println("reader created");
                br.write(dataWithNewLine);
                br.close();
                fr.close();
        } catch (IOException e) {
           System.out.println("Exception : "+e);
        }finally{
           
        }
		//Files.write(Paths.get(fileName), "hello World".getBytes());
		response.getWriter().append("hello world");

	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
