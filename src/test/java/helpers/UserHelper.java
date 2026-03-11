package helpers;

import payloads.request.UserDetailsPOJO;
import payloads.request.UserPOJO;

public class UserHelper {


    public static UserDetailsPOJO userDetailPayload(String firstName , String lastName){
        return UserDetailsPOJO.builder()
                .firstname(firstName)
                .lastname(lastName)
                .build();
    }

    public static UserPOJO userPayload(UserDetailsPOJO userDetailPayload,String email,String username, String password){
        return UserPOJO.builder()
                .email(email)
                .username(username)
                .password(password)
                .details(userDetailPayload)
                .build();
    }

    public static UserPOJO updateUserField(String field, String value){

        UserPOJO.UserPOJOBuilder builder = UserPOJO.builder();

        switch(field){

            case "email":
                builder.email(value);
                break;

            case "username":
                builder.username(value);
                break;

            case "password":
                builder.password(value);
                break;

            case "firstname":
                builder.details(
                        UserDetailsPOJO.builder()
                                .firstname(value)
                                .build()
                );
                break;

            case "lastname":
                builder.details(
                        UserDetailsPOJO.builder()
                                .lastname(value)
                                .build()
                );
                break;
        }

        return builder.build();
    }





}
