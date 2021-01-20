

// Ahmed Amer, 30063097

import java.net.*;
import java.io.*;
import java.util.*;

public class Proxy {
    /** Port for the proxy */
    private static int port;
    /** Socket for client connections */
    private static ServerSocket socket;

    /** Create the Proxy object and the socket */
    public static void init(int p) {
	port = p; //initialize the port number with the port passed as a command line argument
	try {
	    socket = new ServerSocket(port); //Create a new ServerSocket with the portNumber passed
	    //This will be our ServerSocket that listens on the given port
	} catch (IOException e) {
	    System.out.println("Error creating socket: " + e);
	    System.exit(-1);
	}
    }

    public static void handle(Socket client) {
	Socket server = null;
	HttpRequest request = null;
	HttpResponse response = null;

	/* Process request. If there are any exceptions, then simply
	 * return and end this request. This unfortunately means the
	 * client will hang for a while, until it timeouts. */

	/* Read request */
	try {
	    BufferedReader fromClient = new BufferedReader(new InputStreamReader(client.getInputStream())); //open a BufferedReader class to read the request that is incoming from the webpage
	    request = new HttpRequest(fromClient); //Pass this to the HttpRequest class that will use the Buffered reader to read the request and convert it into a string that can be used
	    //to find the server's port number and URL
	} catch (IOException e) {
	    System.out.println("Error reading request from client: " + e);
	    return;
	}
	/* Send request to server */
	try {
	    /* Open socket and write request to socket */
	    server = new Socket(request.getHost(),request.getPort());//Create a new Socket that connects to the server that will respond to the request
	    DataOutputStream toServer = new DataOutputStream(server.getOutputStream()); //Create a DataOutputStream to write to the server socket i.e instead of allowing the request through
	    //normally we intercepted it and send it using this socket so that we get the response back instead of it going directly back to the client
	    toServer.writeBytes(request.toString()); //send the request we intercepted to the server using the socket
	    toServer.flush();
	} catch (UnknownHostException e) {
	    System.out.println("Unknown host: " + request.getHost());
	    System.out.println(e);
	    return;
	} catch (IOException e) {
	    System.out.println("Error writing request to server: " + e);
	    return;
	}
	
	/* Read response and forward it to client */
	try {
	    DataInputStream fromServer = new DataInputStream(server.getInputStream()); //Create DataInputStream to read the response from the server that the socket recieves
	    response = new HttpResponse(fromServer);//Create HttpResponse that will take the Response from the server and turn it into strings and byte arrays we can send back to the client
	    DataOutputStream toClient = new DataOutputStream(client.getOutputStream());//Create the DataOutputStream we will use to return the server's response to the Client
	    String header = response.toString();
	    toClient.writeBytes(header);//Now we have written to the client the header of the HTTP 
	    
	    if(header.contains("Content-Type: text/html")||header.contains("Content-type: text/html")) {//this if statement will make sure the request being sent is a text based response 
	    	String changedBody = new String(response.body, "UTF-8");//changes body from an array of bytes to string in UTF-8 format
	    	changedBody = changedBody.replace("2019", "2219");//changes any instance of 2019 in body to 2219
	    	changedBody = changedBody.replace("NBA", "TBA");//same as last call, format is replace("old string", "newString")
	    	changedBody = changedBody.replace("World", "Titan");
	    	changedBody = changedBody.replace("Drummond", "Kobe-B24");
	    	toClient.write(changedBody.getBytes("UTF-8"));//.getBytes("UTF-8")changes the string back into an array of bytes, "UTF-8" tells it what the string encoding it is using
	    	// the write(...) will write the changed body to the client 
	    }
	    else {
	    	toClient.write(response.body); //if the response is not of type text then just send the response without changing it
	    }
	    
	    toClient.flush(); //flush the stream 
	    /* Write response to client. First headers, then body */
	    
	    server.close();//close response since the response was sent
	    client.close(); //close the client since the request is over
	     
	    /* Insert object into the cache */
	    /* Fill in (optional exercise only) */
	} catch (IOException e) {
	    System.out.println("Error writing response to client: " + e);
	    
	}
    }


    /** Read command line arguments and start proxy */
    public static void main(String args[]) {
	int myPort = 0;
	
	try {
	    myPort = Integer.parseInt(args[0]); // parse the portNumber passed as string to int
	} catch (ArrayIndexOutOfBoundsException e) {
	    System.out.println("Need port number as argument");
	    System.exit(-1);
	} catch (NumberFormatException e) {
	    System.out.println("Please give port number as integer.");
	    System.exit(-1);
	}
	
	init(myPort); //initialize the port number

	/** Main loop. Listen for incoming connections and spawn a new
	 * thread for handling them */
	Socket client = null;
	
	while (true) { //this while loop will keep listening to new connections and handle them
	    try {
		client = socket.accept(); // wait for a client to connect using the port number passed
		//for the client to be able to connect they have to configure their HTTP address and port number such that when they connect to a HTTP page 
		// It will be intercepted by the proxy.
		handle(client);//Method used to handle the request until the response is complete
	    } catch (IOException e) {
		System.out.println("Error reading request from client: " + e);
		/* Definitely cannot continue processing this request,
		 * so skip to next iteration of while loop. */
		continue;
	    }
	}

    }
}