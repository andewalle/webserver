

import java.io.*;
import java.net.*;
import java.util.*;

// The tutorial can be found just here on the SSaurel's Blog :
// https://www.ssaurel.com/blog/create-a-simple-http-web-server-in-java
// Each Client Connection will be managed in a dedicated Thread
    public class JavaHTTPServer implements Runnable{

        static final File WEB_ROOT = new File(".");
        static final String DEFAULT_FILE = "index.html";
        static final String FILE_NOT_FOUND = "404.html";
        static final String METHOD_NOT_SUPPORTED = "not_supported.html";
        // port to listen connection
        static final int PORT = 6543;

        // verbose mode
        static final boolean verbose = true;

        // Client Connection via Socket Class
        private Socket connect;

        public JavaHTTPServer(Socket c) {
            connect = c;
        }


        // we manage our particular client connection
        BufferedReader in = null;
        PrintWriter out = null;
        BufferedOutputStream dataOut = null;
        String fileRequested = null;

        //Server startup
        public static void main(String[] args) {
            try {
                ServerSocket serverConnect = new ServerSocket(PORT);
                System.out.println("Server started.\nListening for connections on port : " + PORT + " ...\n");

                // we listen until user halts server execution
                while (true) {
                    JavaHTTPServer myServer = new JavaHTTPServer(serverConnect.accept());

                    if (verbose) {
                        System.out.println("Connection opened. (" + new Date() + ")");
                    }

                    // create dedicated thread to manage the client connection
                    Thread thread = new Thread(myServer);
                    thread.start();
                }

            } catch (IOException e) {
                System.err.println("Server Connection error : " + e.getMessage());
            }
        }

        //run is where the requests are made
        @Override
        public void run() {

            try {
                // we read characters from the client via input stream on the socket
                in = new BufferedReader(new InputStreamReader(connect.getInputStream()));
                // we get character output stream to client (for headers)
                out = new PrintWriter(connect.getOutputStream());
                // get binary output stream to client (for requested data)
                dataOut = new BufferedOutputStream(connect.getOutputStream());

                // get first line of the request from the client
                String input = in.readLine();
                // we parse the request with a string tokenizer
                StringTokenizer parse = new StringTokenizer(input);
                String method = parse.nextToken().toUpperCase(); // we get the HTTP method of the client
                // we get file requested
                fileRequested = parse.nextToken().toLowerCase();

                // we support only GET and HEAD methods, we check
                if (!method.equals("GET")  &&  !method.equals("HEAD") && !method.equals("POST")) {
                    if (verbose) {
                        System.out.println("501 Not Implemented : " + method + " method.");
                    }

                    // we return the not supported file to the client
                    File file = new File(WEB_ROOT, METHOD_NOT_SUPPORTED);
                    int fileLength = (int) file.length();
                    String contentMimeType = "text/html";
                    //read content to return to client
                    byte[] fileData = readFileData(file, fileLength);

                    // we send HTTP Headers with data to client
                    out.println("HTTP/1.1 501 Not Implemented");
                    out.println("Server: Java HTTP Server from SSaurel : 1.0");
                    out.println("Date: " + new Date());
                    out.println("Content-type: " + contentMimeType);
                    out.println("Content-length: " + fileLength);
                    out.println(); // blank line between headers and content, very important !
                    out.flush(); // flush character output stream buffer
                    // file
                    dataOut.write(fileData, 0, fileLength);
                    dataOut.flush();

                } else {

                    if (fileRequested.endsWith("/")) {
                        fileRequested += DEFAULT_FILE;
                    }







                    //Helenas
                    /*if (fileRequested.equals("/jsonoutput")) {
                        String[] formData = input[input.length-1].split("&");
                        Person p = new Person(formData[0].replace("fname=",""),
                                formData[1].replace("lname=",""));
                        JsonConverter js = new JsonConverter(p);
                        byte[] jsonData = js.personToJsonString().getBytes();
                        out.println("HTTP/1.1 200 OK");
                        out.println("Server: Java HTTP Server from SSaurel : 1.0");
                        out.println("Date: " + new Date());
                        out.println("Content-type: application/json; charset=UTF-8");
                        out.println("Content-length: " + jsonData.length);
                        out.println();  // blank line between headers and content, very important !
                        out.flush();    // flush character output stream buffer
                        dataOut.write(jsonData,0, jsonData.length);
                        dataOut.flush();
                    }*/
                    //Helenas







                    File file = new File(WEB_ROOT, fileRequested);
                    int fileLength = (int) file.length();
                    String content = getContentType(fileRequested);

                    //If the http method is POST the requestPost method is called
                    if (method.equals("POST")){

                        requestPost(content, fileLength);
                    }

                    //If the http method is GET the requestPost method is called
                    else if (method.equals("GET")) { // GET method so we return content

                        requestGet(content, fileLength, file);
                    }

                    if (verbose) {
                       // System.out.println("File " + fileRequested + " of type " + content + " returned");
                    }
                }

            } catch (FileNotFoundException fnfe) {
                try {
                    fileNotFound(out, dataOut, fileRequested);
                } catch (IOException ioe) {
                    System.err.println("Error with file not found exception : " + ioe.getMessage());
                }

            } catch (IOException ioe) {
                System.err.println("Server error : " + ioe);
            } finally {
                try {
                    in.close();
                    out.close();
                    dataOut.close();
                    connect.close(); // we close socket connection
                } catch (Exception e) {
                    System.err.println("Error closing stream : " + e.getMessage());
                }

                if (verbose) {
                    System.out.println("Connection closed.\n");
                }
            }
        }









        //This method handles GET requests from the http
        private void requestGet(String content, int fileLength, File file) {

            // send HTTP Headers
            out.println("HTTP/1.1 200 OK");
            out.println("Server: Java HTTP Server from SSaurel : 1.0");
            out.println("Date: " + new Date());
            out.println("Content-type: " + content);
            out.println("Content-length: " + fileLength);
            out.println(); // blank line between headers and content, very important !
            out.flush(); // flush character output stream buffer

            try {
                byte[] fileData = readFileData(file, fileLength);
                dataOut.write(fileData, 0, fileLength);
                dataOut.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        //This method handles POST requests from the http
        private void requestPost(String content, int fileLength) {

            if(!content.equals("application/x-www-form-urlencoded")){
                System.out.println("Wrong content type");
            }
            if(fileLength <= 0){
                System.out.println("Content length error");
            }

            String line;
            int content_length = 0;
            while(true){
                try {
                    line = in.readLine();
                    if(line.equals("")){
                        char[] buf = new char[content_length];
                        in.read(buf);

                        line = new String(buf);  //String from the request
                        break;
                    }
                    else if( line.startsWith("Content-Length: ")){
                        content_length = Integer.parseInt(  line.substring(16)  );
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            String[] lines = line.split("&");   //Splitting the request string (line) at '&' to separate the
                                                      // parameters and put in the vector "lines"

            HashMap<String, String> hM = new HashMap<>();  //HashMap for storing parameters

            String key;  //Storing temp. string keys for HashMap
            String value;  //Storing temp. string values for HashMap

            //Looping through vector lines (http parameters)
            for(String s : lines){

                String[] temp = s.split("=");  //Splitting the parameters in the vector "lines" into key and value
                                                      //pairs and putting in temp. vector "temp"

                key = temp[0];  //key gets the String-value of each parameters name
                value = temp[1];  //value gets the String-value of each parameters value

                hM.put(key, value);  //The keys and values are put in the HashMap
            }

            //Looping and printing the parameters from the HashMap
            for (String i : hM.keySet()) {

                System.out.println("key: "+ i + " value: " + hM.get(i));
            }


            Person person = new Person(hM.get("firstName"), hM.get("lastName"));  //Creating a Person object from the

            JsonConverter js = new JsonConverter(person);
            String s = js.personToJsonString();

            byte[] jsonData = s.getBytes();

            out.println("HTTP/1.1 200 OK");
            out.println("Server: Java HTTP Server from SSaurel : 1.0");
            out.println("Date: " + new Date());
            out.println("Content-type: " + getContentType(fileRequested));
            out.println("Content-length: " + jsonData.length);
            out.println(); // blank line between headers and content, very important !
            out.flush(); // flush character output stream buffer

            try {
                    dataOut.write(jsonData, 0, jsonData.length);
                    dataOut.flush();
                    dataOut.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

        }










        private byte[] readFileData(File file, int fileLength) throws IOException {
            FileInputStream fileIn = null;
            byte[] fileData = new byte[fileLength];

            try {
                fileIn = new FileInputStream(file);
                fileIn.read(fileData);
            } finally {
                if (fileIn != null)
                    fileIn.close();
            }

            return fileData;
        }

        // return supported MIME Types
        private String getContentType(String fileRequested) {
            if (fileRequested.endsWith(".htm")  ||  fileRequested.endsWith(".html"))
                return "text/html";
            else if(fileRequested.endsWith(".pdf"))
                return "application/pdf";
            else if(fileRequested.endsWith(".json"))
                return "application/json";
            else
                return "text/plain";
        }

        private void fileNotFound(PrintWriter out, OutputStream dataOut, String fileRequested) throws IOException {
            File file = new File(WEB_ROOT, FILE_NOT_FOUND);
            int fileLength = (int) file.length();
            String content = "text/html";
            byte[] fileData = readFileData(file, fileLength);

            out.println("HTTP/1.1 404 File Not Found");
            out.println("Server: Java HTTP Server from SSaurel : 1.0");
            out.println("Date: " + new Date());
            out.println("Content-type: " + content);
            out.println("Content-length: " + fileLength);
            out.println(); // blank line between headers and content, very important !
            out.flush(); // flush character output stream buffer

            dataOut.write(fileData, 0, fileLength);
            dataOut.flush();

            if (verbose) {
                System.out.println("File " + fileRequested + " not found");
            }
        }

    }
