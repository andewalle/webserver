

import java.io.*;
import java.net.*;
import java.util.*;

public class JavaHTTPServer implements Runnable{

    private final String SERVER_NAME = "sic! server : 1.0";
    private final File WEB_ROOT = new File(".");
    private final String DEFAULT_FILE = "index.html";
    private final String FILE_NOT_FOUND = "404.html";
    private final String METHOD_NOT_SUPPORTED = "not_supported.html";

    // Client Connection via Socket Class
    private Socket connect;

    private Database database;

    public JavaHTTPServer(Socket c, Database database) {

        connect = c;
        this.database = database;
    }

    // we manage our particular client connection
    private BufferedReader in = null;
    private PrintWriter out = null;
    private BufferedOutputStream dataOut = null;
    private String fileRequested = null;

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
              //  connect.close(); // we close socket connection
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
            createDatabaseObject(hM);
            fileData = convertToJson(hM);
            fileLength = fileData.length;
        }
        else {
            File file = new File(WEB_ROOT, responseFile);
            fileLength = (int) file.length();
            FileReader fileReader = new FileReader(file, fileLength);
            fileData = fileReader.readData();
        }

        // we send HTTP Headers with data to client
        HttpHeader httpHeader = new HttpHeader(out, statusCode, SERVER_NAME, content, fileLength);
        httpHeader.write();

        dataOut.write(fileData, 0, fileLength);
        dataOut.flush();
    }

    private void createDatabaseObject(HashMap<String, String> hM) {

        PersonHandler personHandler = new PersonHandler(database, hM);
        personHandler.createPerson();
    }

    private byte[] convertToJson(HashMap<String, String> hM)
    {
        JsonConverter js = new JsonConverter(hM);
        String s = js.personToJsonString();
        byte[] jsonData = s.getBytes();
        return jsonData;
    }

    private HashMap<String, String> splittingPostParameters()
    {
        HashMap<String, String> hM = new HashMap<>();  //HashMap for storing parameters
        String key;  //Storing temp. string keys for HashMap
        String value;  //Storing temp. string values for HashMap
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
                                                  // parameters and put in the array "lines"

        //Looping through vector lines (http parameters)
        for(String s : lines){

            String[] temp = s.split("=");  //Splitting the parameters in the array "lines" into key and value
                                                 //pairs and putting in temp. array "temp"

            key = temp[0];  //key gets the String-value of each parameters name
            value = temp[1];  //value gets the String-value of each parameters value

            hM.put(key, value);  //The keys and values are put in the HashMap
        }

        //Looping and printing the parameters from the HashMap
        for (String i : hM.keySet()) {

            System.out.println( i + ": " + hM.get(i));
        }

        return hM;
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
        else if (fileRequested.endsWith(".jpg"))
            return "image/jpeg";
        else if (fileRequested.endsWith(".jpeg"))
            return "image/jpeg";
        else
            return "text/plain";
    }
}