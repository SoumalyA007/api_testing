package helpers;

import payloads.request.LoginRequestPOJO;

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

}
