package utilities;

import java.util.HashMap;
import java.util.Map;

public class TestContext {

    // 🔥 Thread-safe data storage
    private static ThreadLocal<Map<String, Object>> data =
            ThreadLocal.withInitial(HashMap::new);

    public static void set(String key, Object value){
        data.get().put(key, value);
    }

    public static Object get(String key){
        return data.get().get(key);
    }

    public static void clear(){
        data.get().clear();
    }

    // 🔥 Thread-safe headers (already correct)
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

    // 🔥 VERY IMPORTANT (memory leak prevention)
    public static void remove() {
        data.remove();
        headers.remove();
    }
}