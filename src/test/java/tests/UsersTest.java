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

    @Test(groups = {"smoke", "users"}, priority = 1)
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

    @Test(groups = {"smoke", "users"}, priority = 2)
    public void getUserById(){

        int randUserId = UserHelper.getRandomUserId();

        Users.getUserById(randUserId,UserRole.USER).then().spec(success200()).body("id",equalTo(randUserId));

    }

    @Test(
        dataProvider = "createUserData",
        dataProviderClass = UserDataProvider.class,
        groups = {"crud", "regression", "users"},
        priority = 3
    )
    public void createUser(String firstname, String lastname,String email, String username,String password){

        int id = 0;
        try{
                UserDetailsPOJO userDetailsPOJO = UserTestDataFactory.userDetailPayload(firstname , lastname);

                UserPOJO userPOJO = UserTestDataFactory.userPayload(userDetailsPOJO,email,username,password);

                id = Users.createUser(userPOJO,UserRole.ADMIN)
                .then()
                .spec(success201())
                .body("username",equalTo(username))
                .body("id",greaterThan(0))
                .extract()
                .jsonPath()
                .getInt("id");
        }finally{
               UserHelper.deleteUserIfExists(id);
        }

    }

    @Test(
        dataProvider = "updateUserFields",
        dataProviderClass = UserDataProvider.class,
        groups = {"crud", "regression", "users"},
        priority = 4
    )
    public void updateUserField(String field,String value,String firstname, String lastname,String email, String username,String password){

        int userId = 0;
        // create user first
        try{
                System.out.println("Updating field:" + field + " with value" + value);
                UserDetailsPOJO userDetailsPOJO = UserTestDataFactory.userDetailPayload(firstname , lastname);
                UserPOJO user = UserTestDataFactory.userPayload(userDetailsPOJO,email,username,password);

                userId = Users.createUser(user, UserRole.ADMIN)
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
        }finally{
                UserHelper.deleteUserIfExists(userId);
        }

        
    }


    @Test(
        dataProvider = "deleteUserFields",
        dataProviderClass = UserDataProvider.class,
        groups = {"crud", "users"},
        priority = 5,
        alwaysRun = true
    )
    public void deleteUser(String firstname, String lastname, String email, String username, String password, UserRole role , ResponseSpecification resp){

        try{
                UserDetailsPOJO userDetailsPOJO = UserTestDataFactory.userDetailPayload(firstname , lastname);
        UserPOJO user = UserTestDataFactory.userPayload(userDetailsPOJO,email,username,password);

        int userId = Users.createUser(user, UserRole.ADMIN)
                .then()
                .extract()
                .path("id");

        Users.deleteUser(userId,role)
                .then()
                .spec(resp);
        }catch(Exception ignored){}

    }

    @Test(groups = {"negative", "users"}, priority = 6)
    public void getUserByInvalidId(){

        int invalidId = UserHelper.getLastUserId() + 1 ;

        Users.getUserById(invalidId,UserRole.USER).then().spec(fail404());

    }

    @Test(
        dataProvider = "createUserData",
        dataProviderClass = UserDataProvider.class,
        groups = {"negative", "users"},
        priority = 7
    )
    public void createUserWithExistingEmail(String firstname, String lastname,String email, String username,String password){

        int id = 0;
        try{
                UserDetailsPOJO userDetailsPOJO = UserTestDataFactory.userDetailPayload(firstname , lastname);

        UserPOJO userPOJO = UserTestDataFactory.userPayload(userDetailsPOJO,email,username,password);

        Response resp = Users.createUser(userPOJO, UserRole.ADMIN)
                .then()
                .spec(success201())
                .body("username", equalTo(username))
                .body("id", greaterThan(0))
                .extract()
                .response();

                id = resp.path("id");
        String existingemail = resp.path("email");

        UserDetailsPOJO createDetailsPOJO = UserTestDataFactory.userDetailPayload(firstname , lastname);

        UserPOJO createUserPOJO = UserTestDataFactory.userPayload(userDetailsPOJO,existingemail,username+username,password);

        Users.createUser(createUserPOJO,UserRole.ADMIN)
                .then()
                .spec(fail400());
        }finally{
                UserHelper.deleteUserIfExists(id);
        }

    }


    @Test(
        dataProvider = "createWithoutEmailField",
        dataProviderClass = UserDataProvider.class,
        groups = {"negative", "users"},
        priority = 8
    )
    public void createUserWithMissingEmail(String firstname, String lastname,String username , String password){


        UserDetailsPOJO userDetailsPOJO = UserTestDataFactory.userDetailPayload(firstname , lastname);

        UserPOJO userPOJO = UserTestDataFactory.updateCreateUserWithoutEmail(userDetailsPOJO,username,password);

        Users.createUser(userPOJO,UserRole.ADMIN)
                .then()
                .spec(fail400());


    }

    @Test(
        dataProvider = "updateUserFields",
        dataProviderClass = UserDataProvider.class,
        groups = {"negative", "users"},
        priority = 9
    )
    public void updateNonExistingUser(String field,String value,String firstname, String lastname,String email, String username,String password){

        int userId = 0;
        try{
                UserDetailsPOJO userDetailsPOJO = UserTestDataFactory.userDetailPayload(firstname , lastname);
        UserPOJO user = UserTestDataFactory.userPayload(userDetailsPOJO,email,username,password);

        userId = Users.createUser(user, UserRole.ADMIN)
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
        }finally{
                 UserHelper.deleteUserIfExists(userId);
        }
    }

    @Test(
        dataProvider = "deleteUserByInvalidIdFields",
        dataProviderClass = UserDataProvider.class,
        groups = {"negative", "users"},
        priority = 10
    )
    public void deleteUserByInvalidId(String firstname, String lastname, String email, String username, String password , ResponseSpecification resp){


        try{
                UserDetailsPOJO userDetailsPOJO = UserTestDataFactory.userDetailPayload(firstname , lastname);
        UserPOJO user = UserTestDataFactory.userPayload(userDetailsPOJO,email,username,password);

        int userId = Users.createUser(user, UserRole.ADMIN)
                .then()
                .extract()
                .path("id");

        Users.deleteUser(userId + 100000,UserRole.ADMIN).then().spec(resp);
        }catch(Exception ignored){}

    }

    @Test(
        dataProvider = "validateUserData",
        dataProviderClass = UserDataProvider.class,
        groups = {"regression", "users"},
        priority = 11
    )
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
