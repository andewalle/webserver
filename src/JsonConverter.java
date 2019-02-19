import com.google.gson.Gson;

import java.io.*;

public class JsonConverter {
    private Gson gson = new Gson();
    private Person p;

    public JsonConverter(Person p){
        this.p = p;
    }

    public String personToJsonString(){
        return gson.toJson(p);
    }
}
