package _05_FunctionalHotSpots.util.file;

import java.util.*;
import java.io.*;
import java.net.*;

public final class GeneratorInput {
	private static final String PARAM_NAME = "name";
	private static final String SERVLET_LOCATION = "http://localhost:8080/HomepageGenerator/HomeServlet?"+PARAM_NAME+"=";
	
    	public static BufferedReader getInputReader(String page) {
		URL servlet = null;
		try { 
			servlet = new URL(SERVLET_LOCATION+page); 
		}
		catch (Exception e) { System.err.println(e); }
		
		HttpURLConnection conn = null;
		try { 
			conn = (HttpURLConnection)servlet.openConnection(); 
		}
		catch (Exception e) { System.err.println(e); }
		

//		System.out.println(conn.getRequestMethod());
		Map headers = conn.getHeaderFields();
//		System.out.println(headers);
		BufferedReader content = null;
		try {
			content = new BufferedReader(
			              new InputStreamReader((InputStream)conn.getContent()));
		}
		catch (Exception e) { System.err.println(e); }
		
		return content;
    	}
}