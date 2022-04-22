import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;

// Creates a TCP server socket that serves a single file using HTTP.

public class HTTPSocket extends Thread  {

    private ServerSocket serverConnect;
    private String myFile;
    private boolean notDone = true;
    private boolean verbose;
    private int requestNumber = 0;
    private HTTPServer myServer;
	
    public HTTPSocket(int port, String filename, boolean verbose, HTTPServer myServer) throws IOException {
	myFile = filename;
	this.verbose = verbose;
	this.myServer = myServer;
	serverConnect = new ServerSocket(port);
	serverConnect.setReuseAddress(true);
	if ( verbose ) System.out.println("Serving file " + myFile + " on port " + port);
	    
    }
	
    public void handleRequest() {
	try {
	    if (verbose) {
		System.out.println("Waiting for " + myFile + " request");
	    }
	    Socket connection = serverConnect.accept();
	    if (verbose) {
		System.out.println("Received request for file " + myFile);
	    }
	    // we manage our particular client connection
	    BufferedReader in = null;
	    PrintWriter out = null;
	    BufferedOutputStream dataOut = null;
		
	    try {
		in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
		// we get character output stream to client (for HTTP headers)
		out = new PrintWriter(connection.getOutputStream());

		// We ignore the actual request and just serve the file we're in charge of
		File file = new File(myFile);
		int fileLength = (int) file.length();
		Path myFilePath = Paths.get(".", myFile);
		String fileContents = Files.readString(myFilePath);
		fileContents = fileContents.replace("#", Integer.toString(++requestNumber));
					
		// send HTTP Headers
		out.println("HTTP/1.0 200 OK");
		out.println("Server: CSE 410 HTTP Server");
		out.println("Date: " + new Date());
		out.println("Content-type: text/html");
		out.println("Content-length: " + fileLength);
		out.println("Connection: close");
		out.println(); // blank line indicates end of header
		out.println(fileContents);
		out.flush();

		myServer.requestServed();
					
	    } catch (Exception e) {
		System.err.println("Server error : " + e);
	    } finally {
		try {
		    in.close();
		    out.close();
		    connection.close(); // we close socket connection
		} catch (Exception e) {
		    System.err.println("Error closing stream : " + e.getMessage());
		} 
			
		if (verbose) {
		    System.out.println("Connection for " + myFile + " closed.\n");
		}
	    }
	} catch (IOException e) {
	    //System.err.println("Server Connection error : " + e.getMessage());
	}
    }

    public void close() throws IOException {
	try {
	    serverConnect.close();
	} catch (IOException e) {
	    System.err.println("HTTPSocket: error closing serverConnect");
	}
    }

    public void run() {
	    try {
		    while (verbose) {
		    	this.handleRequest();
			if (notDone) {
				verbose = false;
			}
			// set notDone to false if terminated?
		    }
	    } catch (Exception e) {
	    System.out.println(e);
	    }
    }

}
