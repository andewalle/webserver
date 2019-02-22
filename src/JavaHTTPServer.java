

import java.io.*;
import java.net.*;
import java.util.*;

public class JavaHTTPServer implements Runnable{

    private final String SERVER_NAME = "Java HTTP Server from SSaurel : 1.0";  //TODO Byta namn?
    static final File WEB_ROOT = new File(".");  //TODO path till reserverad mapp inte C:/ disk
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

        String responseFile = "";
        String content = "";
        String statusCode = "";
        String contentMimeType = "text/html";

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

                responseFile = METHOD_NOT_SUPPORTED;
                content = contentMimeType;
                statusCode = "HTTP/1.1 501 Not Implemented";

            } else {

                if (fileRequested.endsWith("/")) {
                    fileRequested += DEFAULT_FILE;
                }

                content = getContentType(fileRequested);

                //If the http method is POST the requestPost method is called
                if (method.equals("POST")){

                    statusCode = "HTTP/1.1 200 OK";
                }

                else if (method.equals("HEAD")){

                    responseFile = fileRequested;
                    statusCode = "HTTP/1.1 200 OK";
                }

                //If the http method is GET the requestPost method is called
                else if (method.equals("GET")) { // GET method so we return content

                    responseFile = fileRequested;
                    statusCode = "HTTP/1.1 200 OK";
                }
            }

            response(responseFile, content, statusCode);

        } catch (FileNotFoundException fnfe) {
            try {
                content = "text/html";
                statusCode = "HTTP/1.1 404 File Not Found";
                response(FILE_NOT_FOUND, content, statusCode);
               // fileNotFound(out, dataOut, fileRequested);  //TODO funkar de att använda samma metod som de andra responsen?
                System.out.println("File " + fileRequested + " not found");
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

    private void response(String responseFile, String content, String statusCode) throws IOException
    {
        int fileLength;
        byte[] fileData;

        if(responseFile.equals(""))  //If responseFile are empty it's a post request
        {
            HashMap<String, String> hM = splittingPostParameters();
            fileData = convertToJson(hM);
            fileLength = fileData.length;
        }
        else {
            File file = new File(WEB_ROOT, responseFile);
            fileLength = (int) file.length();
            fileData = readFileData(file, fileLength);
        }

        // we send HTTP Headers with data to client
        HttpHeader httpHeader = new HttpHeader(out, statusCode, SERVER_NAME, content, fileLength);
        httpHeader.write();

        dataOut.write(fileData, 0, fileLength);
        dataOut.flush();
    }

    //TODO Bygga ihop convertToJson och createPerson???
    private byte[] convertToJson(HashMap<String, String> hM)
    {
        createPerson(hM);

        JsonConverter js = new JsonConverter(hM);
        String s = js.personToJsonString();
        byte[] jsonData = s.getBytes();
        return jsonData;
    }

    private void createPerson(HashMap<String, String> hM)
    {
        DatabaseObjectsFactory factory = new DatabaseObjectsFactory();

        //TODO Hantera olika inmatade objekt (t.ex person/företag) if-sats skicka med String för person/företag
        //TODO Skapa metod för att skapa objekt??
        //Creating an object from the HashMap parameters
        DatabaseObject databaseObject = factory.createDatabaseObject("person", hM.get("firstName"), hM.get("lastName"));
        database.addPerson((Person)databaseObject);
        database.listPersons();
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

            String[] temp = s.split("=");  //Splitting the parameters in the array "lines" into key and value
                                                 //pairs and putting in temp. array "temp"

            key = temp[0];  //key gets the String-value of each parameters name
            value = temp[1];  //value gets the String-value of each parameters value

            hM.put(key, value);  //The keys and values are put in the HashMap
        }

        //TODO behövs denna loop? kanske ändra parameternamn?
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
        else if (fileRequested.endsWith(".css"))
            return "text/css";
        else if (fileRequested.endsWith(".js"))
            return ("text/javascript");
        else
            return "text/plain";
    }
























    /*private void requestMethodNotSupported() {

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

        //TODO try catch när metoden throws??
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
        //TODO Skapa metod för att skapa objekt??
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

    }*/

}
