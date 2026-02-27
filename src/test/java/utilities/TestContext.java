package utilities;

import java.util.HashMap;
import java.util.Map;

public class TestContext {

    private static Map<String, Object> data = new HashMap<>();

    public static void set(String key, Object value){
        data.put(key, value);
    }

    public static Object get(String key){
        return data.get(key);
    }

    public static void clear(){
        data.clear();
    }

    //header
    private static ThreadLocal<Map<String, String>> headers =
            ThreadLocal.withInitial(HashMap::new);

    public static void addHeader(String key, String value) {
        headers.get().put(key, value);
    }

    public static Map<String, String> getHeaders() {
        return headers.get();
    }

    public static void clearHeaders() {
        headers.get().clear();
    }




}
