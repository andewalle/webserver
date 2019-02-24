import java.io.IOException;
import java.net.ServerSocket;

public class ConnectionHandler {

    // port to listen connection
    private final int PORT = 6543;

    //Server startup
    public void serverStartUp() {
        try {
            Database database = new Database();
            ServerSocket serverConnect = new ServerSocket(PORT);
            System.out.println("Server started.\nListening for connections on port : " + PORT + " ...\n");

            // we listen until user halts serverName execution
            while (true) {
                JavaHTTPServer myServer = new JavaHTTPServer(serverConnect.accept(), database);

                // create dedicated thread to manage the client connection
                Thread thread = new Thread(myServer);
                thread.start();
            }

        } catch (IOException e) {
            System.err.println("Server Connection error : " + e.getMessage());
        }
    }
}
