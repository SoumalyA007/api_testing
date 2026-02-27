package tests;

import endpoints.Users;
import enums.UserRole;
import org.testng.annotations.Test;
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

        UserPOJO userPOJO = UserPOJO.builder()
                .email("user1@gmail.com")
                .password("user@1")
                .details.setFirstname("user")
                .

    }





}
