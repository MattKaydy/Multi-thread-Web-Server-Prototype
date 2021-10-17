/*
Multi-Threaded Web Server Prototye
Main Method
by Matthew Kong

 */

/*
This main method serves as a "connection manager". It listens to incoming connection requests from the welcoming socket (serverPort) in a infinite while loop.
When a request is detected, a connection socket will be established and passed over to to a thread "handleServiceRequest". That thread will be responsible to handle
all the input requests and responses. This allows more than one connection sockets be established at the same time by starting mutliple threads. The running thread
objects will be appended to an arrayList.

 */

import java.net.*;
import java.util.*;

public class HTTPWebServer {


    public static void main(String argv[]) throws Exception {

        List<Thread> clientsConnections = new ArrayList<Thread>(); //This arrayList stores a list of thread objects for it to run on.

        int serverPort = 8080; //The "welcoming" socket port.
        ServerSocket listenSocket = new ServerSocket(serverPort);
        System.out.println("This server is ready to receive");

        //Runs in an infinte loop and listens to connection requests. When recieved, accepts them, create a new connection socket, and then start a new thread to handle requests
        //for that new socket. Append the new thread into the arrayList for it to run on, and then proceeds to wait for another connection request.
        while (true) {
            Socket connectionSocket = listenSocket.accept();

            Thread newServiceRequest = new Thread(new handleServiceRequest(connectionSocket));
            newServiceRequest.start();

            clientsConnections.add(newServiceRequest);
        }
    }
}

