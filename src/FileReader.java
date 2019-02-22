import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class FileReader {
    File file;
    int fileLength;
    FileInputStream fileIn;
    byte[] fileData;

    public FileReader(File file, int fileLength){
        this.file = file;
        this.fileLength = fileLength;
    }

    public byte[] readData() throws IOException {
        fileData = new byte[fileLength];

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
}
