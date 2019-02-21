import java.io.*;
import java.net.*;
import java.util.*;

// Each Client Connection will be managed in a dedicated Thread
public class JavaHTTPServer implements Runnable{

    static final File WEB_ROOT = new File(".");
    static final String DEFAULT_FILE = "index.html";
    static final String FILE_NOT_FOUND = "404.html";
    static final String METHOD_NOT_SUPPORTED = "not_supported.html";
    static final int PORT = 6543;// port to listen connection

    private Socket connect;// Client Connection via Socket Class

    public JavaHTTPServer(Socket c) {
        connect = c;
    }

    //Server startup
    public static void main(String[] args) {
        try {
            ServerSocket serverConnect = new ServerSocket(PORT);
            System.out.println("Server started.\nListening for connections on port : " + PORT + " ...\n");

            // we listen until user halts server execution
            while (true) {
                JavaHTTPServer myServer = new JavaHTTPServer(serverConnect.accept());

                Thread thread = new Thread(myServer);// create dedicated thread to manage the client connection
                thread.start();
            }
        }
        catch (IOException e) {
            System.err.println("Server Connection error : " + e.getMessage());
        }
    }

    //run is where the requests are made
    @Override
    public void run() {
        // we manage our particular client connection
        BufferedReader in = null;
        PrintWriter out = null;
        BufferedOutputStream dataOut = null;
        String fileRequested = null;

        try {
            in = new BufferedReader(new InputStreamReader(connect.getInputStream()));// we read characters from the client via input stream on the socket
            out = new PrintWriter(connect.getOutputStream());// we get character output stream to client (for headers)
            dataOut = new BufferedOutputStream(connect.getOutputStream());// get binary output stream to client (for requested data)

            String input = in.readLine();// get first line of the request from the client
            StringTokenizer parse = new StringTokenizer(input);// we parse the request with a string tokenizer
            String method = parse.nextToken().toUpperCase(); // we get the HTTP method of the client
            fileRequested = parse.nextToken().toLowerCase();// we get file requested

            // control that request is GET, HEAD or POST
            if (!method.equals("GET")  &&  !method.equals("HEAD") && !method.equals("POST")) {
                File file = new File(WEB_ROOT, METHOD_NOT_SUPPORTED);// we return the not supported file to the client
                int fileLength = (int) file.length();
                String contentMimeType = "text/html";
                byte[] fileData = readFileData(file, fileLength);//read content to return to client

                out.print(HttpResponse.getResponse("501", contentMimeType, fileLength));// we send HTTP Headers with data to client
                out.flush(); // flush character output stream buffer

                dataOut.write(fileData, 0, fileLength);
                dataOut.flush();
            }
            else {
                if (fileRequested.endsWith("/")) {
                    fileRequested += DEFAULT_FILE;
                }

                File file = new File(WEB_ROOT, fileRequested);
                int fileLength = (int) file.length();
                String content = getContentType(fileRequested);

                //If the http method is POST the requestPost method is called
                if (method.equals("POST")){
                    requestPost(in, out, dataOut, content, fileLength);
                }

                //If the http method is GET the requestPost method is called
                else if (method.equals("GET")) { // GET method so we return content
                    requestGet(out, dataOut, content, fileLength, file);
                }
            }
        }
        catch(FileNotFoundException fnfe){
            try{
                fileNotFound(out, dataOut);
            }
            catch(IOException ioe) {
                System.err.println("Error with file not found exception : " + ioe.getMessage());
            }
        }
        catch(IOException ioe){
            System.err.println("Server error : " + ioe);
        }
        finally {
            try {
                in.close();
                out.close();
                dataOut.close();
                connect.close(); // we close socket connection
            }
            catch (Exception e) {
                System.err.println("Error closing stream : " + e.getMessage());
            }
        }
    }

    //This method handles GET requests from the http
    private void requestGet(PrintWriter out, BufferedOutputStream dataOut, String content, int fileLength, File file) throws IOException {
        byte[] fileData = readFileData(file, fileLength);

        // send HTTP Headers
        out.print(HttpResponse.getResponse("200", content, fileLength));
        out.flush(); // flush character output stream buffer

        dataOut.write(fileData, 0, fileLength);
        dataOut.flush();
    }

    //This method handles POST requests from the http
    private void requestPost(BufferedReader in, PrintWriter out, BufferedOutputStream dataOut, String content, int fileLength) throws IOException {
        String line;
        int contentLength = 0;

        while(true){
            line = in.readLine();

            if( line.startsWith("Content-Length: ")){
                contentLength = Integer.parseInt(line.substring(16));
            }
            else if(line.equals("")){
                char[] buf = new char[contentLength];
                in.read(buf);

                line = new String(buf);  //String from the request
                break;
            }
        }
        String[] lines = line.split("&");   //Splitting the request string (line) at '&' to separate the parameters and put in the vector "lines"

        HashMap<String, String> hM = new HashMap<>();  //HashMap for storing parameters
        String key;  //Storing temp. string keys for HashMap
        String value;  //Storing temp. string values for HashMap

        //Looping through vector lines (http parameters)
        for(String s : lines){
            String[] temp = s.split("=");  //Splitting the parameters in the vector "lines" into key and value pairs and putting in temp. vector "temp"

            key = temp[0];  //key gets the String-value of each parameters name
            value = temp[1];  //value gets the String-value of each parameters value

            hM.put(key, value);  //The keys and values are put in the HashMap
        }
        JsonConverter js = new JsonConverter(hM);
        String s = js.personToJsonString();

        byte[] jsonData = s.getBytes();

        out.print(HttpResponse.getResponse("200", content, jsonData.length));
        out.flush(); // flush character output stream buffer

        dataOut.write(jsonData, 0, jsonData.length);
        dataOut.close();
    }

    private byte[] readFileData(File file, int fileLength) throws IOException {
        FileInputStream fileIn = null;
        byte[] fileData = new byte[fileLength];

        try {
            fileIn = new FileInputStream(file);
            fileIn.read(fileData);
        }
        finally {
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

    private void fileNotFound(PrintWriter out, OutputStream dataOut) throws IOException {
        File file = new File(WEB_ROOT, FILE_NOT_FOUND);
        int fileLength = (int) file.length();
        String content = "text/html";
        byte[] fileData = readFileData(file, fileLength);

        out.print(HttpResponse.getResponse("404", content, fileLength));
        out.flush(); // flush character output stream buffer

        dataOut.write(fileData, 0, fileLength);
        dataOut.flush();
    }
}
