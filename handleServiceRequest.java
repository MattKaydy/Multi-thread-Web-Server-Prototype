/*
Multi-Threaded Web Server
by Matthew Kong
 */

/*
This class serves as a thread to handle the requests and give responses for each connectionSocket object created from the main module.
According to the HTTP 1.0 protocol, the MANDATORY response header field is only the response code (e.g 200 OK, 304 Not Modified, 400 Bad Request, 404 File Not Found).
Only 2 headers, including response headers and the Last-Modified header will be implemented in this server program.
 */

import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;


public class handleServiceRequest implements Runnable{

    private Socket connectionSocket;
    private String requestMessageLine;
    private String fileName;
    private BufferedReader inFromClient;
    private DataOutputStream outToClient;
    private String requestCommand;
    private boolean notModified;
    private Date lastModifiedDate;
    private Date ifModifiedDate;
    private SimpleDateFormat dateFormat;

    private String logFileName = "HTTPLogFile.txt";

    public handleServiceRequest(Socket newConnectionSocket) throws IOException {
        //When a thread is constructed, assign connection sockets as well as input and output streams.
        connectionSocket = newConnectionSocket;
        inFromClient =
                new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
        outToClient =
                new DataOutputStream(connectionSocket.getOutputStream());

        notModified = false;
        lastModifiedDate = null;
        ifModifiedDate = null;
        dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz");

        System.out.println("Connection established for IP: "+connectionSocket.getRemoteSocketAddress().toString().substring(1));
    }

    //This is the main body with is triggered by the "start" command from the main module.
    public void run(){

        List<String> clientHeaderLines = new ArrayList<String>(); //Stores a list of lines of client requests.

        try {

            //Step 1: Read all request lines and append them to the clientHeaderLines array list.

            String messageLines;
            messageLines = inFromClient.readLine();

            while (messageLines.length() > 0) {
                //System.out.println("Reading Line from " +connectionSocket.getRemoteSocketAddress().toString().substring(1) + ": " + messageLines);
                clientHeaderLines.add(messageLines);
                messageLines = inFromClient.readLine();
            }

            //System.out.println("Line reading complete from "+connectionSocket.getRemoteSocketAddress().toString().substring(1)+".");

            //Step 2: Get the first request header. It must be at element 0 in the array. If the header is not GET or HEAD, return 400 bad request.

            requestMessageLine = clientHeaderLines.get(0);
            StringTokenizer tokenizedLine = new StringTokenizer(requestMessageLine);
            requestCommand = tokenizedLine.nextToken();

            if (requestCommand.equals("GET") || requestCommand.equals("HEAD") ) {


                //Step 3: Find the file name, length, and then open the file.

                fileName = tokenizedLine.nextToken();
                if (fileName.startsWith("/") == true)
                    fileName = fileName.substring(1);
                File file = new File(fileName);
                int numOfBytes = (int) file.length();

                FileInputStream inFile = new FileInputStream(fileName);
                byte[] fileInBytes = new byte[numOfBytes];
                inFile.read(fileInBytes);



                /*
                Step 4: Get the last modified date from the file's meta data into the date object "lastModifiedDate".
                This is a tricky part, as the date object obtained directly from the metadata is too "accurate" (info down to milliseconds will be recorded), when compared
                to the HTTP date from the request header, the formatted date objects will never be the equal (because the milliseconds in the ifModifiedDate is always 0, given
                it is parsed from a string.)
                To fix this problem, we get the date's metadata, convert it to a string variable in HTTP format, and then RE convert it to the date object. The milliseconds in this
                date object thus will be zero.
                */

                //Make the lastModifiedDate object "less accurate".
                String formattedDate = dateFormat.format(new Date (file.lastModified()));
                dateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Hong_Kong"));
                lastModifiedDate = dateFormat.parse(formattedDate);

                //Find the "if-modified-since" header/field in the arrayList storing the list of headers. Compare the dates. If the requested file is older or equal to the specified date, flag notModified as true.
                for (int i = 1; i < clientHeaderLines.size(); i++) {
                    StringTokenizer tokenizedLine2 = new StringTokenizer(clientHeaderLines.get(i));
                    String additionalCommand = tokenizedLine2.nextToken();

                    if (additionalCommand.equals("If-Modified-Since:")) {

                        String ifModifiedDateString = clientHeaderLines.get(i).substring(additionalCommand.length()+1);
                        ifModifiedDate = dateFormat.parse(ifModifiedDateString);

                        if (lastModifiedDate.compareTo(ifModifiedDate) <= 0) {
                            notModified = true;
                        }
                        break;
                    }
                }


                //Step 5: Output the valid response type - if notModified was detected above, print response 304, else print response 200.


                if (notModified == true)
                    outToClient.writeBytes("HTTP/1.0 304 Not Modified\r\n");

                else
                    outToClient.writeBytes("HTTP/1.0 200 OK\r\n");



                //Step 6: Output the Last-Modified Header
                outToClient.writeBytes("Last-Modified: " + formattedDate + "\r\n");



                //Step 7: Output the requested object if the requested command is GET, and notModified is not detected above.
                outToClient.writeBytes("\r\n");

                if (requestCommand.equals("GET") && notModified == false)
                    outToClient.write(fileInBytes, 0, numOfBytes);


                //Step 8: Update log files, shut the connection.
                if (notModified == true) {
                    printRequestAction(requestCommand, fileName, "HTTP/1.0 304 Not Modified");
                    updateLogFile(connectionSocket.getRemoteSocketAddress().toString(), getServerTime(), fileName, "HTTP/1.0 304 Not Modified");
                }
                else {
                    printRequestAction(requestCommand, fileName, "HTTP/1.0 200 OK");
                    updateLogFile(connectionSocket.getRemoteSocketAddress().toString(), getServerTime(), fileName, "HTTP/1.0 200 OK");
                }

                System.out.println("Connection closed for IP: "+connectionSocket.getRemoteSocketAddress().toString().substring(1));
                connectionSocket.close();
            }

            //If the request methods are not GET or HEAD (as stated in point 1), return response 400 and close the connection.
            else {
                outToClient.writeBytes("HTTP/1.0 400 Bad Request");

                printRequestAction(requestCommand, fileName, "HTTP/1.0 400 Bad Request");
                updateLogFile(connectionSocket.getRemoteSocketAddress().toString(), getServerTime(), fileName, "HTTP/1.0 400 Bad Request");

                System.out.println("Connection closed for IP: "+connectionSocket.getRemoteSocketAddress().toString().substring(1));
                connectionSocket.close();
            }

        }

        //At any point an error occurred when accessing the requested file name, return response 404.
        catch (IOException err404) {
            try {
                outToClient.writeBytes("HTTP/1.0 404 File Not Found");

                printRequestAction(requestCommand, fileName, "HTTP/1.0 404 File Not Found");
                updateLogFile(connectionSocket.getRemoteSocketAddress().toString(), getServerTime(), fileName, "HTTP/1.0 404 File Not Found");

                System.out.println("Connection closed for IP: "+connectionSocket.getRemoteSocketAddress().toString().substring(1));
                connectionSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("Warning: Connection aborted for IP: "+connectionSocket.getRemoteSocketAddress().toString().substring(1));
            }
        }
        //Catches other exceptions. This will lead to a 400 bad request. (This is intended for cases where "if-modified-since" request header is invalid which lead to parsing exceptions.)
        catch (Exception exception) {
            try {
                outToClient.writeBytes("HTTP/1.0 400 Bad Request");

                printRequestAction(requestCommand, fileName, "HTTP/1.0 400 Bad Request");
                updateLogFile(connectionSocket.getRemoteSocketAddress().toString(), getServerTime(), fileName, "HTTP/1.0 400 Bad Request");

                System.out.println("Connection closed for IP: "+connectionSocket.getRemoteSocketAddress().toString().substring(1));
                connectionSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("Warning: Connection aborted for IP: "+connectionSocket.getRemoteSocketAddress().toString().substring(1));
            }
        }
    }

    //Get the current server time in HTTP format.
    //Source: https://stackoverflow.com/questions/7707555/getting-date-in-http-format-in-java
    String getServerTime() {
        Calendar calendar = Calendar.getInstance();
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        return dateFormat.format(calendar.getTime());
    }


    //Appends logs to specified log file using FileWriter and PrintWriter.
    //If the fileName specified above is not detected in the directory, a new .txt file will be created. Else it will append logs to the bottom of the .txt file.
    //Referenced from: https://stackoverflow.com/questions/4269302/how-do-you-append-to-a-text-file-instead-of-overwriting-it-in-java
    void updateLogFile(String clientHostName, String accessTime, String reqFileName, String responseType) {
        FileWriter filer = null;
        PrintWriter printer = null;
        try {
            filer = new FileWriter(logFileName, true);
            printer = new PrintWriter(filer);

            String logLine = "Client Hostname/IP Address: " + clientHostName.substring(1) + "\t Access Time: " + accessTime + "\t Requested File Name: " + reqFileName + "\t Response Type: " + responseType + "\n";

            printer.write(logLine);
            printer.close();
            printer.close();
        } catch (IOException ex) {
            System.out.println("Error: IO Exception when writing log file!");
        }
    }

    //Prints requests and response info into server command console in a formatted way.
    void printRequestAction (String requestCommand, String targetFileName, String response) {
        if (ifModifiedDate == null)
            System.out.println("\""+requestCommand+"\" request is received from client: "+connectionSocket.getRemoteSocketAddress().toString().substring(1) + ". File requested: \"" + targetFileName + "\". Response returned: \"" + response +"\".");
        else if (ifModifiedDate != null)
            System.out.println("\""+requestCommand+"\" request is received from client: "+connectionSocket.getRemoteSocketAddress().toString().substring(1) + " with If-Modified-Since header dated "+ifModifiedDate+". File requested: \"" + targetFileName + "\". Response returned: \"" + response +"\".");
    }
}
