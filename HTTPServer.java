import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;

// This server operates in an unusual way:
//   * it serves only two documents, DocA and DocB
//   * it opens two server sockets
//   * connecting to one of the sockets gives you DocA and to the other docB
// This version is single threaded.  The server alterantes between
// serving DocA and DocB.  If the user asks for them out of alternating
// order, they will hang.

public class HTTPServer {

    static final int MYID = 1973786;  // STEP 1 -- replace with your student ID
    
    static final int PORT_A = MYID % (65545-1026) + 1025;
    static final int PORT_B = PORT_A + 1;

    int requestsServed = 0;
    boolean verbose = true;

    public HTTPServer() {
	HTTPSocket sockA = null;
	HTTPSocket sockB = null;
	boolean notDone = true;
	try {
	    sockA  = new HTTPSocket(PORT_A, "DocA.html", verbose, this);
	    sockB  = new HTTPSocket(PORT_B, "DocB.html", verbose, this);

	    //sockA.start();
	    //sockB.start();

	    // process requests until user kills the server
	    InputStreamReader reader = new InputStreamReader(System.in);
	    BufferedReader keyboard = new BufferedReader(reader);
	    
	    sockA.start();
	    sockB.start();
	 
	    while (notDone) {	
  		System.out.println("Terminate server (y or n)?");
		
		String response = keyboard.readLine();
		if ( response.equals("y") ) {
			notDone = false;
			sockA.close();
			sockB.close();
		}
	    }

	    sockA.join();
	    sockB.join();

	} catch (Exception e) {
	    System.err.println("Server error : " + e.getMessage());
	} finally {
	}
	System.out.println("Served " + requestsServed + " requests");
	System.exit(0);
    }

    public void requestServed() {
	requestsServed++;
    }

    public static void main(String[] args) {
	HTTPServer server = new HTTPServer();
    }
}
