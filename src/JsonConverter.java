import com.google.gson.Gson;

import java.io.*;

public class JsonConverter {
    private Gson gson = new Gson();

    //Returns a Json string of a object.
    public String objectToJsonString(Object obj){
        return gson.toJson(obj);
    }

    //File("path to where you want to save the json file")
    public void objectToJsonFile(Object obj, File f) throws IOException {
        FileWriter fw = new FileWriter(f);
        gson.toJson(obj, fw);
        fw.close();
    }

    //Convert a json string to a object.
    public Object jsonStringToObject(String s){
        return gson.fromJson(s, Object.class);
    }

    //File("path to the file you want to make into a object")
    public Object jsonFileToObject(File f) throws FileNotFoundException {
        return gson.fromJson(new FileReader(f), Object.class);
    }
}
