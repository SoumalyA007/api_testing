package tests;

import endpoints.Users;
import enums.UserRole;
import io.restassured.response.Response;
import org.testng.annotations.Test;
import payloads.request.UserDetailsPOJO;
import payloads.request.UserPOJO;
import testBase.BaseClass;

import java.util.List;
import java.util.Random;

import static org.hamcrest.Matchers.*;

public class UsersTest extends BaseClass {

    @Test
    public void getAllUsers(){

        Users.getAllUsers(UserRole.USER)
                .then().spec(success200())
                .body("id",everyItem(notNullValue()))
                .body("email",everyItem(endsWith("@gmail.com")))
                .body("username",everyItem(notNullValue()))
                .body("role",everyItem(
                        anyOf(
                                equalTo("user"),
                                equalTo("admin")
                        )
                ))
                .body("details.firstname",everyItem(notNullValue()))
                .body("details.lastname",everyItem(notNullValue()));

    }

    @Test
    public void getUserById(){

        List<Integer> userId = Users.getAllUsers(UserRole.USER).then().spec(success200()).extract().jsonPath().getList("id",Integer.class);

        int randUserId = userId.get(new Random().nextInt(userId.size()));

        Users.getUserById(randUserId,UserRole.USER).then().spec(success200()).body("id",equalTo(randUserId));

    }

        @Test
        public void createUser(){

            UserDetailsPOJO userDetailsPOJO = UserDetailsPOJO.builder()
                    .firstname("Soumalya")
                    .lastname("Hajra")
                    .build();

            UserPOJO userPOJO = UserPOJO.builder()
                    .email("user1@gmail.com")
                    .username("Soumalya")
                    .password("user@1")
                    .details(userDetailsPOJO)
                    .build();

            Users.createUser(userPOJO,UserRole.ADMIN)
                    .then()
                    .spec(success201())
                    .body("username",equalTo("Soumalya"))
                    .body("id",notNullValue());

        }


    @Test
    public void updateUser(){

        Response resp = Users.getAllUsers(UserRole.USER);
        List<Integer> userIds = resp.then().extract().jsonPath().getList("id", Integer.class);
        int id = userIds.get(userIds.size()-1);

        UserDetailsPOJO userDetailsPOJO = UserDetailsPOJO.builder()
                .firstname("Soumalya")
                .lastname("Hajra")
                .build();

        UserPOJO userPOJO = UserPOJO.builder()
                .email("soumalya.hajra@gmail.com")
                .username("Soumalya")
                .password("pass@1234")
                .details(userDetailsPOJO)
                .build();

        Users.updateUser(id,userPOJO,UserRole.ADMIN)
                .then()
                .spec(success201())
                .body("username",equalTo("Soumalya"))
                .body("id",notNullValue())
                .body("password",equalTo("pass@1234"));

    }

    @Test
    public void deleteUser(){

        Response resp = Users.getAllUsers(UserRole.USER);
        List<Integer> userIds = resp.then().extract().jsonPath().getList("id", Integer.class);
        int id = userIds.get(userIds.size()-1);

        Users.deleteUser(id,UserRole.ADMIN);


    }

    @Test
    public void getUserByInvalidId(){

        List<Integer> userId = Users.getAllUsers(UserRole.USER).then().spec(success200()).extract().jsonPath().getList("id",Integer.class);

        int id = userId.get(userId.size()-1);
        int invalidId = id + 1 ;

        Users.getUserById(invalidId+1,UserRole.USER).then().spec(fail403());

    }

    @Test
    public void createUserWithExistingEmail(){

        String existingemail = "admin@enterprise.com";

        UserDetailsPOJO userDetailsPOJO = UserDetailsPOJO.builder()
                .firstname("Soumalya")
                .lastname("Hajra")
                .build();

        UserPOJO userPOJO = UserPOJO.builder()
                .email(existingemail)
                .username("Soumalya")
                .password("user@1")
                .details(userDetailsPOJO)
                .build();

        Users.createUser(userPOJO,UserRole.ADMIN)
                .then()
                .spec(fail400())
                .body("username",equalTo("Soumalya"))
                .body("id",notNullValue());

    }


    @Test
    public void createUserWithMissingEmail(){

        String existingemail = "admin@enterprise.com";

        UserDetailsPOJO userDetailsPOJO = UserDetailsPOJO.builder()
                .firstname("Soumalya")
                .lastname("Hajra")
                .build();

        UserPOJO userPOJO = UserPOJO.builder()
                .username("Soumalya")
                .password("user@1")
                .details(userDetailsPOJO)
                .build();

        Users.createUser(userPOJO,UserRole.ADMIN)
                .then()
                .spec(fail400())
                .body("username",equalTo("Soumalya"))
                .body("id",notNullValue());

    }

    @Test
    public void updateNonExistingUser(){

        Response resp = Users.getAllUsers(UserRole.USER);
        List<Integer> userIds = resp.then().extract().jsonPath().getList("id", Integer.class);
        int id = userIds.get(userIds.size()+1);

        UserDetailsPOJO userDetailsPOJO = UserDetailsPOJO.builder()
                .firstname("Soumalya")
                .lastname("Hajra")
                .build();

        UserPOJO userPOJO = UserPOJO.builder()
                .email("soumalya.hajra@gmail.com")
                .username("Soumalya")
                .password("pass@1234")
                .details(userDetailsPOJO)
                .build();

        Users.updateUser(id,userPOJO,UserRole.ADMIN)
                .then()
                .spec(fail404())
                .body("username",equalTo("Soumalya"))
                .body("id",notNullValue())
                .body("password",equalTo("pass@1234"));

    }

    @Test
    public void deleteByInvalidId(){

        Response resp = Users.getAllUsers(UserRole.USER);
        List<Integer> userIds = resp.then().extract().jsonPath().getList("id", Integer.class);
        int id = userIds.get(userIds.size()+10);

        Users.deleteUser(id,UserRole.ADMIN).then().spec(fail404());


    }

    @Test
    public void emailFormatValidation(){

        UserDetailsPOJO userDetailsPOJO = UserDetailsPOJO.builder()
                .firstname("Soumalya")
                .lastname("Hajra")
                .build();

        UserPOJO userPOJO = UserPOJO.builder()
                .email("user1")
                .username("Soumalya")
                .password("user@12345")
                .details(userDetailsPOJO)
                .build();

        Users.createUser(userPOJO,UserRole.ADMIN)
                .then()
                .spec(fail400())
                .body("username",equalTo("Soumalya"))
                .body("id",notNullValue());

    }

    @Test
    public void passwordSizeValidation(){

        UserDetailsPOJO userDetailsPOJO = UserDetailsPOJO.builder()
                .firstname("Soumalya")
                .lastname("Hajra")
                .build();

        UserPOJO userPOJO = UserPOJO.builder()
                .email("user1@gmail.com")
                .username("Soumalya")
                .password("user")
                .details(userDetailsPOJO)
                .build();

        Users.createUser(userPOJO,UserRole.ADMIN)
                .then()
                .spec(fail400())
                .body("username",equalTo("Soumalya"))
                .body("id",notNullValue());

    }

    @Test
    public void roleValidation(){

        UserDetailsPOJO userDetailsPOJO = UserDetailsPOJO.builder()
                .firstname("Soumalya")
                .lastname("Hajra")
                .build();

        UserPOJO userPOJO = UserPOJO.builder()
                .email("user1@gmail.com")
                .username("Soumalya")
                .password("user@131213")
                .details(userDetailsPOJO)
                .role("manager")
                .build();

        Users.createUser(userPOJO,UserRole.ADMIN)
                .then()
                .spec(fail400())
                .body("username",equalTo("Soumalya"))
                .body("id",notNullValue());

    }

    @Test
    public void positiveIdValidation(){

        UserDetailsPOJO userDetailsPOJO = UserDetailsPOJO.builder()
                .firstname("Soumalya")
                .lastname("Hajra")
                .build();

        UserPOJO userPOJO = UserPOJO.builder()
                .id(-1)
                .email("user1@gmail.com")
                .username("Soumalya")
                .password("user@131213")
                .details(userDetailsPOJO)
                .role("admin")
                .build();

        Users.createUser(userPOJO,UserRole.ADMIN)
                .then()
                .spec(fail400())
                .body("username",equalTo("Soumalya"))
                .body("id",notNullValue());

    }








}
