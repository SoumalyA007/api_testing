package endpoints;

import io.restassured.response.Response;
import payloads.request.UserPOJO;
import testBase.BaseClass;

import static io.restassured.RestAssured.given;

public class Users {

    public static Response getAllUsers(){
        return given()
                .spec(BaseClass.get())
                .basePath("/users")
                .when()
                .get();
    }

    public static Response getUserById(int id){
        return given()
                .spec(BaseClass.get())
                .basePath("/users/{id}")
                .pathParam("id", id)
                .when()
                .get();
    }

    public static Response createUser(UserPOJO user){
        return given()
                .spec(BaseClass.get())
                .basePath("/users")
                .body(user)
                .when()
                .post();
    }

    public static Response updateUser(int id, UserPOJO user){
        return given()
                .spec(BaseClass.get())
                .basePath("/users/{id}")
                .pathParam("id", id)
                .body(user)
                .when()
                .put();
    }

    public static Response deleteUser(int id){
        return given()
                .spec(BaseClass.get())
                .basePath("/users/{id}")
                .pathParam("id", id)
                .when()
                .delete();
    }
}
