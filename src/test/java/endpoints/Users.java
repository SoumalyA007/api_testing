package endpoints;

import enums.UserRole;
import io.restassured.response.Response;
import payloads.request.UserPOJO;
import testBase.BaseClass;

import static io.restassured.RestAssured.given;

public class Users {

    public static Response getAllUsers(UserRole role){
        return given()
                .spec(BaseClass.get(role))
                .basePath("/users")
                .when()
                .get();
    }

    public static Response getUserById(int id,UserRole role){
        return given()
                .spec(BaseClass.get(role))
                .basePath("/users/{id}")
                .pathParam("id", id)
                .when()
                .get();
    }

    public static Response createUser(UserPOJO user,UserRole role){
        return given()
                .spec(BaseClass.get(role))
                .basePath("/users")
                .body(user)
                .when()
                .post();
    }

    public static Response updateUser(int id, UserPOJO user,UserRole role){
        return given()
                .spec(BaseClass.get(role))
                .basePath("/users/{id}")
                .pathParam("id", id)
                .body(user)
                .when()
                .put();
    }

    public static Response deleteUser(int id,UserRole role){
        return given()
                .spec(BaseClass.get(role))
                .basePath("/users/{id}")
                .pathParam("id", id)
                .when()
                .delete();
    }
}
