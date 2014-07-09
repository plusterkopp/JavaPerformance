package _07_04_MemoryLeaks;

import java.io.*;
import java.net.*;

public class Client implements Runnable {
	private final Socket	mySocket;

	public Client( int port) throws IOException {
		mySocket = new Socket( InetAddress.getLoopbackAddress(), port);
	}

	public void run() {
		try {
			DataOutputStream out = new DataOutputStream( mySocket.getOutputStream());
			out.writeChars( "hello world");
			out.flush();

			/*
			 * try { Thread.sleep(50); } catch (InterruptedException e) { // TODO Auto-generated
			 * catch block e.printStackTrace(); }
			 */

			mySocket.close();

			// System.out.println("client finished");
		} catch ( IOException e) {
			System.out.println( "exception in Client.run(): " + e);
			e.printStackTrace();
		}
	}
}
