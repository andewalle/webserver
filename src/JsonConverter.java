import com.google.gson.Gson;

import java.io.*;

public class JsonConverter {
    private Gson gson = new Gson();

    public String objectToJsonString(Object obj){
        return gson.toJson(obj);
    }

    public void objectToJsonFile(Object obj, File f) throws IOException {
        FileWriter fw = new FileWriter(f);
        gson.toJson(obj, fw);
        fw.close();
    }

    public Object jsonStringToObject(String s){
        return gson.fromJson(s, Object.class);
    }

    public Object jsonFileToObject(File f) throws FileNotFoundException {
        return gson.fromJson(new FileReader(f), Object.class);
    }
}
