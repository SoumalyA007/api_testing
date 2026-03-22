package tests;

import dataproviders.UserDataProvider;
import endpoints.Users;
import enums.UserRole;
import helpers.UserHelper;
import io.restassured.response.Response;
import io.restassured.specification.ResponseSpecification;
import org.testng.annotations.Test;
import payloads.request.UserDetailsPOJO;
import payloads.request.UserPOJO;
import testBase.BaseClass;
import testData.UserTestDataFactory;

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

        int randUserId = UserHelper.getRandomUserId();

        Users.getUserById(randUserId,UserRole.USER).then().spec(success200()).body("id",equalTo(randUserId));

    }

    @Test(dataProvider = "createUserData",dataProviderClass = UserDataProvider.class)
    public void createUser(String firstname, String lastname,String email, String username,String password){

        UserDetailsPOJO userDetailsPOJO = UserTestDataFactory.userDetailPayload(firstname , lastname);

        UserPOJO userPOJO = UserTestDataFactory.userPayload(userDetailsPOJO,email,username,password);

        int id = Users.createUser(userPOJO,UserRole.ADMIN)
                .then()
                .spec(success201())
                .body("username",equalTo(username))
                .body("id",greaterThan(0))
                .extract()
                .jsonPath()
                .getInt("id");

        Users.deleteUser(id,UserRole.ADMIN);

    }

    @Test(dataProvider = "updateUserFields", dataProviderClass = UserDataProvider.class)
    public void updateUserField(String field,String value,String firstname, String lastname,String email, String username,String password){

        // create user first
        System.out.println("Updating field:" + field + " with value" + value);
        UserDetailsPOJO userDetailsPOJO = UserTestDataFactory.userDetailPayload(firstname , lastname);
        UserPOJO user = UserTestDataFactory.userPayload(userDetailsPOJO,email,username,password);

        int userId = Users.createUser(user, UserRole.ADMIN)
                .then()
                .extract()
                .path("id");

        // update payload
        UserPOJO updateUser = UserTestDataFactory.updateUserField(field, value);

        String jsonPath = field;

        if(field.equals("firstname") || field.equals("lastname")){
            jsonPath = "details." + field;
        }

        Users.updateUser(userId, updateUser, UserRole.ADMIN)
                .then()
                .spec(success200())
                .body(jsonPath,equalTo(value));

        Users.deleteUser(userId,UserRole.ADMIN);
    }


    @Test(dataProvider = "deleteUserFields", dataProviderClass = UserDataProvider.class)
    public void deleteUser(String firstname, String lastname, String email, String username, String password, UserRole role , ResponseSpecification resp){

        UserDetailsPOJO userDetailsPOJO = UserTestDataFactory.userDetailPayload(firstname , lastname);
        UserPOJO user = UserTestDataFactory.userPayload(userDetailsPOJO,email,username,password);

        int userId = Users.createUser(user, UserRole.ADMIN)
                .then()
                .extract()
                .path("id");

        Users.deleteUser(userId,role)
                .then()
                .spec(resp);


    }

    @Test
    public void getUserByInvalidId(){

        int invalidId = UserHelper.getLastUserId() + 1 ;

        Users.getUserById(invalidId,UserRole.USER).then().spec(fail404());

    }

    @Test(dataProvider = "createUserData",dataProviderClass = UserDataProvider.class)
    public void createUserWithExistingEmail(String firstname, String lastname,String email, String username,String password){


        UserDetailsPOJO userDetailsPOJO = UserTestDataFactory.userDetailPayload(firstname , lastname);

        UserPOJO userPOJO = UserTestDataFactory.userPayload(userDetailsPOJO,email,username,password);

        Response resp = Users.createUser(userPOJO, UserRole.ADMIN)
                .then()
                .spec(success201())
                .body("username", equalTo(username))
                .body("id", greaterThan(0))
                .extract()
                .response();

        int id = resp.path("id");
        String existingemail = resp.path("email");


        UserDetailsPOJO createDetailsPOJO = UserTestDataFactory.userDetailPayload(firstname , lastname);

        UserPOJO createUserPOJO = UserTestDataFactory.userPayload(userDetailsPOJO,existingemail,username+username,password);



        Users.createUser(createUserPOJO,UserRole.ADMIN)
                .then()
                .spec(fail400());


        Users.deleteUser(id , UserRole.ADMIN);

    }


    @Test(dataProvider = "createWithoutEmailField",dataProviderClass = UserDataProvider.class)
    public void createUserWithMissingEmail(String firstname, String lastname,String username , String password){


        UserDetailsPOJO userDetailsPOJO = UserTestDataFactory.userDetailPayload(firstname , lastname);

        UserPOJO userPOJO = UserTestDataFactory.updateCreateUserWithoutEmail(userDetailsPOJO,username,password);

        Users.createUser(userPOJO,UserRole.ADMIN)
                .then()
                .spec(fail404());


    }

    @Test(dataProvider = "updateUserFields", dataProviderClass = UserDataProvider.class)
    public void updateNonExistingUser(String field,String value,String firstname, String lastname,String email, String username,String password){


        UserDetailsPOJO userDetailsPOJO = UserTestDataFactory.userDetailPayload(firstname , lastname);
        UserPOJO user = UserTestDataFactory.userPayload(userDetailsPOJO,email,username,password);

        int userId = Users.createUser(user, UserRole.ADMIN)
                .then()
                .extract()
                .path("id");

        // update payload
        UserPOJO updateUser = UserTestDataFactory.updateUserField(field, value);

        String jsonPath = field;

        if(field.equals("firstname") || field.equals("lastname")){
            jsonPath = "details." + field;
        }

        Users.updateUser(userId+ 100000, updateUser, UserRole.ADMIN)
                .then()
                .spec(fail400());

        Users.deleteUser(userId,UserRole.ADMIN);


    }

    @Test(dataProvider = "deleteUserByInvalidIdFields", dataProviderClass = UserDataProvider.class)
    public void deleteUserByInvalidId(String firstname, String lastname, String email, String username, String password , ResponseSpecification resp){


        UserDetailsPOJO userDetailsPOJO = UserTestDataFactory.userDetailPayload(firstname , lastname);
        UserPOJO user = UserTestDataFactory.userPayload(userDetailsPOJO,email,username,password);

        int userId = Users.createUser(user, UserRole.ADMIN)
                .then()
                .extract()
                .path("id");

        Users.deleteUser(userId + 100000,UserRole.ADMIN).then().spec(resp);

    }

    @Test(dataProvider = "validateUserData",dataProviderClass = UserDataProvider.class)
    public void validateUserData(String field,String firstname, String lastname,String email, String username,String password, UserRole role, ResponseSpecification spec){

        UserDetailsPOJO userDetailsPOJO = UserTestDataFactory.userDetailPayload(firstname , lastname);

        UserPOJO user;

        System.out.println("Currently Running :>" + field);

        if(field.equalsIgnoreCase("Positive ID Validation")){

            user = UserTestDataFactory.userPayloadWithId(-10,userDetailsPOJO,email,username,password);
        }else{
            user = UserTestDataFactory.userPayload(userDetailsPOJO,email,username,password);
        }

        int id = Users.createUser(user, role)
                .then()
                .spec(spec)
                .extract()
                .jsonPath()
                .getInt("id");

        Users.deleteUser(id,UserRole.ADMIN);


    }

}
