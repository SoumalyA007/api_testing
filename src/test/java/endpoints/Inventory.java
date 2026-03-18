package endpoints;

import enums.UserRole;
import io.restassured.response.Response;

import static io.restassured.RestAssured.given;

import payloads.request.InventoryPOJO;
import testBase.BaseClass;

public class Inventory {

    public static Response getAllInventory(UserRole role){
        return given()
                .spec(BaseClass.get(role))
                .basePath("/inventory")
                .when()
                .get();
    }

    public static Response getInventoryById(int id,UserRole role){
        return given()
                .spec(BaseClass.get(role))
                .basePath("/inventory/{id}")
                .pathParam("id", id)
                .when()
                .get();
    }

    public static Response getInventoryByProductId(int productId, UserRole role){
        return given()
                .spec(BaseClass.get(role))
                .basePath("/inventory")
                .queryParam("productId", productId)
                .when()
                .get();
    }

    public static Response updateInventory(int id, InventoryPOJO inventory, UserRole role){
        return given()
                .spec(BaseClass.get(role))
                .basePath("/inventory/{id}")
                .pathParam("id", id)
                .body(inventory)
                .when()
                .put();
    }
}
