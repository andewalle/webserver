import com.google.gson.Gson;

import java.io.*;

public class JsonConverter {
    private Gson gson = new Gson();

    public JsonConverter(Person p){
        gson.toJson(p);
    }
}
