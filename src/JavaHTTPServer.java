

import java.io.*;
import java.net.*;
import java.util.*;

// The tutorial can be found just here on the SSaurel's Blog :
// https://www.ssaurel.com/blog/create-a-simple-http-web-server-in-java
// Each Client Connection will be managed in a dedicated Thread
    public class JavaHTTPServer implements Runnable{

        private final String SERVER_NAME = "Java HTTP Server from SSaurel : 1.0";
        static final File WEB_ROOT = new File(".");
        static final String DEFAULT_FILE = "index.html";
        static final String FILE_NOT_FOUND = "404.html";
        static final String METHOD_NOT_SUPPORTED = "not_supported.html";


        // Client Connection via Socket Class
        private Socket connect;

        public JavaHTTPServer(Socket c) {
            connect = c;
        }

        Database database = new Database();

        // we manage our particular client connection
        BufferedReader in = null;
        PrintWriter out = null;
        BufferedOutputStream dataOut = null;
        String fileRequested = null;

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

                    requestMethodNotSupported();

                } else {

                    if (fileRequested.endsWith("/")) {
                        fileRequested += DEFAULT_FILE;
                    }

                    String content = getContentType(fileRequested);

                    //If the http method is POST the requestPost method is called
                    if (method.equals("POST")){

                        requestPost(content);
                    }

                    //If the http method is GET the requestPost method is called
                    else if (method.equals("GET")) { // GET method so we return content

                        requestGet(content);
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
            }
        }







        private void requestMethodNotSupported() {

            // we return the not supported file to the client
            File file = new File(WEB_ROOT, METHOD_NOT_SUPPORTED);
            int fileLength = (int) file.length();
            String statusCode = "HTTP/1.1 501 Not Implemented";
            String contentMimeType = "text/html";

            // we send HTTP Headers with data to client
            HttpHeader httpHeader = new HttpHeader(out, statusCode, SERVER_NAME, contentMimeType, fileLength);
            httpHeader.write();


            try {
                byte[] fileData = readFileData(file, fileLength);  //read content to return to client
                dataOut.write(fileData, 0, fileLength);
                dataOut.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        //This method handles GET requests from the http
        public void requestGet(String content) throws IOException {


            File file = new File(WEB_ROOT, fileRequested);
            int fileLength = (int) file.length();
            byte[] fileData = readFileData(file, fileLength);
            String statusCode = "HTTP/1.1 200 OK";

            // we send HTTP Headers with data to client
            HttpHeader httpHeader = new HttpHeader(out, statusCode, SERVER_NAME, content, fileLength);
            httpHeader.write();

            try {
                dataOut.write(fileData, 0, fileLength);
                dataOut.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        //This method handles POST requests from the http
        public void requestPost(String content) {

            HashMap<String, String> hM = splittingPostParameters();
            DatabaseObjectsFactory factory = new DatabaseObjectsFactory();

            String statusCode = "HTTP/1.1 200 OK";

            JsonConverter js = new JsonConverter(hM);
            String s = js.personToJsonString();
            byte[] jsonData = s.getBytes();
            int fileLength = jsonData.length;

            // we send HTTP Headers with data to client
            HttpHeader httpHeader = new HttpHeader(out, statusCode, SERVER_NAME, content, fileLength);
            httpHeader.write();

            try {
                dataOut.write(jsonData, 0, fileLength);
                dataOut.flush();
                dataOut.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            //TODO Hantera olika inmatade objekt (t.ex person/företag) if-sats skicka med String för person/företag
            //Creating an object from the HashMap parameters
            DatabaseObject databaseObject = factory.createDatabaseObject("person", hM.get("firstName"), hM.get("lastName"));
            database.addPerson((Person)databaseObject);

        }

        private void fileNotFound(PrintWriter out, OutputStream dataOut, String fileRequested) throws IOException {

            File file = new File(WEB_ROOT, FILE_NOT_FOUND);
            int fileLength = (int) file.length();
            String statusCode = "HTTP/1.1 404 File Not Found";
            String content = "text/html";
            byte[] fileData = readFileData(file, fileLength);

            // we send HTTP Headers with data to client
            HttpHeader httpHeader = new HttpHeader(out, statusCode, SERVER_NAME, content, fileLength);
            httpHeader.write();


            dataOut.write(fileData, 0, fileLength);
            dataOut.flush();

            System.out.println("File " + fileRequested + " not found");
        }

        private HashMap<String, String> splittingPostParameters()
        {

            HashMap<String, String> hM = new HashMap<>();  //HashMap for storing parameters
            String key;  //Storing temp. string keys for HashMap
            String value;  //Storing temp. string values for HashMap
            String line;
            int content_length = 0;

               /*  if(!content.equals("application/x-www-form-urlencoded")){  //TODO använda detta???  flytta ner i metoden?
                    System.out.println("Wrong content type");
                }
                if(fileLength <= 0){
                    System.out.println("Content length error");
                }*/

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
                                                      // parameters and put in the array "lines"

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

            return hM;
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


    }
