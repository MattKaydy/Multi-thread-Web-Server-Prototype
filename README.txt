Multi-Threaded Web Server prototype
by Matthew Kong

This is a prototype of coding a mini server using Java and it handles html, jpg and txt requests... Read below on how to activate the server and connect to the server with your browser:

(Please read this file in maximized window for maximum comfort)

=================================
IMPORTANT INFORMATION

1. This program runs on Java and is TESTED on Java version "14.0.2" running on Windows 10 Command Prompt environment. You are advised to use this program under similar environments.
2. When using command-line client programs like "telnet", you can include as many additional header fields as you want with your request. However, only the header field "If-Modified-Since" will be entertained per the requirements. YOU MUST PRESS ENTER TWICE after typing your request command to generate a response from the server. The program interprets "\r\n" as the end of the request information just like how browsers send their request information.

=================================
CONTENTS OF ZIP FILE

HTTPWebServer.java - The main module for the server program (a.ka the "Connection Manager").

handleServiceRequest.java - The runnable thread module for handling requests for every connection socket passed from HTTPWebServer.java.

README.txt - This file.

Some files to test getting a server request including:
helloworld.html
helloworld.jpg
helloworld.txt

=================================
!!HOW TO COMPILE AND RUN THE SERVER PROGRAM!!

1. Unzip everything into a folder. 
2. Run a .bat file named "HTTPWebServer.bat".
3. A command prompt will pop up with a prompt. Press any key to continue.
4. If you see the word "This server is ready to receive", the server program is ready to run and handle requests.

You have to communicate to this small server with the IP address of "127.0.0.1" (local IP). The default port is 8080. This can be modified in "HTTPWebServer.java".

=================================
INFO ON LOG FILE

When the program first runs, a .txt file named "HTTPLogFile.txt" will be created in the directory of THIS PROGRAM. The name of the .txt file can be modified in the source code "handleServerRequest.java".

Every time a request is handled, a log will be appended to this log file, with information including "Client Hostname/IP Address",  "Access Time", "Requested File Name", and "Response Type". (In accordance with the project requirements.)

If this file is deleted, a new one will be generated upon the start of the program.

=================================
SCOPE OF THIS SERVER PROGRAM

This server program follows HTTP 1.0 format. 

According to the format, the MANDATORY response header fields are ONLY the response types (e.g HTTP/1.0 200 OK, HTTP/1.0 400 Bad Request, etc...) (The first line). Other header fields such as "Content-Length", "Content-Type", "Server" are OPTIONAL.

To avoid mark deduction due to implementing extra functions, the server program only handles the following request methods: 
GET
HEAD (as stated in the project requirements).

The server program will only provide four responses including:
"HTTP/1.0 200 OK"
"HTTP/1.0 304 Not Modified" (in parallel to point 3 of project requirement)
"HTTP/1.0 400 Bad Request"
"HTTP/1.0 404 Not Found"

When a request method other than "GET" and "HEAD" is detected, an "HTTP/1.0 400 Bad Request" response will be replied to the client.

For response codes "200 OK" and "304 Not Modified", the header "Last-Modified" and its date/timestamp in HTTP FORMAT will also be included in the server reply to the client. For example, assume a HEAD method is successfully invoked, the server gives the following response:

HTTP/1.0 200 OK
Last Modified: Tue, 23 Feb 2021 15:28:42 HKT

...whereas other response codes will only have 1 line and will not have a "Last Modified" header attached to the response message.

The server command prompt will generate information for users to know if a request is received or if a connection is established or closed. 

For example, when a connection is established, the server's command prompt will print the folllowing line:
Connection established for IP: xxx.xxx.xxx.xxx.

When a client sends a request, the server's command prompt will print a line stating the client's IP, request method etc. For example:
"GET" request is received from client: 127.0.0.1:1166 with If-Modified-Since header dated Tue Feb 23 15:29:07 CST 2021. File requested: "helloworld.html". Response returned: "HTTP/1.0 304 Not Modified".

=================================
EXAMPLES OF UTILIZING THIS PROGRAM

[Example 1: Using a browser]
1. Open any kind of browser of your choice (e.g Google Chrome).
2. Type "http://127.0.0.1:8080/helloworld.txt" in the browser.
3. The helloworld.txt file will be returned and displayed. Observe the log file and the command prompt of the server program.

[Example 2: Using cmd telnet in Windows 10]
1. Make sure you have the telnet service activated. (Refer to https://kencenerelli.wordpress.com/2017/07/16/enabling-telnet-client-in-windows-10/)
2. Open 2 instances of command prompt
3. Type "telnet 127.0.0.1 8080" and press enter for each of the 2 instances.
4. Observe the command prompt of the server program. Notice that the program will state a connection established for both clients. This is an example of multithreading.
5. Now type "HEAD helloworld.txt" and PRESS ENTER TWICE in one of the cmd telnet clients.
6. Observe the response messages.

=================================
REFERENCES

The multi-thread server infrastructure is referenced from:
Computer Networking: A Top-Down Approach, by Kurose and Ross

The method in handleServiceRequest.java for appending text and create a text file is referenced from:
https://stackoverflow.com/questions/4269302/how-do-you-append-to-a-text-file-instead-of-overwriting-it-in-java

The method in handleServiceRequest.java for formatting Java "Date" objects into a string in HTTP date format is referenced from:
https://stackoverflow.com/questions/7707555/getting-date-in-http-format-in-java
