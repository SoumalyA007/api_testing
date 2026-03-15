package helpers;

import endpoints.Users;
import enums.UserRole;
import payloads.request.UserDetailsPOJO;
import payloads.request.UserPOJO;
import testBase.BaseClass;

import java.util.List;
import java.util.Random;

public class UserHelper extends BaseClass {


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

    public static UserPOJO userPayloadWithId(int userId, UserDetailsPOJO userDetailPayload,String email,String username, String password){
        return UserPOJO.builder()
                .id(userId)
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

    public static UserPOJO updateCreateUserWithoutEmail(UserDetailsPOJO userDetailPayload,String username, String password){

        return UserPOJO.builder()
                .username(username)
                .password(password)
                .details(userDetailPayload)
                .build();

    }

    public static int getLastUserId(){

        List<Integer> userId = Users.getAllUsers(UserRole.USER).then().spec(success200()).extract().jsonPath().getList("id",Integer.class);

        int lastUserId = userId.get(new Random().nextInt(userId.size()-1));

        return lastUserId;
    }



}
