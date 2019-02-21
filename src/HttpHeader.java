import java.io.PrintWriter;
import java.util.Date;

public class HttpHeader implements Header{

    PrintWriter out;
    String statusCode;
    String serverName;
    String content;
    int fileLength;
    Date date;

    public HttpHeader(PrintWriter out, String statusCode, String serverName, String content, int fileLength)
    {
        this.out = out;
        this.statusCode = statusCode;
        this.serverName = serverName;
        this.content = content;
        this.fileLength = fileLength;
        date = new Date();
    }

    @Override
    public void write() {

        // send HTTP Headers
        out.println(statusCode);
        out.println("Server: " + serverName);
        out.println("Date: " + date);
        out.println("Content-type: " + content);
        out.println("Content-length: " + fileLength);
        out.println(); // blank line between headers and content, very important !
        out.flush(); // flush character output stream buffer
    }
}
