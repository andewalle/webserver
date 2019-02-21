import com.google.gson.Gson;

public class JsonConverter {
    private Gson gson = new Gson();
    private Object o;

    public JsonConverter(Object o){
        this.o = o;
    }

    public String personToJsonString(){
        return gson.toJson(o);
    }
}
