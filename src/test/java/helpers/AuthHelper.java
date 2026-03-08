package helpers;

import payloads.request.LoginRequestPOJO;

import java.util.HashMap;
import java.util.Map;

public class AuthHelper {

    public static LoginRequestPOJO loginasUserOrAdmin(String username , String password){

        LoginRequestPOJO loginRequestPOJO = LoginRequestPOJO.builder()
                .username(username)
                .password(password)
                .build();

        return loginRequestPOJO;
    }

    public static LoginRequestPOJO loginWithoutPassword(String username){
        return LoginRequestPOJO.builder()
                .username(username)
                .build();
    }

    public static LoginRequestPOJO loginWithoutUsername(String password){
        return LoginRequestPOJO.builder()
                .password(password)
                .build();
    }

    public static Map<String,Object> loginJsonInjectionPayload(){

        Map<String, Object> payload = new HashMap<>();

        Map<String, Object> injection = new HashMap<>();
        injection.put("$ne", null);

        payload.put("username", injection);
        payload.put("password", injection);

        return payload;
    }



}
