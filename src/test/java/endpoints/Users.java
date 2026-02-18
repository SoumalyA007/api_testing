package endpoints;

import io.restassured.response.Response;
import payloads.UserPOJO;
import testBase.BaseClass;

import static io.restassured.RestAssured.given;

public class Users {

    public static Response getUsers(){
        Response resp = given()
                .spec(BaseClass.get())
                .basePath("/users")
                .when()
                .get();

        return resp;
    }

    public static Response createUser(UserPOJO user){
        Response resp = given()
                .spec(BaseClass.get())
                .basePath("/users")
                .body(user)
                .when()
                .post();

        return resp;
    }

    public static Response getSingleUser(int id){
        Response resp = given()
                .spec(BaseClass.get())
                .basePath("/users/{id}")
                .pathParam("id",id)
                .when()
                .get();

        return resp;

    }

    public static Response updateSingleUser(int id , UserPOJO user){
        Response resp = given()
                .spec(BaseClass.get())
                .basePath("/users/{id}")
                .pathParam("id",id)
                .body(user)
                .when()
                .put();

        return resp;

    }

    public static Response deleteSingleUser(int id){
        Response resp = given()
                .spec(BaseClass.get())
                .basePath("/users/{id}")
                .pathParam("id",id)
                .when()
                .delete();

        return resp;

    }




}
